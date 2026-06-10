package io.github.abhinandanjoshii.hygiene.validator;

import io.github.abhinandanjoshii.hygiene.model.ValidationContext;

import java.io.File;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Internal filesystem utilities shared across validators.
 *
 * <p>Centralises skip-directory logic, file extension matching, and file reading
 * so each validator does not duplicate these concerns.</p>
 *
 * @since 0.3.0
 */
final class ValidatorFileUtils {

    /**
     * Built-in directories never descended into during any scan.
     * Merged with {@link io.github.abhinandanjoshii.hygiene.model.HygieneConfiguration#getAdditionalSkipDirectories()}
     * at runtime via {@link #shouldSkipDirectory(File, ValidationContext)}.
     */
    static final Set<String> DEFAULT_SKIP_DIRS = Set.of(
            "target",
            ".git",
            ".idea",
            ".mvn",
            "node_modules"
    );

    private ValidatorFileUtils() {
        // utility class — no instantiation
    }

    /**
     * Returns {@code true} if {@code dir} should not be descended into,
     * considering both built-in defaults and user-configured additional directories.
     *
     * @param dir     a directory file
     * @param context the validation context carrying user configuration
     * @return {@code true} if the directory should be skipped
     */
    static boolean shouldSkipDirectory(File dir, ValidationContext context) {
        if (DEFAULT_SKIP_DIRS.contains(dir.getName())) return true;
        List<String> extra = context.getConfiguration().getAdditionalSkipDirectories();
        return extra != null && extra.contains(dir.getName());
    }

    /**
     * Returns {@code true} if {@code fileName} ends with any of the given {@code extensions}.
     *
     * @param fileName   filename to test
     * @param extensions set of extensions including leading dot (e.g. {@code .java})
     * @return {@code true} if matched
     */
    static boolean hasExtension(String fileName, Set<String> extensions) {
        for (String ext : extensions) {
            if (fileName.endsWith(ext)) return true;
        }
        return false;
    }

    /**
     * Reads all lines from {@code file}, returning an empty list on any I/O error.
     * Binary files that trigger {@link MalformedInputException} are silently skipped.
     *
     * @param file file to read
     * @return list of lines, or empty list on failure
     */
    static List<String> readLinesSilently(File file) {
        try {
            return Files.readAllLines(file.toPath());
        } catch (MalformedInputException e) {
            return Collections.emptyList();
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Reads {@code .gitignore} lines from the project root.
     * Returns an empty list if the file does not exist or cannot be read.
     *
     * @param projectRoot project base directory
     * @return list of raw gitignore lines, or empty list
     */
    static List<String> readGitignoreLines(File projectRoot) {
        File gitignore = new File(projectRoot, ".gitignore");
        if (!gitignore.exists() || !gitignore.isFile()) {
            return Collections.emptyList();
        }
        return readLinesSilently(gitignore);
    }
}