# 주문 생성 API (API-ORD-001) 사용 테이블·컬럼

> Phase7: 어떤 테이블과 컬럼을 사용했는지 명시.

---

## 1. 주문 생성 API (POST /api/orders) 관련

### 1.1 읽기(조회) 사용

| 테이블 | 컬럼 | 용도 |
|--------|------|------|
| **users** | id, email | 사용자 조회(이메일) |
| **addresses** | id, user_id, contact, address_name, postal_code, address_line1, address_line2, location, is_default | 배송지 조회·소유 검증 |
| **payment_methods** | id, user_id, method_type, billing_key, is_default | 결제수단 조회·소유 검증 |
| **carts** | id, user_id | 장바구니 조회(사용자별) |
| **cart_products** | id, cart_id, product_id, store_id, quantity | 선택 장바구니 상품 조회·소유 검증 |
| **products** | id, store_id, product_name, price, sale_price, stock, is_active | 단가·재고·판매상태 검증 |
| **stores** | id, store_name | 마트 정보(StoreOrder 생성 시) |

### 1.2 쓰기(INSERT) 사용

| 테이블 | 컬럼 | 용도 |
|--------|------|------|
| **orders** | order_number, user_id, order_type, status, total_product_price, total_delivery_fee, final_price, delivery_address, delivery_location, delivery_request, ordered_at, created_at, updated_at | 주문 1건 생성 |
| **store_orders** | order_id, store_id, order_type, status, store_product_price, delivery_fee, final_price, created_at, updated_at | 마트별 주문 생성 |
| **order_products** | store_order_id, product_id, product_name_snapshot, price_snapshot, quantity, created_at, updated_at | 주문 상품 스냅샷 생성 |
| **payments** | order_id, payment_method, payment_status, amount, created_at, updated_at | 결제 정보 생성(PENDING) |
| **products** | stock, is_active (UPDATE) | 주문 수량만큼 재고 차감 |

### 1.3 공용 로직(BR-O03)

- **PriceCalculator** (checkout 서비스): `orders`/`store_orders`에 넣을 **total_product_price**, **total_delivery_fee**, **final_price**, 마트별 **store_product_price**, **delivery_fee**, **final_price** 계산에 사용.
- 사용 컬럼 개념: 상품 단가·수량 → 상품총액, 배달비 정책 → 배달비, 할인/포인트 → 최종결제액.

---

## 2. 기존 주문서 미리보기(Checkout)·주문 조회와의 관계

- **GET /api/checkout**: carts, cart_products, addresses, payment_methods, products, stores (읽기만).
- **GET /api/orders/{orderId}**: orders, store_orders, order_products, payments, stores (읽기만).
- **POST /api/orders**: 위 1.1·1.2 테이블 사용.

---

## 3. 더미데이터 구분 주석

- `LocalDataInitializer`: **// 결제 더미데이터** — 장바구니 상품, 배송지 1건, 결제수단 1건 (user@test.com).
- 주문 생성 더미: 별도 시드 없음. **POST /api/orders**로 위 장바구니·배송지·결제수단 더미를 사용해 주문 생성 후 DB에 반영됨.
