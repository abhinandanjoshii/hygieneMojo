package io.github.abhinandanjoshii.hygiene.validator;

import io.github.abhinandanjoshii.hygiene.model.Finding;
import io.github.abhinandanjoshii.hygiene.model.HygieneConfiguration;
import io.github.abhinandanjoshii.hygiene.model.Severity;
import io.github.abhinandanjoshii.hygiene.model.ValidationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates that required project documentation files exist in the project root.
 *
 * <p>Checks for a README and a LICENSE file. The candidate filename lists can be
 * overridden in {@code pom.xml} via {@code readmeCandidates} and
 * {@code licenseCandidates} configuration elements.</p>
 *
 * <h2>Configuration</h2>
 * <pre>{@code
 * <configuration>
 *   <readmeCandidates>
 *     <candidate>README.md</candidate>
 *     <candidate>README.rst</candidate>
 *   </readmeCandidates>
 *   <licenseCandidates>
 *     <candidate>LICENSE</candidate>
 *     <candidate>COPYING</candidate>
 *   </licenseCandidates>
 * </configuration>
 * }</pre>
 *
 * @since 0.3.0
 */
public final class FileExistenceValidator implements HygieneValidator {

    /**
     * Default constructor. Instantiated by HygieneMojo at runtime.
     */
    public FileExistenceValidator() {}

    private static final List<String> DEFAULT_README_CANDIDATES =
            List.of("README.md", "README", "readme.md", "README.rst", "README.txt");

    private static final List<String> DEFAULT_LICENSE_CANDIDATES =
            List.of("LICENSE", "LICENSE.txt", "LICENSE.md", "COPYING");

    private record DocumentGroup(String label, List<String> candidates) {}

    /**
     * Checks that README and LICENSE files are present in the project root.
     *
     * @param context read-only project context
     * @return list of findings; empty if both files are present
     */
    @Override
    public List<Finding> validate(ValidationContext context) {
        HygieneConfiguration cfg = context.getConfiguration();
        File root = context.getProjectRoot();

        List<DocumentGroup> groups = List.of(
                new DocumentGroup("README",
                        HygieneConfiguration.overrideOrDefault(
                                DEFAULT_README_CANDIDATES, cfg.getReadmeCandidates())),
                new DocumentGroup("LICENSE",
                        HygieneConfiguration.overrideOrDefault(
                                DEFAULT_LICENSE_CANDIDATES, cfg.getLicenseCandidates()))
        );

        List<Finding> findings = new ArrayList<>();

        for (DocumentGroup group : groups) {
            boolean found = group.candidates().stream()
                    .anyMatch(name -> new File(root, name).isFile());

            if (!found) {
                findings.add(Finding.of(
                        Severity.WARNING,
                        getClass().getSimpleName(),
                        group.label() + " not found. Expected one of: " + group.candidates()
                ));
            }
        }

        return List.copyOf(findings);
    }
}