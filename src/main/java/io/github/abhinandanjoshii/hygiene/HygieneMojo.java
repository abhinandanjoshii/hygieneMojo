package io.github.abhinandanjoshii.hygiene;

import io.github.abhinandanjoshii.hygiene.validator.*;
import org.apache.maven.plugin.AbstractMojo;
import java.io.File;
import java.util.List;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Main Mojo for the hygiene-maven-plugin.
 *
 * <p>Runs all registered hygiene validators against the project directory,
 * including checks.</p>
 *
 * <p>Usage:</p>
 * <pre>
 *   mvn hygiene:check
 * </pre>
 */
@Mojo(name="check")
public class HygieneMojo extends AbstractMojo{

    /**
     * Default constructor for HygieneMojo.
     * Instantiated by the Maven plugin framework at runtime.
     */
    public HygieneMojo() {
        // Maven instantiates this via reflection
    }

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    @Parameter(property = "hygiene.maxFileSizeMb", defaultValue = "10")
    private int maxFileSizeMb;

    @Override
    public void execute(){
        getLog().info("HygieneMojo running");
        File projectBase = project.getBasedir();
        FileExistenceValidator.validate(projectBase,List.of(
                "README.md",
                "README",
                "readme.md"
                ),getLog());
        FileExistenceValidator.validate(projectBase,
                List.of(
                        "LICENSE",
                        "LICENSE.txt",
                        "LICENSE.md"
                ),
                getLog()
        );
        SnapshotDependencyValidator.validate
                (
                        project.getDependencies(),
                        getLog()
                );
        LargeFileValidator.validate
                (
                        projectBase,
                        getLog(),
                        maxFileSizeMb
                );
        MergeConflictValidator.validate
                (
                        projectBase,
                        getLog()
                );
        HardcodedSecretValidator.validate(
                projectBase,
                getLog()
        );
        SensitiveFileValidator.validate(
                projectBase,
                getLog()
        );
        GitignoreValidator.validate(
                projectBase,
                getLog()
        );
    }
}
