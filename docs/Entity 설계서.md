# Entity 설계서

## 📑 가상 계좌 결제 시스템: 엔티티 상세 설계서 (v1.0)

### 1. 엔티티 설계 개요

- **목적**: 결제 프로세스의 원자성 보장, 민감 정보의 물리적/논리적 격리, 마이크로서비스 확장을 고려한 도메인 분리.
- **설계 원칙**:
    1. **불변성**: 결제 완료 후 정산 데이터(Payment)는 절대 수정 불가.
    2. **보안**: 민감 정보(계좌번호)의 생명주기 관리 및 마스킹 강제화.
    3. **성능**: 대량의 주문 조회 및 만료 스케줄링을 위한 인덱스 최적화.

### 2. 엔티티 설계 목록 및 분류

| **분류** | **엔티티명** | **설명** | **담당 파트** |
| --- | --- | --- | --- |
| **Core** | `User` | 시스템 사용자 (구매자/판매자) 정보 | 공통 |
| **Business** | `Order` | 주문의 상태와 금액 정보를 담는 핵심 도메인 | Service A |
| **Security** | `VirtualAccount` | 1회용 가상 계좌 정보 (Soft Delete/마스킹 대상) | Service A |
| **Finance** | `Payment` | 실제 입금이 확인된 최종 결제 완료 영수증 | Service B |

### 3. 공통 설계 규칙

- **식별자(PK)**: `BIGINT` (MySQL `BIGINT` 기반 `IDENTITY` 전략).
- **공통 필드(BaseEntity)**: 모든 엔티티는 `created_at`(생성일), `updated_at`(수정일)을 포함 (JPA Auditing 사용).
- **시간 타입**: `LocalDateTime` (Precision 6).
- **금액 타입**: `BigDecimal` 또는 `Long` (소수점 처리가 필요 없으므로 본 프로젝트는 `Long` 권장).
- **열거형(Enum)**: 모든 상태값은 `@Enumerated(EnumType.STRING)`으로 관리.

### 4. 상세 엔티티 설계

#### 4.1 Payment (결제)

결제 프로세스의 중심 엔티티입니다.

| **필드명** | **타입** | **제약조건** | **설명** |
| --- | --- | --- | --- |
| `id` | Long | PK, Auto Increment | 시스템 내부 식별자 |
| `pay_uuid` | String | Unique, Not Null, Index | 외부 연동 및 프론트 노출용 고유 키 |
| `user_id` | Long | FK, Index | 결제 요청을 한 사용자 |
| `product_name` | String | Not Null (255) | 구매 상품명 |
| `total_amount` | Long | Not Null, Min(100) | 총 결제 요청 금액 |
| `status` | Enum(TransactionStatus) | Not Null | `PENDING`, `PAID`, `FAILED`, `EXPIRED` |

#### 4.2 VirtualAccount (가상 계좌)

보안과 휘발성을 담당하는 엔티티입니다.

| **필드명** | **타입** | **제약조건** | **설명** |
| --- | --- | --- | --- |
| `id` | Long | PK, Auto Increment | 계좌 식별자 |
| `order_id` | Long | FK, Unique, Index | 주문과의 1:1 관계 |
| `account_number` | String | Encrypted | 원본 계좌번호 (DB 저장 시 암호화 필수) |
| `masked_account_number` | String | Nullable | 마스킹된 계좌번호 (결제 완료 후 생성) |
| `bank_name` | String | Not Null | 은행명 (예: 신한은행) |
| `bank_code` | Enum(BankCode) | Not Null | 은행 식별 코드 (예: SHINHAN)  |
| `expired_at` | LocalDateTime | Not Null, Index | **만료 시간 (생성 시점 + 3h)** |
| `deleted_at` | LocalDateTime | Nullable | **Soft Delete 처리를 위한 일시** |
| `status` | Enum(AccountStatus) | Not Null | ACTIVE, USED, EXPIRED |
- **보안 설정**: `@SQLRestriction("deleted_at IS NULL")` 적용 → Soft Delete

#### 4.3 PaymentHistory (결제 내역)

최종 입금 확인 및 정산용 엔티티입니다.

| **필드명** | **타입** | **제약조건** | **설명** |
| --- | --- | --- | --- |
| `id` | Long | PK, Auto Increment | 결제 내역 식별자 |
| `order_id` | Long | FK, Unique | 주문과의 1:1 관계 |
| `transaction_id` | String | **Unique**, Index | **은행 측 고유 거래 ID (멱등성 키)** |
| `deposited_amount` | Long | Not Null | 실제 입금된 금액 |
| `paid_at` | LocalDateTime | Not Null | 입금 확정 시각 |

---

### 5. 공통 Enum 정의 (Common Codes)

#### 5.1 거래 상태 (`TransactionStatus`)

주문(송금 요청)의 생명주기를 관리하며, 보안 로직의 실행 시점을 결정합니다.

