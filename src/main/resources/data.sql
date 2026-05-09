-- 1. Member 데이터 (ID 1번을 판매자로 설정)
-- 지호님이 강조하신 '안전성'을 위해 역할을 SELLER로 명확히 지정합니다.
MERGE INTO member (id, username, email, password, name, role, created_at, updated_at)
KEY (id)
VALUES (1, 'jiho_seller', 'seller@example.com', 'password123', '홍지호(판매자)', 'SELLER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 2. 일반 사용자 데이터 (테스트용 구매자 추가)
MERGE INTO member (id, username, email, password, name, role, created_at, updated_at)
KEY (id)
VALUES (2, 'user123', 'user@example.com', 'password123', '구매자A', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 3. 고정 상품 데이터 6개 (판매자 ID 1번 고정)
MERGE INTO product (id, name, price, description, seller_id, created_at, updated_at)
KEY (id)
VALUES
(1, '고성능 기계식 키보드', 129000, '코딩 효율을 높여주는 청축 키보드', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '무선 노이즈캔슬링 헤드셋', 350000, '몰입을 돕는 최고의 사운드', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '4K 모니터 27인치', 450000, '선명한 화질의 개발 전용 모니터', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '인체공학 사무용 의자', 280000, '장시간 작업에도 허리가 편안한 의자', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'C타입 멀티 허브', 55000, '다양한 기기 연결을 위한 8in1 허브', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, '노트북 알루미늄 거치대', 32000, '거북목 방지를 위한 각도 조절 거치대', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);