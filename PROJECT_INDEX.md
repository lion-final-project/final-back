# Project Index: 동네 마켓 Backend (final-back)

Generated: 2026-02-21 (--deep refresh, fix/store-api-uc-s06-s08 반영)

## Project Structure

```
final-back/
├── src/main/java/com/example/finalproject/
│   ├── FinalProjectApplication.java          # Entry point (@SpringBootApplication + @EnableScheduling)
│   ├── admin/                                # 관리자 (사용자 관리, 신고, 브로드캐스트, 금융·정산)
│   │   ├── controller/
│   │   │   ├── AdminBroadcastController.java
│   │   │   ├── AdminLegacyController.java
│   │   │   ├── AdminReportController.java
│   │   │   ├── AdminUserController.java
│   │   │   └── finance/AdminFinanceController.java
│   │   ├── dto/finance/                      # 13개 금융 통계 DTO
│   │   └── service/finance/AdminFinanceService.java
│   ├── auth/                                 # 인증/인가 (SMS, Kakao/Naver OAuth2, JWT)
│   │   ├── config/
│   │   │   ├── OAuth2RedirectUriStartupLogger.java
│   │   │   └── SecurityLocalConfig.java
│   │   ├── controller/AuthController.java
│   │   ├── domain/                           # PhoneVerification, RefreshToken
│   │   ├── dto/request|response/
│   │   ├── enums/
│   │   ├── handler/                          # OAuth2LoginSuccessHandler, OAuth2LoginFailureHandler
│   │   ├── repository/
│   │   └── service/                          # AuthService, KakaoService, NaverService, SmsService, RefreshTokenStore
│   ├── checkout/                             # 주문서, 가격 계산 (entity 없음)
│   │   ├── controller/CheckoutController.java
│   │   └── service/                          # CheckoutService, PriceCalculator, DefaultPriceCalculator
│   ├── communication/                        # 문의, 알림(SSE), 이벤트 기반
│   │   ├── controller/
│   │   │   ├── AdminInquiryController.java
│   │   │   ├── InquiryController.java
│   │   │   └── NotificationController.java
│   │   ├── domain/                           # Inquiry, Notification, NotificationBroadcast
│   │   ├── event/
│   │   │   ├── InquiryAnsweredEvent.java
│   │   │   ├── UnreadCountChangedEvent.java
│   │   │   └── listener/                     # InquiryAnsweredNotificationListener, StoreOrderNotificationListener, UnreadCountSseListener
│   │   └── service/                          # InquiryService, NotificationService, DbNotificationService, OrderPaidNotificationService
│   ├── content/                              # FAQ, 공지, 배너, 프로모션
│   │   ├── controller/                       # BannerController, CustomerBannerController, FaqController, CustomerFaqController, NoticeController, CustomerNoticeController
│   │   ├── domain/                           # Faq, Notice, Banner, Promotion, PromotionProduct
│   │   ├── enums/                            # ContentStatus
│   │   └── service/                          # FaqService, NoticeService, BannerService
│   ├── coupon/                               # 쿠폰 관리
│   │   ├── controller/CouponController.java
│   │   └── domain/Coupon.java
│   ├── delivery/                             # 배달, 라이더, 위치추적, 매칭, 완료사진
│   │   ├── controller/
│   │   │   ├── CustomerDeliveryTrackingController.java
│   │   │   └── RiderController.java
│   │   ├── domain/                           # Delivery, DeliveryPhoto, Rider, RiderLocation
│   │   ├── enums/                            # DeliveryStatus, RiderApprovalStatus, RiderOperationStatus
│   │   ├── event/                            # DeliveryStatusChangedEvent, DeliveryEventListener
│   │   └── service/                          # DeliveryServiceImpl, RiderServiceImpl, RiderLocationServiceImpl, CustomerDeliveryTrackingServiceImpl, DeliveryFeeService, DeliveryMatchComponent
│   ├── global/                               # 공통 인프라
│   │   ├── component/                        # UserLoader, UserAuthCache
│   │   ├── config/                           # SecurityConfig, WebConfig, AsyncConfig, RetryConfig, RedisConfig, FeignClientConfig, JpaAuditingConfig, CookieUtil, SmsConfig
│   │   ├── domain/BaseTimeEntity.java
│   │   ├── email/                            # EmailSender, EmailType, EmailContentFactory
│   │   ├── exception/                        # BusinessException, ErrorCode (100+ codes), GlobalExceptionHandler
│   │   ├── jwt/                              # JwtTokenProvider, JwtProperties
│   │   ├── response/ApiResponse.java
│   │   ├── security/                         # JwtAuthenticationFilter, CustomUserDetails, CustomUserDetailsService
│   │   ├── sse/                              # SseService, SseEmitterRepository, SseEventType
│   │   ├── storage/                          # StorageService, S3StorageService, DocumentStorageService, S3DocumentStorageService, S3DeliveryStorageService, StorageController
│   │   │   └── enums/StoreImageType.java
│   │   └── util/                             # GeometryUtil, TemplateRenderer
│   ├── moderation/                           # 승인 심사(마트·라이더), 신고
│   │   ├── controller/admin/
│   │   │   ├── rider/AdminRiderApprovalController.java
│   │   │   └── store/AdminStoreApprovalController.java
│   │   ├── domain/                           # Approval, ApprovalDocument, Report
│   │   ├── dto/admin/
│   │   │   ├── rider/                        # AdminRiderApproval*
│   │   │   └── store/                        # AdminStoreApproval*
│   │   └── service/admin/
│   │       ├── rider/AdminRiderApprovalService.java
│   │       └── store/AdminStoreApprovalService.java
│   ├── order/                                # 주문, 장바구니, 마트주문
│   │   ├── controller/
│   │   │   ├── CartController.java
│   │   │   ├── OrderController.java
│   │   │   ├── StoreOrderCancelController.java
│   │   │   └── StoreOrderController.java
│   │   ├── domain/                           # Order, OrderLine, OrderProduct, StoreOrder, Cart, CartProduct
│   │   ├── enums/                            # OrderStatus, OrderType, StoreOrderStatus
│   │   ├── event/                            # StoreOrderAcceptedEvent, StoreOrderRefundCompletedEvent, StoreOrderRejectedEvent
│   │   ├── listener/                         # StoreOrderRefundListener, StoreOrderTtlExpirationListener
│   │   ├── mapper/StoreOrderDetailMapper.java
│   │   └── service/                          # CartService, OrderCreateService, OrderQueryService, StoreOrderService, StoreOrderStatusService, StoreOrderCancelService, StoreOrderTtlService, StoreOrderAutoReadyService, StoreOrderAutoRejectService
│   ├── payment/                              # 결제 (Toss Payments), 구독 결제
│   │   ├── client/                           # TossPaymentsClient (OpenFeign), TossFeignConfig
│   │   ├── controller/
│   │   │   ├── BillingController.java
│   │   │   └── PaymentController.java
│   │   ├── domain/                           # Payment, PaymentMethod, PaymentRefund, SubscriptionPayment
│   │   ├── enums/                            # PaymentMethodType, RefundStatus, RefundResponsibility
│   │   ├── event/
│   │   │   ├── StoreOrderCreatedEvent.java
│   │   │   └── listener/                     # StoreOrderRedisTtlListener, StoreOrderSseListener
│   │   ├── scheduler/SubscriptionBillingScheduler.java
│   │   └── service/                          # PaymentService, PaymentCommandService, PaymentCancelService, BillingService, SubscriptionBillingService, TossPaymentGateway
│   ├── product/                              # 상품 CRUD, 재고, 카테고리
│   │   ├── controller/ProductController.java
│   │   ├── domain/                           # Product, ProductCategory, ProductStockHistory
│   │   ├── enums/                            # StockEventType
│   │   └── service/ProductService.java
│   ├── review/                               # 리뷰
│   │   ├── controller/ReviewController.java
│   │   └── domain/Review.java
│   ├── settlement/                           # 정산 (마트·라이더 Spring Batch)
│   │   ├── domain/                           # Settlement, SettlementDetail, RiderSettlementDetail
│   │   ├── enums/                            # SettlementStatus, SettlementTargetType
│   │   ├── rider/
│   │   │   ├── batch/                        # RiderSettlementBatchConfig, Launcher, Processor, Reader, Writer
│   │   │   ├── dto/RiderSettlementDto.java
│   │   │   ├── repository/RiderSettlementDetailRepository.java
│   │   │   ├── scheduler/RiderSettlementScheduler.java
│   │   │   └── util/SettlementWeekUtil.java
│   │   └── store/
│   │       ├── batch/                        # StoreSettlementBatchConfig, Launcher, StoreSettlementScheduler
│   │       ├── controller/StoreSettlementController.java
│   │       ├── dto/response/                 # GetStoreSettlement*Response
│   │       ├── repository/                   # SettlementRepository, SettlementDetailRepository
│   │       └── service/                      # StoreSettlementService, StoreSettlementServiceImpl
│   ├── store/                                # 마트 등록/관리, 영업시간, 구독 스케줄
│   │   ├── controller/
│   │   │   ├── StoreController.java          # 사장용 (10 endpoints)
│   │   │   ├── StoreCustomerController.java  # 고객용 (3 endpoints)
│   │   │   ├── StoreSubscriptionController.java
│   │   │   └── StoreSubscriptionProductController.java
│   │   ├── domain/
│   │   │   ├── Store.java
│   │   │   ├── StoreBusinessHour.java
│   │   │   ├── StoreCategory.java
│   │   │   └── embedded/                     # StoreAddress, SettlementAccount, SubmittedDocumentInfo
│   │   ├── dto/
│   │   │   ├── request/                      # PostStoreRegistrationRequest, PatchStoreDescriptionRequest, PatchStoreImageRequest, PatchDeliveryAvailableRequest, PostStoreBusinessHourRequest, AcceptSubscriptionDeliveryRequest
│   │   │   └── response/                     # GetMyStoreResponse, GetStoreDetailForCustomerResponse, GetStoreRegistrationStatusResponse, PostStoreRegistrationResponse, StoreNearbyResponse, GetStoreCategoryResponse
│   │   ├── enums/                            # StoreStatus, StoreActiveStatus
│   │   ├── repository/                       # StoreRepository, StoreCategoryRepository, StoreBusinessHourRepository
│   │   │   └── custom/                       # StoreRepositoryCustom, StoreRepositoryImpl (QueryDSL)
│   │   └── service/                          # StoreService, StoreDeliveryScheduleService, StoreSubscriptionDeliveryService
│   ├── subscription/                         # 정기 구독
│   │   ├── controller/SubscriptionController.java
│   │   ├── domain/                           # Subscription, SubscriptionProduct, SubscriptionHistory, SubscriptionDayOfWeek, SubscriptionProductItem, SubscriptionProductDayOfWeek, SubscriptionStatusLog
│   │   ├── enums/                            # SubscriptionStatus
│   │   ├── scheduler/SubscriptionOrderCreationScheduler.java
│   │   └── service/                          # SubscriptionService, SubscriptionCreationService, SubscriptionProductService, SubscriptionOrderCreationService, SubscriptionStatusService, SubscriptionDeliveryCompletionService
│   └── user/                                 # 사용자 관리, 주소, 역할, 상태 이력
│       ├── controller/
│       │   ├── AddressController.java
│       │   ├── PasswordResetController.java
│       │   └── UserController.java
│       ├── domain/                           # User, Role, UserRole, Address, SocialLogin, UserStatusHistory
│       ├── enums/                            # UserStatus, RoleName
│       ├── event/PasswordResetRequestedEvent.java
│       ├── listener/PasswordResetEmailListener.java
│       └── service/                          # UserServiceImpl, AddressService, PasswordResetService, PasswordResetTokenService
├── src/main/resources/
│   ├── application.yml                       # active profile: local
│   └── application-local.yml                 # DB, Redis, MinIO, JWT, Toss, OAuth2, SMS, Mail 설정
├── src/test/java/
│   ├── settlement/rider/batch/               # RiderWeeklySettlementIntegrationTest, RiderSettlementItemProcessorTest, RiderSettlementItemWriterTest
│   ├── settlement/rider/util/                # SettlementWeekUtilTest
│   ├── settlement/store/batch/               # StoreSettlementBatchIntegrationTest (TC-1: Generate, TC-2: Complete)
│   ├── settlement/store/service/             # StoreSettlementServiceImplTest (TC x4)
│   └── (root)                               # FinalProjectApplicationTests, PasswordTest   총 8개
├── build.gradle                              # Gradle 빌드 설정
├── settings.gradle                           # projectName: Final-Project
├── docker-compose.yml                        # PostgreSQL+PostGIS + MinIO + Redis
├── CLAUDE.md                                 # 프로젝트 컨텍스트 & 코딩 컨벤션
├── PROJECT_INDEX.md                          # 본 파일
├── docs/                                     # 기술 문서
│   ├── US-R03-라이더-실시간-위치-추적.md
│   ├── delivery-improvement-plan.md
│   ├── redis-usage-guide.md
│   └── 2026-02-12-delivery-improvement-work-log.md
└── myMd/sprint3/                             # 스프린트3 문서
    ├── rider-settlement-impl.md
    ├── rider-settlement-test-report.md
    ├── TEST_PLAN.md
    └── TODO.md
```

