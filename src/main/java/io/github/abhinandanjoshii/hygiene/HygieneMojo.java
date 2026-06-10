package io.github.abhinandanjoshii.hygiene;

import io.github.abhinandanjoshii.hygiene.model.Finding;
import io.github.abhinandanjoshii.hygiene.model.HygieneConfiguration;
import io.github.abhinandanjoshii.hygiene.model.Severity;
import io.github.abhinandanjoshii.hygiene.model.ValidationContext;
import io.github.abhinandanjoshii.hygiene.validator.FileExistenceValidator;
import io.github.abhinandanjoshii.hygiene.validator.GitignoreValidator;
import io.github.abhinandanjoshii.hygiene.validator.HardcodedSecretValidator;
import io.github.abhinandanjoshii.hygiene.validator.HygieneValidator;
import io.github.abhinandanjoshii.hygiene.validator.LargeFileValidator;
import io.github.abhinandanjoshii.hygiene.validator.MergeConflictValidator;
import io.github.abhinandanjoshii.hygiene.validator.SensitiveFileValidator;
import io.github.abhinandanjoshii.hygiene.validator.SnapshotDependencyValidator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for the {@code hygiene:check} goal.
 *
 * <p>Runs all registered hygiene validators against the project directory,
 * aggregates {@link Finding} instances, prints a structured report, and
 * enforces the configured build-failure policy.</p>
 *
 * <h3>Minimal usage</h3>
 * <pre>
 *   mvn hygiene:check
 * </pre>
 *
 * <h3>Full pom.xml configuration reference</h3>
 * <pre>{@code
 * <plugin>
 *   <groupId>io.github.abhinandanjoshii</groupId>
 *   <artifactId>hygiene-maven-plugin</artifactId>
 *   <version>0.3.0</version>
 *   <configuration>
 *     <failOnError>true</failOnError>
 *     <failOnWarning>false</failOnWarning>
 *     <skip>false</skip>
 *     <maxFileSizeMb>10</maxFileSizeMb>
 *     <additionalScannedExtensions>
 *       <extension>.tf</extension>
 *     </additionalScannedExtensions>
 *     <additionalSensitiveFilenames>
 *       <filename>db-credentials.conf</filename>
 *     </additionalSensitiveFilenames>
 *     <additionalSensitiveExtensions>
 *       <extension>.vault</extension>
 *     </additionalSensitiveExtensions>
 *     <readmeCandidates>
 *       <candidate>README.md</candidate>
 *     </readmeCandidates>
 *     <licenseCandidates>
 *       <candidate>LICENSE</candidate>
 *     </licenseCandidates>
 *     <additionalSkipDirectories>
 *       <directory>vendor</directory>
 *     </additionalSkipDirectories>
 *     <additionalRequiredGitignorePatterns>
 *       <pattern>*.tfstate</pattern>
 *     </additionalRequiredGitignorePatterns>
 *   </configuration>
 * </plugin>
 * }</pre>
 *
 * @since 0.3.0
 */
@Mojo(name = "check", threadSafe = true)
public class HygieneMojo extends AbstractMojo {

