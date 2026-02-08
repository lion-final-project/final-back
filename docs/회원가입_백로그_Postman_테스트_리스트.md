# 회원가입 백로그 Postman 테스트 리스트

Base URL: `http://localhost:8080` (백엔드 실행 시)

---

## 1. 정상 플로우 (회원가입 완료까지)

아래 순서대로 요청하면 회원가입 백로그 전체를 검증할 수 있습니다.

| # | 이름 | Method | URL | 설명 |
|---|------|--------|-----|------|
| 1 | 이메일 중복 확인 | GET | `{{baseUrl}}/api/auth/check-email?email=hong@example.com` | 중복 아니면 `data.duplicated: false` |
| 2 | 휴대폰 중복 확인 | GET | `{{baseUrl}}/api/auth/check-phone?phone=01012345678` | 중복 아니면 `data.duplicated: false` |
| 3 | 휴대폰 인증번호 발송 | POST | `{{baseUrl}}/api/auth/send-verification` | Body: `{"phone":"01012345678"}` → SMS 수신, 200 + expiresIn, remainingAttempts |
| 4 | 휴대폰 인증번호 검증 | POST | `{{baseUrl}}/api/auth/verify-phone` | Body: `{"phone":"01012345678","verificationCode":"123456"}` (3번에서 받은 6자리) → **응답 `data.phoneVerificationToken` 복사** |
| 5 | 회원가입 | POST | `{{baseUrl}}/api/auth/register` | Body에 4번에서 받은 `phoneVerificationToken` 포함 → 201 + **Headers에 Set-Cookie** (nm_accessToken, nm_refreshToken) |
| 6 | 토큰 갱신 (선택) | POST | `{{baseUrl}}/api/auth/refresh` | Cookie에 `nm_refreshToken` 설정 후 요청 → 200 + 새 Set-Cookie |

---

## 2. 요청 상세 (복사용)

### 1) 이메일 중복 확인
- **Method**: GET  
- **URL**: `http://localhost:8080/api/auth/check-email?email=hong@example.com`  
- **Body**: 없음  
- **기대**: 200, `data.duplicated` (true/false)

---

### 2) 휴대폰 중복 확인
- **Method**: GET  
- **URL**: `http://localhost:8080/api/auth/check-phone?phone=01012345678`  
- **Body**: 없음  
- **기대**: 200, `data.duplicated` (true/false)

---

### 3) 휴대폰 인증번호 발송
- **Method**: POST  
- **URL**: `http://localhost:8080/api/auth/send-verification`  
- **Headers**: `Content-Type: application/json`  
- **Body (raw JSON)**:
```json
{
  "phone": "01012345678"
}
```
- **기대**: 200, `data.expiresIn: 180`, `data.remainingAttempts` (숫자), SMS 수신

---

### 4) 휴대폰 인증번호 검증
- **Method**: POST  
- **URL**: `http://localhost:8080/api/auth/verify-phone`  
- **Headers**: `Content-Type: application/json`  
- **Body (raw JSON)**:
```json
{
  "phone": "01012345678",
  "verificationCode": "123456"
}
```
- **기대**: 200, `data.verified: true`, **`data.phoneVerificationToken`** (이 값을 5번 요청에 사용)

**4번 요청(휴대폰 인증번호 검증) — Tests 탭에 아래 스크립트 추가**  
→ 5번(회원가입)에서 `{{phoneVerificationToken}}`이 자동으로 채워지도록 함.
```javascript
var json = pm.response.json();
if (json.success && json.data && json.data.phoneVerificationToken) {
    pm.environment.set("phoneVerificationToken", json.data.phoneVerificationToken);
}
```

---

### 5) 회원가입
- **Method**: POST  
- **URL**: `http://localhost:8080/api/auth/register`  
- **Headers**: `Content-Type: application/json`  
- **Body (raw JSON)** — **방법 1 적용**: `phoneVerificationToken`은 **선택**.  
  - **옵션 A (토큰 사용)**: 4번 응답의 `data.phoneVerificationToken` 또는 `{{phoneVerificationToken}}` 사용.  
  - **옵션 B (토큰 생략)**: 4번(verify-phone) 성공 후 **5분 이내**면 `phoneVerificationToken` 필드를 **빼거나 null**로 보내도 됨. 서버가 Redis `SMS:PHONE_VERIFIED:<phone>` 상태로 검증.
