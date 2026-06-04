package io.github.abhinandanjoshii.hygiene.validator;

import org.apache.maven.plugin.logging.Log;

import java.io.File;

public class LargeFileValidator {

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;

    public static void validate(File projectRoot, Log log){
        scanDirectory(
                projectRoot,
                log
        );
    }

    private static void scanDirectory(File directory,Log log){

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
                        log
                );

            }
            else {

                long fileSize = file.length();

                if (fileSize > MAX_FILE_SIZE_BYTES) {

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
