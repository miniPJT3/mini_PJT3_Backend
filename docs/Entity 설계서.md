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
| **Core** | `Member` | 시스템 사용자 (구매자/판매자/관리자) 정보 | 공통 |
| **Common** | `BaseEntity` | 공통 감사 필드 (생성/수정 일시) | 공통 |
| **Business** | `Payment` | 결제 요청 및 상태 관리 | 결제 서비스 |
| **Financial** | `PaymentHistory` | 실제 입금 확인된 결제 내역 | 결제 서비스 |
| **Financial** | `VirtualAccount` | 가상 계좌 정보 (발급, 만료 관리) | 결제 서비스 |
| **Statistics** | `SellerSalesStat` | 판매자별 매출 통계 정보 | 통계 서비스 |

### 3. 공통 설계 규칙

- **식별자(PK)**: `BIGINT` (MySQL `BIGINT` 기반 `IDENTITY` 전략).
- **공통 필드(BaseEntity)**: 모든 엔티티는 `createdAt`(생성일), `updatedAt`(수정일)을 포함 (JPA Auditing 사용).
- **시간 타입**: `LocalDateTime` (Precision 6).
- **금액 타입**: `Long` (소수점 처리가 필요 없으므로 `Long` 권장).
- **열거형(Enum)**: 모든 상태값은 `@Enumerated(EnumType.STRING)`으로 관리.

### 4. 상세 엔티티 설계

#### 4.1 BaseEntity (공통 필드)

| **필드명** | **타입** | **제약조건** | **설명** |
|---|---|---|---|
| `createdAt` | LocalDateTime | Not Null (생성시 자동) | 엔티티 생성 일시 |
| `updatedAt` | LocalDateTime | Not Null (수정시 자동) | 엔티티 최종 수정 일시 |

#### 4.2 Member (회원)

**설명**: 사용자(구매자, 판매자, 관리자) 정보를 관리하는 엔티티.
**상속**: `BaseEntity`

| **필드명** | **타입** | **제약조건** | **설명** |
|---|---|---|---|
| `id` | Long | PK, Auto Increment | 회원 고유 식별자 |
| `email` | String | Unique, Not Null | 회원 이메일 (로그인 ID) |
| `username` | String | | 사용자 이름 (일반 로그인 아이디로 사용됨) |
| `password` | String | | 암호화된 비밀번호 |
| `name` | String | | 사용자 실명 또는 닉네임 |
| `provider` | String | | OAuth2 제공자 (google, kakao 등) |
| `role` | Enum(Role) | | 회원 권한 (`USER`, `SELLER`, `ADMIN`) |

#### 4.3 Payment (결제)

**설명**: 결제 요청 및 상태를 관리하는 엔티티.
**상속**: `BaseEntity`

| **필드명** | **타입** | **제약조건** | **설명** |
|---|---|---|---|
| `id` | Long | PK, Auto Increment | 결제 고유 식별자 |
| `payUuid` | String | Unique | 외부 노출용 고유 값 (주문 ID 역할) |
| `amount` | Long | | 결제 금액 |
| `status` | Enum(TransactionStatus) | | 결제 상태 (`PENDING`, `SUCCESS`, `CANCELED`) |
| `member` | Member | ManyToOne | 결제를 요청한 회원 |

#### 4.4 PaymentHistory (결제 이력)

**설명**: 실제 입금 확인된 결제 내역을 기록하는 엔티티.
**상속**: `BaseEntity`

| **필드명** | **타입** | **제약조건** | **설명** |
|---|---|---|---|
| `id` | Long | PK, Auto Increment | 결제 이력 고유 식별자 |
| `payment` | Payment | OneToOne | 연관된 Payment 엔티티 |
| `status` | Enum(TransactionStatus) | | 결제 이력 상태 (`PAID` 등) |
| `finalAmount` | Long | | 최종 입금 확인 금액 |
| `depositedAt` | LocalDateTime | | 입금 확인 일시 |

#### 4.5 SellerSalesStat (판매자 매출 통계)

**설명**: 판매자의 일별 매출 통계를 관리하는 엔티티.
**상속**: `BaseEntity`

| **필드명** | **타입** | **제약조건** | **설명** |
|---|---|---|---|
| `id` | Long | PK, Auto Increment | 통계 고유 식별자 |
| `sellerId` | Long | Not Null | 판매자 ID |
| `statDate` | String | Not Null, Unique | 통계 기준 날짜 (YYYY-MM-DD 형식) |
| `totalSales` | Long | Default 0 | 누적 총 매출액 |
| `todaySales` | Long | Default 0 | 당일 매출액 |
| `paymentCount` | Long | Default 0 | 총 결제 건수 |
| `successCount` | Long | Default 0 | 결제 성공 건수 |