```json
{
  "email": "hong@example.com",
  "password": "Abcd1234!",
  "name": "홍길동",
  "phone": "01012345678",
  "phoneVerificationToken": "{{phoneVerificationToken}}",
  "termsAgreed": true,
  "privacyAgreed": true,
  "marketingAgreed": false
}
```
- **5분 내 회원가입만 테스트할 때** (토큰 없이): 위에서 `phoneVerificationToken` 필드 제거한 Body로 4번 직후 5번 호출.
- **기대**: 201, **Headers > Set-Cookie**에 `nm_accessToken`, `nm_refreshToken`, Body `data`: userId, email, name, roles

**회원가입이 안 될 때 확인**
- **휴대폰 번호 통일**: 3번·4번·5번 요청 모두 **숫자만** 사용 (하이픈 없음). 예: `01012345678` ✅ / `010-1234-5678` ❌ (5번에서 400 발생)
- **토큰 사용 시**: 4번(verify-phone) 성공 후 **10분 이내**에 5번(register) 호출. 같은 토큰은 **1회만** 사용 가능.
- **토큰 생략 시**: 4번(verify-phone) 성공 후 **5분 이내**에 5번(register) 호출. 이때도 1회 사용 후 Redis에서 소비됨.

---

### 6) 토큰 갱신 (선택)
- **Method**: POST  
- **URL**: `http://localhost:8080/api/auth/refresh`  
- **Headers**: 없음  
- **Cookies**: 5번 응답에서 받은 `nm_refreshToken` 값 설정 (Postman: Cookies 메뉴에서 도메인 선택 후 추가)  
- **기대**: 200, **Headers > Set-Cookie**에 새 nm_accessToken, nm_refreshToken

---

## 3. 에러 케이스 (백로그 검증용)

| # | 이름 | Method | URL / Body | 기대 HTTP | 기대 에러 코드 |
|---|------|--------|------------|-----------|----------------|
| E1 | 이메일 형식 오류 | GET | `/api/auth/check-email?email=invalid` | 400 | - |
| E2 | 휴대폰 형식 오류 | GET | `/api/auth/check-phone?phone=02-1234-5678` | 400 | - |
| E3 | 이미 가입된 휴대폰로 인증 발송 | POST | `/api/auth/send-verification` + `{"phone":"이미가입된번호"}` | 409 | AUTH-003 |
| E4 | 인증번호 불일치 | POST | `/api/auth/verify-phone` + `{"phone":"01012345678","verificationCode":"000000"}` | 400 | AUTH-009 |
| E5 | 회원가입 - 인증 토큰 없음/만료 | POST | `/api/auth/register` + `phoneVerificationToken: ""` 또는 잘못된 토큰 | 422 | AUTH-004~006 |
| E6 | 회원가입 - 이메일 형식 오류 | POST | `/api/auth/register` + `email: "invalid"` | 400 | COMMON-001 등 |
| E7 | 회원가입 - 비밀번호 규칙 위반 | POST | `/api/auth/register` + `password: "short"` | 400 | COMMON-001 등 |
| E8 | 회원가입 - 필수 약관 미동의 | POST | `/api/auth/register` + `termsAgreed: false` | 400 | - |
| E9 | 토큰 갱신 - 쿠키 없음 | POST | `/api/auth/refresh` (Cookie 없이) | 401 | AUTH-010 |

---

## 4. Postman에서 쿠키 확인 (5번·6번)

- 회원가입(5번) 응답에서 **Headers** 탭 → **Set-Cookie** 확인.
- Postman 하단 **Cookies** 클릭 → `http://localhost:8080` 선택 → `nm_accessToken`, `nm_refreshToken` 존재 여부 확인.
- 6번(토큰 갱신)은 같은 도메인이라 쿠키가 자동으로 포함되도록 **Send** 시 Cookies가 전송되는지 확인.

---

## 5. 환경 변수 (선택)

Postman **Environments**에 아래 변수를 추가하면 URL·값 변경이 쉽습니다.

| 변수 | 예시 값 | 비고 |
|------|---------|------|
| baseUrl | http://localhost:8080 | URL 공통 |
| email | hong@example.com | 1번·5번 |
| phone | 01012345678 | 2·3·4·5번, **숫자만(하이픈 없음)** |
| phoneVerificationToken | (비워두기) | **4번 요청의 Tests 스크립트가 자동 설정** → 5번 Body에서 `{{phoneVerificationToken}}` 사용 |

**실행 순서**: 4번(verify-phone) 실행 → Tests에서 토큰 저장 → 5번(register) Body에 `{{phoneVerificationToken}}`이 자동 반영되므로 **4번 직후 5번을 실행**하면 회원가입이 됩니다.

---

