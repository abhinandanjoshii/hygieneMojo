package io.github.abhinandanjoshii.hygiene.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User-facing configuration for all hygiene validators.
 *
 * <p>Every field in this class is directly bindable from the plugin's
 * {@code <configuration>} block in {@code pom.xml}. Maven's plugin framework
 * maps XML element names to field names automatically.</p>
 *
 * <p>All {@code additional*} fields are additive — they extend the built-in defaults.
 * Fields without the {@code additional} prefix ({@code readmeCandidates},
 * {@code licenseCandidates}) fully replace the built-in defaults when set.</p>
 *
 * <p>Example {@code pom.xml} configuration:</p>
 * <pre>{@code
 * <configuration>
 *   <additionalScannedExtensions>
 *     <extension>.tf</extension>
 *     <extension>.sh</extension>
 *   </additionalScannedExtensions>
 *   <additionalSensitiveFilenames>
 *     <filename>db-credentials.conf</filename>
 *   </additionalSensitiveFilenames>
 *   <additionalSensitiveExtensions>
 *     <extension>.vault</extension>
 *   </additionalSensitiveExtensions>
 *   <readmeCandidates>
 *     <candidate>README.md</candidate>
 *     <candidate>README.rst</candidate>
 *   </readmeCandidates>
 *   <licenseCandidates>
 *     <candidate>LICENSE</candidate>
 *     <candidate>COPYING</candidate>
 *   </licenseCandidates>
 *   <additionalSkipDirectories>
 *     <directory>vendor</directory>
 *     <directory>.terraform</directory>
 *   </additionalSkipDirectories>
 *   <additionalRequiredGitignorePatterns>
 *     <pattern>.terraform/</pattern>
 *     <pattern>*.tfstate</pattern>
 *   </additionalRequiredGitignorePatterns>
 * </configuration>
 * }</pre>
 *
 * @since 0.3.0
 */
public final class HygieneConfiguration {

    /**
     * Default constructor. All fields initialised to empty lists.
     * Populated via setters by {@link io.github.abhinandanjoshii.hygiene.HygieneMojo}.
     */
    public HygieneConfiguration() {}

    private List<String> additionalScannedExtensions        = new ArrayList<>();
    private List<String> additionalSensitiveFilenames       = new ArrayList<>();
    private List<String> additionalSensitiveExtensions      = new ArrayList<>();
    private List<String> readmeCandidates                   = new ArrayList<>();
    private List<String> licenseCandidates                  = new ArrayList<>();
    private List<String> additionalSkipDirectories          = new ArrayList<>();
    private List<String> additionalRequiredGitignorePatterns = new ArrayList<>();
    private int todoThreshold = 20;

    // -------------------------------------------------------------------------
    // Accessors — unmodifiable views for validators
    // -------------------------------------------------------------------------

    /**
     * Additional file extensions scanned by {@code MergeConflictValidator} and
     * {@code HardcodedSecretValidator}, merged with built-in defaults.
     * Include the leading dot: {@code .tf}, {@code .sh}.
     *
     * @return unmodifiable list
     */
    public List<String> getAdditionalScannedExtensions() {
        return Collections.unmodifiableList(additionalScannedExtensions);
    }

    /**
     * Additional exact filenames treated as sensitive by {@code SensitiveFileValidator},
     * merged with built-in defaults.
     *
     * @return unmodifiable list
     */
    public List<String> getAdditionalSensitiveFilenames() {
        return Collections.unmodifiableList(additionalSensitiveFilenames);
    }

    /**
     * Additional file extensions treated as sensitive by {@code SensitiveFileValidator},
     * merged with built-in defaults. Include the leading dot: {@code .vault}, {@code .gpg}.
     *
     * @return unmodifiable list
     */
    public List<String> getAdditionalSensitiveExtensions() {
        return Collections.unmodifiableList(additionalSensitiveExtensions);
    }

    /**
     * Candidate filenames for the README check.
     * When non-empty, replaces the built-in default list entirely.
     *
     * @return unmodifiable list
     */
    public List<String> getReadmeCandidates() {
        return Collections.unmodifiableList(readmeCandidates);
    }

    /**
     * Candidate filenames for the LICENSE check.
     * When non-empty, replaces the built-in default list entirely.
     *
     * @return unmodifiable list
     */
    public List<String> getLicenseCandidates() {
        return Collections.unmodifiableList(licenseCandidates);
    }

