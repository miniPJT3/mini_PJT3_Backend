# DTO 설계서

## 1. 개요

DTO(Data Transfer Object)는 계층 간 데이터 교환을 위해 사용되는 객체입니다. 본 시스템에서는 주로 클라이언트-컨트롤러, 컨트롤러-서비스, 서비스-레포지토리 간 데이터 전송에 활용됩니다. 민감 정보의 노출을 최소화하고, 유효성 검증을 통해 시스템의 안정성을 확보하는 것을 목표로 합니다.

## 2. 설계 원칙

- **계층 간 분리**: 각 계층의 관심사를 분리하고, 불필요한 데이터 노출을 방지합니다.
- **유효성 검증**: `@Valid` 및 `@NotNull`, `@Size` 등의 어노테이션을 활용하여 입력 데이터의 유효성을 검증합니다.
- **민감 정보 처리**: 계좌 번호 등 민감 정보는 DTO 단계에서 마스킹 또는 암호화 처리 후 전달될 수 있도록 고려합니다. (예: 응답 시 마스킹된 계좌번호 제공)
- **불변성**: DTO 객체는 생성 후 변경되지 않는 불변(Immutable) 객체로 설계하는 것을 권장합니다. (record 또는 final 필드 활용)

## 3. 주요 DTO 목록 및 상세 설계

## 3. 주요 DTO 목록 및 상세 설계

### 3.1. 요청 DTO (Request DTOs)

#### 3.1.1. LoginRequest

**설명**: 사용자 로그인을 위한 요청 DTO.
**관련 엔티티**: `Member`

| 필드명 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| `email` | String | `@NotBlank` | 사용자 이메일 |
| `password` | String | `@NotBlank` | 사용자 비밀번호 |

#### 3.1.2. MemberJoinRequest

**설명**: 새로운 회원 가입을 위한 요청 DTO.
**관련 엔티티**: `Member`

| 필드명 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| `email` | String | `@Email`, `@NotBlank` | 사용자 이메일 |
| `password` | String | `@NotBlank`, `@Size(min = 8, max = 20)` | 사용자 비밀번호 (8~20자) |
| `nickname` | String | `@NotBlank` | 사용자 닉네임 |
| `username` | String | `@NotBlank` | 사용자 실명 |

#### 3.1.3. PaymentRequest

**설명**: 결제 생성을 위한 요청 DTO.
**관련 엔티티**: `Payment`, `VirtualAccount`

| 필드명 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| `amount` | Long | `@NotNull`, `@Min(100)` | 결제 금액 (최소 100원) |
| `bankCode` | String | `@NotBlank` | 가상 계좌 발급을 위한 은행 코드 |
| `virtualAccountId` | Long | (선택 사항) | 기존 가상 계좌 ID (선택 사항) |

### 3.2. 응답 DTO (Response DTOs)

#### 3.2.1. ApiResponse<T>

**설명**: 모든 API 응답의 표준 형식을 제공하는 제네릭 DTO.

| 필드명 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| `success` | boolean | | API 요청 성공 여부 |
| `message` | String | | 응답 메시지 |
| `data` | T | | 실제 응답 데이터 (제네릭 타입) |

#### 3.2.2. MemberResponse

**설명**: 회원 정보를 반환하는 응답 DTO.
**관련 엔티티**: `Member`

| 필드명 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| `memberId` | Long | | 회원 고유 ID |
| `email` | String | | 회원 이메일 |
| `nickname` | String | | 회원 닉네임 |
| `role` | String | | 회원 권한 (예: `USER`, `ADMIN`) |

#### 3.2.3. PaymentResponse

**설명**: 결제 정보를 반환하는 응답 DTO.
**관련 엔티티**: `Payment`, `VirtualAccount`

| 필드명 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| `paymentId` | Long | | 결제 고유 ID |
| `orderId` | String | | 주문 ID 또는 Pay UUID |
| `amount` | Long | | 결제 금액 |
| `status` | String | | 결제 상태 (예: `PENDING`, `PAID`, `FAILED`) |
| `virtualAccountNumber` | String | (선택 사항) | 연결된 가상 계좌 번호 (서비스 계층에서 설정) |

#### 3.2.4. StatResponse

**설명**: 판매자 통계 정보를 반환하는 응답 DTO.
**관련 엔티티**: `SellerSalesStat`

| 필드명 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| `totalSales` | Long | | 총 매출 |
| `todaySales` | Long | | 오늘 매출 |
| `paymentCount` | Long | | 총 결제 건수 |
| `successCount` | Long | | 결제 성공 건수 |

#### 3.2.5. TokenResponse

**설명**: 인증 후 발급되는 JWT 토큰 정보를 반환하는 응답 DTO.

| 필드명 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| `accessToken` | String | | 접근 토큰 |
| `refreshToken` | String | | 갱신 토큰 |

#### 3.2.6. VirtualAccountResponse

**설명**: 가상 계좌 정보를 반환하는 응답 DTO. (현재 비어있음)

| 필드명 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| (현재 필드 없음) | | | |


## 4. 논의 사항

- **공통 응답 포맷**: 모든 API 응답에 대한 표준화된 응답 DTO(예: `ApiResponse<T>`) 적용 여부.
- **예외 처리 DTO**: 공통 예외 응답 DTO(예: `ErrorResponse`) 정의.
- **암호화/복호화 책임**: DTO 자체에 암호화 로직을 포함할지, 서비스 계층에서 처리할지 결정. (현재는 서비스 계층에서 처리하는 것으로 가정)
- **Enum 매핑**: DTO 필드에 `String`으로 받을 경우 `Enum`으로 변환 로직 필요.

---