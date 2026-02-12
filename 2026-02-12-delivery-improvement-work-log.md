# 배달 도메인 개선 작업 로그 (2026-02-12)

## 1. 개요

`delivery-improvement-plan.md`에 기반하여 배달 도메인의 P0(긴급 수정), P1(설계/품질 개선), P2(기능 구현) 항목을 완료하고,
추가로 **라이더 동시 배달 최대 3건** 비즈니스 규칙을 Redis SET 기반으로 구현했습니다.

---

## 2. P0 — 긴급 수정

### P0-1: RuntimeException → BusinessException 전환
- **파일**: `RiderServiceImpl.java`, `Delivery.java`
- `RuntimeException` → `BusinessException` + `ErrorCode` 교체
- 클라이언트에게 구조화된 에러 응답 (`ApiResponse`) 전달 가능

### P0-2: 배달 상태 전이 검증
- **파일**: `Delivery.java`
- `accept()`, `pickUp()`, `startDelivering()`, `complete()`, `cancel()` 각 메서드에 `validateStatusTransition()` 추가
- 허용: `REQUESTED→ACCEPTED→PICKED_UP→DELIVERING→DELIVERED` (또는 `CANCELLED`)

### P0-3: 인증(Authentication) 누락 보완
- **파일**: `RiderController.java`, `RiderServiceImpl.java`
- 위치 업데이트/삭제, 승인 삭제 엔드포인트에 `Authentication` 파라미터 추가
- `deleteApproval`에 본인 소유(ownership) 검증 추가

---

## 3. P1 — 설계/품질 개선

### P1-1: Rider @Setter 제거 → 도메인 메서드
- **파일**: `Rider.java`
- `@Setter` 제거, `goOnline()`, `goOffline()`, `startDelivering()`, `finishDelivering()` 도메인 메서드 추가
- 배달 중(DELIVERING) 상태에서 수동 상태 변경 차단

### P1-2: Rider.createResponse() → RiderResponse.from()
- **파일**: `Rider.java`, `RiderServiceImpl.java`
- Entity에서 DTO 의존 제거, 순환 참조 방지

### P1-3: RiderLocationService 분리 (SRP)
- **신규**: `RiderLocationServiceImpl.java`
- `RiderServiceImpl`에서 위치 관련 로직을 별도 서비스로 분리

### P1-4: Redis 키 상수화
- **신규**: `DeliveryRedisKeys.java`
- 하드코딩된 Redis 키를 상수 클래스로 집중 관리

### P1-5: 배달 수락 시 rider.startDelivering() 호출
- **파일**: `DeliveryMatchComponent.java`

### P1-6: DTO Validation 추가
- **파일**: `PostRiderLocationRequest.java`, `PatchRiderStatusRequest.java`
- `@NotNull`, `@DecimalMin`, `@DecimalMax` 검증 어노테이션 추가
- `RiderController`에 `@Valid` 추가

---

## 4. P2 — 기능 구현

### P2-1: 배달 워크플로우 서비스
- **신규**: `DeliveryService.java` (인터페이스), `DeliveryServiceImpl.java`
- 수락/픽업/배송시작/완료 4단계 워크플로우
- 라이더 배달 취소 불가 (비즈니스 규칙)

### P2-2: DeliveryRepository 쿼리 메서드
- **파일**: `DeliveryRepository.java`
- `findByRider`, `findByRiderAndStatus`, `findByStoreOrderId`, `existsByRiderAndStatusIn`, `countByRiderAndStatusIn`

### P2-3: 배달 응답 DTO
- **신규**: `GetDeliveryResponse.java`, `GetDeliveryDetailResponse.java`
- PostGIS Point → 경도/위도 변환 포함

### P2-4: 배달 상태 변경 이벤트 (SSE)
- **신규**: `DeliveryStatusChangedEvent.java`, `DeliveryEventListener.java`
- `SseEventType.DELIVERY_STATUS_CHANGED` 추가
- 고객에게 SSE 실시간 알림 + 완료/취소 시 푸시 알림 저장

