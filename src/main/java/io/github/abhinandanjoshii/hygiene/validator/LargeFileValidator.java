package io.github.abhinandanjoshii.hygiene.validator;

import io.github.abhinandanjoshii.hygiene.model.Finding;
import io.github.abhinandanjoshii.hygiene.model.Severity;
import io.github.abhinandanjoshii.hygiene.model.ValidationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Detects files committed to the repository that exceed a configurable size threshold.
 *
 * <p>Large files bloat repository size and slow down {@code git clone} and CI pipeline
 * times. Consider using Git LFS for binary assets or removing large generated files
 * from version control entirely.</p>
 *
 * <h2>Configuration</h2>
 * <pre>{@code
 * <configuration>
 *   <maxFileSizeMb>50</maxFileSizeMb>
 *   <additionalSkipDirectories>
 *     <directory>large-assets</directory>
 *   </additionalSkipDirectories>
 * </configuration>
 * }</pre>
 *
 * @since 0.3.0
 */
public final class LargeFileValidator implements HygieneValidator {

    private static final long BYTES_PER_MB = 1024L * 1024L;

    /**
     * Scans the project root recursively for files exceeding the configured size threshold.
     *
     * @param context read-only project context
     * @return one WARNING finding per oversized file; empty if none found
     */
    @Override
    public List<Finding> validate(ValidationContext context) {
        long thresholdBytes = (long) context.getMaxFileSizeMb() * BYTES_PER_MB;
        List<Finding> findings = new ArrayList<>();
        scanDirectory(context.getProjectRoot(), thresholdBytes, findings, context);
        return List.copyOf(findings);
    }

    private void scanDirectory(File directory, long thresholdBytes,
                               List<Finding> findings, ValidationContext context) {
        File[] entries = directory.listFiles();
        if (entries == null) return;

        for (File entry : entries) {
            if (entry.isDirectory()) {
                if (!ValidatorFileUtils.shouldSkipDirectory(entry, context)) {
                    scanDirectory(entry, thresholdBytes, findings, context);
                }
            } else {
                long size = entry.length();
                if (size > thresholdBytes) {
                    findings.add(Finding.of(
                            Severity.WARNING,
                            getClass().getSimpleName(),
                            String.format(
                                    "Large file detected: %s (%.2f MB) — consider using Git LFS or removing from version control.",
                                    entry.getAbsolutePath(),
                                    (double) size / BYTES_PER_MB
                            )
                    ));
                }
            }
        }
    }
}