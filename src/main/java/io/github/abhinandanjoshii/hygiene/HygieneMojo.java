package io.github.abhinandanjoshii.hygiene;

import io.github.abhinandanjoshii.hygiene.validator.FileExistenceValidator;
import io.github.abhinandanjoshii.hygiene.validator.LargeFileValidator;
import io.github.abhinandanjoshii.hygiene.validator.SnapshotDependencyValidator;
import org.apache.commons.lang3.NotImplementedException;
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
//        validatePluginVersions();
        LargeFileValidator.validate
                (
                        projectBase,
                        getLog()
                );
    }

//    private void validateLargeFiles() {
//        getLog().info("Scanning project for files greater than "
//        + (MAX_FILE_SIZE_IN_BYTES/1024/1024) + " MB"
//        );
//
//        scanDirectory(project.getBasedir());
//    }
//
//    private void validateFileExists(List<String> fileNames){
//        for(String fileName : fileNames){
//            File file = new File(
//                    project.getBasedir(),
//                    fileName
//            );
//
//            if(file.exists()){
//                getLog().info(fileName+" found.");
//                return;
//            }
//        }
//        getLog().warn("None of these files were found "+fileNames);
//    }
//
//    private void validateSnapshotDependency(){
//        getLog().info("hello into dependency code" + project.getDependencies());
//        for(Dependency dependency : project.getDependencies()){
//            getLog().info("dependency is " + dependency +
//            "version is "+ dependency.getVersion());
//            String version = dependency.getVersion();
//            if(version != null && version.endsWith("SNAPSHOT")){
//                getLog().warn(
//                        "SNAPSHOT dependency detected: "
//                        + dependency.getGroupId()
//                        + ":"
//                        + dependency.getArtifactId()
//                        + ":"
//                        + version
//                );
//            }
//        }
//    }
//
//    private void validatePluginVersions(){
//
//        if(project.getBuild() == null){
//            return;
//        }
//
//        for(Plugin plugin : project.getBuildPlugins()){
//            getLog().info("plugin version is " + plugin.getVersion());
//            if(plugin.getVersion() == null || plugin.getVersion().isBlank()){
//                getLog().warn("Plugin Missing Version: " +plugin.getArtifactId());
//            }
//        }
//    }
//
//    private void scanDirectory(File directory){
//
//        File[] files=directory.listFiles();
//
//        if(files == null) return;
//
//        for(File file : files){
//
//            if(file.isDirectory()){
//
//                if (file.getName().equals("target")
//                        || file.getName().equals(".git")
//                        || file.getName().equals(".idea")) {
//                    continue;
//                }
//
//                scanDirectory(file);
//            }
//
//            else{
////                getLog().info(
////                        "Checking: " + file.getAbsolutePath()
////                );
//                long fileSize = file.length();
//
//                if(fileSize > MAX_FILE_SIZE_IN_BYTES){
//
//                    double sizeInMb = (double) fileSize/(1024 *1024);
//
//                    getLog().warn(
//                            "Large File Detected: "
//                            + file.getAbsolutePath()
//                            + " ("
//                            +String.format("%.2f",sizeInMb)
//                            + " MB"
//                    );
//                }
//
//            }
//        }
//    }
}