### P2-5, P2-6: 레포지토리
- **신규**: `DeliveryPhotoRepository.java`, `RiderLocationRepository.java`

---

## 5. 동시 배달 지원 (최대 3건) — Redis SET 방식

### 설계 결정
- DB `countByRider...` 대신 **Redis SET** 채택
- 이유: O(1) 원자적 연산, 기존 Redis 인프라 활용, Race Condition 방지

### Redis 키 구조
| 키 패턴 | 타입 | TTL | 용도 |
|---|---|---|---|
| `RIDER:DISPATCH:{riderId}` | SET | 없음 | 라이더의 현재 활성 배달 ID 목록 |

### 수락 흐름 (DeliveryMatchComponent.acceptDelivery)
```
1. Redis 분산 락 선점 (lock:delivery:{deliveryId})
2. 배달 정보 조회 + 상태 확인 (REQUESTED만 수락 가능)
3. 라이더 정보 조회
4. SCARD RIDER:DISPATCH:{riderId} → 3건 이상이면 RIDER_MAX_DELIVERY_EXCEEDED
5. DB에 delivery.accept(rider) + rider.startDelivering()
6. SADD RIDER:DISPATCH:{riderId} deliveryId
7. Redis GEO에서 매칭 완료된 배달 제거
8. 주변 라이더들에게 매칭 알림 SSE 전송
```

### 완료 흐름 (DeliveryServiceImpl.completeDelivery)
```
1. 배달 조회 + 라이더 본인 검증
2. delivery.complete()
3. SREM RIDER:DISPATCH:{riderId} deliveryId
4. SCARD RIDER:DISPATCH:{riderId}
   - 0이면 → rider.finishDelivering() (DELIVERING → ONLINE)
   - 1 이상이면 → 라이더는 계속 DELIVERING 상태 유지
5. SSE 이벤트 발행
```

### SSE 알림 변경
- `sendToRider()`: 기존 ONLINE 라이더만 알림 → **ONLINE + DELIVERING 라이더 모두 알림**
- 동시 배달 3건 미만인 라이더도 새 배달 알림을 받을 수 있도록 변경

### 변경 파일 목록
| 파일 | 변경 |
|---|---|
| `DeliveryRedisKeys.java` | `RIDER_DISPATCH_PREFIX` 상수 추가 |
| `ErrorCode.java` | `RIDER_MAX_DELIVERY_EXCEEDED` (RIDER-005) 추가 |
| `Rider.java` | `MAX_CONCURRENT_DELIVERIES = 3` 상수 추가 |
| `DeliveryRepository.java` | `countByRiderAndStatusIn` 추가 (DB fallback용) |
| `DeliveryMatchComponent.java` | SCARD 체크 + SADD, sendToRider 확장 |
| `DeliveryServiceImpl.java` | SREM + 조건부 finishDelivering, `StringRedisTemplate` 주입 |

---

## 6. 한국어 주석 추가

모든 수정/신규 파일에 한국어 Javadoc 및 인라인 주석을 추가했습니다:
- 클래스 레벨: 역할, 설계 의도, 비즈니스 규칙 설명
- 메서드 레벨: 상태 전이 흐름, 파라미터 설명, 예외 설명
- 인라인: 비즈니스 로직 흐름 단계별 설명

---

## 7. 빌드 검증

```
BUILD SUCCESSFUL in 18s
5 actionable tasks: 4 executed, 1 up-to-date
```

Lint 경고(QueryDSL 생성 파일 unused import, Duration/Long null safety)는 기존 이슈이며 기능에 영향 없음.

---

## 8. 전체 신규/수정 파일 목록