    /**
     * Additional directory names to skip during all file scans, merged with
     * built-in defaults ({@code target}, {@code .git}, {@code .idea},
     * {@code .mvn}, {@code node_modules}).
     *
     * @return unmodifiable list
     */
    public List<String> getAdditionalSkipDirectories() {
        return Collections.unmodifiableList(additionalSkipDirectories);
    }

    /**
     * Additional patterns that must appear in {@code .gitignore}, merged with
     * built-in required patterns. Missing entries produce a {@code WARNING} finding.
     *
     * @return unmodifiable list
     */
    public List<String> getAdditionalRequiredGitignorePatterns() {
        return Collections.unmodifiableList(additionalRequiredGitignorePatterns);
    }

    public int getTodoThreshold() {
        return todoThreshold;
    }

    // -------------------------------------------------------------------------
    // Setters — public, used only by HygieneMojo.buildConfiguration()
    // -------------------------------------------------------------------------

    /**
     * Sets additional file extensions scanned by validators.
     *
     * <p>Configured from the plugin's {@code pom.xml} configuration and merged
     * with built-in defaults.</p>
     *
     * @param v extensions to add, or {@code null} for none
     */
    public void setAdditionalScannedExtensions(List<String> v) {
        this.additionalScannedExtensions = v != null ? new ArrayList<>(v) : new ArrayList<>();
    }

    /**
     * Sets additional sensitive filenames.
     *
     * @param v filenames to add, or {@code null} for none
     */
    public void setAdditionalSensitiveFilenames(List<String> v) {
        this.additionalSensitiveFilenames = v != null ? new ArrayList<>(v) : new ArrayList<>();
    }

    /**
     * Sets additional sensitive file extensions.
     *
     * @param v extensions to add, or {@code null} for none
     */
    public void setAdditionalSensitiveExtensions(List<String> v) {
        this.additionalSensitiveExtensions = v != null ? new ArrayList<>(v) : new ArrayList<>();
    }

    /**
     * Sets README candidate filenames.
     *
     * <p>When non-empty, replaces the built-in defaults.</p>
     *
     * @param v README candidate filenames
     */
    public void setReadmeCandidates(List<String> v) {
        this.readmeCandidates = v != null ? new ArrayList<>(v) : new ArrayList<>();
    }

    /**
     * Sets LICENSE candidate filenames.
     *
     * <p>When non-empty, replaces the built-in defaults.</p>
     *
     * @param v LICENSE candidate filenames
     */
    public void setLicenseCandidates(List<String> v) {
        this.licenseCandidates = v != null ? new ArrayList<>(v) : new ArrayList<>();
    }


    /**
     * Sets additional directory names to skip during scans.
     *
     * @param v directory names to add
     */
    public void setAdditionalSkipDirectories(List<String> v) {
        this.additionalSkipDirectories = v != null ? new ArrayList<>(v) : new ArrayList<>();
    }

    /**
     * Sets additional required {@code .gitignore} patterns.
     *
     * @param v patterns to add
     */
    public void setAdditionalRequiredGitignorePatterns(List<String> v) {
        this.additionalRequiredGitignorePatterns = v != null ? new ArrayList<>(v) : new ArrayList<>();
    }

    void setTodoThreshold(int v) {
        this.todoThreshold = v;
    }

    // -------------------------------------------------------------------------
    // Static merge helpers — used by validators
    // -------------------------------------------------------------------------

    /**
     * Returns a merged unmodifiable set containing all elements of {@code defaults}
     * plus all elements of {@code additional}. Safe to call with an empty list.
     *
     * @param defaults  built-in default set from the validator
     * @param additional user-supplied additions from configuration
     * @return merged unmodifiable set
     */
    public static Set<String> merge(Set<String> defaults, List<String> additional) {
        if (additional == null || additional.isEmpty()) return defaults;
        Set<String> merged = new HashSet<>(defaults);
        merged.addAll(additional);
        return Collections.unmodifiableSet(merged);
    }

    /**
     * Returns {@code override} when non-null and non-empty, otherwise {@code defaults}.
     * Used when a user-supplied list should fully replace built-in defaults.
     *
     * @param defaults  built-in default list
     * @param override  user-supplied override list from configuration
     * @return override if non-empty, otherwise defaults
     */
    public static List<String> overrideOrDefault(List<String> defaults, List<String> override) {
        if (override == null || override.isEmpty()) return defaults;
        return Collections.unmodifiableList(override);
    }
}