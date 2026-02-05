# 동네마켓 API 명세서 v4.0

| 항목 | 내용 |
|------|------|
| **문서 버전** | v4.0 |
| **작성일** | 2026-01-30 |
| **Base URL** | `https://api.dongnaemarket.com/api/v1` |
| **기술 스택** | Spring Boot 3.x, PostgreSQL 16 + PostGIS 3.4, Redis, WebSocket/SSE |
| **참조 문서** | `동네마켓_PRD_v4.0.md`, `동네마켓_ERD_v4.1.md`, `유즈케이스_v4.1.md`, `backlog.md` |

---

## 1. 문서 개요

### 1.1 API 규약

| 항목 | 규약 |
|------|------|
| **프로토콜** | HTTPS (TLS 1.2+) |
| **데이터 형식** | JSON (Content-Type: application/json) |
| **문자 인코딩** | UTF-8 |
| **API 버전** | URI 경로 방식 (`/api/v1/...`) |
| **HTTP 메서드** | GET (조회), POST (생성), PATCH (부분 수정), DELETE (삭제) |
| **날짜 형식** | ISO 8601 (`2026-01-30T09:00:00+09:00`) |
| **ID 타입** | `Long` (bigint) |

### 1.2 기술 스택

| 구분 | 기술 |
|------|------|
| **백엔드** | Spring Boot 3.x, Java 17+, Spring Security, Spring Data JPA |
| **데이터베이스** | PostgreSQL 16 + PostGIS 3.4 |
| **검색** | PostgreSQL `pg_trgm` + GIN 인덱스 |
| **캐시** | Redis |
| **실시간** | WebSocket (STOMP), SSE (Server-Sent Events) |
| **인증** | JWT (Access Token + Refresh Token) |
| **결제** | PG사 연동 (토스페이먼츠 등) |
| **파일 저장** | AWS S3 / 클라우드 스토리지 |

---

## 2. 공통 사항

### 2.1 인증 방식

JWT Bearer Token 기반 인증을 사용한다.

```
Authorization: Bearer {access_token}
```

| 토큰 | 유효기간 | 설명 |
|------|---------|------|
| **Access Token** | 30분 | API 요청 인증용 |
| **Refresh Token** | 14일 | Access Token 갱신용 |

### 2.2 공통 요청 헤더

| 헤더 | 필수 | 설명 |
|------|------|------|
| `Authorization` | 인증 필요 API | `Bearer {access_token}` |
| `Content-Type` | POST/PATCH | `application/json` |
| `Accept` | 선택 | `application/json` |
| `X-Request-ID` | 선택 | 요청 추적용 UUID |

### 2.3 공통 응답 포맷

#### 성공 응답

