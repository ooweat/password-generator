package com.ooweat.password.exception;

import com.ooweat.password.util.PasswordRule;
import java.util.List;
import java.util.stream.Collectors;

public class PasswordValidationException extends RuntimeException {

    private final List<PasswordRule> violations;

    public PasswordValidationException(List<PasswordRule> violations) {
        super("Password does not satisfy: " +
            violations.stream()
                .map(PasswordRule::getDescription)
                .collect(Collectors.joining(", ")));
        this.violations = violations;
    }

    public List<PasswordRule> getViolations() {
        return violations;
    }
}