## Entry Point

- **Main**: `FinalProjectApplication.java` — `@SpringBootApplication` + `@EnableScheduling`
- **Profile**: `local` (기본)
- **Build**: `./gradlew bootRun`

---

## Core Stats (2026-02-21 deep scan)

| Metric | Count |
|--------|-------|
| Domains | 17 |
| Java Files (src/main) | 538 |
| Controllers (@RestController) | 39 |
| Service/Component Beans | 104 |
| Entities (@Entity) | 54 |
| Repositories | 51 |
| Enums | 40 |
| Events | 8 |
| Schedulers | 4 |
| Test Files | 8 |

---

## Domain-Entity Map

| Domain | Entities | Embeddables |
|--------|----------|-------------|
| `admin` | *(없음 — 서비스 레이어 전용)* | — |
| `auth` | PhoneVerification, RefreshToken | — |
| `user` | User, Role, UserRole, Address, SocialLogin, UserStatusHistory | — |
| `store` | Store, StoreBusinessHour, StoreCategory | StoreAddress, SettlementAccount, SubmittedDocumentInfo |
| `product` | Product, ProductCategory, ProductStockHistory | — |
| `order` | Order, OrderLine, OrderProduct, StoreOrder, Cart, CartProduct | — |
| `checkout` | *(없음)* | — |
| `delivery` | Delivery, DeliveryPhoto, Rider, RiderLocation | — |
| `payment` | Payment, PaymentMethod, PaymentRefund, SubscriptionPayment | — |
| `subscription` | Subscription, SubscriptionProduct, SubscriptionHistory, SubscriptionDayOfWeek, SubscriptionProductItem, SubscriptionProductDayOfWeek, SubscriptionStatusLog | — |
| `settlement` | Settlement, SettlementDetail, RiderSettlementDetail | — |
| `review` | Review | — |
| `moderation` | Approval, ApprovalDocument, Report | — |
| `content` | Faq, Notice, Banner, Promotion, PromotionProduct | — |
| `communication` | Inquiry, Notification, NotificationBroadcast | — |
| `coupon` | Coupon | — |

