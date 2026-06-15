package io.github.abhinandanjoshii.hygiene.report;

import io.github.abhinandanjoshii.hygiene.model.Finding;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class HygieneReportWriter {

    public static final String REPORT_FILENAME = "hygiene-report.json";

    private HygieneReportWriter() {}

    public static void write(HygieneReport report, File buildDirectory, Log log) {
        if (!buildDirectory.exists()) {
            buildDirectory.mkdirs();
        }

        File outputFile = new File(buildDirectory, REPORT_FILENAME);

        try (Writer writer = Files.newBufferedWriter(outputFile.toPath(), StandardCharsets.UTF_8)) {
            writer.write(toJson(report));
            log.info("HygieneMojo: report written to " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            log.warn("HygieneMojo: could not write hygiene report â€” " + e.getMessage());
        }
    }

    static String toJson(HygieneReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        sb.append("  \"project\": ").append(jsonString(
                report.getProjectGroupId() + ":"
                        + report.getProjectArtifactId() + ":"
                        + report.getProjectVersion())).append(",\n");

        sb.append("  \"timestamp\": ").append(jsonString(report.getTimestamp().toString())).append(",\n");
        sb.append("  \"passed\": ").append(report.isPassed()).append(",\n");

        sb.append("  \"summary\": {\n");
        sb.append("    \"errors\": ").append(report.getErrorCount()).append(",\n");
        sb.append("    \"warnings\": ").append(report.getWarningCount()).append(",\n");
        sb.append("    \"infos\": ").append(report.getInfoCount()).append("\n");
        sb.append("  },\n");

        sb.append("  \"findings\": [");
        if (report.getFindings().isEmpty()) {
            sb.append("]");
        } else {
            sb.append("\n");
            for (int i = 0; i < report.getFindings().size(); i++) {
                Finding f = report.getFindings().get(i);
                sb.append("    {\n");
                sb.append("      \"severity\": ").append(jsonString(f.severity().name())).append(",\n");
                sb.append("      \"validator\": ").append(jsonString(f.validatorName())).append(",\n");
                sb.append("      \"message\": ").append(jsonString(f.message())).append("\n");
                sb.append("    }");
                if (i < report.getFindings().size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("  ]");
        }

        sb.append("\n}");
        return sb.toString();
    }

    private static String jsonString(String value) {
        if (value == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default   -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}