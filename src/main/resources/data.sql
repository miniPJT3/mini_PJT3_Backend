-- 1. Member 데이터
MERGE INTO member (id, username, email, password, name, role, created_at, updated_at)
KEY (id)
VALUES (1, 'jiho123', 'jiho@example.com', 'password123', '홍지호', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 2. Payment 데이터
--MERGE INTO payment (id, pay_uuid, total_amount, status, product_name, member_id, created_at, updated_at)
--KEY (id)
--VALUES (1, 'test-uuid-0001', 10000, 'PENDING', '프리미엄 헤드폰', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
--
---- 3. Virtual Account 데이터 (엔티티 필드 반영: bank_name, masked_account_number 추가)
--MERGE INTO virtual_account (id, account_number, masked_account_number, bank_code, bank_name, status, expired_at, is_deleted, payment_id, created_at, updated_at)
--KEY (id)
--VALUES (1, '110123456789', '110123456789', 'SHINHAN', '신한은행', 'ACTIVE', DATEADD('HOUR', 3, CURRENT_TIMESTAMP), false, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);