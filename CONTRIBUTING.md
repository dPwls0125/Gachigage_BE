# 🛒 같이가게 (Gachigage) 개발 컨벤션 및 가이드라인

이 문서는 같이가게 팀의 원활한 협업과 일관된 코드 품질 유지를 위한 그라운드 룰(Ground Rules)입니다.  
모든 팀원은 개발 시작 전 이 문서를 숙지해 주세요.

---

## 1. 🛠 기술 스택 (Tech Stack)

버전 호환성 이슈를 방지하기 위해 아래 버전을 엄격히 준수합니다.

| 분류                | 기술                  | 버전 / 비고                   |
|-------------------|---------------------|---------------------------|
| **Language**      | Java                | **17 (LTS)**              |
| **Framework**     | Spring Boot         | **3.5.9**                 |
| **Build Tool**    | Gradle              | **8.14.3**                |
| **Database**      | MySQL               | **8.0** (Docker Image 권장) |
| **Security**      | Spring Security     | Boot Managed (6.x)        |
| **Documentation** | SpringDoc (Swagger) | 최신 버전                     |

---

## 2. 📝 코드 컨벤션 (Code Convention)

### 2-1. 네이밍 규칙

- **클래스(Class):** `PascalCase` (예: `UserResponseDto`)
- **메서드/변수:** `camelCase` (예: `findUserById`, `userCount`)
- **DB 테이블/컬럼:** `snake_case` (예: `users`, `created_at`)
- **상수(Constant):** `UPPER_SNAKE_CASE` (예: `MAX_LOGIN_RETRY`)
- **패키지:** 소문자 사용, 가급적 한 단어 (예: `com.team.project.domain.user`)

### 2-2. 롬복(Lombok) 사용 규칙

- **Entity:** `@Setter` 사용 지양 (의도를 나타내는 메서드 생성 권장).
- **생성자:** `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 사용 (JPA 스펙 준수 및 무분별한 객체 생성 방지).

### 2-3. 코드 포맷팅

- **IntelliJ 기본 포맷터**를 사용합니다.
- [Naver Code Formatter](https://github.com/naver/hackday-conventions-java/blob/master/rule-config/naver-intellij-formatter.xml)
  를 사용
- [Naver Checkstyle Rule](https://github.com/naver/hackday-conventions-java/blob/master/rule-config/naver-checkstyle-rules.xml)
  를 사용
- 커밋 전 `Ctrl + Alt + L` (Mac: `Cmd + Opt + L`)을 눌러 줄맞춤을 수행

---

## 3. 📂 프로젝트 구조 (Package Structure)

도메인형 패키지 구조를 따릅니다. 관련된 코드를 도메인별로 응집시킵니다.

```text
src/main/java/com/team/project
 ├── global         # 전역 설정 (Config, Exception, Common DTO)
 ├── domain
 │    ├── user      # 도메인명
 │    │    ├── controller
 │    │    ├── service
 │    │    ├── repository
 │    │    ├── entity
 │    │    └── dto
 │    └── product
 └── ProjectApplication.java
```

---

## 4. 🐙 깃 & 협업 전략 (Git Workflow Strategies)

### 4-1. 브랜치 전략

- **main**: 배포 가능한 상태의 기준 브랜치
- **develop**: 다음 배포를 위해 개발 중인 브랜치
- **{commit type}/{기능명}**: 실제 기능 개발이 이루어지는 브랜치
    - 예: `feature/login`, `docs/contributing-guide`

### 4-2. 커밋 메시지 컨벤션

`[<type>] <subject>` 형식을 따릅니다.

| **Type**     | **설명**                    |
|--------------|---------------------------|
| **feat**     | 새로운 기능 추가                 |
| **fix**      | 버그 수정                     |
| **docs**     | 문서 수정 (README, Swagger 등) |
| **refactor** | 코드 리팩토링 (기능 변경 없음)        |
| **test**     | 테스트 코드 추가/수정              |
| **chore**    | 빌드 설정, 패키지 매니저 설정 등       |

- **Example:** `[feat] 회원가입 유효성 검사 로직 추가 (#12)`

---

## 5. 📡 API 스펙 및 응답 (API Specs & Response Format)

**Response Wrapper**

프론트엔드와의 협업을 위해 모든 API 응답은 아래 포맷으로 통일합니다.

```json
{
  "status": "SUCCESS",
  // or "FAIL", "ERROR"
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "userId": 1,
    "name": "홍길동"
  }
}
```

- **성공 시:** `GlobalResponse.success(data)` 사용
- **실패 시:** `GlobalResponse.fail(ErrorCode)` 사용 (GlobalExceptionHandler 처리)

**에러 핸들링 (Exception Handling)**

- `try-catch`로 로직 중간에 잡지 않고, `throw new CustomException(ErrorCode.USER_NOT_FOUND)` 형태로 던집니다.
- `GlobalExceptionHandler`에서 일괄적으로 잡아 공통 에러 응답 포맷으로 반환합니다.

**URI 네이밍**

- 복수형 사용 (`/users`)
- kebab-case 사용 (`/user-profiles`)

**문서화 도구**

- SwaggerUI (SpringDoc) 사용

---

## 6. 데이터베이스 & 보안 (Database & Security)

- **데이터베이스 네이밍 규칙:** Java는 `camelCase`지만 DB는 `snake_case`를 사용
    - Table 이름: `PascalCase`
    - attribute: `snake_case`
    - constraints: `snake_case`
- **민감 정보 관리 (Secret Key):**
    - `application.yml`에 DB 비밀번호나 AWS 키를 절대 올리면 안 됨
    - 해결책: `.gitignore` 처리 후 로컬 공유

---

## 7. 이슈 관리 및 일정 (Issues & Schedules)

- **이슈 트래커:** `GitHub Issues`를 사용
- **커밋과 이슈 연동:** 커밋 메시지에 `#12`와 같이 이슈 번호를 남겨 추적 가능함