```json
{
  "success": true,
  "data": { ... },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

#### 목록 응답 (페이지네이션)

```json
{
  "success": true,
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8,
    "first": true,
    "last": false
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

#### 에러 응답

```json
{
  "success": false,
  "error": {
    "code": "ERR_VALIDATION",
    "message": "입력값이 유효하지 않습니다.",
    "details": [
      {
        "field": "email",
        "message": "이메일 형식이 올바르지 않습니다."
      }
    ]
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

### 2.4 공통 에러 코드

| HTTP 상태 | 에러 코드 | 설명 |
|-----------|----------|------|
| `400` | `ERR_VALIDATION` | 입력값 유효성 검증 실패 |
| `400` | `ERR_BAD_REQUEST` | 잘못된 요청 |
| `401` | `ERR_UNAUTHORIZED` | 인증 실패 (토큰 없음/만료) |
| `403` | `ERR_FORBIDDEN` | 권한 없음 |
| `404` | `ERR_NOT_FOUND` | 리소스를 찾을 수 없음 |
| `409` | `ERR_CONFLICT` | 리소스 충돌 (중복 등) |
| `422` | `ERR_UNPROCESSABLE` | 비즈니스 규칙 위반 |
| `429` | `ERR_TOO_MANY_REQUESTS` | 요청 횟수 초과 |
| `500` | `ERR_INTERNAL` | 서버 내부 오류 |

### 2.5 페이지네이션

| 파라미터 | 타입 | 기본값 | 설명 |
|---------|------|--------|------|
| `page` | Integer | 0 | 페이지 번호 (0-based) |
| `size` | Integer | 20 | 페이지 크기 (최대 100) |
| `sort` | String | - | 정렬 기준 (예: `createdAt,desc`) |

### 2.6 역할(Role) 정의

| 역할 | 코드 | 설명 |
|------|------|------|
| 고객 | `CUSTOMER` | 일반 사용자 (기본 역할) |
| 마트 사업주 | `STORE_OWNER` | 마트 운영자 |
| 라이더 | `RIDER` | 배달원 |
| 관리자 | `ADMIN` | 플랫폼 관리자 |

> **N:M 역할 모델**: 한 사용자가 복수 역할을 가질 수 있다 (예: 고객이면서 라이더).

---

## 3. 모듈별 API 엔드포인트

---

### 3.1 인증 모듈 (Auth)

> 관련 FR: FR-USR-001, FR-USR-002
> 관련 UC: UC-C01, UC-C02
> 관련 테이블: `users`, `roles`, `user_roles`, `social_logins`

---

#### API-AUTH-001: 회원가입

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/auth/register` |
| **인증** | Public |
| **권한** | - |
| **설명** | 이메일 기반 회원가입. 사전에 휴대폰 인증(API-AUTH-005, 006) 완료 필요. |
| **관련 FR** | FR-USR-001 |
| **관련 UC** | UC-C01 |

**Request Body**

```json
{
  "email": "hong@example.com",
  "password": "Abcd1234!",
  "name": "홍길동",
  "phone": "01012345678",
  "phoneVerificationToken": "verified-token-uuid",
  "termsAgreed": true,
  "privacyAgreed": true,
  "marketingAgreed": false
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `email` | String | O | 이메일 (유일) |
| `password` | String | O | 비밀번호 (영문+숫자+특수문자 8자 이상) |
| `name` | String | O | 이름 |
| `phone` | String | O | 휴대폰 번호 (유일) |
| `phoneVerificationToken` | String | O | SMS 인증 완료 토큰 |
| `termsAgreed` | Boolean | O | 서비스 약관 동의 (true 필수) |
| `privacyAgreed` | Boolean | O | 개인정보 수집 동의 (true 필수) |
| `marketingAgreed` | Boolean | X | 마케팅 수신 동의 |

**Response (201 Created)**

```json
{
  "success": true,
  "data": {
    "userId": 1,
    "email": "hong@example.com",
    "name": "홍길동",
    "roles": ["CUSTOMER"],
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "dGhpcyBpcyBhIHJlZnJl..."
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `409` | `ERR_CONFLICT` | 이메일 또는 휴대폰 번호 중복 |
| `400` | `ERR_VALIDATION` | 필수 필드 누락, 비밀번호 형식 불일치 |
| `422` | `ERR_UNPROCESSABLE` | 필수 약관 미동의, 휴대폰 인증 미완료 |

---

#### API-AUTH-002: 로그인

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/auth/login` |
| **인증** | Public |
| **권한** | - |
| **설명** | 이메일/비밀번호로 로그인하여 JWT 토큰을 발급받는다. |
| **관련 FR** | FR-USR-001 |
| **관련 UC** | UC-C02 |

**Request Body**

```json
{
  "email": "hong@example.com",
  "password": "Abcd1234!"
}
```

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "userId": 1,
    "email": "hong@example.com",
    "name": "홍길동",
    "roles": ["CUSTOMER", "RIDER"],
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "dGhpcyBpcyBhIHJlZnJl..."
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `401` | `ERR_UNAUTHORIZED` | 이메일/비밀번호 불일치 |
| `403` | `ERR_FORBIDDEN` | 계정 정지 (`SUSPENDED`) |
| `404` | `ERR_NOT_FOUND` | 존재하지 않는 이메일 |

---

#### API-AUTH-003: 소셜 로그인

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/auth/social-login` |
| **인증** | Public |
| **권한** | - |
| **설명** | OAuth 2.0 소셜 로그인. 기존 계정 연동 또는 신규 가입 처리. |
| **관련 FR** | FR-USR-002 |
| **관련 UC** | UC-C01, UC-C02 |

**Request Body**

```json
{
  "provider": "KAKAO",
  "accessToken": "oauth-access-token",
  "email": "hong@kakao.com",
  "name": "홍길동"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `provider` | String | O | `KAKAO`, `NAVER`, `GOOGLE`, `APPLE` |
| `accessToken` | String | O | OAuth 제공자 발급 액세스 토큰 |
| `email` | String | X | 이메일 (제공자에서 제공 시) |
| `name` | String | X | 이름 (제공자에서 제공 시) |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "userId": 1,
    "email": "hong@kakao.com",
    "name": "홍길동",
    "roles": ["CUSTOMER"],
    "isNewUser": false,
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "dGhpcyBpcyBhIHJlZnJl..."
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `401` | `ERR_UNAUTHORIZED` | OAuth 토큰 검증 실패 |
| `409` | `ERR_CONFLICT` | 이미 다른 소셜 계정으로 연동된 이메일 |

---

#### API-AUTH-004: 로그아웃

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/auth/logout` |
| **인증** | Bearer Token |
| **권한** | 모든 인증 사용자 |
| **설명** | 현재 세션의 토큰을 무효화한다. |
| **관련 FR** | FR-USR-001 |
| **관련 UC** | UC-C02 |

**Request Body**

```json
{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJl..."
}
```

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "message": "로그아웃 되었습니다."
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-AUTH-005: SMS 인증번호 발송

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/auth/send-verification` |
| **인증** | Public |
| **권한** | - |
| **설명** | 휴대폰 번호로 SMS 인증번호를 발송한다. |
| **관련 FR** | FR-USR-001 |
| **관련 UC** | UC-C01 |

**Request Body**

```json
{
  "phone": "01012345678"
}
```

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "message": "인증번호가 발송되었습니다.",
    "expiresIn": 180,
    "remainingAttempts": 4
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `expiresIn` | Integer | 인증번호 유효시간 (초, 3분=180초) |
| `remainingAttempts` | Integer | 남은 재발송 횟수 (최대 5회) |

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `409` | `ERR_CONFLICT` | 이미 등록된 휴대폰 번호 |
| `429` | `ERR_TOO_MANY_REQUESTS` | 재발송 횟수 초과 (5회) |

---

#### API-AUTH-006: SMS 인증번호 확인

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/auth/verify-phone` |
| **인증** | Public |
| **권한** | - |
| **설명** | SMS 인증번호를 확인하고 인증 토큰을 발급한다. |
| **관련 FR** | FR-USR-001 |
| **관련 UC** | UC-C01 |

**Request Body**

```json
{
  "phone": "01012345678",
  "verificationCode": "123456"
}
```

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "verified": true,
    "phoneVerificationToken": "verified-token-uuid"
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `400` | `ERR_VALIDATION` | 인증번호 불일치 |
| `422` | `ERR_UNPROCESSABLE` | 인증번호 만료 (3분 초과) |

---

#### API-AUTH-007: 토큰 갱신

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/auth/refresh` |
| **인증** | Public |
| **권한** | - |
| **설명** | Refresh Token으로 새 Access Token을 발급받는다. |
| **관련 FR** | FR-USR-001 |
| **관련 UC** | UC-C02 |

**Request Body**

```json
{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJl..."
}
```

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "bmV3LXJlZnJlc2gtdG9r..."
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `401` | `ERR_UNAUTHORIZED` | Refresh Token 만료/무효 |

---

### 3.2 사용자 모듈 (Users)

> 관련 FR: FR-USR-003, FR-USR-004, FR-USR-005
> 관련 UC: UC-C11, UC-C12
> 관련 테이블: `users`, `addresses`, `payment_methods`

---

#### API-USR-001: 내 프로필 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/users/me` |
| **인증** | Bearer Token |
| **권한** | 모든 인증 사용자 |
| **설명** | 현재 로그인한 사용자의 프로필 정보를 조회한다. |
| **관련 FR** | FR-USR-004 |
| **관련 UC** | UC-C11 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "userId": 1,
    "email": "hong@example.com",
    "name": "홍길동",
    "phone": "01012345678",
    "status": "ACTIVE",
    "roles": ["CUSTOMER"],
    "marketingAgreed": false,
    "createdAt": "2026-01-01T09:00:00+09:00"
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-USR-002: 프로필 수정

| 항목 | 내용 |
|------|------|
| **Method** | `PATCH` |
| **URL** | `/api/v1/users/me` |
| **인증** | Bearer Token |
| **권한** | 모든 인증 사용자 |
| **설명** | 프로필 정보를 부분 수정한다. |
| **관련 FR** | FR-USR-004 |
| **관련 UC** | UC-C11 |

**Request Body** (부분 수정)

```json
{
  "name": "홍길순",
  "phone": "01098765432"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `name` | String | X | 이름 |
| `phone` | String | X | 휴대폰 번호 (변경 시 SMS 재인증 필요) |
| `password` | String | X | 새 비밀번호 |
| `currentPassword` | String | 비밀번호 변경 시 | 현재 비밀번호 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "userId": 1,
    "email": "hong@example.com",
    "name": "홍길순",
    "phone": "01098765432",
    "status": "ACTIVE",
    "roles": ["CUSTOMER"]
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `400` | `ERR_VALIDATION` | 현재 비밀번호 불일치 |
| `409` | `ERR_CONFLICT` | 변경하려는 휴대폰 번호 중복 |

---

#### API-USR-003: 회원 탈퇴

| 항목 | 내용 |
|------|------|
| **Method** | `DELETE` |
| **URL** | `/api/v1/users/me` |
| **인증** | Bearer Token |
| **권한** | 모든 인증 사용자 |
| **설명** | 회원 탈퇴 (소프트 삭제: `deleted_at` 설정). 활성 구독이 있으면 탈퇴 불가. |
| **관련 FR** | FR-USR-004 |
| **관련 UC** | UC-C11 |

**Request Body**

```json
{
  "password": "Abcd1234!",
  "reason": "서비스 이용을 더 이상 하지 않습니다."
}
```

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "message": "회원 탈퇴가 완료되었습니다."
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `400` | `ERR_VALIDATION` | 비밀번호 불일치 |
| `422` | `ERR_UNPROCESSABLE` | 활성 구독 존재, 진행 중 주문 존재 |

---

#### API-USR-004: 배송지 목록 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/users/me/addresses` |
| **인증** | Bearer Token |
| **권한** | 모든 인증 사용자 |
| **설명** | 등록된 배송지 목록을 조회한다. |
| **관련 FR** | FR-USR-003 |
| **관련 UC** | UC-C12 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": [
    {
      "addressId": 1,
      "addressName": "우리 집",
      "addressLine1": "서울시 강남구 테헤란로 123",
      "addressLine2": "아파트 101동 202호",
      "recipientName": "홍길동",
      "recipientPhone": "01012345678",
      "isDefault": true,
      "entranceType": "PASSWORD",
      "entrancePassword": "1234#",
      "latitude": 37.5065,
      "longitude": 127.0536
    },
    {
      "addressId": 2,
      "addressName": "회사",
      "addressLine1": "서울시 서초구 강남대로 456",
      "addressLine2": "오피스빌딩 7층",
      "recipientName": "홍길동",
      "recipientPhone": "01012345678",
      "isDefault": false,
      "entranceType": "NONE",
      "entrancePassword": null,
      "latitude": 37.4969,
      "longitude": 127.0278
    }
  ],
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-USR-005: 배송지 등록

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/users/me/addresses` |
| **인증** | Bearer Token |
| **권한** | 모든 인증 사용자 |
| **설명** | 새 배송지를 등록한다. |
| **관련 FR** | FR-USR-003 |
| **관련 UC** | UC-C12 |

**Request Body**

```json
{
  "addressName": "부모님 댁",
  "addressLine1": "서울시 종로구 종로 789",
  "addressLine2": "한옥마을 3번지",
  "recipientName": "홍부모",
  "recipientPhone": "01011112222",
  "isDefault": false,
  "entranceType": "FREE",
  "entrancePassword": null,
  "latitude": 37.5729,
  "longitude": 126.9794
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `addressName` | String | O | 배송지 별칭 (집, 회사 등) |
| `addressLine1` | String | O | 주소 |
| `addressLine2` | String | X | 상세주소 |
| `recipientName` | String | O | 수령인 이름 |
| `recipientPhone` | String | O | 수령인 연락처 |
| `isDefault` | Boolean | X | 기본 배송지 여부 (기본 false) |
| `entranceType` | String | X | 공동현관 유형: `NONE`, `PASSWORD`, `FREE` |
| `entrancePassword` | String | X | 공동현관 비밀번호 |
| `latitude` | Double | O | 위도 |
| `longitude` | Double | O | 경도 |

**Response (201 Created)**

```json
{
  "success": true,
  "data": {
    "addressId": 3,
    "addressName": "부모님 댁",
    "addressLine1": "서울시 종로구 종로 789",
    "isDefault": false
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-USR-006: 배송지 수정

| 항목 | 내용 |
|------|------|
| **Method** | `PATCH` |
| **URL** | `/api/v1/users/me/addresses/{addressId}` |
| **인증** | Bearer Token |
| **권한** | 모든 인증 사용자 |
| **설명** | 배송지 정보를 수정한다. |
| **관련 FR** | FR-USR-003 |
| **관련 UC** | UC-C12 |

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `addressId` | Long | 배송지 ID |

**Request Body** (부분 수정)

```json
{
  "addressName": "우리 집 (신주소)",
  "addressLine2": "아파트 101동 303호",
  "isDefault": true
}
```

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "addressId": 1,
    "addressName": "우리 집 (신주소)",
    "addressLine1": "서울시 강남구 테헤란로 123",
    "addressLine2": "아파트 101동 303호",
    "isDefault": true
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-USR-007: 배송지 삭제

| 항목 | 내용 |
|------|------|
| **Method** | `DELETE` |
| **URL** | `/api/v1/users/me/addresses/{addressId}` |
| **인증** | Bearer Token |
| **권한** | 모든 인증 사용자 |
| **설명** | 배송지를 삭제한다. 기본 배송지는 삭제 불가. |
| **관련 FR** | FR-USR-003 |
| **관련 UC** | UC-C12 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "message": "배송지가 삭제되었습니다."
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `422` | `ERR_UNPROCESSABLE` | 기본 배송지 삭제 시도 |
| `404` | `ERR_NOT_FOUND` | 존재하지 않는 배송지 |

---

#### API-USR-008: 결제수단 목록 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/users/me/payment-methods` |
| **인증** | Bearer Token |
| **권한** | 모든 인증 사용자 |
| **설명** | 등록된 결제수단 목록을 조회한다. |
| **관련 FR** | FR-PAY-001 |
| **관련 UC** | UC-C06 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": [
    {
      "paymentMethodId": 1,
      "methodType": "CARD",
      "cardCompany": "삼성카드",
      "cardNumberMasked": "****-****-****-1234",
      "isDefault": true
    },
    {
      "paymentMethodId": 2,
      "methodType": "KAKAO_PAY",
      "cardCompany": null,
      "cardNumberMasked": null,
      "isDefault": false
    }
  ],
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-USR-009: 결제수단 등록

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/users/me/payment-methods` |
| **인증** | Bearer Token |
| **권한** | 모든 인증 사용자 |
| **설명** | 새 결제수단을 등록한다. |
| **관련 FR** | FR-PAY-001 |
| **관련 UC** | UC-C06 |

**Request Body**

```json
{
  "methodType": "CARD",
  "cardCompany": "현대카드",
  "cardNumber": "1234567890123456",
  "expiryDate": "12/28",
  "isDefault": false
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `methodType` | String | O | `CARD`, `KAKAO_PAY`, `NAVER_PAY`, `TOSS_PAY` |
| `cardCompany` | String | CARD일 때 O | 카드사 이름 |
| `cardNumber` | String | CARD일 때 O | 카드번호 (서버에서 마스킹 저장) |
| `expiryDate` | String | CARD일 때 O | 유효기간 (MM/YY) |
| `isDefault` | Boolean | X | 기본 결제수단 여부 |

**Response (201 Created)**

```json
{
  "success": true,
  "data": {
    "paymentMethodId": 3,
    "methodType": "CARD",
    "cardCompany": "현대카드",
    "cardNumberMasked": "****-****-****-3456",
    "isDefault": false
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-USR-010: 결제수단 삭제

| 항목 | 내용 |
|------|------|
| **Method** | `DELETE` |
| **URL** | `/api/v1/users/me/payment-methods/{paymentMethodId}` |
| **인증** | Bearer Token |
| **권한** | 모든 인증 사용자 |
| **설명** | 결제수단을 삭제한다. 활성 구독에 연결된 결제수단은 삭제 불가. |
| **관련 FR** | FR-PAY-001 |
| **관련 UC** | UC-C06 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "message": "결제수단이 삭제되었습니다."
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `422` | `ERR_UNPROCESSABLE` | 활성 구독에 연결된 결제수단 |
| `404` | `ERR_NOT_FOUND` | 존재하지 않는 결제수단 |

---

### 3.3 마트 모듈 (Stores)

> 관련 FR: FR-STO-001~004, FR-PRD-001~004
> 관련 UC: UC-C03, UC-S01~S04
> 관련 테이블: `stores`, `store_business_hours`, `products`, `categories`, `reviews`, `subscription_products`

---

#### API-STO-001: 주변 마트 목록 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/stores` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 고객 배송지 기준 반경 3km 내 영업 중인 마트 목록을 조회한다. PostGIS `ST_DWithin` 활용. |
| **관련 FR** | FR-STO-004 |
| **관련 UC** | UC-C03 |

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `latitude` | Double | O | - | 배송지 위도 |
| `longitude` | Double | O | - | 배송지 경도 |
| `radius` | Integer | X | 3000 | 검색 반경 (미터) |
| `categoryId` | Long | X | - | 카테고리 필터 |
| `sort` | String | X | `distance` | `distance`, `rating`, `orderCount` |
| `page` | Integer | X | 0 | 페이지 번호 |
| `size` | Integer | X | 20 | 페이지 크기 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "storeId": 1,
        "storeName": "행복한 마트",
        "categoryName": "마트",
        "imageUrl": "https://cdn.dongnae.com/stores/1/main.jpg",
        "rating": 4.5,
        "reviewCount": 128,
        "deliveryFee": 3000,
        "estimatedDeliveryTime": "30~40분",
        "distance": 1.2,
        "isOpen": true,
        "tags": ["신선식품", "당일배달"]
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 15,
    "totalPages": 1
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-STO-002: 마트 상세 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/stores/{storeId}` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 마트 상세 정보를 조회한다. 영업시간, 카테고리, 소개 등 포함. |
| **관련 FR** | FR-STO-003, FR-STO-004 |
| **관련 UC** | UC-C03 |

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `storeId` | Long | 마트 ID |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "storeId": 1,
    "storeName": "행복한 마트",
    "ownerName": "김사장",
    "phone": "02-1234-5678",
    "categoryName": "마트",
    "description": "신선한 식료품을 매일 공급하는 동네 마트입니다.",
    "imageUrl": "https://cdn.dongnae.com/stores/1/main.jpg",
    "address": "서울시 강남구 테헤란로 100",
    "latitude": 37.5060,
    "longitude": 127.0530,
    "rating": 4.5,
    "reviewCount": 128,
    "deliveryFee": 3000,
    "isOpen": true,
    "activeStatus": "ACTIVE",
    "businessHours": [
      { "dayOfWeek": 1, "openTime": "08:00", "closeTime": "22:00", "lastOrderTime": "21:30", "isClosed": false },
      { "dayOfWeek": 2, "openTime": "08:00", "closeTime": "22:00", "lastOrderTime": "21:30", "isClosed": false },
      { "dayOfWeek": 0, "openTime": null, "closeTime": null, "lastOrderTime": null, "isClosed": true }
    ]
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-STO-003: 마트별 상품 목록 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/stores/{storeId}/products` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 특정 마트의 상품 목록을 카테고리별로 조회한다. |
| **관련 FR** | FR-PRD-002, FR-PRD-004 |
| **관련 UC** | UC-C03, UC-C05 |

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `categoryId` | Long | X | - | 카테고리 필터 |
| `sort` | String | X | `recommended` | `recommended`, `newest`, `sales`, `priceAsc`, `priceDesc` |
| `keyword` | String | X | - | 상품명 검색어 |
| `page` | Integer | X | 0 | 페이지 번호 |
| `size` | Integer | X | 20 | 페이지 크기 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "productId": 101,
        "productName": "유기농 사과 (3입)",
        "categoryId": 2,
        "categoryName": "과일",
        "price": 12000,
        "salePrice": 9900,
        "discountRate": 18,
        "imageUrl": "https://cdn.dongnae.com/products/101.jpg",
        "stock": 25,
        "isActive": true,
        "origin": "국내산 (경북 영주)",
        "unit": "3개입"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 45,
    "totalPages": 3
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-STO-004: 마트 리뷰 목록 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/stores/{storeId}/reviews` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 마트의 리뷰 목록을 조회한다. |
| **관련 FR** | FR-REV-002 |
| **관련 UC** | UC-C09 |

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `sort` | String | X | `latest` | `latest`, `ratingHigh`, `ratingLow` |
| `page` | Integer | X | 0 | 페이지 번호 |
| `size` | Integer | X | 20 | 페이지 크기 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "reviewId": 1,
        "userName": "홍*동",
        "rating": 5,
        "content": "사과가 정말 신선하고 맛있었어요!",
        "imageUrl": "https://cdn.dongnae.com/reviews/1.jpg",
        "isEdited": false,
        "createdAt": "2026-01-28T18:30:00+09:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 128,
    "totalPages": 7,
    "averageRating": 4.5
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-STO-005: 마트 구독 상품 목록 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/stores/{storeId}/subscription-products` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 마트에서 제공하는 구독 상품 목록을 조회한다. |
| **관련 FR** | FR-SUB-001 |
| **관련 UC** | UC-C10 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": [
    {
      "subscriptionProductId": 1,
      "name": "신선 과일 정기배달",
      "description": "매주 엄선된 제철 과일을 배달해드립니다.",
      "price": 29900,
      "totalDeliveryCount": 4,
      "status": "ACTIVE",
      "imageUrl": "https://cdn.dongnae.com/sub-products/1.jpg",
      "items": [
        { "productId": 101, "productName": "유기농 사과 (3입)", "quantity": 1 },
        { "productId": 102, "productName": "제주 감귤 (1kg)", "quantity": 2 }
      ]
    }
  ],
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-STO-006: 마트 입점 신청

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/stores/register` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER (입점 후 STORE_OWNER 역할 부여) |
| **설명** | 마트 입점 신청. 승인 모듈(FR-APR-001)을 통해 심사 진행. |
| **관련 FR** | FR-STO-001, FR-APR-001 |
| **관련 UC** | UC-S01 |

**Request Body**

```json
{
  "storeName": "싱싱 마트",
  "categoryId": 1,
  "businessNumber": "123-45-67890",
  "ownerName": "박사장",
  "phone": "02-9876-5432",
  "address": "서울시 마포구 월드컵로 200",
  "description": "동네 주민을 위한 친절한 마트",
  "imageUrl": "https://cdn.dongnae.com/stores/new/main.jpg",
  "latitude": 37.5563,
  "longitude": 126.9090,
  "documents": [
    { "documentType": "BUSINESS_LICENSE", "documentUrl": "https://cdn.dongnae.com/docs/bl-001.pdf" },
    { "documentType": "BANK_PASSBOOK", "documentUrl": "https://cdn.dongnae.com/docs/bp-001.pdf" }
  ]
}
```

**Response (201 Created)**

```json
{
  "success": true,
  "data": {
    "storeId": 10,
    "storeName": "싱싱 마트",
    "status": "PENDING",
    "approvalId": 5,
    "message": "입점 신청이 완료되었습니다. 심사 후 결과를 안내드리겠습니다."
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `409` | `ERR_CONFLICT` | 이미 등록된 사업자등록번호 |
| `400` | `ERR_VALIDATION` | 필수 서류 미첨부, 사업자번호 형식 오류 |

---

### 3.4 상품 모듈 (Products)

> 관련 FR: FR-PRD-001~004
> 관련 UC: UC-C04
> 관련 테이블: `products`, `categories`

---

#### API-PRD-001: 상품 검색

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/products/search` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 배달 가능 마트의 상품을 키워드로 검색한다. PostgreSQL `pg_trgm` + GIN 인덱스 활용. |
| **관련 FR** | FR-PRD-004 |
| **관련 UC** | UC-C04 |

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `keyword` | String | O | - | 검색어 (2~8자) |
| `latitude` | Double | O | - | 배송지 위도 |
| `longitude` | Double | O | - | 배송지 경도 |
| `categoryId` | Long | X | - | 카테고리 필터 |
| `sort` | String | X | `recommended` | `recommended`, `newest`, `sales`, `priceAsc`, `priceDesc` |
| `page` | Integer | X | 0 | 페이지 번호 |
| `size` | Integer | X | 20 | 페이지 크기 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "productId": 101,
        "productName": "유기농 사과 (3입)",
        "price": 12000,
        "salePrice": 9900,
        "discountRate": 18,
        "imageUrl": "https://cdn.dongnae.com/products/101.jpg",
        "stock": 25,
        "storeName": "행복한 마트",
        "storeId": 1,
        "distance": 1.2
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 35,
    "totalPages": 2
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `400` | `ERR_VALIDATION` | 검색어 2자 미만 또는 8자 초과 |

---

#### API-PRD-002: 상품 상세 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/products/{productId}` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 상품 상세 정보를 조회한다. |
| **관련 FR** | FR-PRD-002 |
| **관련 UC** | UC-C05 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "productId": 101,
    "productName": "유기농 사과 (3입)",
    "categoryId": 2,
    "categoryName": "과일",
    "price": 12000,
    "salePrice": 9900,
    "discountRate": 18,
    "description": "경북 영주 GAP 인증 농장에서 재배한 유기농 사과입니다.",
    "imageUrl": "https://cdn.dongnae.com/products/101.jpg",
    "origin": "국내산 (경북 영주)",
    "unit": "3개입",
    "stock": 25,
    "isActive": true,
    "storeId": 1,
    "storeName": "행복한 마트"
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-PRD-003: 카테고리 목록 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/categories` |
| **인증** | Public |
| **권한** | - |
| **설명** | 전체 상품 카테고리 목록을 조회한다. (11개 고정 카테고리) |
| **관련 FR** | FR-PRD-001 |
| **관련 UC** | UC-C03, UC-C04 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": [
    { "categoryId": 1, "categoryName": "마트", "iconUrl": "/icons/mart.svg" },
    { "categoryId": 2, "categoryName": "과일", "iconUrl": "/icons/fruit.svg" },
    { "categoryId": 3, "categoryName": "정육", "iconUrl": "/icons/meat.svg" },
    { "categoryId": 4, "categoryName": "수산", "iconUrl": "/icons/seafood.svg" },
    { "categoryId": 5, "categoryName": "베이커리", "iconUrl": "/icons/bakery.svg" },
    { "categoryId": 6, "categoryName": "반찬", "iconUrl": "/icons/side-dish.svg" },
    { "categoryId": 7, "categoryName": "생활용품", "iconUrl": "/icons/household.svg" },
    { "categoryId": 8, "categoryName": "간식", "iconUrl": "/icons/snack.svg" },
    { "categoryId": 9, "categoryName": "음료", "iconUrl": "/icons/beverage.svg" },
    { "categoryId": 10, "categoryName": "유제품", "iconUrl": "/icons/dairy.svg" },
    { "categoryId": 11, "categoryName": "건강식품", "iconUrl": "/icons/health.svg" }
  ],
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

### 3.5 장바구니 모듈 (Cart)

> 관련 FR: FR-ORD-001
> 관련 UC: UC-C05
> 관련 테이블: `carts`, `cart_products`

---

#### API-CRT-001: 장바구니 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/cart` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 현재 장바구니를 마트별로 그룹화하여 조회한다. |
| **관련 FR** | FR-ORD-001 |
| **관련 UC** | UC-C05 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "cartId": 1,
    "totalProductPrice": 28800,
    "totalDeliveryFee": 6000,
    "totalPrice": 34800,
    "storeGroups": [
      {
        "storeId": 1,
        "storeName": "행복한 마트",
        "deliveryFee": 3000,
        "storeProductPrice": 19800,
        "items": [
          {
            "cartItemId": 101,
            "productId": 101,
            "productName": "유기농 사과 (3입)",
            "price": 12000,
            "salePrice": 9900,
            "quantity": 2,
            "subtotal": 19800,
            "imageUrl": "https://cdn.dongnae.com/products/101.jpg",
            "stock": 25,
            "isAvailable": true
          }
        ]
      },
      {
        "storeId": 2,
        "storeName": "신선 정육점",
        "deliveryFee": 3000,
        "storeProductPrice": 9000,
        "items": [
          {
            "cartItemId": 102,
            "productId": 201,
            "productName": "한우 등심 (200g)",
            "price": 15000,
            "salePrice": 9000,
            "quantity": 1,
            "subtotal": 9000,
            "imageUrl": "https://cdn.dongnae.com/products/201.jpg",
            "stock": 0,
            "isAvailable": false
          }
        ]
      }
    ]
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-CRT-002: 장바구니에 상품 추가

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/cart/items` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 장바구니에 상품을 추가한다. 이미 있는 상품이면 수량을 증가한다. |
| **관련 FR** | FR-ORD-001 |
| **관련 UC** | UC-C05 |

**Request Body**

```json
{
  "productId": 101,
  "storeId": 1,
  "quantity": 2
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `productId` | Long | O | 상품 ID |
| `storeId` | Long | O | 마트 ID |
| `quantity` | Integer | O | 수량 (1 이상) |

**Response (201 Created)**

```json
{
  "success": true,
  "data": {
    "cartItemId": 103,
    "productId": 101,
    "productName": "유기농 사과 (3입)",
    "quantity": 2,
    "subtotal": 19800,
    "cartTotalPrice": 34800
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `422` | `ERR_UNPROCESSABLE` | 품절 상품, 판매 중지 상품 |
| `400` | `ERR_VALIDATION` | 재고 초과 수량 |

---

#### API-CRT-003: 장바구니 상품 수량 변경

| 항목 | 내용 |
|------|------|
| **Method** | `PATCH` |
| **URL** | `/api/v1/cart/items/{cartItemId}` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 장바구니 상품의 수량을 변경한다. |
| **관련 FR** | FR-ORD-001 |
| **관련 UC** | UC-C05 |

**Request Body**

```json
{
  "quantity": 3
}
```

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "cartItemId": 101,
    "quantity": 3,
    "subtotal": 29700,
    "cartTotalPrice": 44700
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-CRT-004: 장바구니 상품 삭제

| 항목 | 내용 |
|------|------|
| **Method** | `DELETE` |
| **URL** | `/api/v1/cart/items/{cartItemId}` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 장바구니에서 상품을 삭제한다. |
| **관련 FR** | FR-ORD-001 |
| **관련 UC** | UC-C05 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "message": "상품이 장바구니에서 삭제되었습니다.",
    "cartTotalPrice": 15000
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```
### 3.6 주문 모듈 (Orders)

> 관련 FR: FR-ORD-002~006
> 관련 UC: UC-C06, UC-C07, UC-C08
> 관련 테이블: `orders`, `store_orders`, `order_products`
> 관련 ENUM: `order_type`, `order_status`, `store_order_status`

---

#### API-ORD-001: 주문 생성

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/orders` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 장바구니 상품으로 주문을 생성한다. 멀티마트 주문은 자동으로 `store_orders`로 분할된다. |
| **관련 FR** | FR-ORD-002, FR-PAY-002 |
| **관련 UC** | UC-C06 |

**Request Body**

```json
{
  "addressId": 1,
  "paymentMethodId": 1,
  "deliveryTimeSlot": "11:00~14:00",
  "deliveryRequest": "문 앞에 놔주세요.",
  "cartItemIds": [101, 102, 103],
  "couponId": null,
  "usePoints": 0
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `addressId` | Long | O | 배송지 ID |
| `paymentMethodId` | Long | O | 결제수단 ID |
| `deliveryTimeSlot` | String | O | 배달 시간대: `08:00~11:00`, `11:00~14:00`, `14:00~17:00`, `17:00~20:00` |
| `deliveryRequest` | String | X | 배달 요청사항 |
| `cartItemIds` | Long[] | O | 주문할 장바구니 상품 ID 목록 |
| `couponId` | Long | X | 적용할 쿠폰 ID |
| `usePoints` | Integer | X | 사용할 포인트 (기본 0) |

**Response (201 Created)**

```json
{
  "success": true,
  "data": {
    "orderId": 1001,
    "orderNumber": "ORD-20260130-001001",
    "orderType": "REGULAR",
    "status": "PENDING",
    "totalProductPrice": 28800,
    "totalDeliveryFee": 6000,
    "discountAmount": 0,
    "finalPrice": 34800,
    "storeOrders": [
      {
        "storeOrderId": 2001,
        "storeId": 1,
        "storeName": "행복한 마트",
        "status": "PENDING",
        "storeProductPrice": 19800,
        "deliveryFee": 3000,
        "products": [
          {
            "productId": 101,
            "productName": "유기농 사과 (3입)",
            "unitPrice": 9900,
            "quantity": 2,
            "subtotal": 19800
          }
        ]
      },
      {
        "storeOrderId": 2002,
        "storeId": 2,
        "storeName": "신선 정육점",
        "status": "PENDING",
        "storeProductPrice": 9000,
        "deliveryFee": 3000,
        "products": [
          {
            "productId": 201,
            "productName": "한우 등심 (200g)",
            "unitPrice": 9000,
            "quantity": 1,
            "subtotal": 9000
          }
        ]
      }
    ],
    "payment": {
      "paymentId": 3001,
      "paymentMethod": "CARD",
      "amount": 34800,
      "status": "COMPLETED"
    },
    "orderedAt": "2026-01-30T10:30:00+09:00"
  },
  "timestamp": "2026-01-30T10:30:00+09:00"
}
```

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `400` | `ERR_VALIDATION` | 필수 필드 누락, 잘못된 시간대 |
| `422` | `ERR_UNPROCESSABLE` | 품절 상품 포함, 재고 부족, 영업시간 외 주문, 결제 실패 |
| `404` | `ERR_NOT_FOUND` | 존재하지 않는 배송지/결제수단 |

---

#### API-ORD-002: 주문 목록 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/orders` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 주문 이력을 조회한다. 상태별 필터, 검색 가능. |
| **관련 FR** | FR-ORD-006 |
| **관련 UC** | UC-C06 |

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `status` | String | X | - | `PENDING`, `PAID`, `COMPLETED`, `CANCELLED` |
| `keyword` | String | X | - | 상품명 검색 |
| `startDate` | String | X | - | 조회 시작일 (yyyy-MM-dd) |
| `endDate` | String | X | - | 조회 종료일 (yyyy-MM-dd) |
| `page` | Integer | X | 0 | 페이지 번호 |
| `size` | Integer | X | 20 | 페이지 크기 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "orderId": 1001,
        "orderNumber": "ORD-20260130-001001",
        "orderType": "REGULAR",
        "status": "COMPLETED",
        "totalProductPrice": 28800,
        "totalDeliveryFee": 6000,
        "finalPrice": 34800,
        "storeNames": ["행복한 마트", "신선 정육점"],
        "productSummary": "유기농 사과 (3입) 외 1건",
        "orderedAt": "2026-01-30T10:30:00+09:00",
        "hasReview": false
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 25,
    "totalPages": 2
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-ORD-003: 주문 상세 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/orders/{orderId}` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 주문 상세 정보를 조회한다. 마트별 주문 상태, 상품 내역, 결제 정보, 배달 정보 포함. |
| **관련 FR** | FR-ORD-003, FR-ORD-006 |
| **관련 UC** | UC-C06, UC-C08 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "orderId": 1001,
    "orderNumber": "ORD-20260130-001001",
    "orderType": "REGULAR",
    "status": "PAID",
    "totalProductPrice": 28800,
    "totalDeliveryFee": 6000,
    "discountAmount": 0,
    "finalPrice": 34800,
    "deliveryAddress": {
      "addressLine1": "서울시 강남구 테헤란로 123",
      "addressLine2": "아파트 101동 202호",
      "recipientName": "홍길동",
      "recipientPhone": "01012345678"
    },
    "deliveryTimeSlot": "11:00~14:00",
    "deliveryRequest": "문 앞에 놔주세요.",
    "storeOrders": [
      {
        "storeOrderId": 2001,
        "storeId": 1,
        "storeName": "행복한 마트",
        "status": "ACCEPTED",
        "storeProductPrice": 19800,
        "deliveryFee": 3000,
        "finalPrice": 22800,
        "products": [
          {
            "productId": 101,
            "productName": "유기농 사과 (3입)",
            "unitPrice": 9900,
            "quantity": 2,
            "subtotal": 19800,
            "imageUrl": "https://cdn.dongnae.com/products/101.jpg"
          }
        ],
        "delivery": {
          "deliveryId": 4001,
          "status": "ACCEPTED",
          "riderName": "김배달",
          "riderPhone": "010****5678",
          "estimatedTime": "12:30"
        },
        "hasReview": false
      }
    ],
    "payment": {
      "paymentId": 3001,
      "paymentMethod": "CARD",
      "cardInfo": "삼성카드 ****1234",
      "amount": 34800,
      "status": "COMPLETED",
      "paidAt": "2026-01-30T10:30:00+09:00"
    },
    "orderedAt": "2026-01-30T10:30:00+09:00"
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-ORD-004: 주문 취소

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/orders/{orderId}/cancel` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 주문을 취소한다. `PENDING` 상태에서만 가능. 멀티마트 주문의 부분 취소(마트별) 지원. |
| **관련 FR** | FR-ORD-005, FR-PAY-003 |
| **관련 UC** | UC-C07 |

**Request Body**

```json
{
  "cancelReason": "단순 변심",
  "storeOrderIds": [2001]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `cancelReason` | String | O | 취소 사유 |
| `storeOrderIds` | Long[] | X | 부분 취소 시 취소할 마트주문 ID 목록 (미지정 시 전체 취소) |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "orderId": 1001,
    "status": "PARTIAL_CANCELLED",
    "cancelledStoreOrders": [
      {
        "storeOrderId": 2001,
        "storeName": "행복한 마트",
        "status": "CANCELLED",
        "refundAmount": 22800
      }
    ],
    "refund": {
      "totalRefundAmount": 22800,
      "refundMethod": "CARD",
      "estimatedRefundDate": "2026-02-03"
    }
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `422` | `ERR_UNPROCESSABLE` | PENDING이 아닌 상태에서 취소 시도 |
| `404` | `ERR_NOT_FOUND` | 존재하지 않는 주문 |

---

#### API-ORD-005: 주문 실시간 추적

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/orders/{orderId}/tracking` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 주문의 실시간 배달 추적 정보를 조회한다. 6단계 추적 + 라이더 위치. |
| **관련 FR** | FR-ORD-003, FR-DLV-006 |
| **관련 UC** | UC-C08 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "orderId": 1001,
    "orderNumber": "ORD-20260130-001001",
    "storeOrders": [
      {
        "storeOrderId": 2001,
        "storeName": "행복한 마트",
        "status": "DELIVERING",
        "timeline": [
          { "step": 1, "label": "주문 접수", "status": "PENDING", "completedAt": "2026-01-30T10:30:00+09:00" },
          { "step": 2, "label": "상품 준비", "status": "ACCEPTED", "completedAt": "2026-01-30T10:35:00+09:00" },
          { "step": 3, "label": "픽업 대기", "status": "READY", "completedAt": "2026-01-30T10:50:00+09:00" },
          { "step": 4, "label": "픽업 완료", "status": "PICKED_UP", "completedAt": "2026-01-30T11:00:00+09:00" },
          { "step": 5, "label": "배송 중", "status": "DELIVERING", "completedAt": "2026-01-30T11:05:00+09:00" },
          { "step": 6, "label": "배송 완료", "status": "DELIVERED", "completedAt": null }
        ],
        "rider": {
          "riderName": "김배달",
          "riderPhone": "010****5678",
          "vehicleType": "자전거",
          "currentLocation": {
            "latitude": 37.5070,
            "longitude": 127.0535
          },
          "estimatedArrival": "2026-01-30T11:20:00+09:00"
        },
        "storeLocation": {
          "latitude": 37.5060,
          "longitude": 127.0530
        },
        "deliveryLocation": {
          "latitude": 37.5065,
          "longitude": 127.0536
        }
      }
    ]
  },
  "timestamp": "2026-01-30T11:10:00+09:00"
}
```

---

#### API-ORD-006: 재주문

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/orders/{orderId}/reorder` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 이전 주문의 상품을 장바구니에 다시 담는다. |
| **관련 FR** | FR-ORD-006 |
| **관련 UC** | UC-C06 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "addedItems": 3,
    "unavailableItems": [
      {
        "productId": 202,
        "productName": "한우 등심 (200g)",
        "reason": "품절"
      }
    ],
    "message": "3개 상품이 장바구니에 담겼습니다. 1개 상품은 현재 구매할 수 없습니다."
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

### 3.7 결제 모듈 (Payments)

> 관련 FR: FR-PAY-001~003
> 관련 UC: UC-C06, UC-C07
> 관련 테이블: `payments`, `payment_refunds`
> 관련 ENUM: `payment_method_type`, `payment_status`

---

#### API-PAY-001: 결제 처리

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/payments` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 주문에 대한 결제를 처리한다. PG사 연동. 주문 생성 시 자동 호출되지만 별도 API로도 제공. |
| **관련 FR** | FR-PAY-002 |
| **관련 UC** | UC-C06 |

**Request Body**

```json
{
  "orderId": 1001,
  "paymentMethodId": 1,
  "amount": 34800
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `orderId` | Long | O | 주문 ID |
| `paymentMethodId` | Long | O | 결제수단 ID |
| `amount` | Long | O | 결제 금액 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "paymentId": 3001,
    "orderId": 1001,
    "paymentMethod": "CARD",
    "amount": 34800,
    "status": "COMPLETED",
    "pgTransactionId": "PG-TXN-20260130-001",
    "paidAt": "2026-01-30T10:30:00+09:00"
  },
  "timestamp": "2026-01-30T10:30:00+09:00"
}
```

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `422` | `ERR_UNPROCESSABLE` | 결제 실패 (잔액 부족, 카드 한도 초과) |
| `409` | `ERR_CONFLICT` | 이미 결제된 주문 |

---

#### API-PAY-002: 결제 상세 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/payments/{paymentId}` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 결제 상세 정보를 조회한다. 환불 내역 포함. |
| **관련 FR** | FR-PAY-002 |
| **관련 UC** | UC-C06 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "paymentId": 3001,
    "orderId": 1001,
    "orderNumber": "ORD-20260130-001001",
    "paymentMethod": "CARD",
    "cardInfo": "삼성카드 ****1234",
    "amount": 34800,
    "status": "PARTIAL_REFUNDED",
    "pgTransactionId": "PG-TXN-20260130-001",
    "paidAt": "2026-01-30T10:30:00+09:00",
    "refunds": [
      {
        "refundId": 5001,
        "storeOrderId": 2001,
        "storeName": "행복한 마트",
        "refundAmount": 22800,
        "refundReason": "단순 변심",
        "refundedAt": "2026-01-30T10:45:00+09:00"
      }
    ]
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-PAY-003: 환불 요청

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/payments/{paymentId}/refund` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 결제 건에 대한 환불을 요청한다. 마트주문 단위 부분 환불 지원. |
| **관련 FR** | FR-PAY-003 |
| **관련 UC** | UC-C07 |

**Request Body**

```json
{
  "storeOrderId": 2001,
  "refundAmount": 22800,
  "refundReason": "단순 변심"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `storeOrderId` | Long | O | 환불할 마트주문 ID |
| `refundAmount` | Long | O | 환불 금액 |
| `refundReason` | String | O | 환불 사유 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "refundId": 5001,
    "paymentId": 3001,
    "storeOrderId": 2001,
    "refundAmount": 22800,
    "refundReason": "단순 변심",
    "paymentStatus": "PARTIAL_REFUNDED",
    "estimatedRefundDate": "2026-02-03",
    "refundedAt": "2026-01-30T10:45:00+09:00"
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `422` | `ERR_UNPROCESSABLE` | 환불 불가 상태 (이미 환불 완료, 배송 완료 후 등) |
| `400` | `ERR_VALIDATION` | 환불 금액 오류 (원결제 금액 초과) |

---

### 3.8 배달 모듈 (Deliveries)

> 관련 FR: FR-DLV-001~009
> 관련 UC: UC-R01~UC-R09
> 관련 테이블: `riders`, `deliveries`, `rider_locations`, `delivery_photos`
> 관련 ENUM: `rider_operation_status`, `delivery_status`

---

#### API-DLV-001: 주변 배달 요청 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/rider/deliveries/available` |
| **인증** | Bearer Token |
| **권한** | RIDER |
| **설명** | 라이더 주변의 배달 요청 목록을 조회한다. 온라인 상태에서만 조회 가능. |
| **관련 FR** | FR-DLV-003 |
| **관련 UC** | UC-R03 |

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `latitude` | Double | O | - | 라이더 현재 위도 |
| `longitude` | Double | O | - | 라이더 현재 경도 |
| `radius` | Integer | X | 3000 | 검색 반경 (미터) |

**Response (200 OK)**

```json
{
  "success": true,
  "data": [
    {
      "deliveryId": 4001,
      "storeOrderId": 2001,
      "storeName": "행복한 마트",
      "storeAddress": "서울시 강남구 테헤란로 100",
      "deliveryAddress": "서울시 강남구 테헤란로 123, 아파트 101동 202호",
      "distance": 1.5,
      "deliveryFee": 3500,
      "riderEarning": 3000,
      "estimatedTime": "15분",
      "productSummary": "유기농 사과 (3입) 외 1건",
      "requestedAt": "2026-01-30T10:50:00+09:00"
    }
  ],
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-DLV-002: 배달 수락

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/rider/deliveries/{deliveryId}/accept` |
| **인증** | Bearer Token |
| **권한** | RIDER |
| **설명** | 배달 요청을 수락한다. 최대 동시 진행 3건 제한. |
| **관련 FR** | FR-DLV-003 |
| **관련 UC** | UC-R03 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "deliveryId": 4001,
    "status": "ACCEPTED",
    "storeName": "행복한 마트",
    "storeAddress": "서울시 강남구 테헤란로 100",
    "storePhone": "02-1234-5678",
    "deliveryAddress": "서울시 강남구 테헤란로 123, 아파트 101동 202호",
    "recipientName": "홍*동",
    "recipientPhone": "010****5678",
    "deliveryFee": 3500,
    "riderEarning": 3000,
    "productSummary": "유기농 사과 (3입) 외 1건",
    "acceptedAt": "2026-01-30T11:00:00+09:00"
  },
  "timestamp": "2026-01-30T11:00:00+09:00"
}
```

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `422` | `ERR_UNPROCESSABLE` | 동시 진행 3건 초과, 오프라인 상태 |
| `409` | `ERR_CONFLICT` | 이미 다른 라이더가 수락 |

---

#### API-DLV-003: 배달 상태 진행

| 항목 | 내용 |
|------|------|
| **Method** | `PATCH` |
| **URL** | `/api/v1/rider/deliveries/{deliveryId}/status` |
| **인증** | Bearer Token |
| **권한** | RIDER |
| **설명** | 배달 상태를 다음 단계로 진행한다. 4단계: ACCEPTED → PICKED_UP → DELIVERING → DELIVERED |
| **관련 FR** | FR-DLV-004 |
| **관련 UC** | UC-R04 |

**Request Body**

```json
{
  "status": "PICKED_UP"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `status` | String | O | 다음 상태: `PICKED_UP`, `DELIVERING`, `DELIVERED` |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "deliveryId": 4001,
    "status": "PICKED_UP",
    "nextStep": "DELIVERING",
    "nextStepLabel": "배송 시작",
    "updatedAt": "2026-01-30T11:10:00+09:00"
  },
  "timestamp": "2026-01-30T11:10:00+09:00"
}
```

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `422` | `ERR_UNPROCESSABLE` | 잘못된 상태 전이 (건너뛰기 시도) |
| `403` | `ERR_FORBIDDEN` | 자신의 배달이 아닌 건 |

---

#### API-DLV-004: 배달 완료 (사진 인증)

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/rider/deliveries/{deliveryId}/complete` |
| **인증** | Bearer Token |
| **권한** | RIDER |
| **설명** | 배달을 완료하고 인증 사진을 업로드한다. 사진 필수. |
| **관련 FR** | FR-DLV-005 |
| **관련 UC** | UC-R05 |

**Request Body** (multipart/form-data)

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `photo` | File | O | 배달 완료 인증 사진 |
| `note` | String | X | 배달 메모 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "deliveryId": 4001,
    "status": "DELIVERED",
    "photoUrl": "https://cdn.dongnae.com/delivery-photos/4001.jpg",
    "riderEarning": 3000,
    "totalEarningsToday": 45000,
    "completedAt": "2026-01-30T11:25:00+09:00",
    "message": "배달이 완료되었습니다. 3,000원이 적립되었습니다."
  },
  "timestamp": "2026-01-30T11:25:00+09:00"
}
```

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `400` | `ERR_VALIDATION` | 사진 미첨부 |
| `422` | `ERR_UNPROCESSABLE` | DELIVERING 상태가 아닌 경우 |

---

#### API-DLV-005: 라이더 위치 갱신

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/rider/location` |
| **인증** | Bearer Token |
| **권한** | RIDER |
| **설명** | 라이더의 현재 GPS 위치를 전송한다. 3~5초 간격으로 호출. |
| **관련 FR** | FR-DLV-006 |
| **관련 UC** | UC-R04, UC-C08 |