### 신규 파일
| 파일 | 위치 |
|---|---|
| `DeliveryRedisKeys.java` | `delivery/constants/` |
| `RiderLocationServiceImpl.java` | `delivery/service/` |
| `DeliveryService.java` | `delivery/service/interfaces/` |
| `DeliveryServiceImpl.java` | `delivery/service/` |
| `PatchDeliveryCancelRequest.java` | `delivery/dto/request/` |
| `GetDeliveryResponse.java` | `delivery/dto/response/` |
| `GetDeliveryDetailResponse.java` | `delivery/dto/response/` |
| `DeliveryStatusChangedEvent.java` | `delivery/event/` |
| `DeliveryEventListener.java` | `delivery/event/` |
| `DeliveryPhotoRepository.java` | `delivery/repository/` |
| `RiderLocationRepository.java` | `delivery/repository/` |

### 수정 파일
| 파일 | 주요 변경 |
|---|---|
| `Delivery.java` | 상태 전이 검증, startDelivering() 추가 |
| `Rider.java` | @Setter 제거, 도메인 메서드 4개, MAX_CONCURRENT_DELIVERIES |
| `RiderServiceImpl.java` | BusinessException, SRP 분리, 소유권 체크 |
| `RiderController.java` | Authentication, @Valid, 배달 API 7개 |
| `DeliveryMatchComponent.java` | Redis 키 상수화, 배차 SET, DELIVERING 알림 |
| `DeliveryRepository.java` | 쿼리 메서드 5개 추가 |
| `RiderLocationService.java` | username 파라미터 추가 |
| `PostRiderLocationRequest.java` | 좌표 검증 어노테이션 |
| `PatchRiderStatusRequest.java` | @NotNull 검증 |
| `SseEventType.java` | DELIVERY_STATUS_CHANGED 추가 |
| `ErrorCode.java` | DELIVERY/RIDER/APPROVAL 에러코드 추가 |

---

## 9. 상점 대시보드 UI/UX 개선 및 버그 수정

### 9-1. 배차 완료 상태 표시 개선
- **문제**: 라이더가 배차를 수락했음에도 상점 대시보드에서 여전히 "배차 진행중"이나 "준비중"으로 표시되어 혼동 발생.
- **해결**:
  - `StoreDashboard.jsx`: `DELIVERY_MATCHED` SSE 이벤트 수신 로직 추가.
  - `DashboardTab.jsx`: 주문 상태와 관계없이 `deliveryStatus === 'ACCEPTED'`일 경우 "배차 완료" 버튼 또는 "배달원 매칭 완료" 배지 표시.
  - **효과**: "준비중" 상태에서도 배차 여부를 즉시 확인할 수 있어 상점주의 불안감 해소.

### 9-2. 픽업 완료 시 주문 목록 미갱신 해결 (State Synchronization)
- **문제**: 라이더가 픽업을 완료해도 상점의 '신규 주문 현황' 목록에서 주문이 사라지지 않음.
- **원인**:
  1. `Delivery` 엔티티의 상태만 변경되고 `StoreOrder` 엔티티의 상태는 동기화되지 않음.
  2. `DeliveryEventListener`가 트랜잭션 커밋 전에 SSE를 발송하여, 클라이언트가 갱신 전 데이터를 조회하는 Race Condition 발생.
- **해결**:
  - `DeliveryServiceImpl.java`: 픽업/배송시작/완료 시 `StoreOrder` 상태도 함께 변경(`PICKED_UP`, `DELIVERING`, `DELIVERED`)하도록 수정.
  - `StoreOrder.java`: 상태 변경 편의 메서드(`markPickedUp`, `markDelivering`, `markDelivered`) 추가 및 상태 검증 로직 완화(`ACCEPTED` -> `PICKED_UP` 허용).
  - `DeliveryEventListener.java`: `@TransactionalEventListener(phase = AFTER_COMMIT)` 적용하여 데이터 커밋 후 알림 전송 보장.
  - **효과**: 픽업 완료 시 주문 목록에서 즉시 사라지고 "완료된 주문" 탭으로 이동.

### 9-3. 인프라 안정성 확보
- **문제**: Redis 컨테이너 중지로 인해 실시간 위치 정보 및 배차 알림이 정상 작동하지 않음.
- **해결**: Redis 컨테이너 재시작 및 연결 상태 확인.

