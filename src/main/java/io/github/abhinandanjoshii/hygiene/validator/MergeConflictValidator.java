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
 * Detects unresolved Git merge conflict markers in project source files.
 *
 * <p>Scans source, config, and resource files for {@code <<<<<<<}, {@code =======},
 * and {@code >>>>>>>} markers. Developers accidentally commit these after a merge
 * and they cause compilation failures or silent data corruption depending on the
 * file type.</p>
 *
 * <h3>Configuration</h3>
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
public final class MergeConflictValidator implements HygieneValidator {

    private static final List<String> CONFLICT_MARKERS = List.of(
            "<<<<<<<",
            "=======",
            ">>>>>>>"
    );

    /** Built-in extensions. User additions merged at runtime from configuration. */
    private static final Set<String> DEFAULT_SCANNED_EXTENSIONS = Set.of(
            ".java", ".xml", ".yml", ".yaml", ".properties",
            ".json", ".md", ".txt", ".html", ".sql", ".groovy", ".kt"
    );

    /**
     * Scans the project root recursively for unresolved merge conflict markers.
     *
     * @param context read-only project context
     * @return one ERROR finding per conflict marker line; empty if none found
     */
    @Override
    public List<Finding> validate(ValidationContext context) {
        Set<String> extensions = HygieneConfiguration.merge(
                DEFAULT_SCANNED_EXTENSIONS,
                context.getConfiguration().getAdditionalScannedExtensions()
        );

        List<Finding> findings = new ArrayList<>();
        scanDirectory(context.getProjectRoot(), extensions, findings, context);
        return List.copyOf(findings);
    }

    private void scanDirectory(File directory, Set<String> extensions,
                               List<Finding> findings, ValidationContext context) {
        File[] entries = directory.listFiles();
        if (entries == null) return;

        for (File entry : entries) {
            if (entry.isDirectory()) {
                if (!ValidatorFileUtils.shouldSkipDirectory(entry, context)) {
                    scanDirectory(entry, extensions, findings, context);
                }
            } else if (ValidatorFileUtils.hasExtension(entry.getName(), extensions)) {
                scanFile(entry, findings);
            }
        }
    }

    private void scanFile(File file, List<Finding> findings) {
        List<String> lines = ValidatorFileUtils.readLinesSilently(file);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            for (String marker : CONFLICT_MARKERS) {
                if (line.startsWith(marker)) {
                    findings.add(Finding.of(
                            Severity.ERROR,
                            getClass().getSimpleName(),
                            "Unresolved merge conflict marker at "
                                    + file.getAbsolutePath() + ":" + (i + 1)
                                    + " — '" + line.trim() + "'"
                    ));
                    break;
                }
            }
        }
    }
}