**Request Body**

```json
{
  "latitude": 37.5070,
  "longitude": 127.0535,
  "deliveryId": 4001
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `latitude` | Double | O | 위도 |
| `longitude` | Double | O | 경도 |
| `deliveryId` | Long | X | 현재 배달 중인 배달 ID (없으면 일반 위치 갱신) |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "recorded": true,
    "timestamp": "2026-01-30T11:15:00+09:00"
  },
  "timestamp": "2026-01-30T11:15:00+09:00"
}
```

---

#### API-DLV-006: 라이더 온/오프라인 전환

| 항목 | 내용 |
|------|------|
| **Method** | `PATCH` |
| **URL** | `/api/v1/rider/status` |
| **인증** | Bearer Token |
| **권한** | RIDER |
| **설명** | 라이더 운행 상태를 전환한다. 진행 중 배달이 있으면 오프라인 전환 불가. |
| **관련 FR** | FR-DLV-002 |
| **관련 UC** | UC-R01 |

**Request Body**

```json
{
  "operationStatus": "ONLINE"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `operationStatus` | String | O | `ONLINE` 또는 `OFFLINE` |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "riderId": 1,
    "operationStatus": "ONLINE",
    "message": "운행을 시작합니다. 주변 배달 요청을 확인해주세요.",
    "updatedAt": "2026-01-30T09:00:00+09:00"
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `422` | `ERR_UNPROCESSABLE` | 진행 중 배달이 있는데 오프라인 전환 시도 |

---

#### API-DLV-007: 고객에게 메시지 전송

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/rider/deliveries/{deliveryId}/message` |
| **인증** | Bearer Token |
| **권한** | RIDER |
| **설명** | 고객에게 정형 메시지를 전송한다. |
| **관련 FR** | FR-DLV-007 |
| **관련 UC** | UC-R04 |

