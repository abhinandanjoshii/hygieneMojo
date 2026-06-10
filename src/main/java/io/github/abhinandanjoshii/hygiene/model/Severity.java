package io.github.abhinandanjoshii.hygiene.model;

/**
 * Severity level of a hygiene {@link Finding}
 *
 * <p>Ordered from least to most critical. {@link #ERROR} findings can be configured
 * to fail the build; {@link #WARNING} findings can optimally fail the build when
 * {@code failOnWarning} is enabled.</p>
 *
 * @since 0.3.0
 */
public enum Severity {

    /** Informational observation. Never blocks the build. */
    INFO,

    /** A hygiene issue that should be addressed but does not block the build by default. */
    WARNING,

    /**
     * A policy violation that indicates the release-quality, supply-chain or security risk.
     * Blocks the build when {@code failOnError} is {@code true} (the default).
     */
    ERROR
}
