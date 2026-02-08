# 동네마켓_DDL_v4.1

# 동네마켓 DDL v4.1

> DBMS: PostgreSQL 16 + PostGIS 3.4
최종 수정일: 2026-01-30
설명: 동네마켓 플랫폼 데이터베이스 DDL 스크립트. FK 의존성 순서로 정렬되어 순차 실행 가능합니다.
> 

---

[docker-compose](https://www.notion.so/docker-compose-2f8e62c239c080d9a1c8e9de9128f769?pvs=21)

## 요약

| 항목 | 수량 |
| --- | --- |
| 테이블 | 39 |
| ENUM 타입 | 29 |
| 트리거 | 37 |
| GIST 인덱스 (PostGIS) | 4 |
| GIN 인덱스 (pg_trgm) | 1 |

---

## 1. 사전 설정

### 1.1 Extensions

```sql
CREATE EXTENSION IF NOT EXISTS postgis;   -- PostGIS 3.4 공간 데이터
CREATE EXTENSION IF NOT EXISTS pg_trgm;   -- 트라이그램 유사 검색
```

### 1.2 ENUM 타입 정의 (29개)

```sql
-- ========================================
-- 사용자 모듈
-- ========================================
CREATE TYPE user_status         AS ENUM ('ACTIVE','INACTIVE','SUSPENDED','PENDING');
CREATE TYPE social_provider     AS ENUM ('KAKAO','NAVER','GOOGLE','APPLE');

-- ========================================
-- 마트 모듈
-- ========================================
CREATE TYPE store_status        AS ENUM ('PENDING','APPROVED','REJECTED','SUSPENDED');
CREATE TYPE store_active_status AS ENUM ('ACTIVE','INACTIVE','CLOSED');

-- ========================================
-- 주문 모듈
-- ========================================
CREATE TYPE order_type          AS ENUM ('REGULAR','SUBSCRIPTION');
CREATE TYPE order_status        AS ENUM ('PENDING','PAID','PARTIAL_CANCELLED','CANCELLED','COMPLETED');
CREATE TYPE store_order_status  AS ENUM ('PENDING','ACCEPTED','READY','PICKED_UP','DELIVERING','DELIVERED','CANCELLED','REJECTED');

-- ========================================
-- 결제 모듈
-- ========================================
CREATE TYPE payment_method_type AS ENUM ('CARD','KAKAO_PAY','NAVER_PAY','TOSS_PAY');
CREATE TYPE payment_status      AS ENUM ('PENDING','COMPLETED','FAILED','CANCELLED','PARTIAL_REFUNDED','REFUNDED');

-- ========================================
-- 배달 모듈
-- ========================================
CREATE TYPE rider_operation_status AS ENUM ('OFFLINE','ONLINE');
CREATE TYPE rider_approval_status  AS ENUM ('PENDING','APPROVED','REJECTED','SUSPENDED');
CREATE TYPE delivery_status     AS ENUM ('REQUESTED','ACCEPTED','PICKED_UP','DELIVERING','DELIVERED','CANCELLED');

-- ========================================
-- 구독 모듈
-- ========================================
CREATE TYPE subscription_product_status AS ENUM ('ACTIVE','INACTIVE');
CREATE TYPE subscription_status AS ENUM ('ACTIVE','PAUSED','CANCELLATION_PENDING','CANCELLED');
CREATE TYPE sub_history_status  AS ENUM ('SCHEDULED','ORDERED','SKIPPED','COMPLETED');

-- ========================================
-- 정산 모듈
-- ========================================
CREATE TYPE settlement_target_type AS ENUM ('STORE','RIDER');
CREATE TYPE settlement_status   AS ENUM ('PENDING','COMPLETED','FAILED');

-- ========================================
-- 승인 모듈
--NOTE: applicant_type은 approval_documents에서, approval_applicant_type은 approvals에서 사용.
-- 값 차이: STORE vs MART. 향후 통합 권장.
-- ========================================
CREATE TYPE applicant_type      AS ENUM ('STORE','RIDER');
CREATE TYPE approval_applicant_type AS ENUM ('MART','RIDER');
CREATE TYPE approval_status     AS ENUM ('PENDING','APPROVED','REJECTED','HELD');
CREATE TYPE document_type       AS ENUM ('BUSINESS_LICENSE','BUSINESS_REPORT','BANK_PASSBOOK','ID_CARD');

-- ========================================
-- 기타 모듈
-- ========================================
CREATE TYPE notification_ref_type  AS ENUM ('RIDER','STORE','CUSTOMER','ORDER','DELIVERY','PROMOTION');
CREATE TYPE broadcast_ref_type     AS ENUM ('RIDER','STORE','CUSTOMER','ALL');
CREATE TYPE report_target_type     AS ENUM ('STORE','RIDER','CUSTOMER');
CREATE TYPE report_status          AS ENUM ('PENDING','RESOLVED');
CREATE TYPE inquiry_category       AS ENUM ('ORDER_PAYMENT','CANCELLATION_REFUND','DELIVERY','SERVICE','OTHER');
CREATE TYPE inquiry_status         AS ENUM ('PENDING','ANSWERED');
CREATE TYPE content_status         AS ENUM ('ACTIVE','INACTIVE');
CREATE TYPE promotion_status       AS ENUM ('ACTIVE','ENDED');
```

### 1.3 공통 트리거 함수

```sql
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

---

## 2. 테이블 생성 (CREATE TABLE)

> FK 의존성 순서로 정렬. 순차 실행 시 참조 무결성 보장.
> 

---

### 2.1 사용자 모듈

### (1) roles

```sql
CREATE TABLE roles (
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role_name VARCHAR(30) NOT NULL
);

COMMENT ON TABLE  roles IS '사용자 역할 유형 관리';
COMMENT ON COLUMN roles.role_name IS '역할명 (CUSTOMER, STORE_OWNER, RIDER, ADMIN)';
```

### (2) users

```sql
CREATE TABLE users (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email             VARCHAR(255) NOT NULL,
    password          VARCHAR(255) NOT NULL,
    name              VARCHAR(50)  NOT NULL,
    phone             VARCHAR(20)  NOT NULL,
    status            user_status  NOT NULL DEFAULT 'ACTIVE',
    terms_agreed      BOOLEAN      NOT NULL DEFAULT FALSE,
    privacy_agreed    BOOLEAN      NOT NULL DEFAULT FALSE,
    terms_agreed_at   TIMESTAMPTZ  NULL,
    privacy_agreed_at TIMESTAMPTZ  NULL,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at        TIMESTAMPTZ  NULL
);

COMMENT ON TABLE  users IS '시스템 전체 사용자 계정 관리';
COMMENT ON COLUMN users.email IS '이메일 주소 (로그인 ID)';
COMMENT ON COLUMN users.password IS 'BCrypt 해시 비밀번호';
COMMENT ON COLUMN users.status IS '계정 상태';
COMMENT ON COLUMN users.deleted_at IS '탈퇴일시 (Soft Delete)';
```

### (3) user_roles

```sql
CREATE TABLE user_roles (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id    BIGINT      NOT NULL REFERENCES roles (id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE user_roles IS '사용자-역할 N:M 매핑';
```

### (4) addresses

```sql
CREATE TABLE addresses (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id       BIGINT                NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    contact       VARCHAR(20)           NOT NULL,
    address_name  VARCHAR(50)           NOT NULL,
    postal_code   VARCHAR(10)           NOT NULL,
    address_line1 VARCHAR(255)          NOT NULL,
    address_line2 VARCHAR(255)          NULL,
    location      GEOGRAPHY(POINT,4326) NULL,
    is_default    BOOLEAN               NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ           NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ           NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  addresses IS '고객 배송 주소 관리 (최대 5개)';
COMMENT ON COLUMN addresses.location IS 'PostGIS 좌표 — ST_MakePoint(lng, lat)::GEOGRAPHY';
```

### (5) social_logins

```sql
CREATE TABLE social_logins (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id          BIGINT          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    provider         social_provider NOT NULL,
    provider_user_id VARCHAR(255)    NOT NULL,
    connected_at     TIMESTAMPTZ     NOT NULL,
    created_at       TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMPTZ     NULL
);

COMMENT ON TABLE social_logins IS '소셜 로그인 연동 정보';
```

---

### 2.2 마트 모듈

### (6) stores

```sql
CREATE TABLE stores (
    id                            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    owner_id                      BIGINT                NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    store_name                    VARCHAR(100)          NOT NULL,
    business_number               VARCHAR(12)           NOT NULL,
    representative_name           VARCHAR(50)           NOT NULL,
    representative_phone          VARCHAR(20)           NOT NULL,
    phone                         VARCHAR(20)           NULL,
    telecom_sales_report_number   VARCHAR(50)           NULL,
    description                   TEXT                  NULL,
    address_line1                 VARCHAR(255)          NOT NULL,
    address_line2                 VARCHAR(255)          NULL,
    postal_code                   VARCHAR(10)           NOT NULL,
    location                      GEOGRAPHY(POINT,4326) NULL,
    settlement_bank_name          VARCHAR(50)           NOT NULL,
    settlement_bank_account       VARCHAR(255)          NOT NULL,
    settlement_account_holder     VARCHAR(50)           NOT NULL,
    store_image                   VARCHAR(500)          NULL,
    review_count                  INTEGER               NOT NULL DEFAULT 0,
    status                        store_status          NOT NULL DEFAULT 'PENDING',
    is_delivery_available         BOOLEAN               NOT NULL DEFAULT FALSE,
    is_active                     store_active_status   NOT NULL DEFAULT 'INACTIVE',
    commission_rate               NUMERIC(5,2)          NOT NULL DEFAULT 5.00,
    deleted_at                    TIMESTAMPTZ           NULL,
    created_at                    TIMESTAMPTZ           NOT NULL DEFAULT NOW(),
    updated_at                    TIMESTAMPTZ           NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  stores IS '로컬 마트 정보 및 운영 상태';
COMMENT ON COLUMN stores.location IS 'PostGIS 좌표 — ST_DWithin 주변 마트 검색';
COMMENT ON COLUMN stores.settlement_bank_account IS '정산 계좌번호 (암호화 저장)';
```

### (7) store_business_hours

```sql
CREATE TABLE store_business_hours (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    store_id    BIGINT      NOT NULL REFERENCES stores (id) ON DELETE CASCADE,
    day_of_week SMALLINT    NOT NULL CHECK (day_of_week BETWEEN 0 AND 6),
    open_time   TIME        NULL,
    close_time  TIME        NULL,
    is_closed   BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_business_hours_time CHECK (close_time > open_time OR is_closed = TRUE)
);

COMMENT ON TABLE store_business_hours IS '마트 요일별 영업시간';
```

---

### 2.3 상품 모듈

### (8) categories

```sql
CREATE TABLE categories (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category_name VARCHAR(50)  NOT NULL,
    icon_url      VARCHAR(500) NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE categories IS '상품 분류 카테고리';
```

### (9) products

```sql
CREATE TABLE products (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    store_id          BIGINT        NOT NULL REFERENCES stores (id) ON DELETE RESTRICT,
    category_id       BIGINT        NOT NULL REFERENCES categories (id) ON DELETE RESTRICT,
    product_name      VARCHAR(200)  NOT NULL,
    description       TEXT          NULL,
    price             NUMERIC(12,2) NOT NULL CHECK (price > 0),
    sale_price        NUMERIC(12,2) NULL,
    discount_rate     NUMERIC(5,2)  NULL,
    stock             INTEGER       NOT NULL DEFAULT 0,
    unit              VARCHAR(30)   NULL,
    origin            VARCHAR(100)  NULL,
    is_active         BOOLEAN       NOT NULL DEFAULT TRUE,
    order_count       INTEGER       NOT NULL DEFAULT 0,
    product_image_url VARCHAR(500)  NULL,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  products IS '마트별 판매 상품';
COMMENT ON COLUMN products.price IS '정가 (> 0)';
COMMENT ON COLUMN products.sale_price IS '할인가';
```

---

### 2.4 주문 모듈

### (10) orders

```sql
CREATE TABLE orders (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_number        VARCHAR(30)           NOT NULL,
    user_id             BIGINT                NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    order_type          order_type            NOT NULL DEFAULT 'REGULAR',
    status              order_status          NOT NULL DEFAULT 'PENDING',
    total_product_price NUMERIC(12,2)         NOT NULL,
    total_delivery_fee  NUMERIC(12,2)         NOT NULL DEFAULT 0,
    final_price         NUMERIC(12,2)         NOT NULL,
    delivery_address    VARCHAR(255)          NOT NULL,
    delivery_location   GEOGRAPHY(POINT,4326) NULL,
    delivery_request    VARCHAR(255)          NULL,
    ordered_at          TIMESTAMPTZ           NOT NULL,
    created_at          TIMESTAMPTZ           NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ           NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  orders IS '고객 주문 정보';
COMMENT ON COLUMN orders.delivery_location IS '배송지 PostGIS 좌표';
```

### (11) store_orders

```sql
CREATE TABLE store_orders (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id            BIGINT             NOT NULL REFERENCES orders (id) ON DELETE RESTRICT,
    store_id            BIGINT             NOT NULL REFERENCES stores (id) ON DELETE RESTRICT,
    order_type          order_type         NOT NULL DEFAULT 'REGULAR',
    prep_time           INTEGER            NULL,
    status              store_order_status NOT NULL DEFAULT 'PENDING',
    store_product_price NUMERIC(12,2)      NOT NULL,
    delivery_fee        NUMERIC(12,2)      NOT NULL DEFAULT 0,
    final_price         NUMERIC(12,2)      NOT NULL,
    accepted_at         TIMESTAMPTZ        NULL,
    prepared_at         TIMESTAMPTZ        NULL,
    picked_up_at        TIMESTAMPTZ        NULL,
    delivered_at        TIMESTAMPTZ        NULL,
    cancelled_at        TIMESTAMPTZ        NULL,
    cancel_reason       VARCHAR(500)       NULL,
    created_at          TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ        NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE store_orders IS '마트별 분리 주문 (멀티 마트 주문 지원)';
```

### (12) order_products

```sql
CREATE TABLE order_products (
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    store_order_id        BIGINT        NOT NULL REFERENCES store_orders (id) ON DELETE RESTRICT,
    product_id            BIGINT        NOT NULL REFERENCES products (id) ON DELETE RESTRICT,
    product_name_snapshot VARCHAR(200)  NOT NULL,
    price_snapshot        NUMERIC(12,2) NOT NULL,
    quantity              INTEGER       NOT NULL CHECK (quantity >= 1),
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE order_products IS '주문 상품 (가격 스냅샷)';
```

### (13) carts

```sql
CREATE TABLE carts (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE carts IS '고객 장바구니 (1인 1개)';
```

### (14) cart_products

```sql
CREATE TABLE cart_products (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cart_id    BIGINT      NOT NULL REFERENCES carts (id) ON DELETE CASCADE,
    product_id BIGINT      NOT NULL REFERENCES products (id) ON DELETE RESTRICT,
    store_id   BIGINT      NOT NULL REFERENCES stores (id) ON DELETE RESTRICT,
    quantity   INTEGER     NOT NULL CHECK (quantity >= 1),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE cart_products IS '장바구니 상품 목록';
```

---

### 2.5 결제 모듈

### (15) payments

```sql
CREATE TABLE payments (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id          BIGINT              NOT NULL REFERENCES orders (id) ON DELETE RESTRICT,
    payment_method    payment_method_type NOT NULL,
    payment_status    payment_status      NOT NULL DEFAULT 'PENDING',
    amount            NUMERIC(12,2)       NOT NULL,
    pg_provider       VARCHAR(50)         NULL,
    pg_transaction_id VARCHAR(100)        NULL,
    card_company      VARCHAR(50)         NULL,
    card_number_masked VARCHAR(30)        NULL,
    receipt_url       VARCHAR(500)        NULL,
    paid_at           TIMESTAMPTZ         NULL,
    created_at        TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ         NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE payments IS '주문 결제 정보 (1:1)';
```

### (16) payment_refunds

```sql
CREATE TABLE payment_refunds (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    payment_id     BIGINT        NOT NULL REFERENCES payments (id) ON DELETE RESTRICT,
    store_order_id BIGINT        NOT NULL REFERENCES store_orders (id) ON DELETE RESTRICT,
    refund_amount  NUMERIC(12,2) NOT NULL,
    refund_reason  VARCHAR(500)  NULL,
    refunded_at    TIMESTAMPTZ   NULL,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE payment_refunds IS '결제 환불 내역 (부분환불 지원)';
```

### (17) payment_methods

```sql
CREATE TABLE payment_methods (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id            BIGINT              NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    method_type        payment_method_type NOT NULL,
    billing_key        VARCHAR(255)        NOT NULL,
    card_company       VARCHAR(50)         NULL,
    card_number_masked VARCHAR(30)         NULL,
    is_default         BOOLEAN             NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ         NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  payment_methods IS '저장된 결제수단 (빌링키)';
COMMENT ON COLUMN payment_methods.billing_key IS 'PG사 빌링키 (암호화 저장)';
```

---

### 2.6 배달 모듈

### (18) riders

```sql
CREATE TABLE riders (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id           BIGINT                 NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    id_card_verified  BOOLEAN                NOT NULL DEFAULT FALSE,
    id_card_image     VARCHAR(500)           NULL,
    operation_status  rider_operation_status  NOT NULL DEFAULT 'OFFLINE',
    bank_name         VARCHAR(50)            NULL,
    bank_account      VARCHAR(255)           NULL,
    account_holder    VARCHAR(50)            NULL,
    status            rider_approval_status  NOT NULL DEFAULT 'PENDING',
    created_at        TIMESTAMPTZ            NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ            NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  riders IS '배달원 정보 및 운행 상태';
COMMENT ON COLUMN riders.status IS '배달원 승인 상태 (rider_approval_status)';
```

### (19) deliveries

```sql
CREATE TABLE deliveries (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    store_order_id    BIGINT          NOT NULL REFERENCES store_orders (id) ON DELETE RESTRICT,
    rider_id          BIGINT          NOT NULL REFERENCES riders (id) ON DELETE RESTRICT,
    status            delivery_status NOT NULL DEFAULT 'REQUESTED',
    delivery_fee      NUMERIC(12,2)   NOT NULL,
    rider_earning     NUMERIC(12,2)   NOT NULL,
    distance_km       NUMERIC(5,2)    NULL,
    estimated_minutes INTEGER         NULL,
    requested_at      TIMESTAMPTZ     NOT NULL,
    accepted_at       TIMESTAMPTZ     NULL,
    picked_up_at      TIMESTAMPTZ     NULL,
    delivered_at      TIMESTAMPTZ     NULL,
    cancelled_at      TIMESTAMPTZ     NULL,
    cancel_reason     VARCHAR(500)    NULL,
    created_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE deliveries IS '배달 정보 (store_orders 1:1)';
```

### (20) rider_locations

```sql
CREATE TABLE rider_locations (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    rider_id    BIGINT                NOT NULL REFERENCES riders (id) ON DELETE CASCADE,
    delivery_id BIGINT                NULL     REFERENCES deliveries (id) ON DELETE SET NULL,
    location    GEOGRAPHY(POINT,4326) NOT NULL,
    accuracy    NUMERIC(6,2)          NULL,
    speed       NUMERIC(5,2)          NULL,
    heading     NUMERIC(5,2)          NULL,
    recorded_at TIMESTAMPTZ           NOT NULL,
    is_current  BOOLEAN               NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ           NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  rider_locations IS '배달원 실시간 위치 (3~5초 간격)';
COMMENT ON COLUMN rider_locations.location IS 'PostGIS — ST_DWithin 가까운 배달원 검색';
```

### (21) delivery_photos

```sql
CREATE TABLE delivery_photos (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    delivery_id BIGINT       NOT NULL REFERENCES deliveries (id) ON DELETE RESTRICT,
    photo_url   VARCHAR(500) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    expires_at  TIMESTAMPTZ  NOT NULL,
    deleted_at  TIMESTAMPTZ  NULL,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE delivery_photos IS '배송 완료 증빙 사진 (24시간 후 자동 삭제)';
```

---

### 2.7 구독 모듈

### (22) subscription_products

```sql
CREATE TABLE subscription_products (
    id                         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    store_id                   BIGINT                      NOT NULL REFERENCES stores (id) ON DELETE RESTRICT,
    subscription_product_name  VARCHAR(200)                NOT NULL,
    description                TEXT                        NULL,
    price                      NUMERIC(12,2)               NOT NULL CHECK (price > 0),
    total_delivery_count       INTEGER                     NOT NULL,
    delivery_count_of_week     INTEGER                     NOT NULL,
    status                     subscription_product_status NOT NULL DEFAULT 'ACTIVE',
    subscription_url           VARCHAR(500)                NULL,
    created_at                 TIMESTAMPTZ                 NOT NULL DEFAULT NOW(),
    updated_at                 TIMESTAMPTZ                 NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE subscription_products IS '마트 구독 상품 정의';
```

### (23) subscriptions

```sql
CREATE TABLE subscriptions (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id                 BIGINT              NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    store_id                BIGINT              NOT NULL REFERENCES stores (id) ON DELETE RESTRICT,
    subscription_product_id BIGINT              NOT NULL REFERENCES subscription_products (id) ON DELETE RESTRICT,
    delivery_time_slot      VARCHAR(30)         NULL,
    address_id              BIGINT              NOT NULL REFERENCES addresses (id) ON DELETE RESTRICT,
    payment_method_id       BIGINT              NOT NULL REFERENCES payment_methods (id) ON DELETE RESTRICT,
    status                  subscription_status NOT NULL DEFAULT 'ACTIVE',
    next_payment_date       DATE                NULL,
    total_amount            NUMERIC(12,2)       NOT NULL,
    cycle_count             INTEGER             NOT NULL DEFAULT 1,
    started_at              TIMESTAMPTZ         NOT NULL,
    paused_at               TIMESTAMPTZ         NULL,
    cancelled_at            TIMESTAMPTZ         NULL,
    cancel_reason           VARCHAR(500)        NULL,
    created_at              TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ         NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE subscriptions IS '고객 정기 배송 구독';
```

### (24) subscription_day_of_week

```sql
CREATE TABLE subscription_day_of_week (
    subscription_id BIGINT      NOT NULL REFERENCES subscriptions (id) ON DELETE CASCADE,
    day_of_week     SMALLINT    NOT NULL CHECK (day_of_week BETWEEN 0 AND 6),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (subscription_id, day_of_week)
);

COMMENT ON TABLE subscription_day_of_week IS '구독 배송 요일 (복합 PK)';
```

### (25) subscription_product_items

```sql
CREATE TABLE subscription_product_items (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    subscription_product_id BIGINT      NOT NULL REFERENCES subscription_products (id) ON DELETE CASCADE,
    product_id              BIGINT      NOT NULL REFERENCES products (id) ON DELETE RESTRICT,
    quantity                INTEGER     NOT NULL CHECK (quantity >= 1),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE subscription_product_items IS '구독 상품 구성 품목';
```

### (26) subscription_history

```sql
CREATE TABLE subscription_history (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    subscription_id BIGINT             NOT NULL REFERENCES subscriptions (id) ON DELETE RESTRICT,
    cycle_count     INTEGER            NOT NULL,
    scheduled_date  DATE               NOT NULL,
    status          sub_history_status NOT NULL DEFAULT 'SCHEDULED',
    store_order_id  BIGINT             NULL     REFERENCES store_orders (id) ON DELETE SET NULL,
    created_at      TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ        NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE subscription_history IS '구독 배송 스케줄 및 이력';
```

---

### 2.8 리뷰 모듈

### (27) reviews

```sql
CREATE TABLE reviews (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    store_order_id BIGINT      NOT NULL REFERENCES store_orders (id) ON DELETE RESTRICT,
    user_id        BIGINT      NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    rating         SMALLINT    NOT NULL CHECK (rating BETWEEN 1 AND 5),
    content        VARCHAR(100) NULL,
    is_visible     BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE reviews IS '마트 주문 리뷰 (store_orders 1:1)';
```

---

### 2.9 정산 모듈

### (28) settlements

```sql
CREATE TABLE settlements (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    target_type             settlement_target_type NOT NULL,
    target_id               BIGINT                 NOT NULL,
    settlement_period_start DATE                   NOT NULL,
    settlement_period_end   DATE                   NOT NULL,
    total_sales             NUMERIC(12,2)          NOT NULL,
    platform_fee            NUMERIC(12,2)          NOT NULL,
    settlement_amount       NUMERIC(12,2)          NOT NULL,
    status                  settlement_status      NOT NULL DEFAULT 'PENDING',
    bank_name               VARCHAR(50)            NULL,
    bank_account            VARCHAR(255)           NULL,
    settled_at              TIMESTAMPTZ            NULL,
    created_at              TIMESTAMPTZ            NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ            NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  settlements IS '마트/배달원 정산 (다형성 FK)';
COMMENT ON COLUMN settlements.target_id IS '마트 ID 또는 배달원 user ID';
```

### (29) settlement_details

```sql
CREATE TABLE settlement_details (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    settlement_id  BIGINT        NOT NULL REFERENCES settlements (id) ON DELETE CASCADE,
    store_order_id BIGINT        NOT NULL REFERENCES store_orders (id) ON DELETE RESTRICT,
    amount         NUMERIC(12,2) NOT NULL,
    fee            NUMERIC(12,2) NOT NULL,
    net_amount     NUMERIC(12,2) NOT NULL,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE settlement_details IS '정산 상세 내역 (개별 주문 건)';
```

---

### 2.10 승인 관리 모듈

### (30) approvals

```sql
CREATE TABLE approvals (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id        BIGINT                   NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    applicant_type approval_applicant_type   NOT NULL,
    status         approval_status          NOT NULL DEFAULT 'PENDING',
    reason         TEXT                     NULL,
    approved_by    BIGINT                   NULL     REFERENCES users (id) ON DELETE SET NULL,
    approved_at    TIMESTAMPTZ              NULL,
    created_at     TIMESTAMPTZ              NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ              NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE approvals IS '마트/배달원 승인 요청 및 처리 이력';
```

### (31) approval_documents

```sql
CREATE TABLE approval_documents (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    applicant_type applicant_type NOT NULL,
    approval_id    BIGINT         NOT NULL REFERENCES approvals (id) ON DELETE CASCADE,
    document_type  document_type  NOT NULL,
    document_url   VARCHAR(500)   NOT NULL,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE approval_documents IS '승인 서류 (사업자등록증, 신분증 등)';
```

---

### 2.11 기타 모듈

### (32) notifications

```sql
CREATE TABLE notifications (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id        BIGINT                 NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title          VARCHAR(200)           NOT NULL,
    content        TEXT                   NULL,
    reference_type notification_ref_type  NULL,
    sent_at        TIMESTAMPTZ            NULL,
    is_read        BOOLEAN                NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ            NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ            NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE notifications IS '사용자별 알림';
```

### (33) notification_broadcasts

```sql
CREATE TABLE notification_broadcasts (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title          VARCHAR(200)       NOT NULL,
    content        TEXT               NULL,
    reference_type broadcast_ref_type NOT NULL DEFAULT 'ALL',
    created_at     TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ        NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE notification_broadcasts IS '전체 알림 (대량 발송)';
```

### (34) reports

```sql
CREATE TABLE reports (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    store_order_id BIGINT             NULL     REFERENCES store_orders (id) ON DELETE SET NULL,
    reporter_id    BIGINT             NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    reporter_type  report_target_type NOT NULL,
    target_id      BIGINT             NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    target_type    report_target_type NOT NULL,
    reason_detail  TEXT               NOT NULL,
    status         report_status      NOT NULL DEFAULT 'PENDING',
    report_result  TEXT               NULL,
    resolved_at    TIMESTAMPTZ        NULL,
    created_at     TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ        NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE reports IS '사용자 신고 (마트/배달원/고객 상호)';
```

### (35) inquiries

```sql
CREATE TABLE inquiries (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT           NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    category    inquiry_category NOT NULL,
    title       VARCHAR(200)     NOT NULL,
    content     TEXT             NOT NULL,
    file_url    VARCHAR(500)     NULL,
    status      inquiry_status   NOT NULL DEFAULT 'PENDING',
    answer      TEXT             NULL,
    answered_at TIMESTAMPTZ      NULL,
    answered_by BIGINT           NULL     REFERENCES users (id) ON DELETE SET NULL,
    created_at  TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ      NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE inquiries IS '1:1 문의';
```

### (36) notices

```sql
CREATE TABLE notices (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title      VARCHAR(200)   NOT NULL,
    content    TEXT           NOT NULL,
    author_id  BIGINT         NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    is_pinned  BOOLEAN        NOT NULL DEFAULT FALSE,
    status     content_status NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE notices IS '플랫폼 공지사항';
```

### (37) banners

```sql
CREATE TABLE banners (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title            VARCHAR(200)   NOT NULL,
    content          TEXT           NULL,
    image_url        VARCHAR(500)   NOT NULL,
    link_url         VARCHAR(500)   NULL,
    background_color VARCHAR(50)    NULL,
    display_order    INTEGER        NOT NULL DEFAULT 0,
    status           content_status NOT NULL DEFAULT 'ACTIVE',
    started_at       TIMESTAMPTZ    NOT NULL,
    ended_at         TIMESTAMPTZ    NOT NULL,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE banners IS '메인 페이지 배너/광고';
```

### (38) promotions

```sql
CREATE TABLE promotions (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title            VARCHAR(200)     NOT NULL,
    description      TEXT             NULL,
    banner_image_url VARCHAR(500)     NULL,
    start_date       DATE             NOT NULL,
    end_date         DATE             NOT NULL,
    status           promotion_status NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ      NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE promotions IS '프로모션/기획전';
```

### (39) promotion_products

```sql
CREATE TABLE promotion_products (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    promotion_id BIGINT      NOT NULL REFERENCES promotions (id) ON DELETE CASCADE,
    product_id   BIGINT      NOT NULL REFERENCES products (id) ON DELETE RESTRICT,
    sort_order   INTEGER     NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE promotion_products IS '프로모션 포함 상품 매핑';
```

---

## 3. 제약조건 (UNIQUE / CHECK 추가)

> 인라인으로 선언되지 않은 UNIQUE 제약조건을 별도 추가합니다.
> 

```sql
-- 2.1 사용자 모듈
ALTER TABLE roles         ADD CONSTRAINT uq_roles_role_name          UNIQUE (role_name);
ALTER TABLE users         ADD CONSTRAINT uq_users_email              UNIQUE (email);
ALTER TABLE users         ADD CONSTRAINT uq_users_phone              UNIQUE (phone);
ALTER TABLE user_roles    ADD CONSTRAINT uq_user_roles_user_role     UNIQUE (user_id, role_id);
ALTER TABLE addresses     ADD CONSTRAINT uq_addresses_user_address   UNIQUE (user_id, address_line1, address_line2);
ALTER TABLE addresses     ADD CONSTRAINT uq_addresses_user_name      UNIQUE (user_id, address_name);
ALTER TABLE social_logins ADD CONSTRAINT uq_social_provider_user     UNIQUE (provider, provider_user_id);

-- 2.2 마트 모듈
ALTER TABLE stores              ADD CONSTRAINT uq_stores_owner_id            UNIQUE (owner_id);
ALTER TABLE stores              ADD CONSTRAINT uq_stores_business_number     UNIQUE (business_number);
ALTER TABLE store_business_hours ADD CONSTRAINT uq_business_hours_store_day  UNIQUE (store_id, day_of_week);

-- 2.3 상품 모듈
ALTER TABLE categories ADD CONSTRAINT uq_categories_name UNIQUE (category_name);

-- 2.4 주문 모듈
ALTER TABLE orders ADD CONSTRAINT uq_orders_number UNIQUE (order_number);
ALTER TABLE carts  ADD CONSTRAINT uq_cart_user     UNIQUE (user_id);
ALTER TABLE cart_products ADD CONSTRAINT uq_cart_products_cart_product UNIQUE (cart_id, product_id);

-- 2.5 결제 모듈
ALTER TABLE payments        ADD CONSTRAINT uq_payments_order          UNIQUE (order_id);
ALTER TABLE payment_methods ADD CONSTRAINT uq_payment_methods_billing UNIQUE (user_id, billing_key);

-- 2.6 배달 모듈
ALTER TABLE riders     ADD CONSTRAINT uq_riders_user              UNIQUE (user_id);
ALTER TABLE deliveries ADD CONSTRAINT uq_deliveries_store_order   UNIQUE (store_order_id);

-- 2.7 구독 모듈
ALTER TABLE subscription_product_items ADD CONSTRAINT uq_sub_items_product      UNIQUE (subscription_product_id, product_id);

-- 2.8 리뷰 모듈
ALTER TABLE reviews ADD CONSTRAINT uq_reviews_store_order UNIQUE (store_order_id);

-- 2.10 승인 모듈
ALTER TABLE approval_documents ADD CONSTRAINT uq_approval_docs_type UNIQUE (approval_id, document_type);

-- 2.11 기타 모듈
ALTER TABLE promotion_products ADD CONSTRAINT uq_promo_products_unique UNIQUE (promotion_id, product_id);
```

---

## 4. 인덱스 생성 (CREATE INDEX)

### 4.1 사용자 모듈

```sql
-- users
CREATE INDEX idx_users_status     ON users (status);
CREATE INDEX idx_users_deleted_at ON users (deleted_at);

-- user_roles
CREATE INDEX idx_user_roles_user ON user_roles (user_id);
CREATE INDEX idx_user_roles_role ON user_roles (role_id);

-- addresses
CREATE INDEX idx_addresses_user_id      ON addresses (user_id);
CREATE INDEX idx_addresses_user_default ON addresses (user_id, is_default);
CREATE INDEX idx_addresses_location     ON addresses USING GIST (location);

-- social_logins
CREATE INDEX idx_social_logins_user_id ON social_logins (user_id);
```

### 4.2 마트 모듈

```sql
-- stores
CREATE INDEX idx_stores_status_active ON stores (status, is_active);
CREATE INDEX idx_stores_location      ON stores USING GIST (location);
```

### 4.3 상품 모듈

```sql
-- products
CREATE INDEX idx_products_store_category ON products (store_id, category_id);
CREATE INDEX idx_products_store_active   ON products (store_id, is_active);
CREATE INDEX idx_products_name_trgm      ON products USING GIN (product_name gin_trgm_ops);
```

### 4.4 주문 모듈

```sql
-- orders
CREATE INDEX idx_orders_user_status       ON orders (user_id, status);
CREATE INDEX idx_orders_ordered_at        ON orders (ordered_at);
CREATE INDEX idx_orders_delivery_location ON orders USING GIST (delivery_location);

-- store_orders
CREATE INDEX idx_store_order_order_id      ON store_orders (order_id);
CREATE INDEX idx_store_order_store_status  ON store_orders (store_id, status);

-- order_products
CREATE INDEX idx_order_products_store_order ON order_products (store_order_id);
CREATE INDEX idx_order_products_product     ON order_products (product_id);

-- cart_products
CREATE INDEX idx_cart_products_cart  ON cart_products (cart_id);
CREATE INDEX idx_cart_products_store ON cart_products (store_id);
```

### 4.5 결제 모듈

```sql
-- payments
CREATE INDEX idx_payments_status ON payments (payment_status);
CREATE INDEX idx_payments_pg_txn ON payments (pg_transaction_id);

-- payment_refunds
CREATE INDEX idx_refunds_payment     ON payment_refunds (payment_id);
CREATE INDEX idx_refunds_store_order ON payment_refunds (store_order_id);

-- payment_methods
CREATE INDEX idx_payment_methods_user    ON payment_methods (user_id);
CREATE INDEX idx_payment_methods_default ON payment_methods (user_id, is_default);
```

### 4.6 배달 모듈

```sql
-- riders
CREATE INDEX idx_riders_status    ON riders (status);
CREATE INDEX idx_riders_operation ON riders (operation_status);

-- deliveries
CREATE INDEX idx_deliveries_rider_status ON deliveries (rider_id, status);
CREATE INDEX idx_deliveries_status       ON deliveries (status);

-- rider_locations
CREATE INDEX idx_rider_loc_rider_time       ON rider_locations (rider_id, recorded_at);
CREATE INDEX idx_rider_loc_delivery_current ON rider_locations (delivery_id, is_current);
CREATE INDEX idx_rider_loc_recorded         ON rider_locations (recorded_at);
CREATE INDEX idx_rider_loc_location         ON rider_locations USING GIST (location);

-- delivery_photos
CREATE INDEX idx_delivery_photos_delivery ON delivery_photos (delivery_id);
CREATE INDEX idx_delivery_photos_expires  ON delivery_photos (expires_at, deleted_at);
```

### 4.7 구독 모듈

```sql
-- subscription_products
CREATE INDEX idx_sub_products_store_status ON subscription_products (store_id, status);

-- subscriptions
CREATE INDEX idx_subscriptions_user_status     ON subscriptions (user_id, status);
CREATE INDEX idx_subscriptions_next_payment    ON subscriptions (next_payment_date);
CREATE INDEX idx_subscriptions_store           ON subscriptions (store_id);
CREATE INDEX idx_subscriptions_sub_product     ON subscriptions (subscription_product_id);
CREATE INDEX idx_subscriptions_address         ON subscriptions (address_id);
CREATE INDEX idx_subscriptions_payment_method  ON subscriptions (payment_method_id);

-- subscription_product_items
CREATE INDEX idx_sub_items_sub_product ON subscription_product_items (subscription_product_id);

-- subscription_history
CREATE INDEX idx_sub_history_sub_date    ON subscription_history (subscription_id, scheduled_date);
CREATE INDEX idx_sub_history_store_order ON subscription_history (store_order_id);
```

### 4.8 리뷰 모듈

```sql
CREATE INDEX idx_reviews_user ON reviews (user_id);
```

### 4.9 정산 모듈

```sql
-- settlements
CREATE INDEX idx_settlements_target_period ON settlements (target_type, target_id, settlement_period_start);
CREATE INDEX idx_settlements_status        ON settlements (status);

-- settlement_details
CREATE INDEX idx_settlement_details_settlement  ON settlement_details (settlement_id);
CREATE INDEX idx_settlement_details_store_order ON settlement_details (store_order_id);
```

### 4.10 승인 모듈

```sql
-- approvals
CREATE INDEX idx_approvals_user         ON approvals (user_id);
CREATE INDEX idx_approvals_status       ON approvals (status);
CREATE INDEX idx_approvals_type_created ON approvals (applicant_type, created_at);
CREATE INDEX idx_approvals_approved_by  ON approvals (approved_by);

-- approval_documents
CREATE INDEX idx_approval_docs_approval ON approval_documents (approval_id);
```

### 4.11 기타 모듈

```sql
-- notifications
CREATE INDEX idx_notifications_user_read ON notifications (user_id, is_read, sent_at);

-- notification_broadcasts
CREATE INDEX idx_broadcasts_type_created ON notification_broadcasts (reference_type, created_at);

-- reports
CREATE INDEX idx_reports_reporter    ON reports (reporter_id);
CREATE INDEX idx_reports_target      ON reports (target_type, target_id);
CREATE INDEX idx_reports_status      ON reports (status);
CREATE INDEX idx_reports_store_order ON reports (store_order_id);

-- inquiries
CREATE INDEX idx_inquiries_user_status  ON inquiries (user_id, status);
CREATE INDEX idx_inquiries_status       ON inquiries (status);
CREATE INDEX idx_inquiries_answered_by  ON inquiries (answered_by);

-- notices
CREATE INDEX idx_notices_status_pinned ON notices (status, is_pinned, created_at);
CREATE INDEX idx_notices_author        ON notices (author_id);

-- banners
CREATE INDEX idx_banners_status_order ON banners (status, display_order);
CREATE INDEX idx_banners_period       ON banners (started_at, ended_at);

-- promotions
CREATE INDEX idx_promotions_status_period ON promotions (status, start_date, end_date);

-- promotion_products
CREATE INDEX idx_promo_products_promotion ON promotion_products (promotion_id);
```

---

## 5. 트리거 생성 (37개)

```sql
-- 2.1 사용자 모듈
CREATE TRIGGER trg_user_roles_updated_at     BEFORE UPDATE ON user_roles     FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_users_updated_at          BEFORE UPDATE ON users          FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_addresses_updated_at      BEFORE UPDATE ON addresses      FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_social_logins_updated_at  BEFORE UPDATE ON social_logins  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 2.2 마트 모듈
CREATE TRIGGER trg_stores_updated_at              BEFORE UPDATE ON stores              FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_store_business_hours_updated_at BEFORE UPDATE ON store_business_hours FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 2.3 상품 모듈
CREATE TRIGGER trg_categories_updated_at BEFORE UPDATE ON categories FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_products_updated_at   BEFORE UPDATE ON products   FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 2.4 주문 모듈
CREATE TRIGGER trg_orders_updated_at         BEFORE UPDATE ON orders         FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_store_orders_updated_at   BEFORE UPDATE ON store_orders   FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_order_products_updated_at BEFORE UPDATE ON order_products FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_carts_updated_at          BEFORE UPDATE ON carts          FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_cart_products_updated_at  BEFORE UPDATE ON cart_products  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 2.5 결제 모듈
CREATE TRIGGER trg_payments_updated_at        BEFORE UPDATE ON payments        FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_payment_refunds_updated_at BEFORE UPDATE ON payment_refunds FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_payment_methods_updated_at BEFORE UPDATE ON payment_methods FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 2.6 배달 모듈
CREATE TRIGGER trg_riders_updated_at          BEFORE UPDATE ON riders          FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_deliveries_updated_at      BEFORE UPDATE ON deliveries      FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_delivery_photos_updated_at BEFORE UPDATE ON delivery_photos FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 2.7 구독 모듈
CREATE TRIGGER trg_subscription_products_updated_at      BEFORE UPDATE ON subscription_products      FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_subscriptions_updated_at              BEFORE UPDATE ON subscriptions              FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_subscription_day_of_week_updated_at   BEFORE UPDATE ON subscription_day_of_week   FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_subscription_product_items_updated_at BEFORE UPDATE ON subscription_product_items FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_subscription_history_updated_at       BEFORE UPDATE ON subscription_history       FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 2.8 리뷰 모듈
CREATE TRIGGER trg_reviews_updated_at BEFORE UPDATE ON reviews FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 2.9 정산 모듈
CREATE TRIGGER trg_settlements_updated_at        BEFORE UPDATE ON settlements        FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_settlement_details_updated_at BEFORE UPDATE ON settlement_details FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 2.10 승인 모듈
CREATE TRIGGER trg_approvals_updated_at          BEFORE UPDATE ON approvals          FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_approval_documents_updated_at BEFORE UPDATE ON approval_documents FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 2.11 기타 모듈
CREATE TRIGGER trg_notifications_updated_at            BEFORE UPDATE ON notifications            FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_notification_broadcasts_updated_at  BEFORE UPDATE ON notification_broadcasts  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_reports_updated_at                  BEFORE UPDATE ON reports                  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_inquiries_updated_at                BEFORE UPDATE ON inquiries                FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_notices_updated_at                  BEFORE UPDATE ON notices                  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_banners_updated_at                  BEFORE UPDATE ON banners                  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_promotions_updated_at               BEFORE UPDATE ON promotions               FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_promotion_products_updated_at       BEFORE UPDATE ON promotion_products       FOR EACH ROW EXECUTE FUNCTION set_updated_at();
```

> 참고: rider_locations와 roles는 updated_at 컬럼이 없어 트리거 제외. 총 37개.
> 

---

## 6. 초기 데이터 (Seed)

```sql
INSERT INTO roles (role_name) VALUES
    ('CUSTOMER'),
    ('STORE_OWNER'),
    ('RIDER'),
    ('ADMIN');
```

---

## 변경 이력

| 버전 | 일자 | 작성자 | 변경 내용 |
| --- | --- | --- | --- |
| v4.1 | 2026-01-30 | DBA | PostgreSQL 16 + PostGIS 3.4 DDL 초판 작성 (ERD v4.1 기반) |