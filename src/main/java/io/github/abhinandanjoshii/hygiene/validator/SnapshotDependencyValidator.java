package io.github.abhinandanjoshii.hygiene.validator;

import io.github.abhinandanjoshii.hygiene.model.Finding;
import io.github.abhinandanjoshii.hygiene.model.Severity;
import io.github.abhinandanjoshii.hygiene.model.ValidationContext;
import org.apache.maven.model.Dependency;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates that no declared dependencies use SNAPSHOT versions.
 *
 * <p>SNAPSHOT versions are mutable — the same version string can resolve to different
 * artifacts at different points in time. This makes builds non-reproducible across
 * environments, CI pipelines, and developer machines, and is a supply-chain risk
 * in release artifacts.</p>
 *
 * <p>Each detected SNAPSHOT dependency produces an individual {@link Severity#ERROR}
 * finding so that all offenders are visible in a single build execution.</p>
 *
 * @since 0.3.0
 */
public final class SnapshotDependencyValidator implements HygieneValidator {

    private static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";

    /**
     * Checks all declared dependencies for SNAPSHOT version suffixes.
     *
     * @param context read-only project context
     * @return one ERROR finding per SNAPSHOT dependency; empty if none found
     */
    @Override
    public List<Finding> validate(ValidationContext context) {
        List<Dependency> dependencies = context.getProject().getDependencies();

        if (dependencies == null || dependencies.isEmpty()) {
            return List.of();
        }

        List<Finding> findings = new ArrayList<>();

        for (Dependency dep : dependencies) {
            String version = dep.getVersion();
            if (version != null && version.endsWith(SNAPSHOT_SUFFIX)) {
                findings.add(Finding.of(
                        Severity.ERROR,
                        getClass().getSimpleName(),
                        "SNAPSHOT dependency detected: "
                                + dep.getGroupId() + ":"
                                + dep.getArtifactId() + ":"
                                + version
                                + " — SNAPSHOT versions produce non-reproducible builds."
                ));
            }
        }

        return List.copyOf(findings);
    }
}