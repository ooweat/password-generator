package com.ooweat.password.util;

import java.util.regex.Pattern;

public enum PasswordRule {

    UPPER(".*[A-Z].*", "at least one uppercase letter (A-Z)"),
    LOWER(".*[a-z].*", "at least one lowercase letter (a-z)"),
    NUMBER(".*[0-9].*", "at least one digit (0-9)"),
    SPECIAL(".*[!@#$%^&*].*", "at least one special character (!@#$%^&*)");

    private final Pattern pattern;
    private final String description;

    PasswordRule(String regex, String description) {
        this.pattern = Pattern.compile(regex);
        this.description = description;
    }

    public boolean isSatisfiedBy(String password) {
        return password != null && pattern.matcher(password).matches();
    }

    public String getDescription() {
        return description;
    }
}
