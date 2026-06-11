package io.github.abhinandanjoshii.hygiene.validator;

import io.github.abhinandanjoshii.hygiene.model.Finding;
import io.github.abhinandanjoshii.hygiene.model.Severity;
import io.github.abhinandanjoshii.hygiene.model.ValidationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SystemOutValidator implements HygieneValidator{

    private static final Set<String> SCANNED_EXTENSIONS = Set.of(".java");

    private static final List<String> CONSOLE_PATTERNS = List.of(
            "System.out.print",
            "System.out.println",
            "System.out.printf",
            "System.err.print",
            "System.err.println",
            "System.err.printf",
            ".printStackTrace("
    );

    @Override
    public List<Finding> validate(ValidationContext context) {
        List<Finding> findings = new ArrayList<>();
        scanDirectory(context.getProjectRoot(), context.getProjectRoot(), findings, context);
        return List.copyOf(findings);
    }

    private void scanDirectory(File projectRoot, File directory, List<Finding> findings, ValidationContext context) {
        File[] entries = directory.listFiles();
        if (entries == null) return;

        for(File entry : entries){
            if(entry.isDirectory()){
                if(ValidatorFileUtils.shouldSkipDirectory(entry, context)) continue;
                if(isTestSourceDirectory(projectRoot, entry)) continue;
                scanDirectory(projectRoot, entry, findings, context);
            } else if (ValidatorFileUtils.hasExtension(entry.getName(), SCANNED_EXTENSIONS)){
                scanFile(entry, findings);
            }
        }
    }

    private void scanFile(File file, List<Finding> findings){
        List<String> lines = ValidatorFileUtils.readLinesSilently(file);

        for(int i = 0 ; i < lines.size(); i++){
            String trimmed = lines.get(i).trim();
            if(isComment(trimmed)) continue;

            for(String pattern : CONSOLE_PATTERNS){
                if(trimmed.contains(pattern)){
                    findings.add(Finding.of(Severity.WARNING,
                            getClass().getSimpleName(),
                            "Console output call detected: "
                                    +file.getAbsolutePath() + ":" + (i+1)
                                    +" â€” '" + pattern + "' should be replaced with a logger."
                    ));

                }
            }
        }
    }

    private static boolean isComment(String trimmed){
        return trimmed.startsWith("//")
                || trimmed.startsWith("*")
                || trimmed.startsWith("/*")
                || trimmed.startsWith("<!--");
    }

    private static boolean isTestSourceDirectory(File projectRoot, File directory) {
        String relative = projectRoot.toPath()
                .relativize(directory.toPath())
                .toString()
                .replace(File.separatorChar, '/');
        return relative.startsWith("src/test");
    }
}