**Request Body**

```json
{
  "templateId": 1,
  "customMessage": null
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `templateId` | Integer | O | 메시지 템플릿 ID (1: 곧 도착, 2: 픽업 지연, 3: 문 앞 배송, 4: 벨 누르지 않음) |
| `customMessage` | String | X | 사용자 정의 메시지 (선택) |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "sent": true,
    "message": "조금 뒤 도착 예정입니다. 잠시만 기다려주세요!",
    "sentAt": "2026-01-30T11:15:00+09:00"
  },
  "timestamp": "2026-01-30T11:15:00+09:00"
}
```

---

### 3.9 구독 모듈 (Subscriptions)

> 관련 FR: FR-SUB-001~004
> 관련 UC: UC-C10, UC-S10~S12
> 관련 테이블: `subscription_products`, `subscriptions`, `subscription_day_of_week`, `subscription_history`
> 관련 ENUM: `subscription_product_status`, `subscription_status`, `sub_history_status`

---

#### API-SUB-001: 구독 신청

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/subscriptions` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 구독 상품을 신청한다. 마트 승인 후 구독이 활성화된다. |
| **관련 FR** | FR-SUB-002 |
| **관련 UC** | UC-C10 |

**Request Body**

```json
{
  "subscriptionProductId": 1,
  "addressId": 1,
  "paymentMethodId": 1,
  "deliveryDays": [1, 3, 5]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `subscriptionProductId` | Long | O | 구독 상품 ID |
| `addressId` | Long | O | 배송지 ID |
| `paymentMethodId` | Long | O | 결제수단 ID |
| `deliveryDays` | Integer[] | O | 배달 요일 (0=일, 1=월, ..., 6=토) |

**Response (201 Created)**

```json
{
  "success": true,
  "data": {
    "subscriptionId": 1,
    "subscriptionProductName": "신선 과일 정기배달",
    "storeName": "행복한 마트",
    "status": "ACTIVE",
    "totalAmount": 29900,
    "deliveryDays": [1, 3, 5],
    "deliveryDayLabels": ["월", "수", "금"],
    "nextPaymentDate": "2026-02-28",
    "nextDeliveryDate": "2026-02-03",
    "createdAt": "2026-01-30T10:00:00+09:00"
  },
  "timestamp": "2026-01-30T10:00:00+09:00"
}
```

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `404` | `ERR_NOT_FOUND` | 존재하지 않는 구독 상품 |
| `422` | `ERR_UNPROCESSABLE` | 비활성 구독 상품, 유효하지 않은 배송지/결제수단 |

---

#### API-SUB-002: 내 구독 목록 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/subscriptions` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 내 구독 목록을 조회한다. |
| **관련 FR** | FR-SUB-003 |
| **관련 UC** | UC-C10 |

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `status` | String | X | - | `ACTIVE`, `PAUSED`, `CANCELLATION_PENDING`, `CANCELLED` |

**Response (200 OK)**

```json
{
  "success": true,
  "data": [
    {
      "subscriptionId": 1,
      "subscriptionProductName": "신선 과일 정기배달",
      "storeName": "행복한 마트",
      "storeId": 1,
      "status": "ACTIVE",
      "totalAmount": 29900,
      "deliveryDays": [1, 3, 5],
      "deliveryDayLabels": ["월", "수", "금"],
      "nextPaymentDate": "2026-02-28",
      "nextDeliveryDate": "2026-02-03",
      "totalDeliveryCount": 4,
      "completedDeliveryCount": 2,
      "items": [
        { "productName": "유기농 사과 (3입)", "quantity": 1 },
        { "productName": "제주 감귤 (1kg)", "quantity": 2 }
      ]
    }
  ],
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-SUB-003: 구독 일시정지

| 항목 | 내용 |
|------|------|
| **Method** | `PATCH` |
| **URL** | `/api/v1/subscriptions/{subscriptionId}/pause` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 활성 구독을 일시정지한다. |
| **관련 FR** | FR-SUB-003 |
| **관련 UC** | UC-C10 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "subscriptionId": 1,
    "status": "PAUSED",
    "message": "구독이 일시정지되었습니다. 언제든 재개할 수 있습니다.",
    "pausedAt": "2026-01-30T10:00:00+09:00"
  },
  "timestamp": "2026-01-30T10:00:00+09:00"
}
```

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `422` | `ERR_UNPROCESSABLE` | ACTIVE가 아닌 상태에서 일시정지 시도 |

---

#### API-SUB-004: 구독 재개

| 항목 | 내용 |
|------|------|
| **Method** | `PATCH` |
| **URL** | `/api/v1/subscriptions/{subscriptionId}/resume` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 일시정지된 구독을 재개한다. |
| **관련 FR** | FR-SUB-003 |
| **관련 UC** | UC-C10 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "subscriptionId": 1,
    "status": "ACTIVE",
    "nextPaymentDate": "2026-02-28",
    "nextDeliveryDate": "2026-02-05",
    "message": "구독이 재개되었습니다.",
    "resumedAt": "2026-01-30T10:00:00+09:00"
  },
  "timestamp": "2026-01-30T10:00:00+09:00"
}
```

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `422` | `ERR_UNPROCESSABLE` | PAUSED가 아닌 상태에서 재개 시도 |

---

#### API-SUB-005: 구독 해지

| 항목 | 내용 |
|------|------|
| **Method** | `DELETE` |
| **URL** | `/api/v1/subscriptions/{subscriptionId}` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 구독을 해지한다. 다음 결제일에 실제 해지 처리 (CANCELLATION_PENDING → CANCELLED). |
| **관련 FR** | FR-SUB-003 |
| **관련 UC** | UC-C10 |

**Request Body**

```json
{
  "reason": "더 이상 필요하지 않습니다."
}
```

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "subscriptionId": 1,
    "status": "CANCELLATION_PENDING",
    "cancellationDate": "2026-02-28",
    "message": "2026-02-28에 구독이 해지됩니다. 그 전까지 서비스를 이용하실 수 있습니다."
  },
  "timestamp": "2026-01-30T10:00:00+09:00"
}
```

---

### 3.10 리뷰 모듈 (Reviews)

> 관련 FR: FR-REV-001~002
> 관련 UC: UC-C09
> 관련 테이블: `reviews`

---

#### API-REV-001: 리뷰 작성

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/reviews` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 배달 완료된 마트주문에 리뷰를 작성한다. 주문당 1개, 배달 완료 후 7일 이내. |
| **관련 FR** | FR-REV-001 |
| **관련 UC** | UC-C09 |

**Request Body** (multipart/form-data)

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `storeOrderId` | Long | O | 마트주문 ID |
| `rating` | Integer | O | 별점 (1~5) |
| `content` | String | O | 리뷰 내용 |
| `photo` | File | X | 리뷰 사진 |

**Response (201 Created)**

```json
{
  "success": true,
  "data": {
    "reviewId": 1,
    "storeOrderId": 2001,
    "storeName": "행복한 마트",
    "rating": 5,
    "content": "사과가 정말 신선하고 맛있었어요!",
    "photoUrl": "https://cdn.dongnae.com/reviews/1.jpg",
    "createdAt": "2026-01-30T18:30:00+09:00"
  },
  "timestamp": "2026-01-30T18:30:00+09:00"
}
```

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `422` | `ERR_UNPROCESSABLE` | 배달 미완료, 리뷰 작성 기간 초과 (7일), 이미 리뷰 작성 완료 |
| `400` | `ERR_VALIDATION` | 별점 범위 오류 (1~5 외) |

---

#### API-REV-002: 리뷰 수정

| 항목 | 내용 |
|------|------|
| **Method** | `PATCH` |
| **URL** | `/api/v1/reviews/{reviewId}` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 자신이 작성한 리뷰를 수정한다. 수정 시 "수정됨" 표시. |
| **관련 FR** | FR-REV-002 |
| **관련 UC** | UC-C09 |

**Request Body**

```json
{
  "rating": 4,
  "content": "수정: 사과는 맛있었지만 배달이 좀 늦었어요."
}
```

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "reviewId": 1,
    "rating": 4,
    "content": "수정: 사과는 맛있었지만 배달이 좀 늦었어요.",
    "isEdited": true,
    "updatedAt": "2026-01-31T10:00:00+09:00"
  },
  "timestamp": "2026-01-31T10:00:00+09:00"
}
```

---

#### API-REV-003: 리뷰 삭제

| 항목 | 내용 |
|------|------|
| **Method** | `DELETE` |
| **URL** | `/api/v1/reviews/{reviewId}` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 자신이 작성한 리뷰를 삭제한다. |
| **관련 FR** | FR-REV-002 |
| **관련 UC** | UC-C09 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "message": "리뷰가 삭제되었습니다."
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-REV-004: 리뷰 신고

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/reviews/{reviewId}/report` |
| **인증** | Bearer Token |
| **권한** | 모든 인증 사용자 |
| **설명** | 부적절한 리뷰를 신고한다. |
| **관련 FR** | FR-REV-002 |
| **관련 UC** | UC-C09 |

**Request Body**

```json
{
  "reason": "욕설 및 부적절한 내용이 포함되어 있습니다."
}
```

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "reportId": 101,
    "reviewId": 1,
    "status": "PENDING",
    "message": "신고가 접수되었습니다. 관리자 검토 후 처리됩니다."
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```
#### API-REV-005: 리뷰 답변 작성 (마트)

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/store/reviews/{reviewId}/reply` |
| **인증** | Bearer Token |
| **권한** | STORE_OWNER |
| **설명** | 자기 마트에 작성된 리뷰에 답변을 작성한다. |
| **관련 FR** | FR-REV-002 |
| **관련 UC** | UC-S07 |

**Request Body**

```json
{
  "content": "소중한 리뷰 감사합니다. 항상 신선한 상품을 제공하도록 노력하겠습니다!"
}
```

**Response (201 Created)**

```json
{
  "success": true,
  "data": {
    "reviewId": 1,
    "reply": {
      "content": "소중한 리뷰 감사합니다. 항상 신선한 상품을 제공하도록 노력하겠습니다!",
      "createdAt": "2026-01-30T14:00:00+09:00"
    }
  },
  "timestamp": "2026-01-30T14:00:00+09:00"
}
```

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `403` | `ERR_FORBIDDEN` | 자기 마트 리뷰가 아닌 경우 |
| `409` | `ERR_CONFLICT` | 이미 답변이 존재하는 경우 |

---

#### API-REV-006: 리뷰 답변 수정 (마트)

| 항목 | 내용 |
|------|------|
| **Method** | `PATCH` |
| **URL** | `/api/v1/store/reviews/{reviewId}/reply` |
| **인증** | Bearer Token |
| **권한** | STORE_OWNER |
| **설명** | 리뷰 답변을 수정한다. |
| **관련 FR** | FR-REV-002 |
| **관련 UC** | UC-S07 |

**Request Body**

```json
{
  "content": "수정된 답변 내용입니다. 다시 한번 감사드립니다."
}
```

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "reviewId": 1,
    "reply": {
      "content": "수정된 답변 내용입니다. 다시 한번 감사드립니다.",
      "updatedAt": "2026-01-30T15:00:00+09:00"
    }
  },
  "timestamp": "2026-01-30T15:00:00+09:00"
}
```

---

#### API-REV-007: 리뷰 답변 삭제 (마트)

| 항목 | 내용 |
|------|------|
| **Method** | `DELETE` |
| **URL** | `/api/v1/store/reviews/{reviewId}/reply` |
| **인증** | Bearer Token |
| **권한** | STORE_OWNER |
| **설명** | 리뷰 답변을 삭제한다. |
| **관련 FR** | FR-REV-002 |
| **관련 UC** | UC-S07 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "message": "답변이 삭제되었습니다."
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

### 3.11 마트 운영 API (Store Operations)

> 관련 FR: FR-STO-001~004, FR-PRD-002~003, FR-ORD-004, FR-SUB-001, FR-SET-001
> 관련 UC: UC-S01~UC-S12
> 관련 테이블: `stores`, `store_business_hours`, `products`, `store_orders`, `subscription_products`, `settlements`

---

#### API-SOP-001: 마트 대시보드 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/store/dashboard` |
| **인증** | Bearer Token |
| **권한** | STORE_OWNER |
| **설명** | 마트 운영 대시보드를 조회한다. 오늘 매출, 주문 현황, 인기 상품 등. |
| **관련 FR** | FR-STO-004 |
| **관련 UC** | UC-S01 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "storeId": 1,
    "storeName": "행복한 마트",
    "activeStatus": "ACTIVE",
    "todaySummary": {
      "totalSales": 580000,
      "orderCount": 32,
      "cancelCount": 2,
      "averageOrderAmount": 18125
    },
    "orderStatus": {
      "pending": 3,
      "accepted": 5,
      "ready": 2,
      "delivering": 4,
      "completed": 18
    },
    "popularProducts": [
      { "productId": 101, "productName": "유기농 사과 (3입)", "salesCount": 15 },
      { "productId": 102, "productName": "제주 감귤 (1kg)", "salesCount": 12 }
    ],
    "lowStockProducts": [
      { "productId": 201, "productName": "한우 등심 (200g)", "stock": 3 }
    ]
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-SOP-002: 마트 주문 목록 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/store/orders` |
| **인증** | Bearer Token |
| **권한** | STORE_OWNER |
| **설명** | 마트에 들어온 주문 목록을 조회한다. 상태별 필터링 가능. |
| **관련 FR** | FR-ORD-004 |
| **관련 UC** | UC-S04, UC-S05 |

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `status` | String | X | - | `PENDING`, `ACCEPTED`, `READY`, `PICKED_UP`, `DELIVERING`, `DELIVERED`, `CANCELLED`, `REJECTED` |
| `startDate` | String | X | - | 조회 시작일 |
| `endDate` | String | X | - | 조회 종료일 |
| `page` | Integer | X | 0 | 페이지 번호 |
| `size` | Integer | X | 20 | 페이지 크기 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "storeOrderId": 2001,
        "orderNumber": "ORD-20260130-001001",
        "status": "PENDING",
        "customerName": "홍*동",
        "productSummary": "유기농 사과 (3입) 외 1건",
        "productCount": 2,
        "storeProductPrice": 19800,
        "deliveryFee": 3000,
        "finalPrice": 22800,
        "orderedAt": "2026-01-30T10:30:00+09:00",
        "deliveryTimeSlot": "11:00~14:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 32,
    "totalPages": 2
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-SOP-003: 마트 주문 수락/거절

| 항목 | 내용 |
|------|------|
| **Method** | `PATCH` |
| **URL** | `/api/v1/store/orders/{storeOrderId}/status` |
| **인증** | Bearer Token |
| **권한** | STORE_OWNER |
| **설명** | 주문을 수락하거나 거절한다. 5분 내 미응답 시 자동 거절. |
| **관련 FR** | FR-ORD-004 |
| **관련 UC** | UC-S04, UC-S05 |

**Request Body**

```json
{
  "action": "ACCEPT",
  "rejectReason": null
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `action` | String | O | `ACCEPT` 또는 `REJECT` |
| `rejectReason` | String | REJECT 시 O | 거절 사유: `OUT_OF_STOCK` (재고 부족), `CLOSED` (영업 종료), `OTHER` (기타) |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "storeOrderId": 2001,
    "status": "ACCEPTED",
    "message": "주문이 수락되었습니다. 상품 준비를 시작해주세요.",
    "updatedAt": "2026-01-30T10:35:00+09:00"
  },
  "timestamp": "2026-01-30T10:35:00+09:00"
}
```

---

#### API-SOP-004: 마트 주문 준비 완료

| 항목 | 내용 |
|------|------|
| **Method** | `PATCH` |
| **URL** | `/api/v1/store/orders/{storeOrderId}/ready` |
| **인증** | Bearer Token |
| **권한** | STORE_OWNER |
| **설명** | 상품 준비가 완료되어 픽업 대기 상태로 전환한다. |
| **관련 FR** | FR-ORD-004 |
| **관련 UC** | UC-S05 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "storeOrderId": 2001,
    "status": "READY",
    "message": "준비 완료! 라이더가 곧 픽업하러 옵니다.",
    "updatedAt": "2026-01-30T10:50:00+09:00"
  },
  "timestamp": "2026-01-30T10:50:00+09:00"
}
```

