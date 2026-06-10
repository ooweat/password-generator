package com.ooweat.password.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Password {

    public static final String SHA_256 = "SHA-256";
    public static final String SHA_512 = "SHA-512";
    public static final String MD5 = "MD5";

    private static final String CHARS_UPPER = "ABCDEFGHJKLMNOPQRSTUVWXYZ"; // without I
    private static final String CHARS_LOWER = "abcdefghijkmnopqrstuvwxyz"; // without l
    private static final String CHARS_NUMBER = "023456789"; // without 1
    private static final String CHARS_SPECIAL = "!@#$%^&*";

    private Password() {
    }

    /**
     * @param upper     대문자 포함 여부
     * @param lower     소문자 포함 여부
     * @param number    숫자 포함 여부
     * @param special   특수문자 포함 여부
     * @param maxLength 생성할 비밀번호 길이 (선택된 규칙 수 이상이어야 함)
     * @param algorithm 해시 알고리즘 (null 이면 원문 반환)
     * @return algorithm 이 null 이면 원문, 아니면 해시 문자열
     */
    public static String generator(boolean upper, boolean lower,
        boolean number, boolean special,
        int maxLength, String algorithm) {
        if (!upper && !lower && !number && !special) {
            throw new IllegalArgumentException("At least one character type must be selected.");
        }

        SecureRandom random = new SecureRandom();
        StringBuilder pool = new StringBuilder();
        List<Character> chars = new ArrayList<>();

        if (upper) {
            pool.append(CHARS_UPPER);
            chars.add(pick(CHARS_UPPER, random));
        }
        if (lower) {
            pool.append(CHARS_LOWER);
            chars.add(pick(CHARS_LOWER, random));
        }
        if (number) {
            pool.append(CHARS_NUMBER);
            chars.add(pick(CHARS_NUMBER, random));
        }
        if (special) {
            pool.append(CHARS_SPECIAL);
            chars.add(pick(CHARS_SPECIAL, random));
        }

        if (maxLength < chars.size()) {
            throw new IllegalArgumentException(
                "maxLength must be at least " + chars.size() + " for the selected rules.");
        }

        String poolStr = pool.toString();
        while (chars.size() < maxLength) {
            chars.add(poolStr.charAt(random.nextInt(poolStr.length())));
        }
        Collections.shuffle(chars, random);

        StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            sb.append(c);
        }
        String raw = sb.toString();

        return algorithm == null ? raw : hash(raw, algorithm);
    }

    private static char pick(String chars, SecureRandom random) {
        return chars.charAt(random.nextInt(chars.length()));
    }

    public static String hash(String raw, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] bytes = md.digest(raw.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unsupported algorithm: " + algorithm, e);
        }
    }
}
