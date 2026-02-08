# 토큰·JWT 검증 방법 (터미널 / Postman)

로그인·로그아웃·회원가입·토큰 갱신 후 **JWT(Access/Refresh)가 정상 동작하는지** 확인하는 방법을 정리합니다.

---

## 0. Postman에서 404 vs 403 vs 500 (같은 Host 필수)

| 현상 | 원인 | 조치 |
|------|------|------|
| **404** | `/api/users` 등 핸들러 없음 → 인증은 **통과**한 상태 | 정상. 동일 Host·쿠키로 요청했으면 JWT 검증 성공으로 봐도 됨. |
| **403** | 쿠키 없음 / 토큰 만료·위조 → **인증 실패** | 로그인과 **같은 URL(Host)** 로 요청했는지 확인. 쿠키 도메인은 `localhost:8080` 기준. |
| **500** | 필터에서 예외(예: claim `roles` null 등) | 백엔드에서 필터 예외 처리로 500 방지(인증 실패 시 403으로 유도). |

**같은 Host 확인 (쿠키가 안 붙는 문제 방지)**

- 로그인: `POST http://localhost:8080/api/auth/login`
- 인증 확인: `GET http://localhost:8080/api/users`
- **반드시 동일하게** `http://localhost:8080` 사용. `http://127.0.0.1:8080` 이면 쿠키가 다른 도메인으로 저장되어 GET /api/users 시 쿠키가 안 붙을 수 있음.
- Postman **Cookies**에서 저장된 도메인이 `localhost`(또는 `127.0.0.1`)인지 확인.

---

## 1. Postman으로 검증 (권장)

### 1.1 환경 준비

- **Base URL**: `http://localhost:8080` (백엔드 실행 중)
- **Environment 변수**(선택): `baseUrl` = `http://localhost:8080`
- **동일 Host**: 아래 모든 요청은 같은 `baseUrl`(같은 Host)로 보내야 쿠키가 자동 포함됨.

### 1.2 로그인 → 토큰 수신 확인

| 단계 | Method | URL | Body | 기대 |
|------|--------|-----|------|------|
| 1 | POST | `{{baseUrl}}/api/auth/login` | `{"email":"가입된이메일","password":"비밀번호"}` | 200, **Headers**에 Set-Cookie `nm_accessToken`, `nm_refreshToken` |

- **Headers** 탭에서 **Set-Cookie** 두 개 확인.
- Postman 하단 **Cookies** → `http://localhost:8080` → `nm_accessToken`, `nm_refreshToken` 존재 확인.

### 1.3 토큰 전송(인증) 확인

| 단계 | Method | URL | 설정 | 기대 |
|------|--------|-----|------|------|
| 2 | GET | `{{baseUrl}}/api/users` | 쿠키 자동(로그인 직후) | **404** (핸들러 없음) = 인증 **통과** |
| 3 | GET | `{{baseUrl}}/api/users` | Cookie 삭제 후 재요청 | **403** Forbidden |

- 404 → JWT가 넘어가서 인증된 상태.  
- 403 → 토큰 없음/만료/위조.

### 1.4 Refresh(토큰 갱신) 확인

| 단계 | Method | URL | 설정 | 기대 |
|------|--------|-----|------|------|
| 4 | POST | `{{baseUrl}}/api/auth/refresh` | Body 없음, **Cookies** 자동(nm_refreshToken) | 200, **Set-Cookie**에 새 토큰 |

- 로그인 또는 회원가입 직후 6번(refresh) 실행 시 200이면 Refresh 토큰 전송·갱신 정상.

### 1.5 로그아웃(토큰 무효화) 확인

| 단계 | Method | URL | 설정 | 기대 |
|------|--------|-----|------|------|
| 5 | POST | `{{baseUrl}}/api/auth/logout` | **Cookies** 자동(또는 Body `{"refreshToken":"..."}`) | 200, "로그아웃 되었습니다." |
| 6 | POST | `{{baseUrl}}/api/auth/refresh` | 직전과 동일 쿠키 그대로 | **401** (동일 refreshToken으로 재발급 불가) |

- 로그아웃 후 같은 refreshToken으로 refresh 호출 시 401이면 서버에서 무효화된 것이 맞음.

### 1.6 JWT 확인 체크리스트 (한 번에)

| # | 확인 내용 | Postman 작업 | OK 기준 |
|---|-----------|--------------|--------|
| 1 | 로그인 시 토큰 **수신** | POST /api/auth/login → Headers 탭 | Set-Cookie `nm_accessToken`, `nm_refreshToken` |
| 2 | Access 토큰 **전송** | GET /api/users (쿠키 자동) | **404** (403 아님) |
| 3 | Refresh **갱신** | POST /api/auth/refresh (쿠키 자동) | **200** + 새 Set-Cookie |
| 4 | 로그아웃 후 **무효화** | POST /api/auth/logout → 이후 refresh | **401** |

---

## 2. 터미널(curl)로 검증

백엔드 서버가 `http://localhost:8080`에서 동작 중일 때 아래 순서로 실행.

### 2.1 로그인 후 Set-Cookie 확인

```bash
curl -i -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"가입된이메일@example.com","password":"비밀번호"}'
```

- **기대**: `HTTP/1.1 200` + 응답 헤더에 `Set-Cookie: nm_accessToken=...`, `Set-Cookie: nm_refreshToken=...`

### 2.2 쿠키 저장 후 인증 필요 API 호출

```bash
# 로그인 후 쿠키를 파일에 저장
curl -c cookies.txt -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"qwer1234!!"}'

# 저장된 쿠키로 인증 필요 API 호출 (403 없이 404면 인증 통과)
curl -b cookies.txt http://localhost:8080/api/users
```

- **기대**: 첫 요청 200, 두 번째 요청 **404** (403이 아니면 JWT 쿠키가 정상 전송된 것).

### 2.3 토큰 갱신(Refresh)

```bash
curl -i -b cookies.txt -X POST http://localhost:8080/api/auth/refresh
```

- **기대**: 200 + 새 `Set-Cookie` 헤더.

### 2.4 로그아웃

```bash
curl -i -b cookies.txt -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{}'
```

- **기대**: 200 + "로그아웃 되었습니다."  
- 이후 `curl -b cookies.txt -X POST http://localhost:8080/api/auth/refresh` 시 **401** 기대.

---

## 3. 정리

| 목적 | 방법 |
|------|------|
| **토큰 수신** | 로그인/회원가입/refresh 응답 **Headers**에서 Set-Cookie 확인 |
| **토큰 전송** | 인증 필요 API(예: GET /api/users) 호출 시 쿠키 자동 포함 → 404면 통과, 403이면 미인증 |
| **Refresh 동작** | POST /api/auth/refresh (쿠키만) → 200 + 새 Set-Cookie |
| **로그아웃 무효화** | POST /api/auth/logout 후 동일 refreshToken으로 refresh → 401 |

협업 시 타인 환경에서는 **Base URL**만 해당 환경(예: `http://localhost:8080` 또는 배포 URL)으로 바꿔서 동일하게 사용하면 됩니다.
