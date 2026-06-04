package io.github.abhinandanjoshii.hygiene;

import org.apache.maven.plugin.AbstractMojo;
import java.io.File;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name="check")
public class HygieneMojo extends AbstractMojo{

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Override
    public void execute(){
        getLog().info("HygieneMojo running");

        File readme = new File(
                project.getBasedir(),
                "README.md"
        );

        if(readme.exists()){
            getLog().info("README.md found.");
        }
        else {
            getLog().warn("README.md missing.");
        }
        getLog().info("project root : "+ project.getBasedir().getAbsolutePath());
    }

}
