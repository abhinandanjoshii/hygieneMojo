package io.github.abhinandanjoshii.hygiene.validator;

import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Detects hardcoded secrets and credentials in project source and configuration files.
 *
 * <p>Uses regex patterns to identify common credential patterns including passwords,
 * API keys, tokens, AWS access key prefixes, Bearer tokens, and private keys.</p>
 *
 * <p>Reports file path and line number without printing the secret value itself.</p>
 */
public class HardcodedSecretValidator {

    private HardcodedSecretValidator() {
        // utility class — no instantiation
    }

    private static final List<Pattern> SECRET_PATTERNS = List.of(
            Pattern.compile("(?i)(password|passwd|pwd)\\s*[:=]\\s*['\"]?[^\\s'\"]{4,}"),
            Pattern.compile("(?i)(api_key|apikey|api-key)\\s*[:=]\\s*['\"]?[^\\s'\"]{8,}"),
            Pattern.compile("(?i)(secret_key|secret|client_secret)\\s*[:=]\\s*['\"]?[^\\s'\"]{8,}"),
            Pattern.compile("(?i)(token|auth_token|access_token)\\s*[:=]\\s*['\"]?[^\\s'\"]{8,}"),
            Pattern.compile("AKIA[0-9A-Z]{16}"),
            Pattern.compile("(?i)Bearer\\s+[a-zA-Z0-9\\-._~+/]{20,}"),
            Pattern.compile("(?i)(private_key|privatekey)\\s*[:=]\\s*['\"]?[^\\s'\"]{8,}")
    );

    private static final Set<String> SCANNED_EXTENSIONS = Set.of(
            ".java",".xml",".yml",".yaml",".properties",
            ".json", ".env", ".config" , ".conf", ".ini"
    );

    private static final Set<String> SKIP_DIRS = Set.of(
            "target",".git",".idea","node_modules"
    );

    /**
     * Scans the project root directory for files containing hardcoded secrets.
     *
     * @param projectRoot the root directory of the Maven project
     * @param log         the Maven plugin logger
     */
    public static void validate(File projectRoot, Log log){
        boolean found = scanDirectory(projectRoot,log);
        if(!found){
            log.info("No hardcoded secrets detected.");
        }
    }

    /**
     * Recursively scans a directory for files containing hardcoded secret patterns.
     *
     * @param directory the directory to scan
     * @param log       the Maven plugin logger
     * @return {@code true} if any secrets were found, {@code false} otherwise
     */
    public static boolean scanDirectory(File directory, Log log){
        File[] files = directory.listFiles();
        if(files == null) return false;

        boolean anyFound = false;

        for(File file : files){
            if(file.isDirectory()){
                if(SKIP_DIRS.contains(file.getName())) continue;
                if(scanDirectory(file,log)) anyFound = true;
            }
            else {
                if(!hasScannedExtension(file.getName())) continue;
                if(scanFile(file,log)) anyFound = true;
            }
        }

        return anyFound;
    }

    private static boolean scanFile(File file,Log log){
        try{
            List<String> lines = Files.readAllLines(file.toPath());
            boolean anyFound = false;

            for(int i = 0; i < lines.size(); i++){
                String line = lines.get(i);
                for(Pattern pattern : SECRET_PATTERNS){
                    if(pattern.matcher(line).find()){
                        log.warn("Potential hardcoded sec"
                        + file.getAbsolutePath()
                        +" (line "+ (i+1) + ")");
                        anyFound = true;
                        break;
                    }
                }
            }
            return anyFound;
        } catch (IOException e) {
            log.debug("Could not read file: "+file.getAbsolutePath()
            + " - " + e.getMessage());
            return false;
        }
    }

    private static boolean hasScannedExtension(String fileName){
        for(String ext : SCANNED_EXTENSIONS){
            if(fileName.endsWith(ext)) return true;
        }
        return false;
    }
}