---

## Controllers (39개)

### 인증/사용자 (auth, user)
| Controller | Base Path | 역할 |
|-----------|-----------|------|
| `AuthController` | `/api/auth` | 회원가입, 로그인, 토큰 갱신, SMS 인증, Kakao/Naver OAuth2 |
| `UserController` | `/api/users` | 사용자 정보 조회/수정 |
| `AddressController` | `/api/addresses` | 배송지 CRUD (최대 5개) |
| `PasswordResetController` | `/api/password-reset` | 비밀번호 재설정 (이메일 기반) |

### 관리자 (admin)
| Controller | Base Path | 역할 |
|-----------|-----------|------|
| `AdminUserController` | `/api/admin/users` | 사용자 목록/상태 관리 |
| `AdminBroadcastController` | `/api/admin/broadcast` | 전체 공지 브로드캐스트 |
| `AdminReportController` | `/api/admin/reports` | 신고 목록 조회 및 처리 |
| `AdminLegacyController` | `/api/admin/...` | 레거시 관리자 엔드포인트 |
| `AdminFinanceController` | `/api/admin/finance` | 금융·결제·정산 통계 API (13개 DTO) |

### 마트 (store)
| Controller | Base Path | 역할 |
|-----------|-----------|------|
| `StoreController` | `/api/stores` | 마트 입점/관리, 영업시간, 이미지 (사장용, 10 메서드) |
| `StoreCustomerController` | `/api/stores` | 마트 검색, 상세, 상품·구독상품 목록 (고객용) |
| `StoreSubscriptionController` | `/api/stores/subscriptions` | 구독 상품 관리 (사장용) |
| `StoreSubscriptionProductController` | `/api/stores/subscription-products` | 구독 상품 상세 관리 |

### 상품 (product)
| Controller | Base Path | 역할 |
|-----------|-----------|------|
| `ProductController` | `/api/products` | 상품 CRUD, 재고 관리 |

### 주문/결제 (order, checkout, payment)
| Controller | Base Path | 역할 |
|-----------|-----------|------|
| `CartController` | `/api/cart` | 장바구니 관리 |
| `OrderController` | `/api/orders` | 주문 생성/조회 |
| `StoreOrderController` | `/api/store-orders` | 마트 주문 접수/거절/준비완료 (사장용) |
| `StoreOrderCancelController` | `/api/store-orders` | 마트 주문 취소 처리 |
| `CheckoutController` | `/api/checkout` | 주문서 조회, 가격 계산 |
| `PaymentController` | `/api/payments` | 결제 준비/승인 (Toss Payments) |
| `BillingController` | `/api/billing` | 빌링키 발급 및 구독 결제 |
| `CouponController` | `/api/coupons` | 쿠폰 조회 |

