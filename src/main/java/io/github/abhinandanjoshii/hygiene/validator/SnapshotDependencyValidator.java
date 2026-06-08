package io.github.abhinandanjoshii.hygiene.validator;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;

import java.util.List;

/**
 * Validates that no SNAPSHOT dependencies are declared in the project.
 *
 * <p>SNAPSHOT dependencies are non-reproducible — the same version string can resolve
 * to different artifacts at different points in time, causing inconsistent builds
 * across environments and CI pipelines.</p>
 */
public class SnapshotDependencyValidator {

    private SnapshotDependencyValidator() {
        // utility class — no instantiation
    }

    /**
     * Checks the project dependency list for any artifacts with SNAPSHOT versions.
     *
     * @param dependencies the list of declared project dependencies
     * @param log          the Maven plugin logger
     */
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