| **코드명** | **설명** | **비고** |
| --- | --- | --- |
| **PENDING** | 이체 대기 | 가상계좌 발급 직후, 입금 확인 전 상태 |
| **PAID** | 이체 완료 | 입금 확인 완료.  ****가상계좌 마스킹 처리 트리거 |
| **FAILED** | 이체 실패 | 시스템 오류, 은행 거절 등으로 인한 거래 중단 |
| **EXPIRED** | 기한 만료 | 지정된 입금 기한(3시간) 내 미입금 시 자동 전환 |

#### 5.2 가상계좌 상태 (`AccountStatus`)

발급된 계좌 리소스의 활성 여부 및 데이터 보호 상태를 관리합니다.

| **코드명** | **설명** | **비고** |
| --- | --- | --- |
| **ACTIVE** | 활성 | 입금 가능 상태. 원본 계좌번호 노출 |
| **USED** | 사용 완료 | 입금 완료로 인한 계좌 폐쇄. 마스킹 데이터 활성화 |
| **EXPIRED** | 만료 | 시간 초과로 인한 계좌 폐쇄. 입금 차단 |

#### 5.3 은행 식별 코드 (`BankCode`)

연동되는 은행 시스템 및 계좌 형식을 식별합니다.

| **코드명 (Enum)** | **코드값** | **은행명** | **비고** |
| --- | --- | --- | --- |
| **SHINHAN** | 088 | 신한은행 |  |
| **KOOKMIN** | 004 | KB국민은행 |  |
| **WOORI** | 020 | 우리은행 |  |
| **HANA** | 081 | 하나은행 |  |
| **NH** | 011 | NH농협은행 |  |
| **IBK** | 003 | IBK기업은행 |  |
| **KAKAO** | 090 | 카카오뱅크 | 인터넷 전문 은행 |
| **TOSS** | 092 | 토스뱅크 | 인터넷 전문 은행 |
| **K_BANK** | 089 | 케이뱅크 | 인터넷 전문 은행 |
| **CITY** | 027 | 한국씨티은행 |  |
| **SC** | 023 | SC제일은행 |  |
| **POST** | 071 | 우체국 |  |
| **MG** | 045 | 새마을금고 |  |
| **SUHYUP** | 007 | 수협은행 |  |
| **BUSAN** | 032 | 부산은행 | 지방은행 |
| **DAEGU** | 031 | iM뱅크(대구) | 지방은행 |

---

### 6. 연관관계 매핑 전략 및 정의

JPA의 성능 최적화를 위해 지연 로딩(Lazy Loading)을 기본으로 하며, 필요한 경우에만 양방향 매핑을 설정합니다.

#### **[Order] ↔ [VirtualAccount] (1:1 단방향)**

- **전략**: 외래 키를 `VirtualAccount`가 가집니다.
- **이유**: 주문(Order)은 계좌 없이 존재할 수 있지만, 계좌는 반드시 주문에 종속됩니다. 주문 생성 시 계좌를 함께 생성하므로 `VirtualAccount`에서 `order_id`를 관리하는 것이 성능상 유리합니다.

#### **[Order] ↔ [Payment] (1:1 단방향)**

- **전략**: 외래 키를 `Payment`가 가집니다.
- **이유**: 결제 완료 시점에 `Payment`가 생성되므로, 생성 시점에 `order_id`를 할당받는 구조가 데이터 흐름상 적합합니다.

#### **[User] ↔ [Order] (1:N 단방향/양방향 선택)**

- **전략**: `@ManyToOne` (다대일 단방향).
- **이유**: 유저 객체가 수많은 주문 리스트를 직접 들고 있을 필요가 없습니다. 필요시 `OrderRepository`에서 `user_id`로 조회합니다.

---

### 7. DB 인덱스 설계 (Index 정의)

- **IDX_ORDER_STATUS_DATE**: `Order(status, created_at)` - 판매자 대시보드 통계 조회용.

→ **효과**: DB에 주문이 100만 건 있어도, `status`가 'PAID'이고 날짜가 '오늘'인 데이터만 딱 골라내서 계산하기 때문에 대시보드가 버벅거리지 않고 바로 뜹니다.

- **IDX_VA_EXPIRED_AT**: `VirtualAccount(expired_at)` - 스케줄러의 만료 대상 추출 속도 향상.

→ **효과**: 인덱스가 없으면 매분마다 전체 계좌 데이터를 다 뒤져야 해서 서버가 비명을 지릅니다. 이 인덱스가 있으면 `expired_at`이 현재 시간보다 이전인 계좌들만 순식간에 찾아내서 '만료(EXPIRED)' 처리를 할 수 있습니다

- **IDX_PAY_TX_ID**: `Payment(transaction_id)` - 입금 Webhook 수신 시 중복 체크(멱등성) 성능 향상.

→ **효과:** 동일한 입금 신호가 중복으로 들어와도 단 한 번만 처리되도록 보장하여 데이터 결함(중복 결제)을 원천 차단하고 시스템의 신뢰성을 확보