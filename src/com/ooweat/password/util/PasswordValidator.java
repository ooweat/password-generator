package com.ooweat.password.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PasswordValidator {

    private PasswordValidator() {
    }

    public static List<PasswordRule> validate(String password, List<PasswordRule> rules) {
        return rules.stream()
            .filter(rule -> !rule.isSatisfiedBy(password))
            .collect(Collectors.toList());
    }

    public static List<PasswordRule> validate(String password) {
        return validate(password, Arrays.asList(PasswordRule.values()));
    }

    public static boolean isValid(String password) {
        return validate(password).isEmpty();
    }
}