#### 4.6 VirtualAccount (가상 계좌)

**설명**: 결제를 위한 가상 계좌 정보를 관리하는 엔티티.
**상속**: `BaseEntity`

| **필드명** | **타입** | **제약조건** | **설명** |
|---|---|---|---|
| `id` | Long | PK, Auto Increment | 가상 계좌 고유 식별자 |
| `accountNumber` | String | Unique | 가상 계좌 번호 |
| `bankCode` | Enum(BankCode) | | 은행 코드 |
| `status` | Enum(AccountStatus) | | 가상 계좌 상태 (`ACTIVE`, `USED`, `EXPIRED`) |
| `expiredAt` | LocalDateTime | | 만료 일시 |
| `isDeleted` | boolean | Default false | Soft Delete 여부 |
| `payment` | Payment | OneToOne | 연결된 Payment 엔티티 |
| `member` | Member | ManyToOne | 가상 계좌를 발급받은 회원 |

### 5. 공통 Enum 정의 (Common Codes)

#### 5.1 AccountStatus (계좌 상태)
*   `ACTIVE`: 사용 가능 (입금 대기)
*   `USED`: 입금 완료되어 사용 종료
*   `EXPIRED`: 3시간 경과로 인한 무효화

#### 5.2 BankCode (은행 코드)
*   `SHINHAN("088", "신한은행", "시중은행")`
*   `KOOKMIN("004", "KB국민은행", "시중은행")`
*   `WOORI("020", "우리은행", "시중은행")`
*   `HANA("081", "하나은행", "시중은행")`
*   `NH("011", "NH농협은행", "특수은행")`
*   `IBK("003", "IBK기업은행", "특수은행")`
*   `KAKAO("090", "카카오뱅크", "인터넷전문은행")`
*   `TOSS("092", "토스뱅크", "인터넷전문은행")`
*   `K_BANK("089", "케이뱅크", "인터넷전문은행")`
*   `CITY("027", "한국씨티은행", "시중은행")`
*   `SC("023", "SC제일은행", "시중은행")`
*   `POST("071", "우체국", "공공기관")`
*   `MG("045", "새마을금고", "상호금융")`
*   `SUHYUP("007", "수협은행", "특수은행")`
*   `BUSAN("032", "부산은행", "지방은행")`
*   `DAEGU("031", "iM뱅크(대구)", "지방은행")`

#### 5.3 Role (회원 권한)
*   `USER("ROLE_USER", "사용자")`
*   `SELLER("ROLE_SELLER", "판매자")`
*   `ADMIN("ROLE_ADMIN", "관리자")`

#### 5.4 TransactionStatus (거래 상태)
*   `PENDING`: 결제 대기 중 (가상계좌 발급 직후)
*   `SUCCESS`: 결제 성공
*   `CANCELED`: 결제 취소
*   `PAID`: 입금 확인 완료
*   `FAILED`: 결제 실패
*   `EXPIRED`: 시간 초과로 인한 만료

### 6. 연관관계 매핑 전략 및 정의

*   **Payment ↔ Member (N:1)**: `Payment`는 `Member`에 대해 `ManyToOne` 관계로, 한 `Member`는 여러 `Payment`를 가질 수 있습니다.
*   **PaymentHistory ↔ Payment (1:1)**: `PaymentHistory`는 `Payment`에 대해 `OneToOne` 관계로, 하나의 `Payment`는 하나의 `PaymentHistory`를 가질 수 있습니다.
*   **VirtualAccount ↔ Payment (1:1)**: `VirtualAccount`는 `Payment`에 대해 `OneToOne` 관계로, 하나의 `Payment`에 하나의 `VirtualAccount`가 연결됩니다.
*   **VirtualAccount ↔ Member (N:1)**: `VirtualAccount`는 `Member`에 대해 `ManyToOne` 관계로, 한 `Member`는 여러 `VirtualAccount`를 가질 수 있습니다.

### 7. DB 인덱스 설계 (Index 정의)

*   `Payment.payUuid`: Unique Index (자동 생성)
*   `VirtualAccount.accountNumber`: Unique Index (자동 생성)
*   `VirtualAccount.expiredAt`: Index (`expiredAt` 필드에 인덱스가 명시적으로 추가되지 않았으나, 스케줄링 로직의 성능 향상을 위해 필요함)
*   `SellerSalesStat.statDate`, `SellerSalesStat.sellerId`: Unique Index (`sellerId`, `statDate` 복합 유니크 인덱스 필요)

---