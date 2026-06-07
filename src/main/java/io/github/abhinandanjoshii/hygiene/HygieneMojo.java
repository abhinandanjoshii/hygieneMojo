package io.github.abhinandanjoshii.hygiene;

import io.github.abhinandanjoshii.hygiene.validator.FileExistenceValidator;
import io.github.abhinandanjoshii.hygiene.validator.LargeFileValidator;
import io.github.abhinandanjoshii.hygiene.validator.MergeConflictValidator;
import io.github.abhinandanjoshii.hygiene.validator.SnapshotDependencyValidator;
import org.apache.maven.plugin.AbstractMojo;
import java.io.File;
import java.util.List;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name="check")
public class HygieneMojo extends AbstractMojo{

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
        MergeConflictValidator.validate();
    }
}
