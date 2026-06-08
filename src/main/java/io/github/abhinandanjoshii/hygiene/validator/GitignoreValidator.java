package io.github.abhinandanjoshii.hygiene.validator;

import org.apache.maven.plugin.logging.Log;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates that a {@code .gitignore} file exists and contains required ignore patterns.
 *
 * <p>Required patterns include {@code target/}, {@code .env}, {@code *.class},
 * {@code *.log}, {@code *.iml}, and {@code .idea/}.</p>
 *
 * <p>Recommended patterns such as {@code *.pem}, {@code *.key}, and
 * {@code application-local.properties} are reported as INFO rather than WARN.</p>
 */
public class GitignoreValidator {

    private GitignoreValidator() {
        // utility class — no instantiation
    }

    private static final List<String> REQUIRED_PATTERNS = List.of(
            "target/",
            "target",
            ".env",
            "*.class",
            "*.log",
            "*.iml",
            ".idea/",
            ".idea"
    );

    private static final List<String> RECOMMENDED_PATTERNS = List.of(
            "application-local.properties",
            "application-local.yml",
            "*.pem",
            "*.key"
    );

    /**
     * Checks the project root for a {@code .gitignore} file and validates its contents.
     *
     * @param projectRoot the root directory of the Maven project
     * @param log         the Maven plugin logger
     */
    public static void validate(File projectRoot, Log log) {
        File gitignore = new File(projectRoot, ".gitignore");

        if (!gitignore.exists()) {
            log.warn(".gitignore not found : no files are being excluded from version control.");
            return;
        }

        List<String> entries;
        try {
            entries = Files.readAllLines(gitignore.toPath());
        } catch (IOException e) {
            log.warn("Could not read .gitignore: " + e.getMessage());
            return;
        }

        List<String> missingRequired = findMissing(entries, REQUIRED_PATTERNS);
        List<String> missingRecommended = findMissing(entries, RECOMMENDED_PATTERNS);

        if (missingRequired.isEmpty() && missingRecommended.isEmpty()) {
            log.info(".gitignore covers all recommended patterns.");
            return;
        }

        for (String pattern : missingRequired) {
            log.warn(".gitignore missing required pattern: " + pattern);
        }

        for (String pattern : missingRecommended) {
            log.info(".gitignore missing recommended pattern: " + pattern);
        }
    }

    private static List<String> findMissing(List<String> entries, List<String> patterns) {
        List<String> missing = new ArrayList<>();
        for (String pattern : patterns) {
            boolean covered = entries.stream()
                    .map(String::trim)
                    .filter(entry -> !entry.isEmpty() && !entry.startsWith("#"))
                    .anyMatch(entry -> entry.equals(pattern));
            if (!covered) {
                missing.add(pattern);
            }
        }
        return missing;
    }

}
