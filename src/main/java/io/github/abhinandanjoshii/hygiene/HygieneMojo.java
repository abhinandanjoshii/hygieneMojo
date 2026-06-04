package io.github.abhinandanjoshii.hygiene;

import org.apache.maven.plugin.AbstractMojo;
import java.io.File;
import java.util.List;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name="check")
public class HygieneMojo extends AbstractMojo{

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Override
    public void execute(){
        getLog().info("project root : "+ project.getBasedir().getAbsolutePath());
        getLog().info("HygieneMojo running");
        validateFileExists(List.of(
                "README.md",
                "README",
                "readme.md"
                ));
        validateFileExists(
                List.of(
                        "LICENSE",
                        "LICENSE.txt",
                        "LICENSE.md"
                )
        );
        getLog().info("hello");
        validateSnapshotDependency();
        validatePluginVersions();
    }

    private void validateFileExists(List<String> fileNames){
        for(String fileName : fileNames){
            File file = new File(
                    project.getBasedir(),
                    fileName
            );

            if(file.exists()){
                getLog().info(fileName+" found.");
                return;
            }
        }
        getLog().warn("None of these files were found "+fileNames);
    }

    private void validateSnapshotDependency(){
        getLog().info("hello into dependency code" + project.getDependencies());
        for(Dependency dependency : project.getDependencies()){
            getLog().info("dependency is " + dependency +
            "version is "+ dependency.getVersion());
            String version = dependency.getVersion();
            if(version != null && version.endsWith("SNAPSHOT")){
                getLog().warn(
                        "SNAPSHOT dependency detected: "
                        + dependency.getGroupId()
                        + ":"
                        + dependency.getArtifactId()
                        + ":"
                        + version
                );
            }
        }
    }

    private void validatePluginVersions(){

        if(project.getBuild() == null){
            return;
        }

        for(Plugin plugin : project.getBuildPlugins()){
            getLog().info("plugin version is " + plugin.getVersion());
            if(plugin.getVersion() == null || plugin.getVersion().isBlank()){
                getLog().warn("Plugin Missing Version: " +plugin.getArtifactId());
            }
        }
    }

}