### 배달 (delivery)
| Controller | Base Path | 역할 |
|-----------|-----------|------|
| `RiderController` | `/api/rider` | 라이더 등록, 배달 관리, 위치 업데이트 |
| `CustomerDeliveryTrackingController` | `/api/deliveries` | 고객용 배달 실시간 추적 |

### 정산 (settlement)
| Controller | Base Path | 역할 |
|-----------|-----------|------|
| `StoreSettlementController` | `/api/stores/settlement` | 마트 정산 조회 (사장용) |

### 구독 (subscription)
| Controller | Base Path | 역할 |
|-----------|-----------|------|
| `SubscriptionController` | `/api/subscriptions` | 정기 구독 신청/관리 (고객용) |

### 컨텐츠 (content)
| Controller | Base Path | 역할 |
|-----------|-----------|------|
| `FaqController` | `/api/admin/faqs` | FAQ 관리 (관리자용) |
| `CustomerFaqController` | `/api/faqs` | FAQ 조회 (고객용) |
| `NoticeController` | `/api/admin/notices` | 공지사항 관리 (관리자용) |
| `CustomerNoticeController` | `/api/notices` | 공지사항 조회 (고객용) |
| `BannerController` | `/api/admin/banners` | 배너 관리 (관리자용) |
| `CustomerBannerController` | `/api/banners` | 배너 조회 (고객용) |

### 커뮤니케이션 (communication)
| Controller | Base Path | 역할 |
|-----------|-----------|------|
| `InquiryController` | `/api/inquiries` | 1:1 문의 등록/조회 (고객용) |
| `AdminInquiryController` | `/api/admin/inquiries` | 문의 답변 (관리자용) |
| `NotificationController` | `/api/notifications` | 알림 조회, SSE 연결 |

### 승인 관리 (moderation)
| Controller | Base Path | 역할 |
|-----------|-----------|------|
| `AdminStoreApprovalController` | `/api/admin/approvals/stores` | 마트 입점 승인/거절 |
| `AdminRiderApprovalController` | `/api/admin/approvals/riders` | 라이더 승인/거절 |

### 리뷰 (review)
| Controller | Base Path | 역할 |
|-----------|-----------|------|
| `ReviewController` | `/api/reviews` | 리뷰 등록/조회/수정 |

### 파일 업로드 (global)
| Controller | Base Path | 역할 |
|-----------|-----------|------|
| `StorageController` | `/api/storage` | 파일 업로드 API |

---

## StoreController 메서드 목록 (사장용)

| 메서드 | 역할 |
|--------|------|
| `getMyStoreRegistration` | 입점 신청 상태 조회 |
| `createStoreApplication` | 마트 입점 신청 |
| `cancelStoreRegistration` | 입점 신청 취소 |
| `getStoreCategories` | 마트 카테고리 목록 조회 |
| `getMyStore` | 내 마트 정보 조회 |
| `getMyStoreBusinessHours` | 내 마트 영업시간 조회 |
| `updateMyStoreBusinessHours` | 영업시간 수정 |
| `updateMyDeliveryAvailable` | 배달 가능 여부 변경 |
| `updateMyStoreImage` | 마트 이미지 수정 |
| `updateMyStoreDescription` | 마트 소개 수정 |

## StoreCustomerController 메서드 목록 (고객용)

| 메서드 | 역할 |
|--------|------|
| `getStoreDetail` | 마트 상세 조회 |
| `listProducts` | 마트 상품 목록 조회 |
| `listSubscriptionProducts` | 마트 구독 상품 목록 조회 |

---

## 이벤트 시스템 (Spring ApplicationEvent) — 8개

| Event | Publisher | Listener | 용도 |
|-------|-----------|---------|------|
| `StoreOrderCreatedEvent` | `PaymentCommandService` | `StoreOrderSseListener`, `StoreOrderNotificationListener`, `StoreOrderRedisTtlListener` | 주문 생성 시 마트에 SSE + DB 알림 + TTL 설정 |
| `StoreOrderAcceptedEvent` | `StoreOrderStatusService` | *(listener)* | 주문 접수 시 고객 알림 |
| `StoreOrderRejectedEvent` | `StoreOrderStatusService` | *(listener)* | 주문 거절 시 고객 알림 |
| `StoreOrderRefundCompletedEvent` | `StoreOrderCancelService` | `StoreOrderRefundListener` | 환불 완료 알림 |
| `DeliveryStatusChangedEvent` | `DeliveryServiceImpl` | `DeliveryEventListener` | 배달 상태 변경 알림 |
| `InquiryAnsweredEvent` | `InquiryService` | `InquiryAnsweredNotificationListener` | 문의 답변 시 고객 알림 |
| `UnreadCountChangedEvent` | `NotificationService` | `UnreadCountSseListener` | 읽지 않은 알림 수 SSE 전송 |
| `PasswordResetRequestedEvent` | `PasswordResetService` | `PasswordResetEmailListener` | 비밀번호 재설정 이메일 발송 |

---

## SSE 이벤트 타입 (SseEventType)

| 타입 | 이벤트명 | 용도 |
|------|---------|------|
| `CONNECTED` | `connected` | SSE 연결 성공 |
| `UNREAD_COUNT` | `unread-count` | 읽지 않은 알림 수 변경 |
| `STORE_ORDER_CREATED` | `store-order-created` | 마트에 새 주문 도착 |
| `NEW_DELIVERY` | `new-delivery` | 라이더에게 새 배달 요청 |
| `NEARBY_DELIVERIES` | `nearby-deliveries` | 근처 배달 목록 업데이트 |
| `DELIVERY_MATCHED` | `delivery-matched` | 배달 매칭 완료 |

---

## 스케줄러 (4개)

