package io.github.abhinandanjoshii.hygiene.validator;

import io.github.abhinandanjoshii.hygiene.model.Finding;
import io.github.abhinandanjoshii.hygiene.model.ValidationContext;

import java.util.List;

public class SystemOutValidator implements HygieneValidator{

    @Override
    public List<Finding> validate(ValidationContext context) {
        return List.of();
    }
}
