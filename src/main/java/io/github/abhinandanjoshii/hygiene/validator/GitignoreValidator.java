package io.github.abhinandanjoshii.hygiene.validator;

import io.github.abhinandanjoshii.hygiene.model.Finding;
import io.github.abhinandanjoshii.hygiene.model.Severity;
import io.github.abhinandanjoshii.hygiene.model.ValidationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates that a {@code .gitignore} file exists and contains essential ignore patterns.
 *
 * <p>Built-in required patterns cover Maven build output, IDE files, compiled classes,
 * logs, and environment files. Recommended patterns cover credential files and local
 * config overrides.</p>
 *
 * <h2>Configuration</h2>
 * <pre>{@code
 * <configuration>
 *   <additionalRequiredGitignorePatterns>
 *     <pattern>.terraform/</pattern>
 *     <pattern>*.tfstate</pattern>
 *   </additionalRequiredGitignorePatterns>
 * </configuration>
 * }</pre>
 *
 * @since 0.3.0
 */
public final class GitignoreValidator implements HygieneValidator {

    /**
     * Default constructor. Instantiated by HygieneMojo at runtime.
     */
    public GitignoreValidator() {}

    private record PatternGroup(String label, List<String> alternatives, Severity severity) {}

    private static final List<PatternGroup> DEFAULT_PATTERN_GROUPS = List.of(
            new PatternGroup("Maven build output (target)",
                    List.of("target/", "target"),                          Severity.WARNING),
            new PatternGroup(".env files",
                    List.of(".env"),                                        Severity.WARNING),
            new PatternGroup("Compiled class files (*.class)",
                    List.of("*.class"),                                     Severity.WARNING),
            new PatternGroup("Log files (*.log)",
                    List.of("*.log"),                                       Severity.WARNING),
            new PatternGroup("IntelliJ module files (*.iml)",
                    List.of("*.iml"),                                       Severity.WARNING),
            new PatternGroup("IntelliJ workspace (.idea)",
                    List.of(".idea/", ".idea"),                             Severity.WARNING),
            new PatternGroup("Private key files (*.pem, *.key)",
                    List.of("*.pem", "*.key"),                             Severity.INFO),
            new PatternGroup("Local config overrides",
                    List.of("application-local.properties",
                            "application-local.yml"),                       Severity.INFO)
    );

    /**
     * Checks that {@code .gitignore} exists and covers all required and recommended patterns.
     *
     * @param context read-only project context
     * @return findings for missing patterns; empty if all patterns are present
     */
    @Override
    public List<Finding> validate(ValidationContext context) {
        File root = context.getProjectRoot();
        File gitignoreFile = new File(root, ".gitignore");

        if (!gitignoreFile.exists() || !gitignoreFile.isFile()) {
            return List.of(Finding.of(
                    Severity.WARNING,
                    getClass().getSimpleName(),
                    ".gitignore not found — no files are excluded from version control."
            ));
        }

        List<String> lines = ValidatorFileUtils.readGitignoreLines(root);
        Set<String> activeEntries = parseActiveEntries(lines);

        List<Finding> findings = new ArrayList<>();

        for (PatternGroup group : DEFAULT_PATTERN_GROUPS) {
            boolean covered = group.alternatives().stream()
                    .anyMatch(activeEntries::contains);
            if (!covered) {
                findings.add(Finding.of(
                        group.severity(),
                        getClass().getSimpleName(),
                        ".gitignore is missing a pattern for: " + group.label()
                                + ". Add one of: " + group.alternatives()
                ));
            }
        }

        for (String pattern : context.getConfiguration()
                .getAdditionalRequiredGitignorePatterns()) {
            if (!activeEntries.contains(pattern)) {
                findings.add(Finding.of(
                        Severity.WARNING,
                        getClass().getSimpleName(),
                        ".gitignore is missing configured required pattern: '" + pattern + "'"
                ));
            }
        }

        return List.copyOf(findings);
    }

    private static Set<String> parseActiveEntries(List<String> lines) {
        Set<String> entries = new HashSet<>();
        for (String raw : lines) {
            String trimmed = raw.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
            if (trimmed.startsWith("./")) trimmed = trimmed.substring(2);
            if (trimmed.startsWith("/"))  trimmed = trimmed.substring(1);
            entries.add(trimmed);
        }
        return entries;
    }
}