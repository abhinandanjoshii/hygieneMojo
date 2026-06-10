package io.github.abhinandanjoshii.hygiene.validator;

import io.github.abhinandanjoshii.hygiene.model.Finding;
import io.github.abhinandanjoshii.hygiene.model.ValidationContext;
import java.util.List;

/**
 * Contract for all hygiene validation rules.
 *
 * <p>Each validator encapsulates exactly one policy concern. Validators are pure:
 * they receive a read-only {@link ValidationContext}, perform their analysis, and
 * return an immutable list of {@link Finding} instances. They never log directly,
 * never mutate shared state, and have no side effects beyond reading the filesystem
 * or project model.</p>
 *
 * <p>This design keeps each validator independently testable — construct a context,
 * call {@code validate}, assert the finding list.</p>
 *
 * <p>New rules are added by implementing this interface and registering the instance
 * in {@link io.github.abhinandanjoshii.hygiene.HygieneMojo}.</p>
 *
 * @since 0.3.0
 */
public interface HygieneValidator {

    /**
     * Runs this validator against the supplied project context.
     *
     * @param context read-only project context
     * @return an unmodifiable list of findings; empty if the project passes this rule
     */
    List<Finding> validate(ValidationContext context);
}