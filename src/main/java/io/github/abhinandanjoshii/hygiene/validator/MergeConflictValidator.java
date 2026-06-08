package io.github.abhinandanjoshii.hygiene.validator;

import org.apache.maven.plugin.logging.Log;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

public class MergeConflictValidator {

    private static final List<String> CONFLICT_MARKERS = List.of(
            "<<<<<<<",
            "=======",
            ">>>>>>>"
    );

    private static final Set<String> SCANNED_EXTENSIONS = Set.of(
            ".java",".xml",".yml",".yaml",".properties"
            ,".json"
    );

    private static final Set<String> SKIP_DIRS = Set.of(
            ".target",".git", ".idea", "node_modules"
    );


    public static void validate(File projectRoot, Log log)
    {
        boolean found = scanDirectory(projectRoot,log);
        if (!found) {
            log.info("No merge conflict markers found.");
        }
    }

    private static boolean scanDirectory(File directory,Log log){
        File[] files = directory.listFiles();
        if (files == null) return false;

        boolean anyFound = false;

        for(File file : files){
            if(file.isDirectory()){
                if(SKIP_DIRS.contains(file.getName())) continue;
                if(scanDirectory(file,log)) anyFound = true;
            }
            else{
            if(!hasScannedExtensions(file.getName())) continue;
            if(scanFile(file,log)) anyFound = true;
            }
        }
        return anyFound;
    }

    private static boolean scanFile(File file, Log log){
        try{
            List<String> lines = Files.readAllLines(file.toPath());
            boolean anyFound = false;

            for(int i = 0; i < lines.size() ; i++){
                String line = lines.get(i);
                for(String marker : CONFLICT_MARKERS){
                    if(line.startsWith(marker)){
                        log.warn("Merge conflict marker detected: "
                        + file.getAbsolutePath()
                        + " (line " + (i+1) + "): "
                        + line.trim());
                        anyFound = true;
                        break;
                    }
                }
            }
            return anyFound;
        } catch (IOException e) {
            log.debug("Could not read file: " + file.getAbsolutePath() + " - " + e.getMessage());
            return false;
        }
    }

    private static boolean hasScannedExtensions(String fileName){
        for(String ext : SCANNED_EXTENSIONS){
            if(fileName.endsWith(ext)) return true;
        }
        return false;
    }
}