| Scheduler | 패키지 | 트리거 | 역할 |
|-----------|--------|--------|------|
| `SubscriptionOrderCreationScheduler` | `subscription/scheduler/` | `@Scheduled` | 정기 구독 자동 주문 생성 |
| `SubscriptionBillingScheduler` | `payment/scheduler/` | `@Scheduled` | 구독 자동 결제 처리 (SubscriptionRecurringProcessor 위임) |
| `StoreSettlementScheduler` | `settlement/store/batch/` | `@Scheduled` + Spring Batch | 마트 월별 정산 배치 |
| `RiderSettlementScheduler` | `settlement/rider/scheduler/` | `@Scheduled` + Spring Batch | 라이더 주간 정산 배치 |

---

## 에러 코드 체계 (ErrorCode.java)

| 도메인 | 코드 범위 | 주요 에러 |
|--------|----------|----------|
| COMMON | COMMON-000 ~ 006 | 서버오류, 입력오류, 인가오류 |
| STORE | STORE-001 ~ 015 | 중복사업자번호, 마트미발견, 영업시간오류, 배달불가 |
| AUTH | AUTH-001 ~ 017 | 인증필요, 중복이메일/전화, SMS인증, 토큰오류, 비밀번호재설정 |
| STORAGE | STORAGE-001 ~ 002 | 파일업로드오류, 확장자오류 |
| FAQ | FAQ-001 | FAQ미발견 |
| NOTICE | NOTICE-001 | 공지미발견 |
| BANNER | BANNER-001 | 배너미발견 |
| PRODUCT | PRODUCT-001 ~ 016 | 상품미발견, 재고부족, 할인율오류, 운영중수정금지 |
| SUBSCRIPTION | SUBSCRIPTION-001 ~ 010 | 구독상품미발견, 구독자존재, 구독상태오류 |
| CART | CART-001 ~ 002 | 장바구니/상품미발견 |
| ADDRESS | ADDRESS-001 ~ 005 | 주소미발견, 5개초과, 중복주소 |
| DELIVERY | DELIVERY-001 ~ 008 | 배달비계산실패, 배달불가지역, 배달미발견, 상태전이오류 |
| RIDER | RIDER-001 ~ 005 | 라이더상태잠김, 중복등록, 위치미발견, 최대배달수초과 |
| APPROVAL | APPROVAL-001 ~ 003 | 신청미발견, 비대기상태, 미소유 |
| INQUIRY | INQUIRY-001 ~ 002 | 문의미발견, 이미답변 |
| ADMIN | ADMIN-001 | 관리자전용 |
| NOTIFICATION | NOTIFICATION-001 ~ 003 | 알림미발견, 소유권오류, 이메일오류 |
| PAYMENT | PAYMENT-001 ~ 008 | 결제미발견, 중복처리, 결제실패, 취소금액오류 |
| ORDER | ORDER-001 ~ 009 | 주문미발견, 포인트오류, 쿠폰오류, 취소불가 |
| STORE-ORDER | STORE-ORDER-001 ~ 009 | 마트주문미발견, 소속오류, 상태오류, 이미처리됨 |
| REVIEW | REVIEW-001 ~ 005 | 리뷰불가, 중복리뷰, 기간만료, 답글중복 |

**총 에러 코드: 100+개**
**파일**: `global/exception/custom/ErrorCode.java`

---

## 주요 Enum 목록 (40개)

| Enum | 위치 | 값 |
|------|------|----|
| `OrderStatus` | order/enums | 주문 상태 전체 |
| `OrderType` | order/enums | 주문 방식 (일반/구독) |
| `StoreOrderStatus` | order/enums | PENDING, ACCEPTED, REJECTED, READY, ... |
| `DeliveryStatus` | delivery/enums | REQUESTED→ACCEPTED→PICKED_UP→DELIVERING→DELIVERED/CANCELLED |
| `RiderApprovalStatus` | delivery/enums | 라이더 승인 상태 |
| `RiderOperationStatus` | delivery/enums | 라이더 운영 상태 (온라인/오프라인) |
| `StoreStatus` | store/enums | PENDING, APPROVED, REJECTED, INACTIVE, ... |
| `StoreActiveStatus` | store/enums | 마트 영업 상태 (OPEN/CLOSED) |
| `StoreImageType` | global/storage/enums | 마트 이미지 타입 |
| `SubscriptionStatus` | subscription/enums | ACTIVE, PAUSED, CANCELLED, ... |
| `UserStatus` | user/enums | ACTIVE, SUSPENDED, WITHDRAWN, ... |
| `RoleName` | user/enums | CUSTOMER, STORE, RIDER, ADMIN |
| `PaymentMethodType` | payment/enums | 카드, 계좌이체 등 |
| `RefundStatus` | payment/enums | REQUESTED, APPROVED, REJECTED |
| `RefundResponsibility` | payment/enums | CUSTOMER, STORE, PLATFORM, RIDER |
| `SettlementStatus` | settlement/enums | PENDING, COMPLETED, FAILED |
| `SettlementTargetType` | settlement/enums | STORE, RIDER |
| `SseEventType` | global/sse | CONNECTED, UNREAD_COUNT, STORE_ORDER_CREATED, NEW_DELIVERY, NEARBY_DELIVERIES, DELIVERY_MATCHED |
| `ContentStatus` | content/enums | 컨텐츠 상태 |
| `InquiryStatus` | communication/enums | PENDING, ANSWERED |
| `StockEventType` | product/enums | 재고 이벤트 유형 |

---

## Settlement 패키지 상세 구조

