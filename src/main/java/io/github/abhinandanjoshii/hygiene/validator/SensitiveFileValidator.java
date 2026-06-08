package io.github.abhinandanjoshii.hygiene.validator;

import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

/**
 * Detects sensitive files present in the project directory that are not excluded by {@code .gitignore}.
 *
 * <p>Checks for files such as {@code .env}, {@code *.pem}, {@code *.key}, {@code *.p12},
 * {@code *.jks}, {@code application-local.properties}, {@code secrets.yml},
 * and {@code serviceaccount.json}.</p>
 *
 * <p>A file is only flagged if it exists AND is not already covered by an entry
 * in the project's {@code .gitignore}.</p>
 */
public class SensitiveFileValidator {

    private SensitiveFileValidator() {
        // utility class — no instantiation
    }

    private static final List<String> SENSITIVE_FILENAMES = List.of(
            ".env",
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

    /**
     * Scans the project root for sensitive files not excluded by {@code .gitignore}.
     *
     * @param projectRoot the root directory of the Maven project
     * @param log         the Maven plugin logger
     */
    public static void validate(File projectRoot, Log log){
        boolean found = checkGitignoreAwareness(projectRoot,log);
        if(!found){
            log.info("No sensitive files detected in project directory.");
        }
    }

    private static boolean checkGitignoreAwareness(File projectRoot,Log log){
        List<String> gitignoreEntries = readGitignore(projectRoot);
        boolean anyFound = scanDirectory(projectRoot, projectRoot,log,gitignoreEntries);
        return anyFound;
    }

    private static boolean scanDirectory(File projectRoot, File directory, Log log,List<String> gitignoreEntries){
        File[] files = directory.listFiles();
        if(files == null) return false;

        boolean anyFound = false;

        for(File file : files){
            if(file.isDirectory()) {
                if(SKIP_DIRS.contains(file.getName())) continue;
                if(scanDirectory(projectRoot, file,log,gitignoreEntries)) anyFound = true;
            }
            else {
                String relativePath = projectRoot.toPath()
                        .relativize(file.toPath())
                        .toString()
                        .replace(File.separatorChar, '/');
                if(isSensitive(file.getName()) && !isGitignored(file.getName(), relativePath, gitignoreEntries)){
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
            if(fileName.endsWith(ext)) return true;
        }
        return false;
    }

    private static boolean isGitignored(String fileName, String relativePath, List<String> gitignoreEntries){
        for(String entry : gitignoreEntries) {
            String trimmed = entry.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
            String normalized = trimmed.startsWith("./") ? trimmed.substring(2) : trimmed;
            normalized = normalized.startsWith("/") ? normalized.substring(1) : normalized;
            if (normalized.equals(fileName) || normalized.equals(relativePath)) return true;
            if (normalized.startsWith("*")
                    && (fileName.endsWith(normalized.substring(1))
                    || relativePath.endsWith(normalized.substring(1)))) return true;
            if (normalized.endsWith("*")
                    && (fileName.startsWith(normalized.substring(0, normalized.length() - 1))
                    || relativePath.startsWith(normalized.substring(0, normalized.length() - 1)))) return true;
            if (trimmed.contains("*")) {
                String needle = normalized.replace("*", "");
                if (!needle.isEmpty() && (fileName.contains(needle) || relativePath.contains(needle))) return true;
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
