package io.github.abhinandanjoshii.hygiene.validator;

import org.apache.maven.plugin.logging.Log;
import java.io.File;
import java.util.List;

/**
 * Validates the presence of required project files such as README and LICENSE.
 *
 * <p>Checks that at least one candidate filename from the provided list
 * exists in the project root directory.</p>
 */
public class FileExistenceValidator {

    private FileExistenceValidator() {
        // utility class — no instantiation
    }

    /**
     * Checks that at least one of the given candidate filenames exists in the project root.
     *
     * @param projectRoot the root directory of the Maven project
     * @param fileNames  list of acceptable filenames to check (e.g. "README.md", "README")
     * @param log         the Maven plugin logger
     */
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