```
settlement/
├── domain/
│   ├── Settlement.java           # 정산 헤더 (targetType, pgFee, bankName, bankAccount, settledAt)
│   ├── SettlementDetail.java     # 마트 정산 상세 (settlement FK, amount, fee, netAmount)
│   └── RiderSettlementDetail.java  # 라이더 정산 상세
├── enums/
│   ├── SettlementStatus.java     # PENDING, COMPLETED, FAILED
│   └── SettlementTargetType.java # STORE, RIDER
├── rider/
│   ├── batch/
│   │   ├── RiderSettlementBatchConfig.java
│   │   ├── RiderSettlementBatchLauncher.java
│   │   ├── RiderSettlementItemProcessor.java
│   │   ├── RiderSettlementItemReader.java
│   │   └── RiderSettlementItemWriter.java
│   ├── dto/RiderSettlementDto.java
│   ├── repository/RiderSettlementDetailRepository.java
│   ├── scheduler/RiderSettlementScheduler.java
│   └── util/SettlementWeekUtil.java
└── store/
    ├── batch/
    │   ├── StoreSettlementBatchConfig.java
    │   ├── StoreSettlementBatchLauncher.java
    │   └── StoreSettlementScheduler.java
    ├── controller/StoreSettlementController.java
    ├── dto/response/             # GetStoreSettlement*Response
    ├── repository/               # SettlementRepository, SettlementDetailRepository
    └── service/                  # StoreSettlementService (interface), StoreSettlementServiceImpl
                                  #   - getSettlements(), getSettlementDetail()
                                  #   - generateMonthlySettlements(), completePendingSettlements()
                                  #   - processStoreSettlement(), createEmptySettlement()
```

---

## Admin Finance API 상세 (`/api/admin/finance`)

| 엔드포인트 | DTO | 설명 |
|-----------|-----|------|
| GET overview | `AdminOverviewStatsResponse` | 전체 결제·정산 통계 |
| GET payments | `AdminPaymentListResponse` | 거래 목록 |
| GET payments/{id} | `AdminTransactionDetailResponse` | 거래 상세 |
| GET payments/trend | `AdminTransactionTrendResponse` | 거래 트렌드 |
| GET store-settlements | `AdminStoreSettlementListResponse` | 마트 정산 목록 |
| GET store-settlements/summary | `AdminStoreSettlementSummaryResponse` | 마트 정산 요약 |
| GET store-settlements/trend | `AdminStoreSettlementTrendResponse` | 마트 정산 트렌드 |
| POST store-settlements/execute | `AdminStoreSettlementExecuteRequest/Response` | 마트 정산 수동 실행 |
| GET rider-settlements | `AdminRiderSettlementListResponse` | 라이더 정산 목록 |
| GET rider-settlements/summary | `AdminRiderSettlementSummaryResponse` | 라이더 정산 요약 |
| GET rider-settlements/trend | `AdminRiderSettlementTrendResponse` | 라이더 정산 트렌드 |

---

## 외부 API 클라이언트

| Client | 방식 | 역할 |
|--------|------|------|
| `TossPaymentsClient` | OpenFeign | Toss Payments 결제 승인/취소 API |
| Kakao OAuth2 | Spring Security OAuth2 | 소셜 로그인 |
| Naver OAuth2 | Spring Security OAuth2 | 소셜 로그인 |
| CoolSMS (nurigo 4.3.2) | SDK | SMS 인증 코드 발송 |

---

## Configuration 파일

| 파일 | 용도 |
|------|------|
| `application.yml` | active profile: local |
| `application-local.yml` | DB(PostgreSQL), Redis, MinIO, JWT(Access 30분, Refresh 14일), Toss(test key), OAuth2(Kakao/Naver), SMS(CoolSMS), Mail 설정 |
| `build.gradle` | Spring Boot 3.5.10, Gradle 8.14, QueryDSL 6.10.1, PostGIS, JJWT, etc. |
| `docker-compose.yml` | PostgreSQL+PostGIS, MinIO+Setup, Redis 컨테이너 |
| `settings.gradle` | projectName: Final-Project |

---

## 주요 의존성

| 의존성 | 버전 | 용도 |
|--------|------|------|
| Spring Boot | 3.5.10 | 전체 프레임워크 |
| Spring Cloud | 2025.0.0 | OpenFeign, AWS S3 |
| Spring Cloud AWS S3 | 3.1.1 | MinIO/S3 (`S3Template`) |
| QueryDSL (OpenFeign fork) | 6.10.1 | 타입 안전 JPQL |
| hibernate-spatial | managed | PostGIS 지원 |
| postgresql + postgis | 16 + 3.4 | 공간 데이터베이스 |
| JJWT | 0.12.5 | JWT 생성/검증 |
| nurigo (CoolSMS) | 4.3.2 | SMS 인증 |
| Testcontainers | managed | PostgreSQL 통합 테스트 |
| Spring Batch | managed | 정산 배치 처리 |
| Spring Mail | managed | 비밀번호 재설정 이메일 |

---

## PostGIS 핵심 패턴

```java
// Entity: GEOGRAPHY(POINT,4326) — 미터 단위 거리
@Column(columnDefinition = "GEOGRAPHY(POINT,4326)")
private Point location;

// Point 생성 (GeometryUtil)
GeometryUtil.createPoint(longitude, latitude);  // JTS: X=경도, Y=위도

// QueryDSL 거리 계산 (ST_Distance)
Expressions.numberTemplate(Double.class, "ST_Distance({0}, {1})", loc1, loc2);

// 반경 검색 (ST_DWithin, 미터 단위)
Expressions.booleanTemplate("ST_DWithin({0}, {1}, {2}) is true", loc1, loc2, 3000.0);
```

- SRID 항상 `4326`, Hibernate 6에서 `ST_DWithin` 뒤에 `is true` 필수

---

## Redis 키 패턴

```
SMS:AUTH:{phoneNumber}           # 인증 코드 (TTL: 3분)
SMS:RESEND:{phoneNumber}         # 재발송 카운트 (TTL: 10분)
SMS:VERIFIED:{token}             # 인증 완료 토큰 (TTL: 10분)
SMS:PHONE_VERIFIED:{phoneNumber} # 번호 인증 상태 (TTL: 5분)
```

**패턴**: `{DOMAIN}:{PURPOSE}:{ID}` — TTL 필수, 1회 소비 토큰은 사용 후 즉시 `delete()`

---

## MinIO (S3) 설정

- 버킷: `market-bucket` (public read, anonymous download)
- Path-style access: `true` (MinIO 필수)
- 허용 확장자: `jpg`, `jpeg`, `png`, `pdf`
- 파일명: `UUID + "_" + 원본파일명`
- 최대 크기: 파일 5MB, 요청 10MB

---

## 로컬 개발 빠른 시작

