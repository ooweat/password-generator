package com.ooweat.password;

import com.ooweat.password.exception.PasswordValidationException;
import com.ooweat.password.model.Password;
import com.ooweat.password.util.PasswordEncoder;
import com.ooweat.password.util.PasswordRule;
import com.ooweat.password.util.PasswordValidator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PasswordTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== Password test ===\n");

        System.out.println(Password.generator(true, true, true, true, 8, null));

        // testGenerator();
        // testValidationAllRules();
        // testValidationSelectedRules();
        // testValidationException();
        // testEncoding();

        System.out.println("\n=== result: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) System.exit(1);
    }

    // ------------------------------------------------------------------ //
    //  Password.generator()
    // ------------------------------------------------------------------ //

    private static void testGenerator() {
        System.out.println("[Password.generator()]");

        // null algorithm → 원문 반환
        String raw = Password.generator(true, true, true, true, 12, Password.MD5);
        assertEquals("원문 길이 = 12",    12, raw.length());
        assertTrue("대문자 포함",          PasswordRule.UPPER.isSatisfiedBy(raw));
        assertTrue("소문자 포함",          PasswordRule.LOWER.isSatisfiedBy(raw));
        assertTrue("숫자 포함",            PasswordRule.NUMBER.isSatisfiedBy(raw));
        assertTrue("특수문자 포함",        PasswordRule.SPECIAL.isSatisfiedBy(raw));

        // SHA-256 → 해시 반환 (64자)
        String h256 = Password.generator(true, true, true, true, 12, Password.SHA_256);
        assertEquals("SHA-256 해시 길이 = 64",  64, h256.length());
        assertFalse("원문 ≠ SHA-256 해시",       raw.equals(h256));

        // SHA-512 → 128자
        String h512 = Password.generator(true, true, true, true, 12, Password.SHA_512);
        assertEquals("SHA-512 해시 길이 = 128", 128, h512.length());

        // MD5 → 32자
        String hMd5 = Password.generator(true, true, true, true, 12, Password.MD5);
        assertEquals("MD5 해시 길이 = 32",       32, hMd5.length());

        // maxLength 반영
        String raw16 = Password.generator(true, true, true, true, 16, null);
        assertEquals("원문 길이 = 16", 16, raw16.length());

        // 일부 규칙만
        String raw2 = Password.generator(true, false, true, false, 8, null);
        assertEquals("원문 길이 = 8",  8, raw2.length());
        assertTrue("UPPER 포함",       PasswordRule.UPPER.isSatisfiedBy(raw2));
        assertTrue("NUMBER 포함",      PasswordRule.NUMBER.isSatisfiedBy(raw2));

        // 두 번 호출 → 다른 원문 (확률적)
        String raw3 = Password.generator(true, true, true, true, 12, null);
        assertFalse("두 번 호출 → 다른 raw", raw.equals(raw3));

        // null algorithm 일 때 원문 == hash(raw, SHA_256) 과 다름
        String hashed = Password.hash(raw, Password.SHA_256);
        assertFalse("null 결과는 해시가 아님", raw.equals(hashed));

        // non-null algorithm 일 때 결과 == hash(raw, algorithm)
        String rawFixed = Password.generator(true, true, true, true, 12, null);
        String hashFixed = Password.hash(rawFixed, Password.SHA_256);
        assertEquals("hash(raw, SHA_256) 일관성", 64, hashFixed.length());

        // maxLength < 선택된 규칙 수 → 예외
        try {
            Password.generator(true, true, true, true, 2, null);
            fail("maxLength 너무 작음 → 예외 기대");
        } catch (IllegalArgumentException e) {
            assertTrue("maxLength 최소값 예외", e.getMessage().contains("maxLength"));
        }

        // 모두 false → 예외
        try {
            Password.generator(false, false, false, false, 12, null);
            fail("모두 false → 예외 기대");
        } catch (IllegalArgumentException e) {
            assertTrue("IllegalArgumentException 발생", true);
        }

        // 잘못된 알고리즘 → 예외
        try {
            Password.generator(true, true, true, true, 12, "INVALID");
            fail("잘못된 알고리즘 → 예외 기대");
        } catch (RuntimeException e) {
            assertTrue("잘못된 알고리즘 예외", e.getMessage().contains("INVALID"));
        }
    }

    // ------------------------------------------------------------------ //
    //  PasswordValidator
    // ------------------------------------------------------------------ //

    private static void testValidationAllRules() {
        System.out.println("\n[전체 규칙 검증]");

        assertTrue("유효한 비밀번호 - 위반 없음",
                PasswordValidator.validate("NewPass1!").isEmpty());

        assertTrue("대문자 누락 → UPPER 위반",
                PasswordValidator.validate("newpass1!").contains(PasswordRule.UPPER));

        assertFalse("대문자 누락 → LOWER 위반 없음",
                PasswordValidator.validate("newpass1!").contains(PasswordRule.LOWER));

        assertTrue("소문자 누락 → LOWER 위반",
                PasswordValidator.validate("NEWPASS1!").contains(PasswordRule.LOWER));

        assertTrue("숫자 누락 → NUMBER 위반",
                PasswordValidator.validate("NewPass!!").contains(PasswordRule.NUMBER));

        assertTrue("특수문자 누락 → SPECIAL 위반",
                PasswordValidator.validate("NewPass11").contains(PasswordRule.SPECIAL));

        assertTrue("isValid - 유효",    PasswordValidator.isValid("NewPass1!"));
        assertFalse("isValid - 무효",   PasswordValidator.isValid("newpass"));
    }

    private static void testValidationSelectedRules() {
        System.out.println("\n[선택 규칙 검증]");

        List<PasswordRule> upperLower = Arrays.asList(PasswordRule.UPPER, PasswordRule.LOWER);
        assertTrue("UPPER,LOWER만 - 숫자/특수문자 없어도 통과",
                PasswordValidator.validate("NewPass", upperLower).isEmpty());

        assertTrue("UPPER,LOWER만 - 대문자 없으면 위반",
                PasswordValidator.validate("newpass", upperLower).contains(PasswordRule.UPPER));

        List<PasswordRule> specialOnly = Collections.singletonList(PasswordRule.SPECIAL);
        assertTrue("SPECIAL만 - 특수문자 없으면 위반",
                PasswordValidator.validate("NewPass11", specialOnly).contains(PasswordRule.SPECIAL));

        assertTrue("SPECIAL만 - 특수문자 있으면 통과",
                PasswordValidator.validate("NoNumber!", specialOnly).isEmpty());

        List<PasswordRule> numberSpecial = Arrays.asList(PasswordRule.NUMBER, PasswordRule.SPECIAL);
        assertEquals("NUMBER,SPECIAL - 둘 다 없으면 위반 2개",
                2, PasswordValidator.validate("NewPass", numberSpecial).size());
    }

    private static void testValidationException() {
        System.out.println("\n[PasswordValidationException]");

        try {
            List<PasswordRule> v = PasswordValidator.validate("newpass");
            throw new PasswordValidationException(v);
        } catch (PasswordValidationException e) {
            assertTrue("예외 메시지에 UPPER 설명 포함",  e.getMessage().contains("uppercase"));
            assertTrue("예외 메시지에 NUMBER 설명 포함", e.getMessage().contains("digit"));
            assertTrue("violations 목록 존재",           !e.getViolations().isEmpty());
        }
    }

    // ------------------------------------------------------------------ //
    //  PasswordEncoder
    // ------------------------------------------------------------------ //

    private static void testEncoding() {
        System.out.println("\n[인코딩]");

        String password = "NewPass1!";

        PasswordEncoder sha256 = buildEncoder("SHA-256");
        String hash256 = sha256.encode(password);
        assertEquals("SHA-256 해시 길이 = 64",  64,  hash256.length());
        assertTrue("SHA-256 일관성",              sha256.encode(password).equals(hash256));
        assertTrue("SHA-256 matches() true",      sha256.matches(password, hash256));
        assertFalse("SHA-256 matches() false",    sha256.matches("WrongPass!", hash256));

        String hash512 = buildEncoder("SHA-512").encode(password);
        assertEquals("SHA-512 해시 길이 = 128", 128, hash512.length());

        String hashMd5 = buildEncoder("MD5").encode(password);
        assertEquals("MD5 해시 길이 = 32",       32, hashMd5.length());

        assertFalse("SHA-256 ≠ SHA-512", hash256.equals(hash512));
        assertFalse("SHA-256 ≠ MD5",     hash256.equals(hashMd5));

        try {
            buildEncoder("INVALID").encode(password);
            fail("잘못된 알고리즘 → 예외 기대");
        } catch (RuntimeException e) {
            assertTrue("잘못된 알고리즘 예외 메시지", e.getMessage().contains("INVALID"));
        }
    }

    private static PasswordEncoder buildEncoder(final String algorithm) {
        return new PasswordEncoder() {
            @Override
            public String encode(String raw) {
                try {
                    MessageDigest md = MessageDigest.getInstance(algorithm);
                    byte[] hash = md.digest(raw.getBytes());
                    StringBuilder sb = new StringBuilder();
                    for (byte b : hash) sb.append(String.format("%02x", b));
                    return sb.toString();
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException("Unsupported algorithm: " + algorithm, e);
                }
            }
            @Override
            public boolean matches(String raw, String encoded) {
                return encode(raw).equals(encoded);
            }
        };
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    private static void assertTrue(String name, boolean condition) {
        if (condition) {
            System.out.println("  PASS  " + name);
            passed++;
        } else {
            System.out.println("  FAIL  " + name);
            failed++;
        }
    }

    private static void assertFalse(String name, boolean condition) {
        assertTrue(name, !condition);
    }

    private static void assertEquals(String name, int expected, int actual) {
        assertTrue(name + " (expected=" + expected + ", actual=" + actual + ")", expected == actual);
    }

    private static void assertEquals(String name, String expected, String actual) {
        assertTrue(name + " (expected=" + expected + ", actual=" + actual + ")",
                expected != null && expected.equals(actual));
    }

    private static void fail(String name) {
        System.out.println("  FAIL  " + name);
        failed++;
    }
}
