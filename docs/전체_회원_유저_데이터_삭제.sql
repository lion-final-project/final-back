-- 일반 회원 + 소셜 로그인 회원 포함, 전체 회원(users) 및 연관 데이터 삭제 (PostgreSQL)
-- 실행: psql -U myuser -d mydatabase -f docs/전체_회원_유저_데이터_삭제.sql
-- 또는 IDE/클라이언트에서 DB 연결 후 스크립트 실행
-- 주의: 실행 시 복구할 수 없습니다. 필요 시 백업 후 실행하세요.
PGPASSWORD=mypassword psql -h localhost -p 5432 -U myuser -d mydatabase -f docs/전체_회원_유저_데이터_삭제.sql

-- 1) refresh_tokens
DELETE FROM refresh_tokens;

-- 2) notifications (user_id)
DELETE FROM notifications;

-- 3) inquiries (user_id)
DELETE FROM inquiries;

-- 4) reviews (user_id, store_order_id)
DELETE FROM reviews;

-- 5) subscriptions 관련 (subscription_history, subscription_product_items가 subscriptions 참조)
DELETE FROM subscription_history;
DELETE FROM subscription_product_items;
DELETE FROM subscriptions;

-- 6) approvals (user_id), approval_documents (approval_id)
DELETE FROM approval_documents;
DELETE FROM approvals;

-- 7) delivery_photos → deliveries (store_order_id, rider_id 참조)
DELETE FROM delivery_photos;
DELETE FROM deliveries;

-- 8) payments가 order_id 참조하므로 orders 보다 먼저 삭제
DELETE FROM payment_refunds;
DELETE FROM payments;

-- 9) order_products → store_orders → orders 순
DELETE FROM order_products;
DELETE FROM store_orders;
DELETE FROM orders;

-- 10) payment_methods (user_id)
DELETE FROM payment_methods;

-- 11) cart_products, carts (user_id)
DELETE FROM cart_products;
DELETE FROM carts;

-- 12) addresses (user_id)
DELETE FROM addresses;

-- 13) user_roles (user_id)
DELETE FROM user_roles;

-- 14) social_logins (user_id)
DELETE FROM social_logins;

-- 15) rider_locations, riders (user_id) — deliveries 삭제 후
DELETE FROM rider_locations;
DELETE FROM riders;

-- 16) users
DELETE FROM users;