```bash
# 1. 인프라 시작 (PostgreSQL+PostGIS 5432, MinIO 9000/9001, Redis 6379)
cd final-back && docker-compose up -d

# 2. 백엔드 시작 (profile: local, ddl-auto: update)
./gradlew bootRun

# 3. 프론트엔드 (별도 프로젝트, port: 5173 → proxy → 8080)
cd ../final-front && npm run dev
```

**로컬 전용 컴포넌트:**
- `SecurityLocalConfig` — CORS/CSRF 완화
- `LocalDataInitializer` — 시드 데이터 자동 생성
- `LocalTestUserArgumentResolver` — `@AuthenticationPrincipal` 대체 테스트 사용자 주입

---

## 현재 브랜치 상태

```
Current:  fix/store-api-uc-s06-s08   # 마트 API 유스케이스 S06~S08 수정 작업
Main dev: dev
```

---

## 변경 이력 (2026-02-21 기준)

| PR/브랜치 | 내용 |
|---------|------|
| `fix/store-api-uc-s06-s08` | **마트 정산 서비스 구현 강화** (UC-S06~S08): `StoreOrder.markSettled(Settlement)` 도메인 메서드 + `settlement` FK 필드; `SettlementDetail` 엔티티 리팩터링 (amount/fee/netAmount); `SettlementDetailRepository.findBySettlementId/deleteBySettlementId` 추가; `StoreOrderRepository` 쿼리 메서드 다수 추가 (`findAllBySettlementIdWithOrder` 포함); `StoreSettlementServiceImpl` 전면 구현 (`generateMonthlySettlements`, `completePendingSettlements`, `processStoreSettlement`, `createEmptySettlement`); `application-test.yml`에 `toss.payments.base-url` 추가(RiderWeeklySettlementIntegrationTest 컨텍스트 로드 픽스); 테스트 2개 추가(`StoreSettlementBatchIntegrationTest`, `StoreSettlementServiceImplTest`) |
| PR #80 | `AdminFinanceController` + `AdminFinanceService` + 13개 DTO 신설; 관리자 금융·결제·정산 통계 API |
| PR #79 | OAuth2 JWT 리팩토링 (`OAuth2LoginSuccessHandler`); Naver OAuth2 지원 추가 |
| PR #78 | `RiderSettlementScheduler` + Spring Batch 신설 (`settlement/rider/`); `RiderSettlementDetail` 엔티티; 테스트 4개 추가 |
| 이전 | `StoreOrderAutoReadyService`, `StoreOrderAutoRejectService` (TTL 기반 자동처리) |
| 이전 | `PasswordResetController`, `PasswordResetService`, `PasswordResetTokenService` (이메일 기반) |
| 이전 | `StoreSettlementController` → `settlement/store/controller/` 이동 |
| 이전 | `DeliveryStatus`: `PICKED_UP` 상태 추가 |
| 이전 | `Delivery` 엔티티: `riderEarning`, `distanceKm` 필드 추가 |
| 이전 | `RefundStatus`: `REQUESTED, APPROVED, REJECTED` 변경 |
| 이전 | `RefundResponsibility`: `RIDER` 귀책 추가 |
| 이전 | `SettlementTargetType`: `STORE, RIDER` 변경 |
| 이전 | `Settlement` 엔티티: `targetType`, `pgFee`, `bankName`, `bankAccount`, `settledAt` 추가 |
| 이전 | `GlobalExceptionHandler`: `AsyncConfig`, `RetryConfig`, `UserAuthCache`, `TemplateRenderer`, Email 클래스 추가 |
| 이전 | `PasswordResetRequestedEvent` + `PasswordResetEmailListener` 추가 (총 8개 이벤트) |

---

## 문서 링크

| 파일 | 주제 |
|------|------|
| `CLAUDE.md` | 전체 프로젝트 컨텍스트, 코딩 컨벤션, 기술 가이드 |
| `docs/US-R03-라이더-실시간-위치-추적.md` | 라이더 실시간 위치 추적 설계 |
| `docs/delivery-improvement-plan.md` | 배달 개선 계획 |
| `docs/redis-usage-guide.md` | Redis 사용 가이드 |
| `docs/2026-02-12-delivery-improvement-work-log.md` | 배달 개선 작업 로그 |
| `myMd/sprint3/rider-settlement-impl.md` | 라이더 정산 구현 기록 (아키텍처, 핵심 구현, 트러블슈팅) |
| `myMd/sprint3/rider-settlement-test-report.md` | 라이더 정산 테스트 보고서 (19TC 전원 통과) |
| `myMd/sprint3/TEST_PLAN.md` | 스프린트3 통합 테스트 계획 (TC-1~3, 테스트 데이터 전략) |
| `myMd/sprint3/TODO.md` | 스프린트3 TODO — 라이더 정산 Task 1~7 체크리스트 + DDL |

---

## 라이더 정산 시스템 상세 (myMd/sprint3 요약)

### 배치 처리 흐름

```
[매주 목요일 02:00 Asia/Seoul]
RiderSettlementScheduler.runRiderWeeklySettlement()
  └─ JobLauncher.run(riderWeeklySettlementJob, {targetWeekStart, requestedAt})
       └─ riderWeeklySettlementStep (chunk=100)
            ├─ RiderSettlementItemReader   : 직전주 DELIVERED & isSettled=false 라이더 100명씩 조회
            │    └─ 4-Query 전략 (Java-side 집계, QueryDSL 6.x API 제약)
            ├─ RiderSettlementItemProcessor: 동일 기간 중복 정산 skip (null 반환)
            └─ RiderSettlementItemWriter   :
                 ① Settlement 마스터 저장 (targetType=RIDER)
                 ② RiderSettlementDetail 저장 (배달 건별 수익 스냅샷)
                 ③ deliveries 벌크 UPDATE: is_settled=true, settlement_id=? (JdbcTemplate)
                 ④ payment_refunds 벌크 UPDATE: is_settled=true (RIDER귀책 APPROVED)
```

### 정산 금액 공식

