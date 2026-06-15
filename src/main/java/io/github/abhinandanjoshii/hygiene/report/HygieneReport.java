package io.github.abhinandanjoshii.hygiene.report;

import io.github.abhinandanjoshii.hygiene.model.Finding;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

// TODO : Add Javadocs to each
public final class HygieneReport {

    private final String projectGroupId;
    private final String projectArtifactId;
    private final String projectVersion;
    private final Instant timestamp;
    private final List<Finding> findings;
    private final long errorCount;
    private final long warningCount;
    private final long infoCount;

    public HygieneReport(String projectGroupId,
                         String projectArtifactId,
                         String projectVersion,
                         Instant timestamp,
                         List<Finding> findings) {
        this.projectGroupId    = Objects.requireNonNull(projectGroupId);
        this.projectArtifactId = Objects.requireNonNull(projectArtifactId);
        this.projectVersion    = Objects.requireNonNull(projectVersion);
        this.timestamp         = Objects.requireNonNull(timestamp);
        this.findings          = List.copyOf(Objects.requireNonNull(findings));
        this.errorCount   = findings.stream().filter(f -> f.severity().name().equals("ERROR")).count();
        this.warningCount = findings.stream().filter(f -> f.severity().name().equals("WARNING")).count();
        this.infoCount    = findings.stream().filter(f -> f.severity().name().equals("INFO")).count();
    }

    public String getProjectGroupId()    { return projectGroupId; }
    public String getProjectArtifactId() { return projectArtifactId; }
    public String getProjectVersion()    { return projectVersion; }
    public Instant getTimestamp()        { return timestamp; }
    public List<Finding> getFindings()   { return findings; }
    public long getErrorCount()          { return errorCount; }
    public long getWarningCount()        { return warningCount; }
    public long getInfoCount()           { return infoCount; }
    public boolean isPassed()            { return errorCount == 0; }
}