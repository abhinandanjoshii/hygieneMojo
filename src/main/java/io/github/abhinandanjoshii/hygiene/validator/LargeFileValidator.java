package io.github.abhinandanjoshii.hygiene.validator;

import org.apache.maven.plugin.logging.Log;
import java.io.File;

/**
 * Detects files in the project directory that exceed a configured size threshold.
 *
 * <p>Large files committed to version control bloat repository size and slow down
 * {@code git clone} and CI pipeline times. Skips the {@code target/} build directory.</p>
 */
public class LargeFileValidator {

    private LargeFileValidator() {
        // utility class — no instantiation
    }

    /**
     * Scans the project root directory for files exceeding the configured size limit.
     *
     * @param projectRoot   the root directory of the Maven project
     * @param log           the Maven plugin logger
     * @param maxFileSizeMb the maximum allowed file size in megabytes
     */
    public static void validate(File projectRoot, Log log , int maxFileSizeMb){
        long maxFileSizeBytes = maxFileSizeMb * 1024L * 1024L;
        scanDirectory(
                projectRoot,
                log,
                maxFileSizeBytes
        );
    }

    private static void scanDirectory(File directory,Log log, long maxFileSizeBytes){

        File[] files = directory.listFiles();

        if(files == null) return;

        for(File file : files){

            if(file.isDirectory()){

                if (file.getName().equals("target")
                        || file.getName().equals(".git")
                        || file.getName().equals(".idea")) {
                    continue;
                }

                scanDirectory(
                        file,
                        log,
                        maxFileSizeBytes
                );

            }
            else {

                long fileSize = file.length();

                if (fileSize > maxFileSizeBytes) {

                    double sizeInMb =
                            (double) fileSize / (1024 * 1024);

                    log.warn(
                            "Large file detected: "
                                    + file.getAbsolutePath()
                                    + " ("
                                    + String.format("%.2f", sizeInMb)
                                    + " MB)"
                    );
                }

            }
        }

    }

}
