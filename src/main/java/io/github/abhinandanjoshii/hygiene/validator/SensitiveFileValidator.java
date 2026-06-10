package io.github.abhinandanjoshii.hygiene.validator;

import io.github.abhinandanjoshii.hygiene.model.Finding;
import io.github.abhinandanjoshii.hygiene.model.HygieneConfiguration;
import io.github.abhinandanjoshii.hygiene.model.Severity;
import io.github.abhinandanjoshii.hygiene.model.ValidationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Detects sensitive files present in the project directory that are not excluded
 * by {@code .gitignore}.
 *
 * <p>A file is only flagged if it exists AND is not already covered by an entry
 * in the project's {@code .gitignore} — avoiding noise for projects that
 * legitimately have these files but correctly ignore them.</p>
 *
 * <h2>Configuration</h2>
 * <pre>{@code
 * <configuration>
 *   <additionalSensitiveFilenames>
 *     <filename>db-credentials.conf</filename>
 *     <filename>vault-token.txt</filename>
 *   </additionalSensitiveFilenames>
 *   <additionalSensitiveExtensions>
 *     <extension>.vault</extension>
 *     <extension>.gpg</extension>
 *   </additionalSensitiveExtensions>
 * </configuration>
 * }</pre>
 *
 * @since 0.3.0
 */
public final class SensitiveFileValidator implements HygieneValidator {

    /**
     * Default constructor. Instantiated by HygieneMojo at runtime.
     */
    public SensitiveFileValidator() {}

    private static final Set<String> DEFAULT_SENSITIVE_FILENAMES = Set.of(
            ".env", ".env.local", ".env.production", ".env.staging", ".env.development",
            "application-local.properties", "application-local.yml", "application-local.yaml",
            "secrets.yml", "secrets.yaml", "secrets.json",
            "credentials.json", "serviceaccount.json", "service-account.json"
    );

    private static final Set<String> DEFAULT_SENSITIVE_EXTENSIONS = Set.of(
            ".pem", ".key", ".p12", ".pfx", ".jks", ".keystore", ".cer", ".der"
    );

    /**
     * Scans the project root for sensitive files not excluded by {@code .gitignore}.
     *
     * @param context read-only project context
     * @return one ERROR finding per unignored sensitive file; empty if none found
     */
    @Override
    public List<Finding> validate(ValidationContext context) {
        HygieneConfiguration cfg = context.getConfiguration();

        Set<String> sensitiveFilenames  = HygieneConfiguration.merge(
                DEFAULT_SENSITIVE_FILENAMES,  cfg.getAdditionalSensitiveFilenames());
        Set<String> sensitiveExtensions = HygieneConfiguration.merge(
                DEFAULT_SENSITIVE_EXTENSIONS, cfg.getAdditionalSensitiveExtensions());

        File root = context.getProjectRoot();
        List<String> gitignoreLines = ValidatorFileUtils.readGitignoreLines(root);

        List<Finding> findings = new ArrayList<>();
        scanDirectory(root, root, gitignoreLines,
                sensitiveFilenames, sensitiveExtensions, findings, context);
        return List.copyOf(findings);
    }

    private void scanDirectory(File projectRoot, File directory,
                               List<String> gitignoreLines,
                               Set<String> filenames, Set<String> extensions,
                               List<Finding> findings, ValidationContext context) {
        File[] entries = directory.listFiles();
        if (entries == null) return;

        for (File entry : entries) {
            if (entry.isDirectory()) {
                if (!ValidatorFileUtils.shouldSkipDirectory(entry, context)) {
                    scanDirectory(projectRoot, entry, gitignoreLines,
                            filenames, extensions, findings, context);
                }
            } else if (isSensitive(entry.getName(), filenames, extensions)) {
                String relativePath = projectRoot.toPath()
                        .relativize(entry.toPath())
                        .toString()
                        .replace(File.separatorChar, '/');

                if (!isGitignored(entry.getName(), relativePath, gitignoreLines)) {
                    findings.add(Finding.of(
                            Severity.ERROR,
                            getClass().getSimpleName(),
                            "Sensitive file present and not excluded by .gitignore: "
                                    + entry.getAbsolutePath()
                                    + " — add '" + entry.getName() + "' to .gitignore."
                    ));
                }
            }
        }
    }

    private static boolean isSensitive(String fileName,
                                       Set<String> filenames, Set<String> extensions) {
        if (filenames.contains(fileName)) return true;
        for (String ext : extensions) {
            if (fileName.endsWith(ext)) return true;
        }
        return false;
    }

    /**
     * Package-private for unit testing without instantiation.
     */
    static boolean isGitignored(String fileName, String relativePath,
                                List<String> gitignoreLines) {
        for (String rawEntry : gitignoreLines) {
            String entry = rawEntry.trim();
            if (entry.isEmpty() || entry.startsWith("#")) continue;
            if (entry.startsWith("./")) entry = entry.substring(2);
            if (entry.startsWith("/"))  entry = entry.substring(1);
            String pattern = entry.endsWith("/")
                    ? entry.substring(0, entry.length() - 1) : entry;

            if (pattern.equals(fileName))     return true;
            if (pattern.equals(relativePath)) return true;

            if (pattern.startsWith("*") && !pattern.contains("/")) {
                if (fileName.endsWith(pattern.substring(1))) return true;
            }
            if (pattern.endsWith("*") && !pattern.contains("/")) {
                String prefix = pattern.substring(0, pattern.length() - 1);
                if (!prefix.isEmpty() && fileName.startsWith(prefix)) return true;
            }
        }
        return false;
    }
}