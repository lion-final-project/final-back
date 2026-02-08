# 소셜 로그인 (OAuth2) 백로그 — 최종본

> **Phase1** 회원 ID 저장·단일 유저 관리 · **Phase2** 카카오 API 정보 정리 · **Phase3** 엔티티(프론트 기준) · **Phase4** 소셜 회원가입 폼(기존 디자인 참고) · **Phase5** docs 준수 · **Phase6** 모든 환경에서 실행 가능

---

## Phase 1: 소셜 로그인 후 회원 ID 저장 및 단일 유저 관리

- **회원 ID**: 소셜 로그인 성공 시 **users.id**(회원 ID)를 생성·저장한다. 이 ID가 플랫폼 내 **단일 유저**의 기준이 된다.
- **연결 구조**:
  - **users**: 회원 ID, 이메일, 전화번호, 이름, 약관 동의 등 **모든 회원 정보**를 한 레코드로 관리.
  - **social_logins**: `user_id`(FK → users), `provider`, `provider_user_id`로 **소셜 계정과 회원 ID를 연결**. 동일 회원(users.id)에 여러 소셜 연동 가능(예: 카카오+네이버).
- **추가 수집 정보(이메일·전화번호 등)**: 소셜 최초 로그인 후 온보딩에서 받은 **이메일·전화번호·주소·약관**은 모두 **동일한 users.id**에 저장되며, **하나의 유저**로 관리된다.
- **정리**: `social_logins.provider` + `social_logins.provider_user_id`로 “이미 가입한 회원인지” 판별하고, 해당 회원의 **users.id**로 JWT 발급·API 권한을 부여한다.

---

## Phase 1 (docs): docs 기준 위배 여부 검토 결과

| 검토 항목 | docs 기준 | 백로그 반영 | 비고 |
|-----------|-----------|-------------|------|
| **API URL** | `인증 API .md`, `동네마켓_API_명세서_v4.md`: `/api/auth/*` (v1 미사용) | `/api/auth/social-login`, `/api/auth/social-register` 사용 | ✅ `/api/v1/auth/*` 사용 금지 |
| **에러 코드** | `ErrorCode.java`, `인증 API .md`: `AUTH-xxx`, `COMMON-xxx` | OAuth/온보딩 실패 시 AUTH-xxx 매핑 | ✅ 신규 에러코드 추가 시 동일 규칙 |
| **응답 형식** | `ApiResponse<T>`, Set-Cookie(`nm_accessToken`, `nm_refreshToken`) | 동일 적용 | ✅ 기존 로그인/회원가입과 동일 |
| **테이블** | `social_logins`: `provider` + `provider_user_id` (ERD·JPA 엔티티) | 기존 유저 판별: `social_logins` 조회 시 위 컬럼 사용 | ✅ `SocialLogin` 엔티티와 일치 |
| **회원가입 필수값** | `인증 API .md` 회원가입: email, name, phone, phoneVerificationToken, termsAgreed, privacyAgreed | 소셜 신규 가입에도 동일 + **주소** 필수 (Phase2) | ✅ 주소는 `addresses` 테이블·Address 엔티티 사용 |

**결론**: docs와 충돌 없음. URL은 `/api/auth/*`로 통일하고, 에러코드·응답·테이블은 기존 컨벤션 준수.

---

## User Story

고객은 **카카오/네이버** 소셜 계정으로 로그인할 수 있어야 하며, 서버는 OAuth 토큰을 검증하고 기존 계정 연동 또는 신규 가입 처리 후 JWT 토큰을 발급해야 한다.  
**단, 소셜 최초 로그인(신규 유저)인 경우** 서비스 정책상 **이메일 중복 확인 + 휴대폰 인증/중복 확인 + 약관 동의 + 주소 입력** 등 필수 가입 요소를 모두 충족해야 하며, 이를 완료하기 전에는 정식 JWT를 발급하지 않는다.

- **우선 적용 provider**: **카카오(KAKAO)** 먼저 구현, 이후 네이버(NAVER) 확장.

---

## Phase 2: 카카오 API 정보 정리

아래 값은 **모든 컴퓨터에서 실행 가능**하도록 **application-local.yml**(또는 환경별 설정)에서 읽어 사용한다. Kakao 개발자 콘솔에서 앱별로 발급·등록한 값으로 교체한다.

