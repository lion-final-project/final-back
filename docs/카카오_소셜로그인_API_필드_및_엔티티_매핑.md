# 카카오 소셜 로그인 — API 전달 필드 및 엔티티 매핑

> 카카오 로그인 완료 시 카카오 API가 전달하는 필드와, 동네마켓 서버의 **어느 엔티티·어느 컬럼**에 저장되는지 정리.

---

## 1. 카카오 API가 전달하는 필드 (GET /v2/user/me)

카카오 로그인 후 **액세스 토큰**으로 `GET https://kapi.kakao.com/v2/user/me` 호출 시 아래 구조로 응답이 옵니다.  
**동의 항목·scope**에 따라 일부 필드는 없을 수 있습니다.

### 1.1 최상위 필드

| 카카오 필드(JSON 키) | 타입 | 설명 | 비고 |
|----------------------|------|------|------|
| **id** | Long | 카카오 회원 번호 (고유 식별자) | 항상 있음 |
| **connected_at** | String(ISO 8601) | 서비스에 연결된 시각 | 선택 |
| **synced_at** | String(ISO 8601) | 카카오싱크 동의 시각 | 선택 |
| **properties** | Object | 앱에 공개된 프로필 정보 (닉네임, 이미지 등) | scope/동의에 따라 다름 |
| **kakao_account** | Object | 카카오계정 정보 (이메일, 프로필 등) | 동의 항목에 따라 다름 |

### 1.2 properties (프로필 기본)

| 카카오 필드 | 타입 | 설명 |
|-------------|------|------|
| **nickname** | String | 프로필 닉네임 |
| **profile_image** | String | 프로필 이미지 URL |
| **thumbnail_image** | String | 썸네일 이미지 URL |

### 1.3 kakao_account (동의 시에만 존재)

| 카카오 필드 | 타입 | 설명 |
|-------------|------|------|
| **email** | String | 카카오 계정 이메일 (동의 필요) |
| **is_email_valid** | Boolean | 이메일 유효 여부 |
| **is_email_verified** | Boolean | 이메일 인증 여부 |
| **profile** | Object | 프로필 (닉네임, 이미지 등) |
| **profile.nickname** | String | 닉네임 |
| **profile.thumbnail_image_url** | String | 썸네일 URL |
| **profile.profile_image_url** | String | 프로필 이미지 URL |
| **phone_number** | String | 전화번호 (동의 필요) |
| **birthyear** | String | 출생 연도 |
| **birthday** | String | 생일 |
| **gender** | String | 성별 |

---

## 2. 현재 우리 서버에서 사용하는 값

- **사용하는 카카오 필드**: `id`, `properties.nickname`, `kakao_account.profile.nickname`  
  (이메일·전화번호는 사용하지 않고, **플레이스홀더**로 채움)

---

## 3. 엔티티별 매핑 (카카오 → 우리 DB)

### 3.1 users 테이블 (User 엔티티)

| users 컬럼 | 카카오 API 필드 | 현재 저장 방식 |
|------------|-----------------|----------------|
| **id** | — | DB 자동 생성 (PK) |
| **email** | kakao_account.email (미사용) | `kakao_{카카오id}@kakao.local` (플레이스홀더) |
| **password** | — | 랜덤 문자열 암호화 저장 |
| **name** | **properties.nickname** 또는 **kakao_account.profile.nickname** | 카카오 닉네임; 없으면 `"카카오사용자"` |
| **phone** | kakao_account.phone_number (미사용) | `kakao-{카카오id}` 또는 충돌 시 `kakao-{uuid}` (플레이스홀더) |
| **status** | — | `ACTIVE` (기본) |
| **terms_agreed** | — | `false` (카카오 로그인만 한 상태) |
| **privacy_agreed** | — | `false` |
| **terms_agreed_at** | — | null |
| **privacy_agreed_at** | — | null |
| **deleted_at** | — | null |
| (BaseTimeEntity) **created_at**, **updated_./,.at** | — | JPA 자동 |

### 3.2 social_logins 테이블 (SocialLogin 엔티티)

| social_logins 컬럼 | 카카오 API 필드 | 현재 저장 방식 |
|--------------------|-----------------|----------------|
| **id** | — | DB 자동 생성 (PK) |
| **user_id** | — | 위에서 생성/조회한 **users.id** (FK) |
| **provider** | — | `KAKAO` (enum) |
| **provider_user_id** | **id** (카카오 회원 번호) | `String.valueOf(kakaoId)` |
| **connected_at** | — | 카카오 로그인 처리 시점 `LocalDateTime.now()` |
| **deleted_at** | — | null |
| (BaseTimeEntity) **created_at**, **updated_at** | — | JPA 자동 |

### 3.3 user_roles / roles

- 카카오로 **최초 가입** 시: 해당 User에 `CUSTOMER` 역할 부여 (role, user_roles 테이블에 저장).
- 카카오 API에서 오는 별도 “역할” 필드는 없음.

---

## 4. 카카오 최초 로그인 → 회원가입 폼 흐름

- **User** 엔티티는 일반 회원가입과 동일하게 사용. **social_logins**는 카카오 연동만 저장하고, **social_logins.user_id**로 **users.id**와 연결.
- 카카오 소셜 **최초 로그인** 시:
  1. 백엔드가 **SocialLogin** 존재 여부로 최초 로그인 판별.
  2. 없으면 세션에 `kakao_pending_provider_user_id`, `kakao_pending_nickname` 저장 후 프론트로 `?kakao=signup_required` 리다이렉트.
  3. 프론트에서 **회원가입 폼** 표시 (이름, 이메일, 연락처, 주소, 약관 — 일반 회원가입과 동일).
  4. 폼 제출 시 **POST /api/auth/social-signup/complete** 호출. 세션의 `kakao_pending_provider_user_id`로 **User** 생성 + **Address** 1건 + **SocialLogin**(provider=KAKAO, provider_user_id=카카오 id) 저장. 이후 세션에 로그인 처리.

---

## 5. 요약 (다음 과정 진행 시 참고)

- **카카오에서 실제로 쓰는 값**: `id`(회원번호), `properties.nickname` / `kakao_account.profile.nickname`(이름용).
- **User 엔티티**: `email`, `phone`은 카카오 필드가 아니라 **플레이스홀더**로 채움. `name`만 카카오 닉네임.
- **SocialLogin 엔티티**: `provider`=KAKAO, `provider_user_id`=카카오 `id` 문자열.
- 이메일/전화번호를 카카오 값으로 채우려면:  
  - 카카오 동의 항목·scope에서 **email**, **phone_number** 허용 후  
  - `KakaoUserInfoResponse`·`registerKakaoUser`에서 해당 필드를 매핑해 저장하면 됨.
