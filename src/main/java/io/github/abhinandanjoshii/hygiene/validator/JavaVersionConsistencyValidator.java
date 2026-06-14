package io.github.abhinandanjoshii.hygiene.validator;

import io.github.abhinandanjoshii.hygiene.model.Finding;
import io.github.abhinandanjoshii.hygiene.model.Severity;
import io.github.abhinandanjoshii.hygiene.model.ValidationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public final class JavaVersionConsistencyValidator implements HygieneValidator {

    private static final String PROP_SOURCE  = "maven.compiler.source";
    private static final String PROP_TARGET  = "maven.compiler.target";
    private static final String PROP_RELEASE = "maven.compiler.release";
    private static final String PROP_JAVA    = "java.version";

    @Override
    public List<Finding> validate(ValidationContext context) {
        Properties props = context.getProject().getProperties();
        if (props == null) {
            return List.of();
        }

        String source  = normalize(props.getProperty(PROP_SOURCE));
        String target  = normalize(props.getProperty(PROP_TARGET));
        String release = normalize(props.getProperty(PROP_RELEASE));
        String java    = normalize(props.getProperty(PROP_JAVA));

        List<Finding> findings = new ArrayList<>();

        if (source == null && target == null && release == null && java == null) {
            findings.add(Finding.of(
                    Severity.INFO,
                    getClass().getSimpleName(),
                    "No Java version properties found in pom.xml."
                            + " Consider declaring <maven.compiler.release>21</maven.compiler.release>"
                            + " (or source/target) to ensure consistent bytecode across environments."
            ));
            return List.copyOf(findings);
        }

        if (source != null && target != null && !source.equals(target)) {
            findings.add(Finding.of(
                    Severity.WARNING,
                    getClass().getSimpleName(),
                    "Java version mismatch: maven.compiler.source=" + source
                            + " but maven.compiler.target=" + target
                            + " â€” these must match to produce consistent bytecode."
            ));
        }

        if (java != null) {
            if (source != null && !java.equals(source)) {
                findings.add(Finding.of(
                        Severity.WARNING,
                        getClass().getSimpleName(),
                        "Java version mismatch: java.version=" + java
                                + " but maven.compiler.source=" + source
                                + " â€” align these to avoid unexpected bytecode version."
                ));
            }
            if (target != null && !java.equals(target)) {
                findings.add(Finding.of(
                        Severity.WARNING,
                        getClass().getSimpleName(),
                        "Java version mismatch: java.version=" + java
                                + " but maven.compiler.target=" + target
                                + " â€” align these to avoid unexpected bytecode version."
                ));
            }
        }

        if (release != null) {
            if (source != null && !release.equals(source)) {
                findings.add(Finding.of(
                        Severity.WARNING,
                        getClass().getSimpleName(),
                        "Java version mismatch: maven.compiler.release=" + release
                                + " but maven.compiler.source=" + source
                                + " â€” when using 'release', source and target are redundant."
                                + " Remove them or align values."
                ));
            }
            if (target != null && !release.equals(target)) {
                findings.add(Finding.of(
                        Severity.WARNING,
                        getClass().getSimpleName(),
                        "Java version mismatch: maven.compiler.release=" + release
                                + " but maven.compiler.target=" + target
                                + " â€” when using 'release', source and target are redundant."
                                + " Remove them or align values."
                ));
            }
        }

        return List.copyOf(findings);
    }


    private static String normalize(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String trimmed = raw.trim();
        // Normalise "1.8" -> "8", "1.11" -> "11"  (old-style Java version naming)
        if (trimmed.startsWith("1.") && trimmed.length() > 2) {
            return trimmed.substring(2);
        }
        return trimmed;
    }
}