| 항목 | 값 | 비고 |
|------|-----|------|
| **REST API 키 (Client ID)** | Kakao 앱 → 앱 키 → REST API 키 | 요청 시 `client_id` 또는 `Authorization` 등에 사용. 예: `c485fc33d1da7f2e33acc8f7078fc9fd`(환경별 교체) |
| **Redirect URI** | Kakao 앱 → 카카오 로그인 → Redirect URI에 **동일하게** 등록 필요 | 로그인/로그아웃 후 돌아올 URL. 예: `http://localhost:8080/callback` (백엔드) 또는 `http://localhost:5173/callback` (프론트). **환경마다 콘솔에 등록 필수** |
| **인가 코드 요청 (Authorization)** | `GET https://kauth.kakao.com/oauth/authorize` | 쿼리: `client_id`, `redirect_uri`, `response_type=code`, `scope`(선택) |
| **토큰 요청 (Token)** | `POST https://kauth.kakao.com/oauth/token` | Body: `grant_type=authorization_code`, `client_id`, `redirect_uri`, `code`. 헤더: `Content-Type: application/x-www-form-urlencoded` |
| **사용자 정보 조회 (User Me)** | `GET https://kapi.kakao.com/v2/user/me` | 헤더: `Authorization: Bearer {access_token}`. 토큰 검증 + 회원 식별(카카오 회원번호 등)에 사용 |
| **로그아웃(카카오)** | `POST https://kapi.kakao.com/v1/user/logout` | 헤더: `Authorization: Bearer {access_token}`. 카카오 측 세션 로그아웃 시 사용(선택) |

