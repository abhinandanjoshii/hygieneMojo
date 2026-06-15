package io.github.abhinandanjoshii.hygiene.validator;

import io.github.abhinandanjoshii.hygiene.model.Finding;
import io.github.abhinandanjoshii.hygiene.model.Severity;
import io.github.abhinandanjoshii.hygiene.model.ValidationContext;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO : Add Javadocs to each
public class TodoCommentValidator implements HygieneValidator{

    private static final Set<String> SCANNED_EXTENSIONS = Set.of(
            ".java", ".xml", ".yml", ".yaml", ".properties",
            ".json", ".md", ".sql", ".groovy", ".kt"
    );

    private static final List<String> TODO_MARKERS = List.of(
            "TODO", "FIXME", "HACK", "XXX", "NOSONAR" , "BUG"
    );

    @Override
    public List<Finding> validate(ValidationContext context) {
        int threshold = context.getConfiguration().getTodoThreshold();

        Map<String, Integer> packageCounts = new LinkedHashMap<>();
        int[] totalCount = {0};

        scanDirectory(context.getProjectRoot(), context.getProjectRoot(), packageCounts, totalCount, context);

        int total = totalCount[0];

        if(total == 0) return List.of();

        String summary = buildSummary(total, packageCounts, threshold);

        Severity severity = total > threshold ? Severity.WARNING : Severity.INFO;

        return List.of(Finding.of(severity, getClass().getSimpleName(), summary));
    }

    private void scanDirectory(File projectRoot, File directory, Map<String, Integer> packageCounts, int[] totalCount, ValidationContext context){
        File[] entries = directory.listFiles();
        if(entries == null) return;

        for( File entry : entries){
            if(entry.isDirectory()){
                if(ValidatorFileUtils.shouldSkipDirectory(entry, context)) continue;
                if(isTestSourceDirectory(projectRoot, entry)) continue;
                scanDirectory(projectRoot, entry, packageCounts, totalCount, context);
            } else if(ValidatorFileUtils.hasExtension(entry.getName(), SCANNED_EXTENSIONS)){
                scanFile(projectRoot, entry, packageCounts,totalCount);
            }
        }
    }

    private void scanFile(File projectRoot, File file, Map<String, Integer> packageCounts, int[] totalCount){
        List<String> lines = ValidatorFileUtils.readLinesSilently(file);

        String relativePath = projectRoot.toPath()
                .toString()
            .replace(File.separatorChar, '/');
        String packageKey = derivePackageKey(relativePath);

        for(String line : lines) {
            String upper = line.toUpperCase();
            for(String marker : TODO_MARKERS){
                if(upper.contains(marker)){
                    totalCount[0]++;
                    packageCounts.merge(packageKey, 1, Integer::sum);
                    break;
                }
            }
        }
    }

    private String derivePackageKey(String relativePath) {
        int lastSlash =  relativePath.lastIndexOf('/');
        if (lastSlash <= 0) return "(root)";
        String parentPath = relativePath.substring(0, lastSlash);
        String stripped = parentPath
                .replaceFirst("^src/main/java/", "")
                .replaceFirst("^src/main/resources/", "")
                .replaceFirst("^src/main/", "");
        return stripped.isEmpty() ? "(root)" : stripped;
    }

    private static String buildSummary(int total, Map<String, Integer> packageCounts, int threshold){
        StringBuilder sb = new StringBuilder();
        sb.append(total).append(" TODO/FIXME/HACK comments found");
        if (total > threshold) {
            sb.append(" (threshold: ").append(threshold).append(" â€” consider tracking these as issues)");
        }
        sb.append(".");

        packageCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> sb.append("\n    ").append(e.getKey())
                        .append(": ").append(e.getValue()));

        return sb.toString();
    }

    private static boolean isTestSourceDirectory(File projectRoot, File directory) {
        String relative = projectRoot.toPath()
                .relativize(directory.toPath())
                .toString()
                .replace(File.separatorChar, '/');
        return relative.startsWith("src/test");
    }
}