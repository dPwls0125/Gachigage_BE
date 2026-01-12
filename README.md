# 🚩 같이가게

> **자영업자를 위한 중고 거래 및 커뮤니티 종합 플랫폼**

<br>

## 1. 📝 프로젝트 소개

이 프로젝트는 **중고 물품 거래를 통해 예비 창업자의 창업 비용, 폐엽하는 자영업자의 철거 시간**을 해결하기 위해 시작되었습니다.
현재 **초기 개발 단계**이며, 주요 기능 구현을 위한 환경 설정 및 기본 아키텍처를 구축 중입니다.

* **개발 기간:** 2026.01 ~ (진행 중)
* **팀원:** 백엔드 3명, 프론트엔드 2명, 디자이너 2명, PM 1명

<br>

## 2. 🛠 개발 환경

팀 내에서 합의된 개발 환경입니다. 버전 호환성을 위해 반드시 아래 버전을 사용해 주세요.

* **JDK:** Java 17 (LTS)
* **Framework:** Spring Boot 3.5.9
* **Build Tool:** Gradle 8.14.3
* **Database:** MySQL 8.0.44, Redis
* **Documentation:** SpringDoc (Swagger)

<br>

## 3. ⚙️ 프로젝트 실행 방법

### 3-1. 환경 변수 설정

이 프로젝트는 보안을 위해 `application.yml`의 민감 정보를 환경 변수(혹은 별도 파일)로 관리합니다.
프로젝트 루트에 `application-local.yml`을 생성하거나 아래 환경 변수를 설정해주세요.

```yaml
# 예시: application.yml 설정
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: root
    password: [ DB 비밀번호 ]
```

### 3-2. 실행 명령어

#### 1. 저장소 클론

`git clone https://github.com/Gachigage/Gachigage_BE.git`

#### 2. 프로젝트 폴더로 이동

`cd Gachigage_BE`

#### 3. 빌드 및 실행 (Mac/Linux)

`./gradlew bootRun`

#### 3. 빌드 및 실행 (Windows)

`gradlew.bat bootRun`

<br>

## 4. 🤝 협업 가이드

팀원 간의 원활한 협업을 위해 아래 규칙을 준수합니다. 자세한 내용은 자세한 내용은 [CONTRIBUTING.md](./CONTRIBUTING.md) 문서를 참고해 주세요.

Code Style: Naver Java Style Guide

Commit Message: `[feat] 기능명`, `[fix] 버그수정` 등 커밋 메시지 컨벤션 준수

Branch Strategy: Git Flow

<br>

## 5. 📅 개발 로드맵

- [x] 데이터베이스 ERD 설계

- [x] API 명세서 작성

- [x] 프로젝트 초기 세팅 및 레포지토리 생성

- [ ] CI/CD 파이프라인 구축

- [ ] 회원가입/소셜 로그인 구현

- [ ] 마이페이지 구현

- [ ] 상품 등록, 수정, 삭제, 조회 페이지 구현

- [ ] 유저 간에 1:1 채팅 구현