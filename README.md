# password

비밀번호 생성·검증·인코딩을 담은 순수 Java 라이브러리.  
프레임워크 의존 없이 동작하며, `Password.generator()` 한 번 호출로 랜덤 비밀번호 생성부터 해시까지 처리한다.

---

## 패키지

```
com.ooweat.password
```

---

## 공개 API

### `Password.generator()`

```java
PasswordResult result = Password.generator(
    boolean upper,     // 대문자 A-Z 포함 여부
    boolean lower,     // 소문자 a-z 포함 여부
    boolean number,    // 숫자 0-9 포함 여부
    boolean special,   // 특수문자 !@#$%^&* 포함 여부
    String  algorithm  // 해시 알고리즘 (Password.SHA_256 등)
);
```

선택한 문자 종류로 12자리 랜덤 비밀번호를 생성하고, 지정한 알고리즘으로 해시한 결과를 반환한다.  
선택된 각 문자 종류에서 최소 1자를 보장한다.

### 알고리즘 상수

| 상수 | 값 | 해시 길이 |
|------|----|-----------|
| `Password.SHA_256` | `"SHA-256"` | 64자 |
| `Password.SHA_512` | `"SHA-512"` | 128자 |
| `Password.MD5`     | `"MD5"`     | 32자 |

### `PasswordResult`

| 메서드 | 설명 |
|--------|------|
| `getRaw()` | 생성된 평문 비밀번호 |
| `getEncoded()` | 해시된 비밀번호 |
| `getAlgorithm()` | 사용된 알고리즘 이름 |

---

## 사용 예시

```java
// 전체 규칙, SHA-256
PasswordResult result = Password.generator(true, true, true, true, Password.SHA_256);
System.out.println(result.getRaw());      // e.g. "aB3!kLm9@qRz"
System.out.println(result.getEncoded());  // SHA-256 해시

// 대문자 + 숫자만, SHA-512
PasswordResult result2 = Password.generator(true, false, true, false, Password.SHA_512);

// MD5
PasswordResult result3 = Password.generator(true, true, true, true, Password.MD5);
```

---

## 내부 구조

```
src/com/ooweat/password/
├── Password.java                         공개 API (generator, 알고리즘 상수)
├── PasswordResult.java                   결과 객체 (raw, encoded, algorithm)
├── Main.java                             CLI 진입점
├── util/
│   ├── PasswordRule.java                 규칙 enum (UPPER / LOWER / NUMBER / SPECIAL)
│   ├── PasswordValidator.java            규칙 검증기
│   └── PasswordEncoder.java              인코더 인터페이스
└── exception/
    └── PasswordValidationException.java  규칙 위반 예외 (위반 목록 포함)

src/test/com/ooweat/password/
└── PasswordTest.java                     테스트 러너 (의존성 없음)
```

---

## CLI 실행

```bash
# 기본 (전체 규칙, SHA-256)
java com.ooweat.password.Main

# SHA-512, 대문자+숫자만
java com.ooweat.password.Main --encoding SHA-512 --rules UPPER,NUMBER

# MD5, 소문자+특수문자만
java com.ooweat.password.Main -e MD5 -r LOWER,SPECIAL
```

옵션:

| 옵션 | 설명 | 기본값 |
|------|------|--------|
| `-e`, `--encoding` | 해시 알고리즘 | `SHA-256` |
| `-r`, `--rules` | 쉼표 구분 규칙 | `UPPER,LOWER,NUMBER,SPECIAL` |

---

## 왜 이렇게 설계했는가

### `PasswordRule` 을 `enum` 으로 선언한 이유

규칙을 `Predicate<String>` 리스트로 관리하면 어떤 규칙이 실패했는지 호출자에게 전달할 방법이 없다.  
`enum` 을 사용하면 위반 목록을 `List<PasswordRule>` 로 반환할 수 있고,  
`PasswordValidationException` 에 위반된 규칙 전체를 담아 호출자가 구체적인 안내를 할 수 있다.

### `PasswordEncoder` 를 인터페이스로 분리한 이유

SHA-256 은 salt 가 없어 Rainbow Table 공격에 취약하다.  
BCrypt · Argon2 같은 더 안전한 알고리즘은 외부 라이브러리가 필요하다.  
라이브러리가 특정 알고리즘을 강제하면 소비자의 기술 스택을 제한하게 되므로,  
인터페이스만 정의하고 구현은 소비자가 주입하도록 했다.

---

## 요구사항

- Java 1.8 이상
- 외부 의존성 없음
