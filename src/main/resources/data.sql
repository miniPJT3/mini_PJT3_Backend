-- 기존 데이터와 충돌 방지를 위해 무시하고 넘어가기 (MariaDB/MySQL용)
INSERT IGNORE INTO member (id, username, email, password, name, role, created_at, updated_at)
VALUES (1, 'jiho123', 'jiho@example.com', 'password123', '홍지호', 'USER', NOW(), NOW());

INSERT IGNORE INTO payment (id, pay_uuid, total_amount, status, member_id, created_at, updated_at)
VALUES (1, 'test-uuid-0001', 10000, 'PENDING', 1, NOW(), NOW());

INSERT IGNORE INTO virtual_account (id, account_number, bank_code, status, expired_at, is_deleted, payment_id, created_at, updated_at)
VALUES (1, '110123456789', 'SHINHAN', 'ACTIVE', DATE_ADD(NOW(), INTERVAL 3 HOUR), 0, 1, NOW(), NOW());