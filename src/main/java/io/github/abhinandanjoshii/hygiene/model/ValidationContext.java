package io.github.abhinandanjoshii.hygiene.model;

import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.Objects;

/**
 * Encapsulates all project information and user configuration that validators may need.
 *
 * <p>A single {@code ValidationContext} is constructed once by the mojo and passed
 * to every validator. Validators read from it; they never modify it.</p>
 *
 * @since 0.3.0
 */
public final class ValidationContext {

    private final MavenProject       project;
    private final int                maxFileSizeMb;
    private final HygieneConfiguration configuration;

    private ValidationContext(Builder builder) {
        this.project       = Objects.requireNonNull(builder.project,        "project must not be null");
        this.maxFileSizeMb = builder.maxFileSizeMb;
        this.configuration = Objects.requireNonNull(builder.configuration,  "configuration must not be null");
    }

    /**
     * Returns the Maven project.
     *
     * @return Maven project, never null
     */
    public MavenProject getProject() {
        return project;
    }

    /**
     * Convenience accessor for the project base directory.
     *
     * @return project root directory
     */
    public File getProjectRoot() {
        return project.getBasedir();
    }

    /**
     * The configured large-file threshold in megabytes. Default is {@code 10}.
     *
     * @return max file size in MB
     */
    public int getMaxFileSizeMb() {
        return maxFileSizeMb;
    }

    /**
     * The full user-supplied plugin configuration.
     *
     * @return hygiene configuration, never null
     */
    public HygieneConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Creates a new builder for {@code ValidationContext}.
     *
     * @return new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link ValidationContext}.
     */
    public static final class Builder {

        private MavenProject         project;
        private int                  maxFileSizeMb = 10;
        private HygieneConfiguration configuration = new HygieneConfiguration();

        private Builder() {}

        /**
         * Sets the Maven project.
         *
         * @param project Maven project
         * @return this builder
         */
        public Builder project(MavenProject project) {
            this.project = project;
            return this;
        }

        /**
         * Sets the large-file threshold in megabytes. Must be positive.
         *
         * @param maxFileSizeMb threshold in MB
         * @return this builder
         */
        public Builder maxFileSizeMb(int maxFileSizeMb) {
            if (maxFileSizeMb <= 0) {
                throw new IllegalArgumentException(
                        "maxFileSizeMb must be positive, got: " + maxFileSizeMb);
            }
            this.maxFileSizeMb = maxFileSizeMb;
            return this;
        }

        /**
         * Sets the hygiene configuration.
         *
         * @param configuration user-supplied configuration
         * @return this builder
         */
        public Builder configuration(HygieneConfiguration configuration) {
            this.configuration = Objects.requireNonNull(configuration);
            return this;
        }

        /**
         * Builds the {@link ValidationContext}.
         *
         * @return new context instance
         */
        public ValidationContext build() {
            return new ValidationContext(this);
        }
    }
}