**참고**: [Kakao 로그인 REST API](https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api), [사용자 정보 /v2/user/me](https://developers.kakao.com/docs/latest/ko/rest-api/reference).

- **환경별 설정**: REST API 키·Redirect URI는 **application-local.yml**의 `kakao.client-id`, `kakao.redirect-uri` 등으로 두고, 팀원/배포 환경마다 각자 Kakao 콘솔에 등록한 값으로 교체하면 **모든 컴퓨터에서 동일하게 동작**한다.

### Spring OAuth2 Client 전환 후 카카오 콘솔 설정

**Redirect URI**를 아래로 등록 (application-local과 동일하게 고정됨):

- `http://localhost:8080/login/oauth2/code/kakao`

**사이트 도메인**: `http://localhost:8080`  
진입 URL(프론트): `http://localhost:8080/oauth2/authorization/kakao`

**KOE006이 계속 나올 때**: "등록하지 않은 리다이렉트 URI"라고 하면서 위 URI와 같다면, **지금 사용 중인 REST API 키(client_id)와 Redirect URI를 등록한 앱이 같은지** 반드시 확인. 앱이 여러 개면, **같은 앱**의 [제품 설정 > 카카오 로그인 > Redirect URI]에 위 주소를 넣어야 함.

---

### 카카오 "등록하지 않은 URI" 400 해결 체크리스트

redirect_uri가 application-local과 동일해도 400이 나면 **아래를 반드시 확인**한다.

| 확인 위치 | 할 일 |
|-----------|--------|
| **같은 앱인지** | 사용 중인 **REST API 키(client_id)**가 Redirect URI를 등록한 **그 앱**의 키인지 확인. 앱이 여러 개면 키와 Redirect URI가 같은 앱에 있어야 함. |
| **플랫폼 > 웹** | [내 애플리케이션] → **플랫폼** → **Web** 플랫폼이 **추가**되어 있는지 확인. 없으면 "웹" 추가. |
| **사이트 도메인** | [플랫폼] → [Web] → **사이트 도메인**에 `http://localhost:8080` 을 **등록**. (Redirect URI만 등록하고 사이트 도메인을 비우면 400 발생할 수 있음) |
| **제품 > 카카오 로그인** | [제품 설정] → [카카오 로그인] → **활성화** ON. **Redirect URI**에 `http://localhost:8080/api/auth/kakao/callback` 정확히 등록(앞뒤 공백·슬래시 없음). |
| **백엔드 로그** | 서버 로그에 찍히는 `Kakao authorize redirect_uri = [...]` 와 `전체 URL` 을 복사해 브라우저 주소창에 붙여넣어 보면, 우리가 보내는 값이 맞는지·카카오 응답이 같은지 확인 가능. |

---

## 할 일 목록 (Tasks)

### 1) API 구현

- **POST /api/auth/social-login** 컨트롤러/서비스/DTO 구현
  - 요청: `provider`, `accessToken` 필수
  - 처리 결과에 따라 **기존 유저 로그인(200)** 또는 **온보딩 시작(202)** 응답

- **POST /api/auth/social-register** 컨트롤러/서비스/DTO 구현 (신규 유저 필수 가입 루틴 완료용)
  - 요청: `onboardingToken`, `email`, `name`, `phone`, `phoneVerificationToken`, 약관 동의, **주소 정보** 필수
  - 성공 시 200 OK 또는 201 Created, 정식 JWT 발급 및 Set-Cookie

### 2) OAuth 토큰 검증

- **카카오(KAKAO)** 사용자 정보 조회 로직 구현 (우선)
  - Kakao API: `GET https://kapi.kakao.com/v2/user/me`, 헤더 `Authorization: Bearer {accessToken}` (설정은 Phase 2 참고)
  - 응답에서 카카오 회원 식별자(예: `id`)를 `provider_user_id`로 저장해 **회원 ID(users.id)**와 **social_logins**로 연결
  - 검증 실패 시 **401** + `AUTH-015`(또는 신규 에러코드) 반환
- (추가) **네이버(NAVER)** 사용자 정보 조회 로직 구현
- provider 미지원 또는 토큰 무효 시 **401** 반환

### 3) 계정 처리 정책 (기존 유저 vs 신규 유저)

- **social_logins**에 동일 `provider` + `provider_user_id`(provider 측 사용자 ID) 존재 시
  - **기존 유저**로 간주 → 로그인 처리
  - JWT 발급 및 **isNewUser=false** 반환, **200 OK**
- 존재하지 않으면 **신규 유저(최초 소셜 로그인)**
  - **온보딩 세션** 생성 (예: Redis TTL, 키 예: `ONBOARDING:{onboardingToken}`)
  - **onboardingToken** 발급 후 **202 Accepted** 반환
  - **requiredSteps** 반환: `["EMAIL", "PHONE_VERIFICATION", "TERMS", "ADDRESS"]`
  - 정식 JWT 발급 **보류**

### 4) 신규 유저 필수 가입 루틴(온보딩 완료)

- **POST /api/auth/social-register**
  - 입력값(필수): `onboardingToken`, `email`, `name`, `phone`, `phoneVerificationToken`, `termsAgreed`, `privacyAgreed`
  - 입력값(필수): **주소** — `postalCode`, `addressLine1`, `addressLine2`(선택), `addressName`, `contact`(연락처·주소 수령용)
  - **이메일 중복 확인**: 이미 존재하는 이메일(일반 가입/다른 소셜 연동 포함) → **409** + `AUTH-002`
  - **휴대폰 인증 및 중복 확인**: 기존 `send-verification`, `verify-phone` 플로우 사용, 휴대폰 번호 중복 시 **409** + `AUTH-003`
  - **온보딩 토큰 검증**: Redis에서 조회, 무효/만료 시 **401**
  - **필수 약관 미동의** 시 **422** 또는 **400** + `COMMON-001` 등 (팀 규칙에 맞춤)
  - 온보딩 완료 시: **users** 생성(필수값 포함), **social_logins**에 소셜 연동 정보 저장, **addresses**에 주소 1건 저장, 기본 역할 **CUSTOMER** 부여
  - 가입 완료 후 **정식 JWT 발급** 및 Set-Cookie, 본문에 `LoginResponse` 형태로 반환

### 5) JWT 발급/응답 표준화

- **기존 유저 로그인 (200 OK)**: `accessToken`, `refreshToken`, `isNewUser=false` + Set-Cookie
- **신규 유저 온보딩 시작 (202 Accepted)**: `onboardingToken`, `isNewUser=true`, `requiredSteps`
- **신규 유저 온보딩 완료 (200 OK 또는 201 Created)**: `accessToken`, `refreshToken`, `isNewUser=true` + Set-Cookie

---

## 완료 조건 (Acceptance Criteria)

| 시나리오 | HTTP | 응답 내용 |
|----------|------|-----------|
| 기존 소셜 연동 유저 | 200 OK | `accessToken`, `refreshToken`, `isNewUser=false` + Set-Cookie |
| 신규 소셜 유저(최초 로그인) | 202 Accepted | `onboardingToken`, `isNewUser=true`, `requiredSteps` |
| 신규 유저가 필수 가입(이메일·휴대폰·약관·주소) 완료 후 social-register 성공 | 200/201 | 정식 JWT(`accessToken`, `refreshToken`) + Set-Cookie, `isNewUser=true` |

**오류 처리 (docs 에러코드와 매핑)**

| 상황 | HTTP | 에러 코드 (제안) |
|------|------|------------------|
| OAuth 토큰 검증 실패 | 401 | `AUTH-015` (신규: "OAuth 토큰이 유효하지 않습니다.") |
| 이메일 충돌(이미 가입/다른 소셜 연동) | 409 | `AUTH-002` |
| 휴대폰 번호 중복 | 409 | `AUTH-003` |
| 온보딩 토큰/휴대폰 인증 토큰 무효·만료 | 401 | `AUTH-004`~`AUTH-006` 또는 신규 `AUTH-016` |
| 필수 약관 미동의 등 필수값 미충족 | 422 또는 400 | `COMMON-001` 또는 팀 규칙에 맞는 AUTH-xxx |

---

## Phase 2 반영 사항 (카카오 우선, 필수 가입 요소)

- **Provider 우선순위**: **카카오(KAKAO)** 먼저 구현. 네이버(NAVER)는 동일 플로우로 확장.
- **신규 소셜 유저 필수 수집 항목** (기본 회원가입과 동일 수준):
  - **이름**, **이메일**, **연락처(휴대폰)**, **주소**, **약관 동의**(서비스 약관, 개인정보 수집 동의)
- **requiredSteps**: `["EMAIL", "PHONE_VERIFICATION", "TERMS", "ADDRESS"]`
- **POST /api/auth/social-register** 요청 본문에 **주소** 필드 포함:
  - `postalCode`, `addressLine1`, `addressLine2`(선택), `addressName`, `contact` (배송지 연락처)
  - 가입 완료 시 `users` 생성 후 `addresses` 테이블에 1건 저장 (Address 엔티티 사용)

---

## Phase 3: 엔티티 (프론트 기준, 추가 생성 가능)

- **기준**: 엔티티·테이블 구조는 **프론트에서 사용하는 API·화면 흐름**에 맞춘다. 필요 시 엔티티·테이블 **추가 생성** 가능.
- **기존 유지**: `users`, `social_logins`, `user_roles`, `addresses`, `refresh_tokens` (동네마켓_ERD_v4.md, JPA 엔티티 정의서) — 회원 ID(users.id)와 소셜 연동(social_logins)으로 단일 유저 관리.
- **추가 가능**: 온보딩 세션용 Redis 키, DTO·VO 등은 필요 시 추가. **users.password**는 소셜 전용 시 nullable 또는 더미 값으로 두어 일반 로그인과 구분.

---

## Phase 4: 소셜 최초 로그인 후 회원가입 양식 폼 (프론트)

- **요구**: 소셜 **최초 로그인** 후 이메일·전화번호·주소·약관 등 **필수 가입 정보**를 받는 **회원가입 양식 폼**이 필요하다.
- **현황**: 해당 폼이 **프론트에 없음**.
- **작업**: **기존 회원가입(이메일 가입) 디자인·레이아웃**을 참고하여, 소셜 온보딩용 **회원가입 폼**을 구현한다.
  - 수집 항목: **이메일**(중복 확인), **이름**, **휴대폰**(인증·중복 확인), **주소**(우편번호, 주소1, 주소2, 주소별칭, 연락처), **약관 동의**(서비스 약관, 개인정보 수집 동의).
  - 플로우: `POST /api/auth/social-login` → 202 + `onboardingToken`, `requiredSteps` 수신 → 해당 폼 표시 → 입력 완료 후 `POST /api/auth/social-register` 호출 → 200/201 + JWT 수신.
- **백엔드**: `POST /api/auth/social-register` 스펙은 본 백로그 및 `final-back/docs`(인증 API 등)에 맞춰 제공. 프론트는 동일 스펙으로 폼 필드·검증 후 전송.

---

## Phase 5: docs 위배 금지

- **적용**: `final-back/docs` 내 **인증 API .md**, **동네마켓_API_명세서_v4.md**, **동네마켓_ERD_v4.md**, **동네마켓_JPA_엔티티_정의서_v4.md**, **코딩 컨벤션 v1.md** 등에 **위배되지 않도록** 구현·문서 작성.
- **확인 항목**: API URL(`/api/auth/*`), 에러코드(`AUTH-xxx`, `COMMON-xxx`), 응답 형식(`ApiResponse<T>`, Set-Cookie), 테이블·엔티티명·컬럼명.

---

## Phase 6: 모든 컴퓨터에서 실행 가능

- **목표**: 소셜 로그인·회원가입 플로우가 **어느 환경에서든** 동일하게 동작하도록 한다.
- **설정 방식**:
  - **카카오**: REST API 키, Redirect URI를 **application-local.yml**(또는 환경별 설정 파일)에서 읽도록 한다. 각 PC/배포 환경에서 Kakao 개발자 콘솔에 등록한 **client-id**, **redirect-uri**를 해당 설정에 넣으면 된다.
  - **Redirect URI**: Kakao 콘솔에 등록한 값과 **설정 파일의 redirect-uri를 반드시 동일**하게 둔다. (예: 로컬 A는 `http://localhost:8080/callback`, 로컬 B는 `http://localhost:5173/callback` 등 환경별로 등록 후 동일 값 설정.)
- **공통 코드**: URL·키를 하드코딩하지 않고, **설정 주입**(예: `@Value`, `KakaoProperties`)으로 사용하면 코드 수정 없이 환경만 바꿔 실행 가능하다.

---

## Phase 3 (기존): 보완·추가 백로그

| # | 항목 | 내용 |
|---|------|------|
| 1 | **온보딩 TTL** | Redis 키 예: `ONBOARDING:{onboardingToken}`, TTL 예: 10분~15분, 값에 provider·provider_user_id 등 최소 정보 저장 |
| 2 | **신규 ErrorCode** | `AUTH-015`: OAuth 토큰 유효하지 않음(401), `AUTH-016`: 온보딩 토큰 유효하지 않음/만료(401) — 필요 시 `ErrorCode.java`에 추가 |
| 3 | **SecurityConfig** | `POST /api/auth/social-login`, `POST /api/auth/social-register` → `permitAll()` 추가 |
| 4 | **소셜 비밀번호** | 소셜 전용 유저는 `users.password` nullable 또는 더미 값 저장 (기존 일반 로그인과 구분 가능하도록) |
| 5 | **주소 DTO** | `SocialRegisterRequest` 내부 또는 별도 `AddressInput` DTO: postalCode, addressLine1, addressLine2, addressName, contact |
| 6 | **인증 API .md 반영** | API-AUTH-003 소셜 로그인: 200/202 응답 구분, Request Body에 provider·accessToken 필수 명시; **API-AUTH-003-2** 또는 신규 번호로 **POST /api/auth/social-register** 명세 추가 |

---

## 참고: 기존 docs·코드와의 일치

- **URL**: `/api/auth/*` (인증 API .md, 동네마켓_API_명세서_v4.md)
- **테이블**: `users`, `user_roles`, `social_logins`, `addresses`, `refresh_tokens` (동네마켓_ERD_v4.md, JPA 엔티티)
- **엔티티**: `User`, `SocialLogin`(provider, providerUserId), `Address` (동네마켓_JPA_엔티티_정의서_v4.md)
- **에러코드**: `AUTH-xxx`, `COMMON-xxx` (ErrorCode.java, 인증 API .md)
- **응답**: `ApiResponse<T>`, Set-Cookie `nm_accessToken`, `nm_refreshToken` (로그인_로그아웃_백로그_구현_검증.md, 회원가입_백로그_구현_검증_및_플로우.md)

---

---

## 카카오 설정 예시 (application-local.yml)

모든 컴퓨터에서 실행 가능하도록 **환경별로 값만 교체**하면 된다. Kakao 개발자 콘솔에서 앱별 REST API 키·Redirect URI를 등록한 뒤 아래에 반영한다.

```yaml
# Kakao 소셜 로그인 (각 환경에서 Kakao 앱 설정과 동일하게 등록)
kakao:
  client-id: "c485fc33d1da7f2e33acc8f7078fc9fd"   # REST API 키, 환경별 교체
  redirect-uri: "http://localhost:8080/callback"    # Kakao 콘솔에 등록한 값과 동일
  authorization-uri: "https://kauth.kakao.com/oauth/authorize"
  token-uri: "https://kauth.kakao.com/oauth/token"
  user-info-uri: "https://kapi.kakao.com/v2/user/me"
  logout-uri: "https://kapi.kakao.com/v1/user/logout"
```

---

*문서 버전: 1.1 — Phase1~6 반영, 회원 ID·단일 유저 관리, 카카오 API 정리, 엔티티·폼·docs·실행 환경*
