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
        checker("README.md");
        checker("LICENSE");
        getLog().info("project root : "+ project.getBasedir().getAbsolutePath());
    }

    public void checker(String filename){
        File file = new File(
                project.getBasedir(),
                filename
        );

        if(file.exists()){
            getLog().info(filename+" found.");
        }
        else{
            getLog().warn(filename+" missing.");
        }

    }

}
