package io.github.abhinandanjoshii.hygiene.validator;

import io.github.abhinandanjoshii.hygiene.model.Finding;
import io.github.abhinandanjoshii.hygiene.model.HygieneConfiguration;
import io.github.abhinandanjoshii.hygiene.model.Severity;
import io.github.abhinandanjoshii.hygiene.model.ValidationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Detects hardcoded credentials and secrets in project source and configuration files.
 *
 * <p>Uses regex patterns to identify common credential patterns including passwords,
 * API keys, tokens, AWS access key prefixes, Bearer tokens, private keys, and PEM
 * block headers.</p>
 *
 * <p>Reports file path and line number only — never prints the secret value itself.</p>
 *
 * <h2>Configuration</h2>
 * <pre>{@code
 * <configuration>
 *   <additionalScannedExtensions>
 *     <extension>.tf</extension>
 *     <extension>.sh</extension>
 *   </additionalScannedExtensions>
 * </configuration>
 * }</pre>
 *
 * @since 0.3.0
 */
public final class HardcodedSecretValidator implements HygieneValidator {

    private static final List<Pattern> SECRET_PATTERNS = List.of(
            Pattern.compile("(?i)(password|passwd|pwd)\\s*[:=]\\s*['\"]?[^\\s'\"${}]{6,}"),
            Pattern.compile("(?i)(api_key|apikey|api-key)\\s*[:=]\\s*['\"]?[^\\s'\"${}]{8,}"),
            Pattern.compile("(?i)(secret_key|client_secret|secret)\\s*[:=]\\s*['\"]?[^\\s'\"${}]{8,}"),
            Pattern.compile("(?i)(token|auth_token|access_token)\\s*[:=]\\s*['\"]?[^\\s'\"${}]{8,}"),
            Pattern.compile("(?i)(private_key|privatekey)\\s*[:=]\\s*['\"]?[^\\s'\"${}]{8,}"),
            Pattern.compile("AKIA[0-9A-Z]{16}"),
            Pattern.compile("(?i)Bearer\\s+[a-zA-Z0-9\\-._~+/]{20,}"),
            Pattern.compile("(?i)-----BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY-----")
    );

    /**
     * Strings that indicate a value is a placeholder, environment variable reference,
     * or Spring expression — not a real hardcoded secret. Lines containing any of these
     * are skipped to reduce false positives.
     */
    private static final List<String> PLACEHOLDER_INDICATORS = List.of(
            "${", "#{", "<your", "your-", "your_",
            "example", "placeholder", "changeme", "change_me",
            "xxxxxxxx", "aaaaaaaa", "password123",
            "@Value", "System.getenv", "System.getProperty"
    );

    /** Built-in extensions. User additions merged at runtime from configuration. */
    private static final Set<String> DEFAULT_SCANNED_EXTENSIONS = Set.of(
            ".java", ".xml", ".yml", ".yaml", ".properties",
            ".json", ".env", ".config", ".conf", ".ini", ".gradle"
    );

    /**
     * Scans the project root recursively for hardcoded secrets.
     *
     * @param context read-only project context
     * @return one ERROR finding per matched line; empty if none found
     */
    @Override
    public List<Finding> validate(ValidationContext context) {
        Set<String> extensions = HygieneConfiguration.merge(
                DEFAULT_SCANNED_EXTENSIONS,
                context.getConfiguration().getAdditionalScannedExtensions()
        );

        List<Finding> findings = new ArrayList<>();
        scanDirectory(context.getProjectRoot(), context.getProjectRoot(),
                extensions, findings, context);
        return List.copyOf(findings);
    }

    private void scanDirectory(File projectRoot, File directory, Set<String> extensions,
                               List<Finding> findings, ValidationContext context) {
        File[] entries = directory.listFiles();
        if (entries == null) return;

        for (File entry : entries) {
            if (entry.isDirectory()) {
                if (ValidatorFileUtils.shouldSkipDirectory(entry, context)) continue;
                // Test fixtures legitimately contain mock credentials (e.g. test API keys,
                // dummy passwords in test application.properties). Scanning them produces
                // high false-positive noise with no actionable signal.
                if (isTestSourceDirectory(projectRoot, entry)) continue;
                scanDirectory(projectRoot, entry, extensions, findings, context);
            } else if (ValidatorFileUtils.hasExtension(entry.getName(), extensions)) {
                scanFile(entry, findings);
            }
        }
    }

    private void scanFile(File file, List<Finding> findings) {
        List<String> lines = ValidatorFileUtils.readLinesSilently(file);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String trimmed = line.trim();
            if (isComment(trimmed)) continue;
            if (containsPlaceholder(trimmed)) continue;

            for (Pattern pattern : SECRET_PATTERNS) {
                if (pattern.matcher(line).find()) {
                    findings.add(Finding.of(
                            Severity.ERROR,
                            getClass().getSimpleName(),
                            "Potential hardcoded secret at "
                                    + file.getAbsolutePath() + ":" + (i + 1)
                                    + " — matched pattern: " + patternLabel(pattern)
                    ));
                    break;
                }
            }
        }
    }

    private static boolean isComment(String trimmed) {
        return trimmed.startsWith("//")
                || trimmed.startsWith("#")
                || trimmed.startsWith("*")
                || trimmed.startsWith("<!--");
    }

    private static boolean containsPlaceholder(String line) {
        String lower = line.toLowerCase();
        for (String indicator : PLACEHOLDER_INDICATORS) {
            if (lower.contains(indicator.toLowerCase())) return true;
        }
        return false;
    }

    private static boolean isTestSourceDirectory(File projectRoot, File directory) {
        String rel = projectRoot.toPath()
                .relativize(directory.toPath())
                .toString()
                .replace(File.separatorChar, '/');
        return rel.startsWith("src/test");
    }

    private static String patternLabel(Pattern p) {
        String src = p.pattern();
        if (src.contains("password|passwd"))          return "password/passwd assignment";
        if (src.contains("api_key|apikey"))           return "api_key assignment";
        if (src.contains("secret_key|client_secret")) return "secret/client_secret assignment";
        if (src.contains("token|auth_token"))         return "token assignment";
        if (src.contains("private_key|privatekey"))   return "private_key assignment";
        if (src.startsWith("AKIA"))                   return "AWS access key ID";
        if (src.contains("Bearer"))                   return "Bearer token literal";
        if (src.contains("PRIVATE KEY"))              return "private key block (PEM header)";
        return "credential pattern";
    }
}