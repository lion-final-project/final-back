# Project Index: 동네 마켓 Backend (final-back)

Generated: 2026-02-19

## Project Structure

```
final-back/
├── src/main/java/com/example/finalproject/
│   ├── FinalProjectApplication.java          # Entry point (@EnableScheduling)
│   ├── admin/                                # 관리자 (사용자 관리, 신고 처리, 브로드캐스트)
│   ├── auth/                                 # 인증/인가 (SMS, Kakao/Naver OAuth2, JWT)
│   ├── user/                                 # 사용자 관리, 주소, 역할, 상태 이력
│   ├── store/                                # 마트 등록/관리, 영업시간
│   ├── product/                              # 상품 CRUD, 재고, 카테고리
│   ├── order/                                # 주문, 장바구니, 마트주문
│   ├── checkout/                             # 주문서, 가격 계산
│   ├── delivery/                             # 배달, 라이더, 위치추적, 매칭, 완료사진
│   ├── payment/                              # 결제 (Toss Payments), 구독 결제
│   ├── subscription/                         # 정기 구독
│   ├── settlement/                           # 정산
│   ├── review/                               # 리뷰
│   ├── moderation/                           # 승인 심사, 신고
│   ├── content/                              # FAQ, 공지, 배너, 프로모션
│   ├── communication/                        # 문의, 알림 (SSE)
│   ├── coupon/                               # 쿠폰
│   └── global/                               # 공통 (보안, JWT, 예외, 응답, SSE, 스토리지)
├── src/main/resources/
│   ├── application.yml                       # 프로필: local
│   └── application-local.yml                 # 로컬 환경 설정
├── src/test/java/                            # 테스트 (2 files)
├── build.gradle                              # Gradle 빌드 설정
├── docker-compose.yml                        # PostgreSQL + MinIO + Redis
├── CLAUDE.md                                 # 프로젝트 컨텍스트
└── myMd/                                     # 프로젝트 문서 (API 명세, 엔티티, 유스케이스)
```

## Entry Point

- **Main**: `FinalProjectApplication.java` - `@SpringBootApplication` + `@EnableScheduling`
- **Profile**: `local` (기본)
- **Build**: `./gradlew bootRun`

## Core Stats

| Metric | Count |
|--------|-------|
| Domains | 17 |
| Controllers | 36 |
| Services | 65 |
| Entities | 54+ |
| Repositories | 53 |
| Error Codes | 95+ |
| Enums | 20+ |
| Events | 7 |
| Schedulers | 1 |
| Total Java Files | 472 |
| Test Files | 2 |

## Domain-Entity Map

| Domain | Entities |
|--------|----------|
| admin | *(no entities - service layer only)* |
| auth | PhoneVerification, RefreshToken |
| user | User, Role, UserRole, Address, SocialLogin, UserStatusHistory |
| store | Store, StoreCategory, StoreBusinessHour, StoreAddress(E), SettlementAccount(E), SubmittedDocumentInfo(E) |
| product | Product, ProductCategory, ProductStockHistory, StockEventType |
| order | Order, OrderLine, OrderProduct, StoreOrder, Cart, CartProduct |
| checkout | *(no entities)* |
| delivery | Delivery, Rider, RiderLocation, DeliveryPhoto |
| payment | Payment, PaymentMethod, PaymentRefund, SubscriptionPayment |
| subscription | Subscription, SubscriptionProduct, SubscriptionHistory, SubscriptionDayOfWeek, SubscriptionProductItem, SubscriptionProductDayOfWeek, SubscriptionStatusLog |
| settlement | Settlement, SettlementDetail |
| review | Review |
| moderation | Approval, ApprovalDocument, Report |
| content | Faq, Notice, Banner, Promotion, PromotionProduct |
| communication | Inquiry, Notification, NotificationBroadcast |
| coupon | Coupon |

(E) = @Embeddable

## Key Enums

| Enum | Values |
|------|--------|
| OrderStatus | 주문 상태 |
| OrderType | 주문 방식 |
| StoreOrderStatus | 마트 주문 상태 (PENDING, ACCEPTED, REJECTED, READY, ...) |
| DeliveryStatus | 배달 상태 |
| RiderApprovalStatus | 라이더 승인 상태 |
| RiderOperationStatus | 라이더 운영 상태 |
| StoreStatus | 마트 상태 (PENDING, APPROVED, REJECTED, ...) |
| StoreActiveStatus | 마트 영업 상태 |
| SubscriptionStatus | 구독 상태 |
| UserStatus | 사용자 상태 |
| PaymentMethodType | 결제 수단 |
| SseEventType | SSE 이벤트 타입 (6개) |
| ContentStatus | 컨텐츠 상태 |
| InquiryStatus | 문의 상태 |

## Configuration

| File | Purpose |
|------|---------|
| `application.yml` | 프로필 설정 (active: local) |
| `application-local.yml` | DB, Redis, MinIO, JWT, Toss, OAuth2, SMS 설정 |
| `build.gradle` | 의존성 관리 (Spring Boot 3.5.10, QueryDSL, PostGIS, etc.) |
| `docker-compose.yml` | PostgreSQL+PostGIS, MinIO, Redis 인프라 |
| `settings.gradle` | 프로젝트명: Final-Project |

## Key Dependencies

| Dependency | Version | Purpose |
|-----------|---------|---------|
| spring-boot-starter-data-jpa | 3.5.10 | JPA ORM |
| spring-boot-starter-security | 3.5.10 | 보안 |
| spring-boot-starter-oauth2-client | 3.5.10 | Kakao OAuth2 |
| spring-boot-starter-data-redis | 3.5.10 | Redis 캐시 |
| spring-boot-starter-validation | 3.5.10 | Bean Validation |
| hibernate-spatial | (managed) | PostGIS 지원 |
| querydsl-jpa (OpenFeign) | 6.10.1 | 타입 안전 쿼리 |
| spring-cloud-starter-openfeign | 2025.0.0 | Toss API 호출 |
| spring-cloud-aws-starter-s3 | 3.1.1 | MinIO/S3 스토리지 |
| jjwt-api | 0.12.5 | JWT 생성/검증 |
| nurigo sdk | 4.3.2 | CoolSMS 문자 인증 |
| testcontainers | (managed) | 테스트용 PostgreSQL |

## Documentation

| File | Topic |
|------|-------|
| `CLAUDE.md` | 프로젝트 컨텍스트, 코딩 컨벤션, 기술 가이드 |
| `myMd/동네마켓_API_명세서.md` | 전체 API 명세 |
| `myMd/엔티티_명세서.md` | 전체 엔티티 명세 |
| `myMd/도메인별 엔티티 명세/` | 도메인별 엔티티 상세 (10개 파일) |
| `myMd/api/` | 개발자별 API 명세 (7개 파일) |
| `myMd/uc/` | 유스케이스 문서 (역할별) |
| `myMd/컨벤션/` | 코딩 컨벤션 (5개 파일) |
| `myMd/sprint2/TODO.md` | Sprint 2 작업 목록 |
| `myMd/sprint3/TODO.md` | Sprint 3 작업 목록 |
| `docs/US-R03-*.md` | 라이더 실시간 위치 추적 설계 |

## Quick Start

```bash
# 1. 인프라 시작
cd final-back && docker-compose up -d

# 2. 백엔드 시작 (profile: local)
./gradlew bootRun

# 3. 프론트엔드 (별도 프로젝트)
cd ../final-front && npm run dev
```

## Current Branch

`feature/rider-api-UC-R07` - 라이더 API 개발 중
