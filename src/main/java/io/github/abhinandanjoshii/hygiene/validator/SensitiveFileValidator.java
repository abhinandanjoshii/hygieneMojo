package io.github.abhinandanjoshii.hygiene.validator;

import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

public class SensitiveFileValidator {
    private static final List<String> SENSITIVE_FILENAMES = List.of(
            "env",
            ".env.local",
            ".env.production",
            ".env.staging",
            "application-local.properties",
            "application-local.yml",
            "application-local.yaml",
            "secrets.yml",
            "secrets.yaml",
            "credentials.json",
            "serviceaccount.json"
    );

    private static final List<String> SENSITIVE_EXTENSIONS = List.of(
            ".pem",
            ".key",
            ".p12",
            ".pfx",
            ".jks",
            ".keystore"
    );

    private static final Set<String> SKIP_DIRS = Set.of(
            "target", ".git", ".idea", "node_modules"
    );

    public static void validate(File projectRoot, Log log){
        boolean found = checkGitignoreAwareness(projectRoot,log);
        if(!found){
            log.info("No sensitive files detected in project directory.");
        }
    }

    private static boolean checkGitignoreAwareness(File projectRoot,Log log){
        List<String> gitignoreEntries = readGitignore(projectRoot);
        boolean anyFound = scanDirectory(projectRoot,log,gitignoreEntries);
        return anyFound;
    }

    private static boolean scanDirectory(File directory, Log log,List<String> gitignoreEntries){
        File[] files = directory.listFiles();
        if(files == null) return false;

        boolean anyFound = false;

        for(File file : files){
            if(file.isDirectory()) {
                if(SKIP_DIRS.contains(file.getName())) continue;
                if(scanDirectory(file,log,gitignoreEntries)) anyFound = true;
            }
            else {
                if(isSensitive(file.getName()) && !isGitignored(file.getName(),gitignoreEntries)){
                    log.warn("Sensitive file detected (not in .gitignore): "
                    + file.getAbsolutePath());
                    anyFound = true;
                }
            }
        }

        return anyFound;
    }

    private static boolean isSensitive(String fileName){
        for(String name : SENSITIVE_FILENAMES){
            if(fileName.equals(name)) return true;
        }
        for(String ext : SENSITIVE_EXTENSIONS){
            if(fileName.startsWith(ext)) return true;
        }
        return false;
    }

    private static boolean isGitignored(String fileName, List<String> gitignoreEntries){
        for(String entry : gitignoreEntries){
            String trimmed = entry.trim();
            if(trimmed.isEmpty() || trimmed.startsWith("#")) continue;
            if(trimmed.equals(fileName) || fileName.endsWith(trimmed.replace("*",""))) {
                return true;
            }
        }
        return false;
    }

    private static List<String> readGitignore(File projectRoot){
        File gitignore = new File(projectRoot,".gitignore");
        if(!gitignore.exists()) return List.of();
        try{
            return Files.readAllLines(gitignore.toPath());
        }
        catch (IOException e) {
            return List.of();
        }
    }
}
