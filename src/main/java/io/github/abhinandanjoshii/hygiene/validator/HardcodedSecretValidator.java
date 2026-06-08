package io.github.abhinandanjoshii.hygiene.validator;

import java.util.List;
import java.util.regex.Pattern;

public class HardcodedSecretValidator {

    private static final List<Pattern> SECRET_PATTERNS = List.of(
            // TODO : add more checks for other secrets
            Pattern.compile("(?i)(password|passwd|pwd)\\s*=\\s*['\"]?[^\\s'\"]{4,}"),
            Pattern.compile("(?i)(api_key|apikey|api-key)\\s*[:=]\\s*['\"]?[^\\s'\"]{8,}"),
            Pattern.compile("(?i)(secret_key|secret|client_secret)\\s*[:=]\\s*['\"]?[^\\s'\"]{8,}"),
            Pattern.compile("(?i)(token|auth_token|access_token)\\s*[:=]\\s*['\"]?[^\\s'\"]{8,}"),
            Pattern.compile("AKIA[0-9A-Z]{16}"),
            Pattern.compile("(?i)Bearer\\s+[a-zA-Z0-9\\-._~+/]{20,}"),
            Pattern.compile("(?i)(private_key|privatekey)\\s*[:=]\\s*['\"]?[^\\s'\"]{8,}")
    );
}