## 6. JWT 토큰 확인 (Postman) — 무엇을 보면 되는지

JWT가 제대로 구현됐는지는 **세 가지**만 확인하면 됩니다.

### ① 토큰이 **서버 → 클라이언트**로 잘 넘어오는지 (수신)

| 확인 대상 | 방법 | 기대 결과 |
|-----------|------|-----------|
| 회원가입 시 토큰 발급 | 5번(회원가입) 실행 후 **응답 Headers** 탭 | **Set-Cookie**에 `nm_accessToken`, `nm_refreshToken` 두 개 존재 |
| 토큰 갱신 시 새 토큰 발급 | 6번(refresh) 실행 후 **응답 Headers** 탭 | **Set-Cookie**에 새 `nm_accessToken`, `nm_refreshToken` 존재 |

- Postman 하단 **Cookies** → `http://localhost:8080` 선택 시 `nm_accessToken`, `nm_refreshToken`이 보이면 **토큰이 잘 넘어온 것**입니다.

---

### ② 토큰이 **클라이언트 → 서버**로 잘 넘어가는지 (전송)

| 확인 대상 | 방법 | 기대 결과 |
|-----------|------|-----------|
| Refresh Token 전송 | 5번(회원가입) 성공 후 **바로 6번(refresh)** 실행 (별도 헤더/쿠키 설정 없이) | **200** + 응답 Headers에 새 Set-Cookie |
| Access Token 전송 | 아래 ③에서 인증 필요 URL 호출 시 쿠키 자동 포함 | 토큰 없으면 403, 쿠키 있으면 인증 통과(200 또는 404) |

- 6번에서 **쿠키를 수동으로 지우지 않았다면** Postman이 같은 도메인 쿠키를 자동으로 붙여 보냅니다. **200이 오면 refresh 토큰이 잘 넘어간 것**입니다.

---

### ③ Access Token으로 **인증이 통과하는지**

이 프로젝트는 **인증이 필요한 API**가 `/api/auth/*` 외의 경로입니다. 아무 인증 필요 URL로 아래처럼 테스트하면 됩니다.

| 요청 | Method | URL | Cookie / 헤더 | 기대 |
|------|--------|-----|----------------|------|
| A. 토큰 없음 | GET | `http://localhost:8080/api/users` | 없음 | **403** Forbidden |
| B. 쿠키로 Access Token | GET | `http://localhost:8080/api/users` | 5번/6번 후 저장된 쿠키 그대로(자동 전송) | **404** (핸들러 없음) = 인증은 **통과** |
| C. Bearer 헤더로 Access Token | GET | `http://localhost:8080/api/users` | `Authorization: Bearer <nm_accessToken 값>` | **404** = 인증 **통과** |

- **403** → 토큰이 안 넘어갔거나 만료/잘못된 토큰  
- **404** → 해당 URL에 컨트롤러는 없지만, **인증은 통과**한 상태 (JWT가 잘 넘어간 것)

**Bearer로 테스트하는 방법**

1. 5번(회원가입) 또는 6번(refresh) 실행 후 **Cookies**에서 `nm_accessToken` 값을 복사.
2. 새 요청: **GET** `http://localhost:8080/api/users`
3. **Headers**에 추가:  
   - Key: `Authorization`  
   - Value: `Bearer 붙여넣은_액세스토큰_값`  
4. Send → **404**가 나오면 JWT가 잘 넘어가서 인증이 된 것입니다.

---

### JWT 확인 체크리스트 (한 번에 보기)

| # | 확인 내용 | Postman에서 할 일 | OK 기준 |
|---|-----------|-------------------|--------|
| 1 | 토큰 **수신** (회원가입) | 5번 실행 → Headers 탭에서 Set-Cookie 확인 | `nm_accessToken`, `nm_refreshToken` 있음 |
| 2 | 토큰 **수신** (갱신) | 6번 실행 → Headers 탭에서 Set-Cookie 확인 | 새 토큰 두 개 있음 |
| 3 | Refresh 토큰 **전송** | 5번 직후 6번 실행 (쿠키 자동) | 6번 응답 **200** |
| 4 | Access 토큰 **전송** (쿠키) | GET `/api/users` (쿠키 자동) | **404** (403이 아님) |
| 5 | Access 토큰 **전송** (Bearer) | GET `/api/users` + `Authorization: Bearer <토큰>` | **404** (403이 아님) |

위 5가지가 모두 맞으면 **JWT가 Postman 기준으로 잘 넘어오고, 잘 넘어가는 것**까지 확인된 것입니다.