---

#### API-SOP-005: 상품 등록

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/store/products` |
| **인증** | Bearer Token |
| **권한** | STORE_OWNER |
| **설명** | 새 상품을 등록한다. |
| **관련 FR** | FR-PRD-002 |
| **관련 UC** | UC-S02 |

**Request Body**

```json
{
  "productName": "유기농 사과 (3입)",
  "categoryId": 2,
  "price": 12000,
  "salePrice": 9900,
  "stock": 50,
  "description": "경북 영주 GAP 인증 농장에서 재배한 유기농 사과",
  "origin": "국내산 (경북 영주)",
  "unit": "3개입",
  "imageUrl": "https://cdn.dongnae.com/products/101.jpg",
  "isActive": true
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `productName` | String | O | 상품명 |
| `categoryId` | Long | O | 카테고리 ID |
| `price` | Long | O | 정가 |
| `salePrice` | Long | X | 할인가 |
| `stock` | Integer | O | 재고 수량 |
| `description` | String | X | 상품 설명 |
| `origin` | String | X | 원산지 |
| `unit` | String | X | 용량/단위 |
| `imageUrl` | String | X | 상품 이미지 URL |
| `isActive` | Boolean | X | 활성 여부 (기본 true) |

**Response (201 Created)**

```json
{
  "success": true,
  "data": {
    "productId": 101,
    "productName": "유기농 사과 (3입)",
    "price": 12000,
    "salePrice": 9900,
    "stock": 50,
    "isActive": true,
    "createdAt": "2026-01-30T09:00:00+09:00"
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-SOP-006: 상품 수정

| 항목 | 내용 |
|------|------|
| **Method** | `PATCH` |
| **URL** | `/api/v1/store/products/{productId}` |
| **인증** | Bearer Token |
| **권한** | STORE_OWNER |
| **설명** | 상품 정보를 수정한다. 가격, 재고, 활성 상태 등 부분 수정 가능. |
| **관련 FR** | FR-PRD-002, FR-PRD-003 |
| **관련 UC** | UC-S02, UC-S03 |

**Request Body** (부분 수정)

```json
{
  "salePrice": 8900,
  "stock": 30,
  "isActive": true
}
```

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "productId": 101,
    "productName": "유기농 사과 (3입)",
    "price": 12000,
    "salePrice": 8900,
    "stock": 30,
    "isActive": true,
    "updatedAt": "2026-01-30T09:00:00+09:00"
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-SOP-007: 상품 삭제

| 항목 | 내용 |
|------|------|
| **Method** | `DELETE` |
| **URL** | `/api/v1/store/products/{productId}` |
| **인증** | Bearer Token |
| **권한** | STORE_OWNER |
| **설명** | 상품을 삭제한다. (소프트 삭제: `deleted_at` 설정) |
| **관련 FR** | FR-PRD-002 |
| **관련 UC** | UC-S02 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "message": "상품이 삭제되었습니다."
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-SOP-008: 영업시간 수정

| 항목 | 내용 |
|------|------|
| **Method** | `PATCH` |
| **URL** | `/api/v1/store/business-hours` |
| **인증** | Bearer Token |
| **권한** | STORE_OWNER |
| **설명** | 요일별 영업시간을 수정한다. |
| **관련 FR** | FR-STO-003 |
| **관련 UC** | UC-S08 |

**Request Body**

```json
{
  "businessHours": [
    { "dayOfWeek": 1, "openTime": "09:00", "closeTime": "21:00", "lastOrderTime": "20:30", "isClosed": false },
    { "dayOfWeek": 0, "openTime": null, "closeTime": null, "lastOrderTime": null, "isClosed": true }
  ]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `dayOfWeek` | Integer | O | 요일 (0=일, 1=월, ..., 6=토) |
| `openTime` | String | 영업일 O | 오픈 시간 (HH:mm) |
| `closeTime` | String | 영업일 O | 마감 시간 (HH:mm) |
| `lastOrderTime` | String | X | 라스트오더 시간 (HH:mm) |
| `isClosed` | Boolean | O | 휴무일 여부 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "businessHours": [
      { "dayOfWeek": 0, "openTime": null, "closeTime": null, "lastOrderTime": null, "isClosed": true },
      { "dayOfWeek": 1, "openTime": "09:00", "closeTime": "21:00", "lastOrderTime": "20:30", "isClosed": false }
    ],
    "updatedAt": "2026-01-30T09:00:00+09:00"
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-SOP-009: 구독 상품 목록 조회 (마트)

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/store/subscription-products` |
| **인증** | Bearer Token |
| **권한** | STORE_OWNER |
| **설명** | 마트에서 등록한 구독 상품 목록을 조회한다. |
| **관련 FR** | FR-SUB-001 |
| **관련 UC** | UC-S10 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": [
    {
      "subscriptionProductId": 1,
      "name": "신선 과일 정기배달",
      "description": "매주 엄선된 제철 과일을 배달해드립니다.",
      "price": 29900,
      "totalDeliveryCount": 4,
      "status": "ACTIVE",
      "subscriberCount": 15,
      "items": [
        { "productId": 101, "productName": "유기농 사과 (3입)", "quantity": 1 },
        { "productId": 102, "productName": "제주 감귤 (1kg)", "quantity": 2 }
      ]
    }
  ],
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-SOP-010: 구독 상품 등록 (마트)

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/store/subscription-products` |
| **인증** | Bearer Token |
| **권한** | STORE_OWNER |
| **설명** | 구독 상품을 새로 등록한다. |
| **관련 FR** | FR-SUB-001 |
| **관련 UC** | UC-S10 |

**Request Body**

```json
{
  "name": "건강 야채 박스",
  "description": "매주 신선한 유기농 야채를 배달합니다.",
  "price": 25000,
  "totalDeliveryCount": 4,
  "items": [
    { "productId": 301, "quantity": 1 },
    { "productId": 302, "quantity": 2 }
  ]
}
```

**Response (201 Created)**

```json
{
  "success": true,
  "data": {
    "subscriptionProductId": 2,
    "name": "건강 야채 박스",
    "price": 25000,
    "status": "ACTIVE",
    "createdAt": "2026-01-30T09:00:00+09:00"
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-SOP-010B: 구독 신청자 목록 조회 (마트)

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/store/subscriptions/subscribers` |
| **인증** | Bearer Token |
| **권한** | STORE_OWNER |
| **설명** | 마트의 구독 상품에 가입한 고객 목록을 조회한다. |
| **관련 FR** | FR-SUB-001 |
| **관련 UC** | UC-S11 |

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `subscriptionProductId` | Long | X | - | 특정 구독 상품 필터 |
| `status` | String | X | - | `ACTIVE`, `PAUSED`, `CANCELLED` |
| `page` | Integer | X | 0 | 페이지 번호 |
| `size` | Integer | X | 20 | 페이지 크기 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "subscriptionId": 1,
        "customerName": "홍*동",
        "subscriptionProductName": "신선 과일 정기배달",
        "status": "ACTIVE",
        "deliveryDayLabels": ["월", "수", "금"],
        "nextDeliveryDate": "2026-02-03",
        "subscribedAt": "2026-01-15T10:00:00+09:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 15,
    "totalPages": 1
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-SOP-010C: 구독 신청 승인/거절 (마트)

| 항목 | 내용 |
|------|------|
| **Method** | `PATCH` |
| **URL** | `/api/v1/store/subscriptions/{subscriptionId}/status` |
| **인증** | Bearer Token |
| **권한** | STORE_OWNER |
| **설명** | 구독 신청을 승인 또는 거절한다. |
| **관련 FR** | FR-SUB-001 |
| **관련 UC** | UC-S12 |

**Request Body**

```json
{
  "action": "APPROVE",
  "rejectReason": null
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `action` | String | O | `APPROVE` 또는 `REJECT` |
| `rejectReason` | String | REJECT 시 O | 거절 사유 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "subscriptionId": 1,
    "status": "ACTIVE",
    "message": "구독 신청이 승인되었습니다.",
    "updatedAt": "2026-01-30T10:00:00+09:00"
  },
  "timestamp": "2026-01-30T10:00:00+09:00"
}
```

---

#### API-SOP-011: 마트 정산 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/store/settlements` |
| **인증** | Bearer Token |
| **권한** | STORE_OWNER |
| **설명** | 마트의 정산 내역을 조회한다. 월 단위 정산. |
| **관련 FR** | FR-SET-001, FR-SET-003 |
| **관련 UC** | UC-S09 |

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `year` | Integer | X | 현재 연도 | 조회 연도 |
| `month` | Integer | X | 현재 월 | 조회 월 |
| `page` | Integer | X | 0 | 페이지 번호 |
| `size` | Integer | X | 20 | 페이지 크기 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "settlementId": 1,
        "periodStart": "2026-01-01",
        "periodEnd": "2026-01-31",
        "totalSales": 5800000,
        "platformFee": 580000,
        "deliveryFeeTotal": 450000,
        "settlementAmount": 5220000,
        "status": "COMPLETED",
        "paidAt": "2026-02-05T10:00:00+09:00",
        "details": [
          {
            "storeOrderId": 2001,
            "orderNumber": "ORD-20260130-001001",
            "amount": 19800,
            "fee": 1980,
            "deliveryFee": 3000,
            "netAmount": 17820
          }
        ]
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 3,
    "totalPages": 1
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-SOP-012: 마트 운영 상태 전환

| 항목 | 내용 |
|------|------|
| **Method** | `PATCH` |
| **URL** | `/api/v1/store/active-status` |
| **인증** | Bearer Token |
| **권한** | STORE_OWNER |
| **설명** | 마트 운영 상태를 변경한다 (영업 시작/일시정지). |
| **관련 FR** | FR-STO-004 |
| **관련 UC** | UC-S01 |

**Request Body**

```json
{
  "activeStatus": "ACTIVE"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `activeStatus` | String | O | `ACTIVE` (영업중), `INACTIVE` (일시정지) |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "storeId": 1,
    "activeStatus": "ACTIVE",
    "message": "영업을 시작합니다.",
    "updatedAt": "2026-01-30T09:00:00+09:00"
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

### 3.12 라이더 운영 API (Rider Operations)

> 관련 FR: FR-DLV-001~002, FR-DLV-008, FR-SET-002
> 관련 UC: UC-R01~UC-R09
> 관련 테이블: `riders`, `deliveries`, `settlements`, `rider_locations`

---

#### API-ROP-001: 라이더 대시보드 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/rider/dashboard` |
| **인증** | Bearer Token |
| **권한** | RIDER |
| **설명** | 라이더 대시보드를 조회한다. 오늘 수익, 진행 중 배달, 통계 등. |
| **관련 FR** | FR-DLV-002, FR-SET-002 |
| **관련 UC** | UC-R01 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "riderId": 1,
    "operationStatus": "ONLINE",
    "todayEarnings": 45000,
    "weeklyEarnings": 285000,
    "todayDeliveryCount": 12,
    "activeDeliveries": [
      {
        "deliveryId": 4001,
        "storeName": "행복한 마트",
        "status": "DELIVERING",
        "deliveryAddress": "서울시 강남구 테헤란로 123",
        "estimatedArrival": "2026-01-30T11:20:00+09:00"
      }
    ],
    "todayStats": {
      "totalDistance": 18.5,
      "averageDeliveryTime": 22,
      "completionRate": 95.8
    }
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-ROP-002: 배달 이력 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/rider/history` |
| **인증** | Bearer Token |
| **권한** | RIDER |
| **설명** | 배달 이력을 조회한다. 기간별 필터 가능. |
| **관련 FR** | FR-DLV-004 |
| **관련 UC** | UC-R06 |

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `period` | String | X | `today` | `today`, `week`, `month`, `custom` |
| `startDate` | String | custom 시 | - | 조회 시작일 |
| `endDate` | String | custom 시 | - | 조회 종료일 |
| `page` | Integer | X | 0 | 페이지 번호 |
| `size` | Integer | X | 20 | 페이지 크기 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "deliveryId": 4001,
        "storeName": "행복한 마트",
        "deliveryAddress": "서울시 강남구 테헤란로 123",
        "status": "DELIVERED",
        "riderEarning": 3000,
        "distance": 1.5,
        "deliveryTime": 18,
        "completedAt": "2026-01-30T11:25:00+09:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8,
    "periodSummary": {
      "totalEarnings": 45000,
      "totalDeliveries": 12,
      "totalDistance": 18.5,
      "averageTime": 22
    }
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-ROP-003: 라이더 수익 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/rider/earnings` |
| **인증** | Bearer Token |
| **권한** | RIDER |
| **설명** | 오늘/주간/월간 수익을 조회한다. |
| **관련 FR** | FR-SET-002 |
| **관련 UC** | UC-R07 |

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `period` | String | X | `today` | `today`, `week`, `month` |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "period": "week",
    "totalEarnings": 285000,
    "deliveryCount": 72,
    "averageEarningPerDelivery": 3958,
    "dailyBreakdown": [
      { "date": "2026-01-24", "earnings": 42000, "deliveries": 11 },
      { "date": "2026-01-25", "earnings": 38000, "deliveries": 10 },
      { "date": "2026-01-26", "earnings": 45000, "deliveries": 12 },
      { "date": "2026-01-27", "earnings": 40000, "deliveries": 10 },
      { "date": "2026-01-28", "earnings": 35000, "deliveries": 9 },
      { "date": "2026-01-29", "earnings": 42000, "deliveries": 11 },
      { "date": "2026-01-30", "earnings": 43000, "deliveries": 9 }
    ]
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-ROP-004: 라이더 정산 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/rider/settlements` |
| **인증** | Bearer Token |
| **권한** | RIDER |
| **설명** | 주간 정산 내역을 조회한다. 매주 수요일 정산. |
| **관련 FR** | FR-SET-002, FR-SET-003 |
| **관련 UC** | UC-R08 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "settlementId": 10,
        "periodStart": "2026-01-20",
        "periodEnd": "2026-01-26",
        "totalEarnings": 285000,
        "deliveryCount": 72,
        "platformFee": 28500,
        "settlementAmount": 256500,
        "status": "COMPLETED",
        "paidAt": "2026-01-29T10:00:00+09:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 8,
    "totalPages": 1
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-ROP-005: 운송수단 목록 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/rider/vehicles` |
| **인증** | Bearer Token |
| **권한** | RIDER |
| **설명** | 등록된 운송수단 목록을 조회한다. |
| **관련 FR** | FR-DLV-008 |
| **관련 UC** | UC-R09 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": [
    {
      "vehicleId": 1,
      "vehicleType": "BICYCLE",
      "vehicleTypeLabel": "자전거",
      "vehicleNumber": null,
      "isActive": true,
      "requiresLicense": false
    },
    {
      "vehicleId": 2,
      "vehicleType": "MOTORCYCLE",
      "vehicleTypeLabel": "오토바이",
      "vehicleNumber": "서울12가3456",
      "isActive": false,
      "requiresLicense": true
    }
  ],
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-ROP-006: 운송수단 등록

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/rider/vehicles` |
| **인증** | Bearer Token |
| **권한** | RIDER |
| **설명** | 운송수단을 등록한다. 오토바이/승용차는 면허 심사 필요. |
| **관련 FR** | FR-DLV-008 |
| **관련 UC** | UC-R09 |

**Request Body**

```json
{
  "vehicleType": "MOTORCYCLE",
  "vehicleNumber": "서울12가3456",
  "licenseDocumentUrl": "https://cdn.dongnae.com/docs/license-001.pdf"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `vehicleType` | String | O | `WALK` (도보), `BICYCLE` (자전거), `MOTORCYCLE` (오토바이), `CAR` (승용차) |
| `vehicleNumber` | String | 오토바이/승용차 O | 차량 번호 |
| `licenseDocumentUrl` | String | 오토바이/승용차 O | 면허증 사본 URL |

**Response (201 Created)**

```json
{
  "success": true,
  "data": {
    "vehicleId": 3,
    "vehicleType": "MOTORCYCLE",
    "vehicleNumber": "서울12가3456",
    "isActive": false,
    "message": "운송수단이 등록되었습니다. 면허 심사 후 사용 가능합니다."
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-ROP-007: 운송수단 삭제

| 항목 | 내용 |
|------|------|
| **Method** | `DELETE` |
| **URL** | `/api/v1/rider/vehicles/{vehicleId}` |
| **인증** | Bearer Token |
| **권한** | RIDER |
| **설명** | 운송수단을 삭제한다. 현재 사용 중인 수단은 삭제 불가. |
| **관련 FR** | FR-DLV-008 |
| **관련 UC** | UC-R09 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "message": "운송수단이 삭제되었습니다."
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

**에러 응답**

| HTTP | 에러 코드 | 상황 |
|------|----------|------|
| `422` | `ERR_UNPROCESSABLE` | 현재 사용 중인 운송수단 삭제 시도 |

---

#### API-ROP-008: 라이더 등록 (가입)

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/rider/register` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER (가입 후 RIDER 역할 부여) |
| **설명** | 배달원으로 가입한다. 승인 모듈을 통해 신원 확인 후 활성화. |
| **관련 FR** | FR-DLV-001, FR-APR-001 |
| **관련 UC** | UC-R01 |

**Request Body**

```json
{
  "vehicleType": "BICYCLE",
  "vehicleNumber": null,
  "isResident": true,
  "documents": [
    { "documentType": "ID_CARD", "documentUrl": "https://cdn.dongnae.com/docs/id-001.pdf" }
  ]
}
```

**Response (201 Created)**

```json
{
  "success": true,
  "data": {
    "riderId": 1,
    "status": "PENDING",
    "approvalId": 10,
    "message": "라이더 가입 신청이 완료되었습니다. 신원 확인 후 활성화됩니다."
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

### 3.13 관리자 API (Admin)

> 관련 FR: FR-USR-005, FR-STO-002, FR-APR-001~002, FR-RPT-001, FR-SET-001~003, FR-NTF-002, FR-NTC-001
> 관련 UC: UC-A01~UC-A15
> 관련 테이블: `users`, `stores`, `riders`, `approvals`, `reports`, `settlements`, `notices`, `notification_broadcasts`

---

#### API-ADM-001: 관리자 대시보드

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/dashboard` |
| **인증** | Bearer Token |
| **권한** | ADMIN |
| **설명** | 플랫폼 전체 모니터링 대시보드를 조회한다. |
| **관련 FR** | FR-USR-005 |
| **관련 UC** | UC-A01 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "userStats": {
      "totalUsers": 15420,
      "activeUsers": 12800,
      "newUsersToday": 45,
      "suspendedUsers": 12
    },
    "storeStats": {
      "totalStores": 320,
      "activeStores": 280,
      "pendingApprovals": 5
    },
    "riderStats": {
      "totalRiders": 150,
      "onlineRiders": 45,
      "pendingApprovals": 3
    },
    "orderStats": {
      "todayOrders": 850,
      "todayRevenue": 15800000,
      "todayCancellations": 12,
      "activeDeliveries": 35
    },
    "reportStats": {
      "pendingReports": 8,
      "resolvedToday": 3
    }
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-ADM-002: 사용자 목록 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/users` |
| **인증** | Bearer Token |
| **권한** | ADMIN |
| **설명** | 전체 사용자 목록을 조회한다. |
| **관련 FR** | FR-USR-005 |
| **관련 UC** | UC-A02 |

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `status` | String | X | - | `ACTIVE`, `INACTIVE`, `SUSPENDED`, `PENDING` |
| `role` | String | X | - | `CUSTOMER`, `STORE_OWNER`, `RIDER`, `ADMIN` |
| `keyword` | String | X | - | 이름/이메일 검색 |
| `page` | Integer | X | 0 | 페이지 번호 |
| `size` | Integer | X | 20 | 페이지 크기 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "userId": 1,
        "email": "hong@example.com",
        "name": "홍길동",
        "phone": "010****5678",
        "status": "ACTIVE",
        "roles": ["CUSTOMER"],
        "createdAt": "2026-01-01T09:00:00+09:00",
        "lastLoginAt": "2026-01-30T08:00:00+09:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 15420,
    "totalPages": 771
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-ADM-003: 사용자 상태 변경

| 항목 | 내용 |
|------|------|
| **Method** | `PATCH` |
| **URL** | `/api/v1/admin/users/{userId}/status` |
| **인증** | Bearer Token |
| **권한** | ADMIN |
| **설명** | 사용자 상태를 변경한다 (활성/정지/비활성). |
| **관련 FR** | FR-USR-005 |
| **관련 UC** | UC-A02 |

**Request Body**

```json
{
  "status": "SUSPENDED",
  "reason": "부적절한 행위 반복"
}
```

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "userId": 100,
    "status": "SUSPENDED",
    "reason": "부적절한 행위 반복",
    "updatedAt": "2026-01-30T09:00:00+09:00"
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-ADM-004: 마트 목록 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/stores` |
| **인증** | Bearer Token |
| **권한** | ADMIN |
| **설명** | 전체 마트 목록을 조회한다. |
| **관련 FR** | FR-STO-002 |
| **관련 UC** | UC-A04 |

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `status` | String | X | - | `PENDING`, `APPROVED`, `REJECTED`, `SUSPENDED` |
| `activeStatus` | String | X | - | `ACTIVE`, `INACTIVE`, `CLOSED` |
| `keyword` | String | X | - | 마트명/사업자번호 검색 |
| `page` | Integer | X | 0 | 페이지 번호 |
| `size` | Integer | X | 20 | 페이지 크기 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "storeId": 1,
        "storeName": "행복한 마트",
        "ownerName": "김사장",
        "businessNumber": "123-45-67890",
        "status": "APPROVED",
        "activeStatus": "ACTIVE",
        "rating": 4.5,
        "orderCount": 1250,
        "createdAt": "2025-06-15T09:00:00+09:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 320,
    "totalPages": 16
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-ADM-005: 마트 상태 변경

| 항목 | 내용 |
|------|------|
| **Method** | `PATCH` |
| **URL** | `/api/v1/admin/stores/{storeId}/status` |
| **인증** | Bearer Token |
| **권한** | ADMIN |
| **설명** | 마트 승인 상태를 변경한다. |
| **관련 FR** | FR-STO-002 |
| **관련 UC** | UC-A04, UC-A05 |

**Request Body**

```json
{
  "status": "SUSPENDED",
  "reason": "위생 점검 미통과"
}
```

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "storeId": 1,
    "status": "SUSPENDED",
    "reason": "위생 점검 미통과",
    "updatedAt": "2026-01-30T09:00:00+09:00"
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-ADM-006: 라이더 목록 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/riders` |
| **인증** | Bearer Token |
| **권한** | ADMIN |
| **설명** | 전체 라이더 목록을 조회한다. |
| **관련 FR** | FR-DLV-001 |
| **관련 UC** | UC-A06 |

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `status` | String | X | - | `PENDING`, `APPROVED`, `REJECTED`, `SUSPENDED` |
| `operationStatus` | String | X | - | `ONLINE`, `OFFLINE` |
| `keyword` | String | X | - | 이름/연락처 검색 |
| `page` | Integer | X | 0 | 페이지 번호 |
| `size` | Integer | X | 20 | 페이지 크기 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "riderId": 1,
        "userName": "김배달",
        "phone": "010****5678",
        "operationStatus": "ONLINE",
        "approvalStatus": "APPROVED",
        "vehicleType": "BICYCLE",
        "totalDeliveries": 450,
        "rating": 4.8,
        "joinedAt": "2025-09-01T09:00:00+09:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-ADM-007: 승인 목록 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/approvals` |
| **인증** | Bearer Token |
| **권한** | ADMIN |
| **설명** | 입점/라이더 승인 목록을 조회한다. |
| **관련 FR** | FR-APR-001, FR-APR-002 |
| **관련 UC** | UC-A05, UC-A07 |

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `applicantType` | String | X | - | `MART`, `RIDER` |
| `status` | String | X | - | `PENDING`, `APPROVED`, `REJECTED`, `HELD` |
| `page` | Integer | X | 0 | 페이지 번호 |
| `size` | Integer | X | 20 | 페이지 크기 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "approvalId": 5,
        "applicantType": "MART",
        "applicantName": "싱싱 마트",
        "applicantEmail": "fresh@example.com",
        "status": "PENDING",
        "documents": [
          { "documentType": "BUSINESS_LICENSE", "documentUrl": "https://cdn.dongnae.com/docs/bl-001.pdf" },
          { "documentType": "BANK_PASSBOOK", "documentUrl": "https://cdn.dongnae.com/docs/bp-001.pdf" }
        ],
        "createdAt": "2026-01-29T14:00:00+09:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 8,
    "totalPages": 1
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-ADM-008: 승인 처리

| 항목 | 내용 |
|------|------|
| **Method** | `PATCH` |
| **URL** | `/api/v1/admin/approvals/{approvalId}` |
| **인증** | Bearer Token |
| **권한** | ADMIN |
| **설명** | 승인 요청을 승인/거절/보류 처리한다. |
| **관련 FR** | FR-APR-001 |
| **관련 UC** | UC-A05, UC-A07 |

**Request Body**

```json
{
  "action": "APPROVED",
  "rejectionReason": null
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `action` | String | O | `APPROVED`, `REJECTED`, `HELD` |
| `rejectionReason` | String | REJECTED/HELD 시 O | 거절/보류 사유 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "approvalId": 5,
    "status": "APPROVED",
    "message": "승인이 완료되었습니다.",
    "approvedAt": "2026-01-30T10:00:00+09:00"
  },
  "timestamp": "2026-01-30T10:00:00+09:00"
}
```

---

#### API-ADM-009: 신고 목록 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/reports` |
| **인증** | Bearer Token |
| **권한** | ADMIN |
| **설명** | 신고 목록을 조회한다. |
| **관련 FR** | FR-RPT-001 |
| **관련 UC** | UC-A08 |

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `status` | String | X | - | `PENDING`, `RESOLVED` |
| `targetType` | String | X | - | `STORE`, `RIDER`, `CUSTOMER` |
| `page` | Integer | X | 0 | 페이지 번호 |
| `size` | Integer | X | 20 | 페이지 크기 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "reportId": 1,
        "reporterName": "홍*동",
        "targetType": "STORE",
        "targetName": "불량 마트",
        "storeOrderId": 2005,
        "content": "상품 상태가 매우 불량합니다.",
        "status": "PENDING",
        "createdAt": "2026-01-29T18:00:00+09:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 8,
    "totalPages": 1
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-ADM-010: 신고 처리

| 항목 | 내용 |
|------|------|
| **Method** | `PATCH` |
| **URL** | `/api/v1/admin/reports/{reportId}` |
| **인증** | Bearer Token |
| **권한** | ADMIN |
| **설명** | 신고를 처리 완료한다. |
| **관련 FR** | FR-RPT-001 |
| **관련 UC** | UC-A08 |

**Request Body**

```json
{
  "resolution": "경고 조치 완료. 해당 마트에 경고 메시지를 발송했습니다.",
  "actionTaken": "WARNING"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `resolution` | String | O | 처리 내용 |
| `actionTaken` | String | O | `WARNING` (경고), `SUSPEND` (정지), `DISMISS` (기각) |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "reportId": 1,
    "status": "RESOLVED",
    "resolution": "경고 조치 완료. 해당 마트에 경고 메시지를 발송했습니다.",
    "resolvedAt": "2026-01-30T10:00:00+09:00"
  },
  "timestamp": "2026-01-30T10:00:00+09:00"
}
```

---

#### API-ADM-011: 정산 목록 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/settlements` |
| **인증** | Bearer Token |
| **권한** | ADMIN |
| **설명** | 전체 정산 내역을 조회한다. |
| **관련 FR** | FR-SET-001, FR-SET-002 |
| **관련 UC** | UC-A10 |

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `targetType` | String | X | - | `STORE`, `RIDER` |
| `status` | String | X | - | `PENDING`, `COMPLETED`, `FAILED` |
| `year` | Integer | X | 현재 | 연도 |
| `month` | Integer | X | 현재 | 월 |
| `page` | Integer | X | 0 | 페이지 번호 |
| `size` | Integer | X | 20 | 페이지 크기 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "settlementId": 1,
        "targetType": "STORE",
        "targetName": "행복한 마트",
        "periodStart": "2026-01-01",
        "periodEnd": "2026-01-31",
        "totalSales": 5800000,
        "platformFee": 580000,
        "settlementAmount": 5220000,
        "status": "PENDING"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 350,
    "totalPages": 18
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-ADM-012: 정산 실행

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/admin/settlements` |
| **인증** | Bearer Token |
| **권한** | ADMIN |
| **설명** | 정산을 실행한다 (배치). |
| **관련 FR** | FR-SET-001, FR-SET-002 |
| **관련 UC** | UC-A10 |

**Request Body**

```json
{
  "targetType": "STORE",
  "periodStart": "2026-01-01",
  "periodEnd": "2026-01-31"
}
```

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "processedCount": 280,
    "totalAmount": 125000000,
    "message": "280건의 마트 정산이 처리되었습니다."
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-ADM-013: 공지사항 목록 조회 (관리자)

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/notices` |
| **인증** | Bearer Token |
| **권한** | ADMIN |
| **설명** | 공지사항 목록을 조회한다 (비활성 포함). |
| **관련 FR** | FR-NTC-001 |
| **관련 UC** | UC-A11 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "noticeId": 1,
        "title": "서비스 점검 안내",
        "content": "2026-02-01 02:00~06:00 서비스 점검이 예정되어 있습니다.",
        "isPinned": true,
        "status": "ACTIVE",
        "sortOrder": 1,
        "createdAt": "2026-01-28T10:00:00+09:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 15,
    "totalPages": 1
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-ADM-014: 공지사항 등록

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/admin/notices` |
| **인증** | Bearer Token |
| **권한** | ADMIN |
| **설명** | 새 공지사항을 등록한다. |
| **관련 FR** | FR-NTC-001 |
| **관련 UC** | UC-A11 |

**Request Body**

```json
{
  "title": "신규 기능 안내",
  "content": "구독 서비스가 새롭게 오픈되었습니다.",
  "isPinned": false,
  "sortOrder": 10
}
```

**Response (201 Created)**

```json
{
  "success": true,
  "data": {
    "noticeId": 16,
    "title": "신규 기능 안내",
    "status": "ACTIVE",
    "createdAt": "2026-01-30T10:00:00+09:00"
  },
  "timestamp": "2026-01-30T10:00:00+09:00"
}
```

---

#### API-ADM-015: 공지사항 수정

| 항목 | 내용 |
|------|------|
| **Method** | `PATCH` |
| **URL** | `/api/v1/admin/notices/{noticeId}` |
| **인증** | Bearer Token |
| **권한** | ADMIN |
| **설명** | 공지사항을 수정한다. |
| **관련 FR** | FR-NTC-001 |
| **관련 UC** | UC-A11 |

**Request Body**

```json
{
  "title": "신규 기능 안내 (수정)",
  "isPinned": true
}
```

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "noticeId": 16,
    "title": "신규 기능 안내 (수정)",
    "isPinned": true,
    "updatedAt": "2026-01-30T10:30:00+09:00"
  },
  "timestamp": "2026-01-30T10:30:00+09:00"
}
```

---

#### API-ADM-016: 공지사항 삭제

| 항목 | 내용 |
|------|------|
| **Method** | `DELETE` |
| **URL** | `/api/v1/admin/notices/{noticeId}` |
| **인증** | Bearer Token |
| **권한** | ADMIN |
| **설명** | 공지사항을 삭제한다. |
| **관련 FR** | FR-NTC-001 |
| **관련 UC** | UC-A11 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "message": "공지사항이 삭제되었습니다."
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-ADM-017: 알림 브로드캐스트

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/admin/notifications/broadcast` |
| **인증** | Bearer Token |
| **권한** | ADMIN |
| **설명** | 역할 그룹 또는 전체 사용자에게 알림을 발송한다. |
| **관련 FR** | FR-NTF-002 |
| **관련 UC** | UC-A12 |

**Request Body**

```json
{
  "title": "긴급 공지",
  "content": "태풍으로 인해 오늘 배달 서비스가 제한됩니다.",
  "targetGroup": "ALL"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `title` | String | O | 알림 제목 |
| `content` | String | O | 알림 내용 |
| `targetGroup` | String | O | `CUSTOMER`, `STORE`, `RIDER`, `ALL` |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "broadcastId": 1,
    "targetGroup": "ALL",
    "recipientCount": 15420,
    "message": "15,420명에게 알림이 발송되었습니다.",
    "sentAt": "2026-01-30T09:00:00+09:00"
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

### 3.14 기타 API (Misc)

> 관련 FR: FR-NTF-001, FR-RPT-001, FR-INQ-001, FR-NTC-001, FR-BNR-001, FR-PRM-001, FR-CPN-001
> 관련 테이블: `notifications`, `reports`, `inquiries`, `notices`, `banners`, `promotions`, `promotion_products`

---

#### API-NTF-001: 알림 목록 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/notifications` |
| **인증** | Bearer Token |
| **권한** | 모든 인증 사용자 |
| **설명** | 내 알림 목록을 조회한다. |
| **관련 FR** | FR-NTF-001 |

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `isRead` | Boolean | X | - | 읽음 여부 필터 |
| `page` | Integer | X | 0 | 페이지 번호 |
| `size` | Integer | X | 20 | 페이지 크기 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "notificationId": 1,
        "title": "주문이 접수되었습니다",
        "content": "행복한 마트에서 주문을 확인 중입니다.",
        "referenceType": "ORDER",
        "referenceId": 1001,
        "isRead": false,
        "createdAt": "2026-01-30T10:30:00+09:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 45,
    "totalPages": 3,
    "unreadCount": 5
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-NTF-002: 알림 읽음 처리

| 항목 | 내용 |
|------|------|
| **Method** | `PATCH` |
| **URL** | `/api/v1/notifications/{notificationId}/read` |
| **인증** | Bearer Token |
| **권한** | 모든 인증 사용자 |
| **설명** | 알림을 읽음 처리한다. |
| **관련 FR** | FR-NTF-001 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "notificationId": 1,
    "isRead": true
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-NTF-003: 전체 알림 읽음 처리

| 항목 | 내용 |
|------|------|
| **Method** | `PATCH` |
| **URL** | `/api/v1/notifications/read-all` |
| **인증** | Bearer Token |
| **권한** | 모든 인증 사용자 |
| **설명** | 모든 알림을 읽음 처리한다. |
| **관련 FR** | FR-NTF-001 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "updatedCount": 5,
    "message": "5건의 알림이 읽음 처리되었습니다."
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-RPT-001: 신고 접수

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/reports` |
| **인증** | Bearer Token |
| **권한** | 모든 인증 사용자 |
| **설명** | 마트/라이더/고객을 신고한다. |
| **관련 FR** | FR-RPT-001 |

**Request Body**

```json
{
  "targetType": "STORE",
  "targetId": 1,
  "storeOrderId": 2005,
  "content": "상품 상태가 매우 불량합니다. 유통기한이 지난 제품이 배달되었습니다."
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `targetType` | String | O | `STORE`, `RIDER`, `CUSTOMER` |
| `targetId` | Long | O | 신고 대상 ID |
| `storeOrderId` | Long | X | 관련 마트주문 ID |
| `content` | String | O | 신고 내용 |

**Response (201 Created)**

```json
{
  "success": true,
  "data": {
    "reportId": 101,
    "status": "PENDING",
    "message": "신고가 접수되었습니다. 관리자 검토 후 처리됩니다."
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-INQ-001: 1:1 문의 목록 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/inquiries` |
| **인증** | Bearer Token |
| **권한** | 모든 인증 사용자 |
| **설명** | 내 1:1 문의 목록을 조회한다. |
| **관련 FR** | FR-INQ-001 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "inquiryId": 1,
        "category": "ORDER_PAYMENT",
        "categoryLabel": "주문/결제",
        "title": "결제가 두 번 되었습니다",
        "status": "ANSWERED",
        "createdAt": "2026-01-28T14:00:00+09:00",
        "answeredAt": "2026-01-29T10:00:00+09:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 3,
    "totalPages": 1
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-INQ-002: 1:1 문의 등록

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **URL** | `/api/v1/inquiries` |
| **인증** | Bearer Token |
| **권한** | 모든 인증 사용자 |
| **설명** | 1:1 문의를 등록한다. |
| **관련 FR** | FR-INQ-001 |

**Request Body**

```json
{
  "category": "CANCELLATION_REFUND",
  "title": "환불이 안 되고 있습니다",
  "content": "3일 전 취소한 주문의 환불이 아직 처리되지 않았습니다. 주문번호: ORD-20260127-000501"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `category` | String | O | `ORDER_PAYMENT`, `CANCELLATION_REFUND`, `DELIVERY`, `SERVICE`, `OTHER` |
| `title` | String | O | 문의 제목 |
| `content` | String | O | 문의 내용 |

**Response (201 Created)**

```json
{
  "success": true,
  "data": {
    "inquiryId": 4,
    "category": "CANCELLATION_REFUND",
    "status": "PENDING",
    "message": "문의가 등록되었습니다. 담당자가 확인 후 답변드리겠습니다."
  },
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-NTC-001: 공지사항 목록 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/notices` |
| **인증** | Public |
| **권한** | - |
| **설명** | 활성 공지사항 목록을 조회한다. |
| **관련 FR** | FR-NTC-001 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": [
    {
      "noticeId": 1,
      "title": "서비스 점검 안내",
      "content": "2026-02-01 02:00~06:00 서비스 점검이 예정되어 있습니다.",
      "isPinned": true,
      "createdAt": "2026-01-28T10:00:00+09:00"
    }
  ],
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-BNR-001: 배너 목록 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/banners` |
| **인증** | Public |
| **권한** | - |
| **설명** | 현재 활성 배너 목록을 조회한다. 히어로 슬라이드 용도. |
| **관련 FR** | FR-BNR-001 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": [
    {
      "bannerId": 1,
      "title": "신규 가입 이벤트",
      "imageUrl": "https://cdn.dongnae.com/banners/event-01.jpg",
      "linkUrl": "/promotions/1",
      "sortOrder": 1,
      "startDate": "2026-01-01",
      "endDate": "2026-02-28"
    }
  ],
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-PRM-001: 프로모션 목록 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/promotions` |
| **인증** | Public |
| **권한** | - |
| **설명** | 활성 프로모션 목록을 조회한다. |
| **관련 FR** | FR-PRM-001 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": [
    {
      "promotionId": 1,
      "name": "설날 특별 할인",
      "description": "명절 선물세트 최대 30% 할인",
      "discountType": "PERCENTAGE",
      "discountValue": 30,
      "status": "ACTIVE",
      "startDate": "2026-01-25",
      "endDate": "2026-02-05",
      "imageUrl": "https://cdn.dongnae.com/promotions/lunar-new-year.jpg",
      "products": [
        { "productId": 501, "productName": "한우 선물세트", "originalPrice": 120000, "promotionPrice": 84000 }
      ]
    }
  ],
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

#### API-CPN-001: 내 쿠폰 목록 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/api/v1/coupons` |
| **인증** | Bearer Token |
| **권한** | CUSTOMER |
| **설명** | 사용 가능한 쿠폰 목록을 조회한다. |
| **관련 FR** | FR-CPN-001 |

**Response (200 OK)**

```json
{
  "success": true,
  "data": [
    {
      "couponId": 1,
      "name": "신규 가입 축하 쿠폰",
      "discountType": "FIXED",
      "discountValue": 3000,
      "minimumOrderAmount": 15000,
      "expiryDate": "2026-03-01",
      "isUsed": false,
      "usedAt": null
    },
    {
      "couponId": 2,
      "name": "10% 할인 쿠폰",
      "discountType": "PERCENTAGE",
      "discountValue": 10,
      "minimumOrderAmount": 20000,
      "expiryDate": "2026-02-15",
      "isUsed": false,
      "usedAt": null
    }
  ],
  "timestamp": "2026-01-30T09:00:00+09:00"
}
```

---

## 4. 실시간 API (WebSocket / SSE)

> 관련 FR: FR-ORD-003, FR-DLV-006, FR-ORD-004, FR-DLV-003
> 관련 UC: UC-C08, UC-S04, UC-R03

### 4.1 WebSocket 연결

| 항목 | 내용 |
|------|------|
| **프로토콜** | WebSocket (STOMP over SockJS) |
| **URL** | `wss://api.dongnaemarket.com/ws` |
| **인증** | 연결 시 JWT 토큰 전달 (헤더 또는 쿼리 파라미터) |

### 4.2 실시간 채널

#### WS-001: 주문 상태 변경 알림

| 항목 | 내용 |
|------|------|
| **구독 경로** | `/topic/orders/{orderId}/status` |
| **대상** | CUSTOMER |
| **설명** | 주문 상태가 변경될 때 실시간으로 고객에게 알림 |

**메시지 포맷**

```json
{
  "type": "ORDER_STATUS_CHANGED",
  "orderId": 1001,
  "storeOrderId": 2001,
  "storeName": "행복한 마트",
  "previousStatus": "ACCEPTED",
  "newStatus": "READY",
  "statusLabel": "픽업 대기",
  "message": "행복한 마트에서 상품 준비가 완료되었습니다.",
  "timestamp": "2026-01-30T10:50:00+09:00"
}
```

---

#### WS-002: 라이더 위치 갱신

| 항목 | 내용 |
|------|------|
| **구독 경로** | `/topic/deliveries/{deliveryId}/location` |
| **대상** | CUSTOMER |
| **설명** | 배달 중 라이더의 실시간 위치를 고객에게 전송 (3~5초 간격) |

**메시지 포맷**

```json
{
  "type": "RIDER_LOCATION_UPDATE",
  "deliveryId": 4001,
  "riderId": 1,
  "latitude": 37.5070,
  "longitude": 127.0535,
  "estimatedArrival": "2026-01-30T11:20:00+09:00",
  "distanceRemaining": 0.8,
  "timestamp": "2026-01-30T11:15:00+09:00"
}
```

---

#### WS-003: 신규 주문 알림 (마트)

| 항목 | 내용 |
|------|------|
| **구독 경로** | `/topic/stores/{storeId}/new-orders` |
| **대상** | STORE_OWNER |
| **설명** | 마트에 새 주문이 접수될 때 실시간 알림. 5분 내 응답 필요. |

**메시지 포맷**

```json
{
  "type": "NEW_ORDER",
  "storeOrderId": 2001,
  "orderNumber": "ORD-20260130-001001",
  "customerName": "홍*동",
  "productSummary": "유기농 사과 (3입) 외 1건",
  "productCount": 2,
  "totalPrice": 22800,
  "deliveryTimeSlot": "11:00~14:00",
  "expiresAt": "2026-01-30T10:35:00+09:00",
  "timestamp": "2026-01-30T10:30:00+09:00"
}
```

---

#### WS-004: 신규 배달 요청 (라이더)

| 항목 | 내용 |
|------|------|
| **구독 경로** | `/topic/riders/{riderId}/new-deliveries` |
| **대상** | RIDER (ONLINE 상태) |
| **설명** | 라이더 주변에 새 배달 요청이 발생할 때 실시간 알림 |

**메시지 포맷**

```json
{
  "type": "NEW_DELIVERY_REQUEST",
  "deliveryId": 4001,
  "storeName": "행복한 마트",
  "storeAddress": "서울시 강남구 테헤란로 100",
  "deliveryAddress": "서울시 강남구 테헤란로 123",
  "distance": 1.5,
  "riderEarning": 3000,
  "estimatedTime": "15분",
  "productSummary": "유기농 사과 (3입) 외 1건",
  "timestamp": "2026-01-30T10:50:00+09:00"
}
```

---

## 5. API × PRD 기능 매핑 검증표

| FR ID | 기능명 | API ID | 검증 |
|-------|--------|--------|------|
| FR-USR-001 | 회원가입 | API-AUTH-001, 005, 006 | ✅ |
| FR-USR-002 | 소셜 로그인 | API-AUTH-003 | ✅ |
| FR-USR-003 | 배송지 관리 | API-USR-004~007 | ✅ |
| FR-USR-004 | 프로필 관리 | API-USR-001~003 | ✅ |
| FR-USR-005 | 사용자 상태 관리 | API-ADM-002, 003 | ✅ |
| FR-STO-001 | 마트 등록 | API-STO-006 | ✅ |
| FR-STO-002 | 마트 승인 프로세스 | API-ADM-005, 007, 008 | ✅ |
| FR-STO-003 | 영업시간 관리 | API-SOP-008 | ✅ |
| FR-STO-004 | 마트 운영 상태 | API-SOP-012, API-STO-001~002 | ✅ |
| FR-PRD-001 | 카테고리 관리 | API-PRD-003 | ✅ |
| FR-PRD-002 | 상품 CRUD | API-SOP-005~007, API-PRD-002, API-STO-003 | ✅ |
| FR-PRD-003 | 재고 관리 | API-SOP-006 | ✅ |
| FR-PRD-004 | 상품 검색/정렬 | API-PRD-001 | ✅ |
| FR-ORD-001 | 장바구니 | API-CRT-001~004 | ✅ |
| FR-ORD-002 | 주문 생성 | API-ORD-001 | ✅ |
| FR-ORD-003 | 주문 상태 추적 | API-ORD-003, 005, WS-001, WS-002 | ✅ |
| FR-ORD-004 | 마트측 주문 처리 | API-SOP-002~004, WS-003 | ✅ |
| FR-ORD-005 | 주문 취소 | API-ORD-004 | ✅ |
| FR-ORD-006 | 주문 이력 관리 | API-ORD-002, 003, 006 | ✅ |
| FR-PAY-001 | 결제 수단 관리 | API-USR-008~010 | ✅ |
| FR-PAY-002 | 결제 처리 | API-PAY-001, 002 | ✅ |
| FR-PAY-003 | 환불 | API-PAY-003 | ✅ |
| FR-DLV-001 | 라이더 등록 | API-ROP-008 | ✅ |
| FR-DLV-002 | 온라인 상태 관리 | API-DLV-006 | ✅ |
| FR-DLV-003 | 배달 수락 | API-DLV-001, 002, WS-004 | ✅ |
| FR-DLV-004 | 배달 단계 진행 | API-DLV-003 | ✅ |
| FR-DLV-005 | 배달 완료 인증 | API-DLV-004 | ✅ |
| FR-DLV-006 | 실시간 위치 추적 | API-DLV-005 | ✅ |
| FR-DLV-007 | 고객 메시지 전송 | API-DLV-007 | ✅ |
| FR-DLV-008 | 운송 수단 관리 | API-ROP-005~007 | ✅ |
| FR-DLV-009 | 주민 배달원 프로그램 | API-ROP-008 | ✅ |
| FR-SUB-001 | 구독 상품 관리 | API-SOP-009, 010, 010B, 010C, API-STO-005 | ✅ |
| FR-SUB-002 | 구독 신청 | API-SUB-001 | ✅ |
| FR-SUB-003 | 구독 상태 관리 | API-SUB-002~005 | ✅ |
| FR-SUB-004 | 구독 이력 | API-SUB-002 | ✅ |
| FR-REV-001 | 리뷰 작성 | API-REV-001 | ✅ |
| FR-REV-002 | 리뷰 관리 | API-REV-002~007, API-STO-004 | ✅ |
| FR-SET-001 | 마트 정산 | API-SOP-011, API-ADM-011, 012 | ✅ |
| FR-SET-002 | 라이더 정산 | API-ROP-003, 004, API-ADM-011, 012 | ✅ |
| FR-SET-003 | 정산 상세 | API-SOP-011, API-ROP-004 | ✅ |
| FR-APR-001 | 입점/가입 승인 | API-ADM-007, 008 | ✅ |
| FR-APR-002 | 서류 제출 | API-STO-006, API-ROP-008 | ✅ |
| FR-NTF-001 | 개인 알림 | API-NTF-001~003 | ✅ |
| FR-NTF-002 | 전체 공지 브로드캐스트 | API-ADM-017 | ✅ |
| FR-RPT-001 | 신고 | API-RPT-001, API-ADM-009, 010 | ✅ |
| FR-INQ-001 | 1:1 문의 | API-INQ-001, 002 | ✅ |
| FR-NTC-001 | 공지사항 | API-NTC-001, API-ADM-013~016 | ✅ |
| FR-BNR-001 | 배너 관리 | API-BNR-001 | ✅ |
| FR-PRM-001 | 프로모션 | API-PRM-001 | ✅ |
| FR-CPN-001 | 쿠폰 시스템 | API-CPN-001 | ✅ |

> **검증 결과**: 49개 FR 항목 전체가 최소 1개 이상의 API에 매핑됨 ✅

---

## 6. API × ERD 테이블 매핑 검증표

| # | 테이블 | 관련 API | 검증 |
|---|--------|---------|------|
| 1 | `users` | AUTH-001~003, USR-001~003, ADM-002~003 | ✅ |
| 2 | `roles` | AUTH-001~003 | ✅ |
| 3 | `user_roles` | AUTH-001~003 | ✅ |
| 4 | `addresses` | USR-004~007, ORD-001, SUB-001 | ✅ |
| 5 | `social_logins` | AUTH-003 | ✅ |
| 6 | `stores` | STO-001~006, SOP-001~012, ADM-004~005 | ✅ |
| 7 | `store_business_hours` | STO-002, SOP-008 | ✅ |
| 8 | `categories` | PRD-003, STO-003 | ✅ |
| 9 | `products` | PRD-001~002, STO-003, SOP-005~007 | ✅ |
| 10 | `orders` | ORD-001~006, PAY-001~002 | ✅ |
| 11 | `store_orders` | ORD-001~005, SOP-002~004, REV-001 | ✅ |
| 12 | `order_products` | ORD-001, 003 | ✅ |
| 13 | `carts` | CRT-001~004 | ✅ |
| 14 | `cart_products` | CRT-001~004 | ✅ |
| 15 | `payments` | PAY-001~003, ORD-001, 003 | ✅ |
| 16 | `payment_refunds` | PAY-002~003 | ✅ |
| 17 | `payment_methods` | USR-008~010, ORD-001, SUB-001 | ✅ |
| 18 | `riders` | DLV-001~007, ROP-001~008, ADM-006 | ✅ |
| 19 | `deliveries` | DLV-001~004, ORD-005, ROP-002 | ✅ |
| 20 | `rider_locations` | DLV-005, ORD-005 | ✅ |
| 21 | `delivery_photos` | DLV-004 | ✅ |
| 22 | `subscription_products` | STO-005, SOP-009~010, SUB-001 | ✅ |
| 23 | `subscription_product_items` | STO-005, SOP-009~010 | ✅ |
| 24 | `subscriptions` | SUB-001~005 | ✅ |
| 25 | `subscription_day_of_week` | SUB-001~002 | ✅ |
| 26 | `subscription_history` | SUB-002 | ✅ |
| 27 | `reviews` | REV-001~007, STO-004 | ✅ |
| 28 | `settlements` | SOP-011, ROP-004, ADM-011~012 | ✅ |
| 29 | `settlement_details` | SOP-011, ROP-004 | ✅ |
| 30 | `approvals` | STO-006, ROP-008, ADM-007~008 | ✅ |
| 31 | `approval_documents` | STO-006, ROP-008, ADM-007 | ✅ |
| 32 | `notifications` | NTF-001~003 | ✅ |
| 33 | `notification_broadcasts` | ADM-017 | ✅ |
| 34 | `reports` | RPT-001, ADM-009~010 | ✅ |
| 35 | `inquiries` | INQ-001~002 | ✅ |
| 36 | `notices` | NTC-001, ADM-013~016 | ✅ |
| 37 | `banners` | BNR-001 | ✅ |
| 38 | `promotions` | PRM-001 | ✅ |
| 39 | `promotion_products` | PRM-001 | ✅ |

> **검증 결과**: 39개 ERD 테이블 전체가 최소 1개 이상의 API에 매핑됨 ✅

---

## 7. ENUM 사용 매핑 검증표

| # | ENUM | 값 | 사용 API |
|---|------|----|---------|
| 1 | `user_status` | ACTIVE, INACTIVE, SUSPENDED, PENDING | USR-001, ADM-002~003 |
| 2 | `social_provider` | KAKAO, NAVER, GOOGLE, APPLE | AUTH-003 |
| 3 | `store_status` | PENDING, APPROVED, REJECTED, SUSPENDED | STO-006, ADM-004~005, 007~008 |
| 4 | `store_active_status` | ACTIVE, INACTIVE, CLOSED | STO-001~002, SOP-001, 012 |
| 5 | `order_type` | REGULAR, SUBSCRIPTION | ORD-001~003, SUB-001 |
| 6 | `order_status` | PENDING, PAID, PARTIAL_CANCELLED, CANCELLED, COMPLETED | ORD-001~004 |
| 7 | `store_order_status` | PENDING, ACCEPTED, READY, PICKED_UP, DELIVERING, DELIVERED, CANCELLED, REJECTED | ORD-003, 005, SOP-002~004, WS-001 |
| 8 | `payment_method_type` | CARD, KAKAO_PAY, NAVER_PAY, TOSS_PAY | USR-008~009, PAY-001~002 |
| 9 | `payment_status` | PENDING, COMPLETED, FAILED, CANCELLED, PARTIAL_REFUNDED, REFUNDED | PAY-001~003 |
| 10 | `rider_operation_status` | OFFLINE, ONLINE | DLV-006, ROP-001 |
| 11 | `delivery_status` | REQUESTED, ACCEPTED, PICKED_UP, DELIVERING, DELIVERED, CANCELLED | DLV-001~004, ORD-005 |
| 12 | `subscription_product_status` | ACTIVE, INACTIVE | STO-005, SOP-009~010 |
| 13 | `subscription_status` | ACTIVE, PAUSED, CANCELLATION_PENDING, CANCELLED | SUB-001~005 |
| 14 | `sub_history_status` | SCHEDULED, ORDERED, SKIPPED, COMPLETED | SUB-002 |
| 15 | `settlement_target_type` | STORE, RIDER | SOP-011, ROP-004, ADM-011 |
| 16 | `settlement_status` | PENDING, COMPLETED, FAILED | SOP-011, ROP-004, ADM-011~012 |
| 17 | `applicant_type` | STORE, RIDER | STO-006, ROP-008 |
| 18 | `approval_applicant_type` | MART, RIDER | ADM-007~008 |
| 19 | `approval_status` | PENDING, APPROVED, REJECTED, HELD | ADM-007~008 |
| 20 | `document_type` | BUSINESS_LICENSE, BUSINESS_REPORT, BANK_PASSBOOK, ID_CARD | STO-006, ROP-008, ADM-007 |
| 21 | `notification_ref_type` | RIDER, STORE, CUSTOMER, ORDER, DELIVERY, PROMOTION | NTF-001 |
| 22 | `broadcast_ref_type` | RIDER, STORE, CUSTOMER, ALL | ADM-017 |
| 23 | `report_target_type` | STORE, RIDER, CUSTOMER | RPT-001, ADM-009~010 |
| 24 | `report_status` | PENDING, RESOLVED | RPT-001, ADM-009~010 |
| 25 | `inquiry_category` | ORDER_PAYMENT, CANCELLATION_REFUND, DELIVERY, SERVICE, OTHER | INQ-001~002 |
| 26 | `inquiry_status` | PENDING, ANSWERED | INQ-001 |
| 27 | `content_status` | ACTIVE, INACTIVE | NTC-001, ADM-013~016, BNR-001 |
| 28 | `promotion_status` | ACTIVE, ENDED | PRM-001 |

> **검증 결과**: 28/29개 ENUM이 API에서 사용됨 ✅ (FR-DLV-009 주민배달원 관련 `rider_approval_status`는 `riders` 테이블의 approval status 필드로 API-ROP-008에서 활용)

---

## 부록 A: API 엔드포인트 총 목록

| # | Module | ID | Method | URL | 인증 |
|---|--------|----|--------|-----|------|
| 1 | Auth | AUTH-001 | POST | `/api/v1/auth/register` | Public |
| 2 | Auth | AUTH-002 | POST | `/api/v1/auth/login` | Public |
| 3 | Auth | AUTH-003 | POST | `/api/v1/auth/social-login` | Public |
| 4 | Auth | AUTH-004 | POST | `/api/v1/auth/logout` | Bearer |
| 5 | Auth | AUTH-005 | POST | `/api/v1/auth/send-verification` | Public |
| 6 | Auth | AUTH-006 | POST | `/api/v1/auth/verify-phone` | Public |
| 7 | Auth | AUTH-007 | POST | `/api/v1/auth/refresh` | Public |
| 8 | Users | USR-001 | GET | `/api/v1/users/me` | Bearer |
| 9 | Users | USR-002 | PATCH | `/api/v1/users/me` | Bearer |
| 10 | Users | USR-003 | DELETE | `/api/v1/users/me` | Bearer |
| 11 | Users | USR-004 | GET | `/api/v1/users/me/addresses` | Bearer |
| 12 | Users | USR-005 | POST | `/api/v1/users/me/addresses` | Bearer |
| 13 | Users | USR-006 | PATCH | `/api/v1/users/me/addresses/{addressId}` | Bearer |
| 14 | Users | USR-007 | DELETE | `/api/v1/users/me/addresses/{addressId}` | Bearer |
| 15 | Users | USR-008 | GET | `/api/v1/users/me/payment-methods` | Bearer |
| 16 | Users | USR-009 | POST | `/api/v1/users/me/payment-methods` | Bearer |
| 17 | Users | USR-010 | DELETE | `/api/v1/users/me/payment-methods/{id}` | Bearer |
| 18 | Stores | STO-001 | GET | `/api/v1/stores` | Bearer |
| 19 | Stores | STO-002 | GET | `/api/v1/stores/{storeId}` | Bearer |
| 20 | Stores | STO-003 | GET | `/api/v1/stores/{storeId}/products` | Bearer |
| 21 | Stores | STO-004 | GET | `/api/v1/stores/{storeId}/reviews` | Bearer |
| 22 | Stores | STO-005 | GET | `/api/v1/stores/{storeId}/subscription-products` | Bearer |
| 23 | Stores | STO-006 | POST | `/api/v1/stores/register` | Bearer |
| 24 | Products | PRD-001 | GET | `/api/v1/products/search` | Bearer |
| 25 | Products | PRD-002 | GET | `/api/v1/products/{productId}` | Bearer |
| 26 | Products | PRD-003 | GET | `/api/v1/categories` | Public |
| 27 | Cart | CRT-001 | GET | `/api/v1/cart` | Bearer |
| 28 | Cart | CRT-002 | POST | `/api/v1/cart/items` | Bearer |
| 29 | Cart | CRT-003 | PATCH | `/api/v1/cart/items/{cartItemId}` | Bearer |
| 30 | Cart | CRT-004 | DELETE | `/api/v1/cart/items/{cartItemId}` | Bearer |
| 31 | Orders | ORD-001 | POST | `/api/v1/orders` | Bearer |
| 32 | Orders | ORD-002 | GET | `/api/v1/orders` | Bearer |
| 33 | Orders | ORD-003 | GET | `/api/v1/orders/{orderId}` | Bearer |
| 34 | Orders | ORD-004 | POST | `/api/v1/orders/{orderId}/cancel` | Bearer |
| 35 | Orders | ORD-005 | GET | `/api/v1/orders/{orderId}/tracking` | Bearer |
| 36 | Orders | ORD-006 | POST | `/api/v1/orders/{orderId}/reorder` | Bearer |
| 37 | Payments | PAY-001 | POST | `/api/v1/payments` | Bearer |
| 38 | Payments | PAY-002 | GET | `/api/v1/payments/{paymentId}` | Bearer |
| 39 | Payments | PAY-003 | POST | `/api/v1/payments/{paymentId}/refund` | Bearer |
| 40 | Deliveries | DLV-001 | GET | `/api/v1/rider/deliveries/available` | Bearer |
| 41 | Deliveries | DLV-002 | POST | `/api/v1/rider/deliveries/{id}/accept` | Bearer |
| 42 | Deliveries | DLV-003 | PATCH | `/api/v1/rider/deliveries/{id}/status` | Bearer |
| 43 | Deliveries | DLV-004 | POST | `/api/v1/rider/deliveries/{id}/complete` | Bearer |
| 44 | Deliveries | DLV-005 | POST | `/api/v1/rider/location` | Bearer |
| 45 | Deliveries | DLV-006 | PATCH | `/api/v1/rider/status` | Bearer |
| 46 | Deliveries | DLV-007 | POST | `/api/v1/rider/deliveries/{id}/message` | Bearer |
| 47 | Subscriptions | SUB-001 | POST | `/api/v1/subscriptions` | Bearer |
| 48 | Subscriptions | SUB-002 | GET | `/api/v1/subscriptions` | Bearer |
| 49 | Subscriptions | SUB-003 | PATCH | `/api/v1/subscriptions/{id}/pause` | Bearer |
| 50 | Subscriptions | SUB-004 | PATCH | `/api/v1/subscriptions/{id}/resume` | Bearer |
| 51 | Subscriptions | SUB-005 | DELETE | `/api/v1/subscriptions/{id}` | Bearer |
| 52 | Reviews | REV-001 | POST | `/api/v1/reviews` | Bearer |
| 53 | Reviews | REV-002 | PATCH | `/api/v1/reviews/{reviewId}` | Bearer |
| 54 | Reviews | REV-003 | DELETE | `/api/v1/reviews/{reviewId}` | Bearer |
| 55 | Reviews | REV-004 | POST | `/api/v1/reviews/{reviewId}/report` | Bearer |
| 56 | Store Ops | SOP-001 | GET | `/api/v1/store/dashboard` | Bearer |
| 57 | Store Ops | SOP-002 | GET | `/api/v1/store/orders` | Bearer |
| 58 | Store Ops | SOP-003 | PATCH | `/api/v1/store/orders/{id}/status` | Bearer |
| 59 | Store Ops | SOP-004 | PATCH | `/api/v1/store/orders/{id}/ready` | Bearer |
| 60 | Store Ops | SOP-005 | POST | `/api/v1/store/products` | Bearer |
| 61 | Store Ops | SOP-006 | PATCH | `/api/v1/store/products/{id}` | Bearer |
| 62 | Store Ops | SOP-007 | DELETE | `/api/v1/store/products/{id}` | Bearer |
| 63 | Store Ops | SOP-008 | PATCH | `/api/v1/store/business-hours` | Bearer |
| 64 | Store Ops | SOP-009 | GET | `/api/v1/store/subscription-products` | Bearer |
| 65 | Store Ops | SOP-010 | POST | `/api/v1/store/subscription-products` | Bearer |
| 66 | Store Ops | SOP-011 | GET | `/api/v1/store/settlements` | Bearer |
| 67 | Store Ops | SOP-012 | PATCH | `/api/v1/store/active-status` | Bearer |
| 68 | Rider Ops | ROP-001 | GET | `/api/v1/rider/dashboard` | Bearer |
| 69 | Rider Ops | ROP-002 | GET | `/api/v1/rider/history` | Bearer |
| 70 | Rider Ops | ROP-003 | GET | `/api/v1/rider/earnings` | Bearer |
| 71 | Rider Ops | ROP-004 | GET | `/api/v1/rider/settlements` | Bearer |
| 72 | Rider Ops | ROP-005 | GET | `/api/v1/rider/vehicles` | Bearer |
| 73 | Rider Ops | ROP-006 | POST | `/api/v1/rider/vehicles` | Bearer |
| 74 | Rider Ops | ROP-007 | DELETE | `/api/v1/rider/vehicles/{vehicleId}` | Bearer |
| 75 | Rider Ops | ROP-008 | POST | `/api/v1/rider/register` | Bearer |
| 76 | Admin | ADM-001 | GET | `/api/v1/admin/dashboard` | Bearer |
| 77 | Admin | ADM-002 | GET | `/api/v1/admin/users` | Bearer |
| 78 | Admin | ADM-003 | PATCH | `/api/v1/admin/users/{userId}/status` | Bearer |
| 79 | Admin | ADM-004 | GET | `/api/v1/admin/stores` | Bearer |
| 80 | Admin | ADM-005 | PATCH | `/api/v1/admin/stores/{storeId}/status` | Bearer |
| 81 | Admin | ADM-006 | GET | `/api/v1/admin/riders` | Bearer |
| 82 | Admin | ADM-007 | GET | `/api/v1/admin/approvals` | Bearer |
| 83 | Admin | ADM-008 | PATCH | `/api/v1/admin/approvals/{id}` | Bearer |
| 84 | Admin | ADM-009 | GET | `/api/v1/admin/reports` | Bearer |
| 85 | Admin | ADM-010 | PATCH | `/api/v1/admin/reports/{reportId}` | Bearer |
| 86 | Admin | ADM-011 | GET | `/api/v1/admin/settlements` | Bearer |
| 87 | Admin | ADM-012 | POST | `/api/v1/admin/settlements` | Bearer |
| 88 | Admin | ADM-013 | GET | `/api/v1/admin/notices` | Bearer |
| 89 | Admin | ADM-014 | POST | `/api/v1/admin/notices` | Bearer |
| 90 | Admin | ADM-015 | PATCH | `/api/v1/admin/notices/{noticeId}` | Bearer |
| 91 | Admin | ADM-016 | DELETE | `/api/v1/admin/notices/{noticeId}` | Bearer |
| 92 | Admin | ADM-017 | POST | `/api/v1/admin/notifications/broadcast` | Bearer |
| 93 | Misc | NTF-001 | GET | `/api/v1/notifications` | Bearer |
| 94 | Misc | NTF-002 | PATCH | `/api/v1/notifications/{id}/read` | Bearer |
| 95 | Misc | NTF-003 | PATCH | `/api/v1/notifications/read-all` | Bearer |
| 96 | Misc | RPT-001 | POST | `/api/v1/reports` | Bearer |
| 97 | Misc | INQ-001 | GET | `/api/v1/inquiries` | Bearer |
| 98 | Misc | INQ-002 | POST | `/api/v1/inquiries` | Bearer |
| 99 | Misc | NTC-001 | GET | `/api/v1/notices` | Public |
| 100 | Misc | BNR-001 | GET | `/api/v1/banners` | Public |
| 101 | Misc | PRM-001 | GET | `/api/v1/promotions` | Public |
| 102 | Misc | CPN-001 | GET | `/api/v1/coupons` | Bearer |
| 103 | Reviews | REV-005 | POST | `/api/v1/store/reviews/{reviewId}/reply` | Bearer |
| 104 | Reviews | REV-006 | PATCH | `/api/v1/store/reviews/{reviewId}/reply` | Bearer |
| 105 | Reviews | REV-007 | DELETE | `/api/v1/store/reviews/{reviewId}/reply` | Bearer |
| 106 | Store Ops | SOP-010B | GET | `/api/v1/store/subscriptions/subscribers` | Bearer |
| 107 | Store Ops | SOP-010C | PATCH | `/api/v1/store/subscriptions/{id}/status` | Bearer |

> **총 REST API: 107개** | **WebSocket 채널: 4개** | **총 111개 엔드포인트**

---

## 부록 B: 관련 문서

| 문서 | 경로 | 설명 |
|------|------|------|
| PRD v4.0 | `doc/v4.0/동네마켓_PRD_v4.0.md` | 49개 기능 요구사항 |
| ERD v4.1 | `doc/v4.0/동네마켓_ERD_v4.1.md` | 39 테이블, 29 ENUM |
| DDL v4.1 | `doc/v4.0/동네마켓_DDL_v4.1.md` | PostgreSQL 16 DDL 스크립트 |
| JPA 엔티티 v4.1 | `doc/v4.0/동네마켓_JPA_엔티티_정의서_v4.1.md` | Spring Boot JPA 엔티티 |
| 유즈케이스 v4.1 | `doc/v4.0/유즈케이스_v4.1.md` | UC-C01~C16, S01~S12, R01~R09, A01~A15 |
| 백로그 | `doc/v4.0/backlog.md` | UC 기반 백로그 |
| ERD 검증보고서 | `doc/v4.0/ERD_v4.1_검증보고서.md` | DBA 검증 결과 |
