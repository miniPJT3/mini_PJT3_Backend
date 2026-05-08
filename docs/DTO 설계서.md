# DTO 설계서

## 1. 개요

DTO(Data Transfer Object)는 계층 간 데이터 교환을 위해 사용되는 객체입니다. 본 시스템에서는 주로 클라이언트-컨트롤러, 컨트롤러-서비스, 서비스-레포지토리 간 데이터 전송에 활용됩니다. 민감 정보의 노출을 최소화하고, 유효성 검증을 통해 시스템의 안정성을 확보하는 것을 목표로 합니다.

## 2. 설계 원칙

- **계층 간 분리**: 각 계층의 관심사를 분리하고, 불필요한 데이터 노출을 방지합니다.
- **유효성 검증**: `@Valid` 및 `@NotNull`, `@Size` 등의 어노테이션을 활용하여 입력 데이터의 유효성을 검증합니다.
- **민감 정보 처리**: 계좌 번호 등 민감 정보는 DTO 단계에서 마스킹 또는 암호화 처리 후 전달될 수 있도록 고려합니다. (예: 응답 시 마스킹된 계좌번호 제공)
- **불변성**: DTO 객체는 생성 후 변경되지 않는 불변(Immutable) 객체로 설계하는 것을 권장합니다. (record 또는 final 필드 활용)

## 3. 주요 DTO 목록 및 상세 설계

### 3.1. 가상 계좌 발급 요청 (VirtualAccountCreateRequest)

**설명**: 클라이언트로부터 가상 계좌 발급을 요청받을 때 사용되는 DTO.
**관련 엔티티**: `Order`, `VirtualAccount`

| 필드명 | 타입 | 제약조건 | 설명 | 비고 |
|---|---|---|---|---|
| `userId` | Long | `@NotNull` | 가상 계좌 발급을 요청한 사용자 ID | `User` 엔티티의 `id` |
| `orderId` | Long | `@NotNull` | 가상 계좌가 매칭될 주문 ID | `Order` 엔티티의 `id` |
| `productName` | String | `@NotBlank`, `@Size(max=255)` | 상품명 | `Payment` 엔티티 `product_name` |
| `totalAmount` | Long | `@NotNull`, `@Min(100)` | 결제 요청 금액 | `Payment` 엔티티 `total_amount` |

### 3.2. 가상 계좌 발급 응답 (VirtualAccountCreateResponse)

**설명**: 가상 계좌 발급 성공 시 클라이언트에게 전달되는 응답 DTO.
**관련 엔티티**: `VirtualAccount`

| 필드명 | 타입 | 제약조건 | 설명 | 비고 |
|---|---|---|---|---|
| `accountNumber` | String | `@NotNull` | 발급된 가상 계좌 번호 | 마스킹 처리 여부 논의 필요 (초기에는 원본, 결제 완료 후 마스킹?) |
| `bankName` | String | `@NotNull` | 은행명 | `VirtualAccount` 엔티티 `bank_name` |
| `bankCode` | String | `@NotNull` | 은행 코드 | `VirtualAccount` 엔티티 `bank_code` (Enum String) |
| `expiredAt` | LocalDateTime | `@NotNull` | 가상 계좌 만료 시각 | `VirtualAccount` 엔티티 `expired_at` |
| `orderId` | Long | `@NotNull` | 매칭된 주문 ID | `VirtualAccount` 엔티티 `order_id` |
| `payUuid` | String | `@NotNull` | 외부 연동 및 프론트 노출용 고유 키 | `Payment` 엔티티 `pay_uuid` |

### 3.3. 입금 알림 요청 (DepositNotificationRequest) - 외부 은행 Webhook 수신용

**설명**: 외부 은행 Mock API로부터 입금 알림을 수신할 때 사용되는 DTO.
**관련 엔티티**: `PaymentHistory`

| 필드명 | 타입 | 제약조건 | 설명 | 비고 |
|---|---|---|---|---|
| `transactionId` | String | `@NotBlank` | 은행 측 고유 거래 ID (멱등성 키) | `PaymentHistory` 엔티티 `transaction_id` |
| `orderId` | Long | `@NotNull` | 입금된 주문 ID | `PaymentHistory` 엔티티 `order_id` |
| `depositedAmount` | Long | `@NotNull`, `@Min(1)` | 실제 입금된 금액 | `PaymentHistory` 엔티티 `deposited_amount` |
| `paidAt` | LocalDateTime | `@NotNull` | 입금 확정 시각 | `PaymentHistory` 엔티티 `paid_at` |
| `apiKey` | String | `@NotBlank` | Webhook 인증용 API Key | 보안 검증에 사용 |

### 3.4. 결제 상태 조회 응답 (PaymentStatusResponse)

**설명**: 클라이언트가 주문 또는 결제의 현재 상태를 조회할 때 사용되는 DTO.
**관련 엔티티**: `Payment`, `VirtualAccount`

| 필드명 | 타입 | 제약조건 | 설명 | 비고 |
|---|---|---|---|---|
| `payUuid` | String | `@NotNull` | 외부 연동 및 프론트 노출용 고유 키 | `Payment` 엔티티 `pay_uuid` |
| `orderId` | Long | `@NotNull` | 주문 ID | `Payment` 엔티티 `order_id` |
| `paymentStatus` | String | `@NotNull` | 결제 상태 (PENDING, PAID, FAILED, EXPIRED) | `Payment` 엔티티 `status` (Enum String) |
| `maskedAccountNumber` | String | Nullable | 마스킹된 가상 계좌 번호 | 결제 완료 후 `VirtualAccount` 엔티티 `masked_account_number` |
| `bankName` | String | Nullable | 은행명 | `VirtualAccount` 엔티티 `bank_name` |
| `totalAmount` | Long | `@NotNull` | 결제 요청 금액 | `Payment` 엔티티 `total_amount` |
| `depositedAmount` | Long | Nullable | 실제 입금된 금액 | `PaymentHistory` 엔티티 `deposited_amount` (PAID 상태일 경우) |
| `paidAt` | LocalDateTime | Nullable | 입금 확정 시각 | `PaymentHistory` 엔티티 `paid_at` (PAID 상태일 경우) |

### 3.5. 판매자 매출 통계 응답 (SalesStatisticsResponse)

**설명**: 판매자 관리 페이지에서 매출 통계를 제공할 때 사용되는 DTO.
**관련 엔티티**: `Payment` (PaymentHistory), `User`

| 필드명 | 타입 | 제약조건 | 설명 | 비고 |
|---|---|---|---|---|
| `date` | LocalDate | `@NotNull` | 통계 기준 날짜 | |
| `totalSalesAmount` | Long | `@NotNull` | 해당 날짜 총 매출 금액 | |
| `totalPaidCount` | Long | `@NotNull` | 해당 날짜 결제 완료 건수 | |
| `totalFailedCount` | Long | `@NotNull` | 해당 날짜 결제 실패 건수 | |

## 4. 논의 사항

- **공통 응답 포맷**: 모든 API 응답에 대한 표준화된 응답 DTO(예: `ApiResponse<T>`) 적용 여부.
- **예외 처리 DTO**: 공통 예외 응답 DTO(예: `ErrorResponse`) 정의.
- **암호화/복호화 책임**: DTO 자체에 암호화 로직을 포함할지, 서비스 계층에서 처리할지 결정. (현재는 서비스 계층에서 처리하는 것으로 가정)
- **Enum 매핑**: DTO 필드에 `String`으로 받을 경우 `Enum`으로 변환 로직 필요.

---