    /**
     * Default constructor for HygieneMojo.
     * Instantiated by the Maven plugin framework at runtime via reflection.
     */
    public HygieneMojo() {
        // Maven instantiates this via reflection
    }

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "hygiene.maxFileSizeMb", defaultValue = "10")
    private int maxFileSizeMb;

    /** When {@code true}, any ERROR finding fails the build. Default: {@code true}. */
    @Parameter(property = "hygiene.failOnError", defaultValue = "true")
    private boolean failOnError;

    /** When {@code true}, any WARNING finding also fails the build. Default: {@code false}. */
    @Parameter(property = "hygiene.failOnWarning", defaultValue = "false")
    private boolean failOnWarning;

    /** When {@code true}, skips all checks. Useful for bypassing in specific Maven profiles. */
    @Parameter(property = "hygiene.skip", defaultValue = "false")
    private boolean skip;

    @Parameter
    private List<String> additionalScannedExtensions         = new ArrayList<>();

    @Parameter
    private List<String> additionalSensitiveFilenames        = new ArrayList<>();

    @Parameter
    private List<String> additionalSensitiveExtensions       = new ArrayList<>();

    @Parameter
    private List<String> readmeCandidates                    = new ArrayList<>();

    @Parameter
    private List<String> licenseCandidates                   = new ArrayList<>();

    @Parameter
    private List<String> additionalSkipDirectories           = new ArrayList<>();

    @Parameter
    private List<String> additionalRequiredGitignorePatterns = new ArrayList<>();

    @Override
    public void execute() throws MojoFailureException {
        if (skip) {
            getLog().info("HygieneMojo: skipping all checks (hygiene.skip=true)");
            return;
        }

        ValidationContext context = ValidationContext.builder()
                .project(project)
                .maxFileSizeMb(maxFileSizeMb)
                .configuration(buildConfiguration())
                .build();

        List<Finding> allFindings = runValidators(context);
        printReport(allFindings);
        enforcePolicy(allFindings);
    }

    private HygieneConfiguration buildConfiguration() {
        HygieneConfiguration cfg = new HygieneConfiguration();
        cfg.setAdditionalScannedExtensions(additionalScannedExtensions);
        cfg.setAdditionalSensitiveFilenames(additionalSensitiveFilenames);
        cfg.setAdditionalSensitiveExtensions(additionalSensitiveExtensions);
        cfg.setReadmeCandidates(readmeCandidates);
        cfg.setLicenseCandidates(licenseCandidates);
        cfg.setAdditionalSkipDirectories(additionalSkipDirectories);
        cfg.setAdditionalRequiredGitignorePatterns(additionalRequiredGitignorePatterns);
        return cfg;
    }

    private List<HygieneValidator> buildValidators() {
        return List.of(
                new FileExistenceValidator(),
                new SnapshotDependencyValidator(),
                new LargeFileValidator(),
                new MergeConflictValidator(),
                new HardcodedSecretValidator(),
                new SensitiveFileValidator(),
                new GitignoreValidator()
        );
    }

    private List<Finding> runValidators(ValidationContext context) {
        List<Finding> all = new ArrayList<>();
        for (HygieneValidator validator : buildValidators()) {
            try {
                all.addAll(validator.validate(context));
            } catch (Exception e) {
                getLog().error("Validator '" + validator.getClass().getSimpleName()
                        + "' threw an unexpected exception: " + e.getMessage(), e);
            }
        }
        return all;
    }

    private void printReport(List<Finding> findings) {
        getLog().info("");
        getLog().info("------------------------------------------");
        getLog().info("  HygieneMojo Report");
        getLog().info("------------------------------------------");

        if (findings.isEmpty()) {
            getLog().info("  All hygiene checks passed.");
        } else {
            for (Finding f : findings) {
                switch (f.severity()) {
                    case INFO    -> getLog().info("  "  + f.toLogLine());
                    case WARNING -> getLog().warn("  "  + f.toLogLine());
                    case ERROR   -> getLog().error("  " + f.toLogLine());
                }
            }
        }

        long errors   = findings.stream().filter(f -> f.severity() == Severity.ERROR).count();
        long warnings = findings.stream().filter(f -> f.severity() == Severity.WARNING).count();
        long infos    = findings.stream().filter(f -> f.severity() == Severity.INFO).count();

        getLog().info("------------------------------------------");
        getLog().info(String.format("  Summary: %d error(s)  %d warning(s)  %d info(s)",
                errors, warnings, infos));
        getLog().info("------------------------------------------");
        getLog().info("");
    }

    private void enforcePolicy(List<Finding> findings) throws MojoFailureException {
        boolean hasErrors   = findings.stream().anyMatch(f -> f.severity() == Severity.ERROR);
        boolean hasWarnings = findings.stream().anyMatch(f -> f.severity() == Severity.WARNING);

        if (failOnError && hasErrors) {
            throw new MojoFailureException(
                    "HygieneMojo: build failed — ERROR-level hygiene violations detected. "
                            + "Review the findings above and resolve them before releasing."
            );
        }
        if (failOnWarning && hasWarnings) {
            throw new MojoFailureException(
                    "HygieneMojo: build failed — WARNING-level hygiene violations detected "
                            + "(failOnWarning=true). Review the findings above."
            );
        }
    }
}