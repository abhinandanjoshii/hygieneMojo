package io.github.abhinandanjoshii.hygiene.model;

import java.util.Objects;

/**
 * An immutable observation produced by a {@link io.github.abhinandanjoshii.hygiene.validator.HygieneValidator}.
 *
 * <p>Validators do not log or print output directly. Each validator returns a list of
 * {@code Finding} instances. The mojo aggregates them and handles all logging and
 * build-outcome decisions centrally.</p>
 *
 * <p>Example:</p>
 * <pre>
 *   Finding.of(Severity.WARNING, "SnapshotDependencyValidator",
 *       "SNAPSHOT dependency detected: com.example:demo-library:1.0.0-SNAPSHOT")
 * </pre>
 *
 * @param severity      how serious this finding is
 * @param validatorName simple class name of the validator that produced this finding
 * @param message       human-readable description of the issue
 * @since 0.3.0
 */
public record Finding(Severity severity, String validatorName , String message) {

    /**
     * Canonical compact constructor : validates invariants.
     */
    public Finding{
        Objects.requireNonNull(severity,"severity must not be null");
        Objects.requireNonNull(validatorName,"validatorName must not be null");
        Objects.requireNonNull(message,"message must not be null");
        if (validatorName.isBlank()) throw new IllegalArgumentException("validatorName must not be blank");
        if (message.isBlank())  throw new IllegalArgumentException("message must not be blank");
    }

    /**
     * Convenience factory — avoids {@code new Finding(…)} verbosity in validators.
     *
     * @param severity      severity level
     * @param validatorName producing validator
     * @param message       description
     * @return a new {@code Finding}
     */
    public static Finding of(Severity severity, String validatorName, String message) {
        return new Finding(severity, validatorName, message);
    }

    /**
     * Returns a single-line representation suitable for log output.
     *
     * <p>Format: {@code [ValidatorName] message}</p>
     * <p>Severity is already conveyed by Maven's log level (INFO/WARN/ERROR) —
     * printing it again in the message body produces redundant output.</p>
     *
     * @return formatted log line
     */
    public String toLogLine() {
        return "[" + validatorName + "] " + message;
    }
}