```
netAmount = MAX(0, SUM(riderEarning) - SUM(refundAmount [RIDER 귀책·APPROVED]))
```

### JobParameter

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `targetWeekStart` | String (yyyy-MM-dd) | 직전 주 월요일. `@StepScope` Reader가 기간 계산에 사용 |
| `requestedAt` | Long (ms) | 동일 주차 재실행 시 새 JobInstance 생성용 |

### 신규 DDL (라이더 정산)

```sql
-- deliveries 테이블
ALTER TABLE deliveries ADD COLUMN is_settled BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE deliveries ADD COLUMN settlement_id BIGINT;
ALTER TABLE deliveries ADD CONSTRAINT fk_deliveries_settlement FOREIGN KEY (settlement_id) REFERENCES settlements(id);

-- payment_refunds 테이블
ALTER TABLE payment_refunds ADD COLUMN is_settled BOOLEAN NOT NULL DEFAULT FALSE;

-- 신규 테이블
CREATE TABLE rider_settlement_details (
  id BIGSERIAL PRIMARY KEY, settlement_id BIGINT NOT NULL, delivery_id BIGINT NOT NULL,
  rider_earning INT NOT NULL, refund_amount INT NOT NULL, net_amount INT NOT NULL,
  created_at TIMESTAMP, updated_at TIMESTAMP,
  CONSTRAINT fk_rider_settlement_details_settlement FOREIGN KEY (settlement_id) REFERENCES settlements(id),
  CONSTRAINT fk_rider_settlement_details_delivery FOREIGN KEY (delivery_id) REFERENCES deliveries(id)
);
```

### 테스트 결과 (라이더 정산: 19TC, 마트 정산: 6TC)

**라이더 정산 (PR #78)**

| 파일 | 종류 | TC수 | 결과 |
|------|------|:---:|:---:|
| `SettlementWeekUtilTest` | 순수 단위 | 7 | ✅ PASS |
| `RiderSettlementItemProcessorTest` | Mockito 단위 | 3 | ✅ PASS |
| `RiderSettlementItemWriterTest` | Mockito 단위 | 6 | ✅ PASS |
| `RiderWeeklySettlementIntegrationTest` | Spring Boot 통합 (★`application-test.yml` toss 설정 필수) | 3 | ✅ PASS |

**마트 정산 (`fix/store-api-uc-s06-s08`)**

| 파일 | 종류 | TC수 | 주요 케이스 |
|------|------|:---:|------------|
| `StoreSettlementBatchIntegrationTest` | Spring Boot 통합 | 2 | TC-1 Generate(PENDING 생성), TC-2 Complete(PENDING→COMPLETED) |
| `StoreSettlementServiceImplTest` | Mockito 단위 | 4 | getSettlements, getSettlementDetail, generateMonthlySettlements(수수료·환불 계산), completePendingSettlements |

### QueryDSL 6.x openfeign fork 주의 사항

`NumberPath<Integer>.sum()` 및 multi-column Tuple API가 미지원 → **Java-side 집계**로 대응:
- `selectFrom(entity).fetch()` 후 `stream().collect(groupingBy + summingInt)` 사용
- 다중 컬럼 select 결과는 `Object`로 추론됨 (Tuple API 사용 불가)

---

## 마트 정산 시스템 상세 (fix/store-api-uc-s06-s08)

### StoreOrder ↔ Settlement 연결

`StoreOrder` 엔티티에 `settlement` FK 필드 추가:
```java
// StoreOrder.java
@ManyToOne(fetch = FetchType.LAZY)
private Settlement settlement;

public void markSettled(Settlement settlement) {
    this.settlement = settlement;
}
```

### SettlementDetail 구조 (리팩터링)

```java
// SettlementDetail.java — settlement_details 테이블
SettlementDetail {
    id           Long
    settlement   Settlement  (FK → settlements)
    amount       Integer     // 총 매출액 (gross)
    fee          Integer     // 플랫폼 + PG 수수료 합산
    netAmount    Integer     // 실 정산액 = amount - fee
}
```

### 배치 처리 흐름 (StoreSettlementBatchConfig)

```
[매월 1일 02:00 Asia/Seoul (설정값)]
StoreSettlementScheduler → storeSettlementGenerateJob
  └─ storeSettlementStep (chunk 단위)
       ├─ Reader  : target month에 DELIVERED 완료 & is_settled=false 마트주문 조회
       ├─ Processor: 수수료(platformFee 5%, pgFee 3.3%) 계산
       └─ Writer  :
            ① Settlement 마스터 저장 (targetType=STORE, status=PENDING)
            ② SettlementDetail 저장 (amount/fee/netAmount 스냅샷)
            ③ StoreOrder.markSettled(settlement) 호출

storeSettlementCompleteJob (별도 실행)
  └─ PENDING → COMPLETED 일괄 전환 + settledAt 설정
```

### 정산 금액 공식 (마트)

```
platformFee = totalSales * 0.05   (5%)
pgFee       = totalSales * 0.033  (3.3%, 소수점 버림)
settlementAmount = totalSales - platformFee - pgFee
```

### JobParameter (마트 정산)

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `targetYearMonth` | String (yyyy-MM) | 정산 대상 연월 |
| `run.id` | Long (ms) | 동일 월 재실행 시 새 JobInstance 생성용 |

### application-test.yml 픽스 (RiderWeeklySettlementIntegrationTest)

**문제**: `@ActiveProfiles({ "local", "test" })` 조합에서 OpenFeign이 `${toss.payments.base-url}` 미해석 → `http://${toss.payments.base-url} is malformed`
**원인**: `application.yml`의 `spring.profiles.active: local` 이 `@ActiveProfiles` 명시 목록 적용 시 우선순위에서 밀려 `application-local.yml` 로드 타이밍 이슈
**해결**: `application-test.yml`에 `toss.payments.base-url` 직접 명시 → `test` 프로파일은 항상 로드되므로 안전

```yaml
# application-test.yml (추가 내용)
toss:
  payments:
    base-url: https://api.tosspayments.com
    secret-key: test_sk_dummy_for_testing
```
