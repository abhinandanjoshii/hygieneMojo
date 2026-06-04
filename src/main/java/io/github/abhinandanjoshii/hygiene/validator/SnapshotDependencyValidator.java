package io.github.abhinandanjoshii.hygiene.validator;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;

import java.util.List;

public class SnapshotDependencyValidator {
    public static void validate(List<Dependency> dependencies, Log log){

        for(Dependency dependency : dependencies){

            String version = dependency.getVersion();

            if(version!= null && version.endsWith("SNAPSHOT")){
                log.warn("SNAPSHOT dependency detected: "
                        + dependency.getGroupId()
                        +":"
                        +dependency.getArtifactId()
                        +":"
                        +version
                        );
            }

        }

    }
}
