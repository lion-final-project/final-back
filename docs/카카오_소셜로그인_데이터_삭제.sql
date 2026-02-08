-- 카카오 소셜 로그인 관련 DB 데이터 전부 삭제 (PostgreSQL)
-- 실행: psql -U myuser -d mydatabase -f docs/카카오_소셜로그인_데이터_삭제.sql
-- 또는 IDE/클라이언트에서 mydatabase 연결 후 스크립트 실행

-- 1) 카카오로 가입한 user_id 목록 보관 (재실행 시 기존 임시테이블 제거 후 생성)
DROP TABLE IF EXISTS kakao_user_ids;
CREATE TEMP TABLE kakao_user_ids AS
SELECT user_id AS id FROM social_logins WHERE provider = 'KAKAO';

-- 2) 해당 사용자들의 refresh_tokens 삭제
DELETE FROM refresh_tokens
WHERE user_id IN (SELECT id FROM kakao_user_ids);

-- 3) 해당 사용자들의 addresses 삭제
DELETE FROM addresses
WHERE user_id IN (SELECT id FROM kakao_user_ids);

-- 4) 해당 사용자들의 user_roles 삭제
DELETE FROM user_roles
WHERE user_id IN (SELECT id FROM kakao_user_ids);

-- 5) 카카오 social_logins 삭제
DELETE FROM social_logins
WHERE provider = 'KAKAO';

-- 6) 주문/장바구니/구독 등 다른 테이블에 없는 카카오 전용 사용자만 users에서 삭제
DELETE FROM users
WHERE id IN (
  SELECT k.id FROM kakao_user_ids k
  WHERE NOT EXISTS (SELECT 1 FROM orders o WHERE o.user_id = k.id)
    AND NOT EXISTS (SELECT 1 FROM carts c WHERE c.user_id = k.id)
    AND NOT EXISTS (SELECT 1 FROM subscriptions s WHERE s.user_id = k.id)
    AND NOT EXISTS (SELECT 1 FROM reviews r WHERE r.user_id = k.id)
    AND NOT EXISTS (SELECT 1 FROM payment_methods p WHERE p.user_id = k.id)
    AND NOT EXISTS (SELECT 1 FROM approvals a WHERE a.user_id = k.id)
    AND NOT EXISTS (SELECT 1 FROM riders r WHERE r.user_id = k.id)
    AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id = k.id)
    AND NOT EXISTS (SELECT 1 FROM inquiries i WHERE i.user_id = k.id)
);

-- 7) 임시 테이블 제거 (선택)
DROP TABLE IF EXISTS kakao_user_ids;
