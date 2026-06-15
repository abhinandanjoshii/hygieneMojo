package io.github.abhinandanjoshii.hygiene.validator;

import io.github.abhinandanjoshii.hygiene.model.Finding;
import io.github.abhinandanjoshii.hygiene.model.Severity;
import io.github.abhinandanjoshii.hygiene.model.ValidationContext;
import org.apache.maven.model.Dependency;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// TODO : Add Javadocs to each
public final class DependencyVersionPinningValidator implements HygieneValidator {

    private static final Set<String> TEST_ONLY_GROUP_PREFIXES = Set.of(
            "junit",
            "org.junit",
            "org.mockito",
            "org.assertj",
            "org.hamcrest",
            "org.testng",
            "org.easymock",
            "com.github.tomakehurst",
            "com.h2database", // maybe we may use future use in case of unit tests
            "org.springframework.boot:spring-boot-test"
            // TODO : Add more prefixes for prod cases
    );

    @Override
    public List<Finding> validate(ValidationContext context) {
        List<Dependency> dependencies = context.getProject().getDependencies();
        if (dependencies == null || dependencies.isEmpty()) {
            return List.of();
        }

        List<Finding> findings = new ArrayList<>();

        for (Dependency dep : dependencies) {
            String version = dep.getVersion();

            if (isVersionRange(version)) {
                findings.add(Finding.of(
                        Severity.ERROR,
                        getClass().getSimpleName(),
                        "Version range detected: "
                                + dep.getGroupId() + ":" + dep.getArtifactId()
                                + ":" + version
                                + " â€” version ranges break reproducible builds."
                                + " Use a pinned version instead."
                ));
            }

            if (isTestLibraryInCompileScope(dep)) {
                findings.add(Finding.of(
                        Severity.WARNING,
                        getClass().getSimpleName(),
                        "Test library in compile scope: "
                                + dep.getGroupId() + ":" + dep.getArtifactId()
                                + " â€” declare with <scope>test</scope> to prevent"
                                + " it bleeding into production artifacts."
                ));
            }
        }

        return List.copyOf(findings);
    }

    private static boolean isVersionRange(String version) {
        if (version == null || version.isBlank()) return false;
        char first = version.trim().charAt(0);
        return first == '[' || first == '(';
    }


    private static boolean isTestLibraryInCompileScope(Dependency dep) {
        String scope = dep.getScope();
        if ("test".equalsIgnoreCase(scope) || "provided".equalsIgnoreCase(scope)) {
            return false;
        }

        String groupId = dep.getGroupId();
        if (groupId == null) return false;

        for (String prefix : TEST_ONLY_GROUP_PREFIXES) {
            if (groupId.startsWith(prefix)) return true;
        }
        return false;
    }
}