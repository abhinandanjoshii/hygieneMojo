package io.github.abhinandanjoshii.hygiene.validator;

import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.util.List;

public class FileExistenceValidator {
    public static void validate(
            File projectRoot,
            List<String> fileNames,
            Log log
    ) {
        for(String fileName : fileNames){

            File file = new File(
                    projectRoot,
                    fileName
            );

            if(file.exists()){
                log.info(fileName + " found.");
                return;
            }
        }

        log.warn("None of the files were found: " +
                fileNames);

    }
}
