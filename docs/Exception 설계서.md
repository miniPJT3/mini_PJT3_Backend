# Exception 설계서

## 1. 개요

본 시스템의 예외 처리는 예상치 못한 오류 상황을 안정적으로 관리하고, 클라이언트에게는 의미 있는 에러 메시지를 일관된 형식으로 제공하는 것을 목표로 합니다. Spring의 예외 처리 메커니즘을 활용하여 비즈니스 로직과 기술적 예외를 분리하고, 전역적으로 처리합니다.

## 2. 설계 원칙

- **일관된 에러 응답**: 모든 API 에러 응답은 사전에 정의된 표준화된 형식(예: `ErrorResponse` DTO)을 따릅니다.
- **비즈니스 예외 명확화**: 애플리케이션의 도메인 규칙을 위반하는 상황에 대해서는 명확한 비즈니스 예외(Custom Exception)를 정의하여 사용합니다.
- **기술적 예외 분리**: 데이터베이스 접근 오류, 네트워크 오류 등 기술적인 예외는 비즈니스 예외와 분리하여 처리하고, 필요한 경우 로깅 후 클라이언트에게는 일반적인 에러 메시지를 전달합니다.
- **전역 예외 처리**: `@RestControllerAdvice`와 `@ExceptionHandler`를 활용하여 Controller 계층에서 발생하는 모든 예외를 중앙 집중적으로 처리합니다.
- **로그 기록**: 모든 예외 발생 시 상세한 정보를 로깅하여 문제 진단 및 해결에 활용합니다.

## 3. 주요 예외 유형 및 처리 전략

### 3.1. 비즈니스 예외 (Business Exceptions)

**설명**: 애플리케이션의 비즈니스 규칙을 위반하는 경우 발생하는 예외입니다. 예측 가능하며, 클라이언트에게 특정 오류 상황을 명확하게 전달해야 합니다. `RuntimeException`을 상속받아 Unchecked Exception으로 처리합니다.

| 예외 클래스명 | HTTP Status | 에러 코드 | 설명 | 발생 시나리오 (예시) |
|---|---|---|---|---|
| `VirtualAccountNotFoundException` | `404 Not Found` | `VA_001` | 가상 계좌를 찾을 수 없음 | 존재하지 않는 `orderId`로 가상 계좌 조회 시 |
| `VirtualAccountExpiredException` | `400 Bad Request` | `VA_002` | 만료된 가상 계좌에 입금 시도 | `EXPIRED` 상태의 가상 계좌에 입금 요청 시 |
| `DuplicateOrderPaymentException` | `409 Conflict` | `PAY_001` | 이미 처리된 주문/결제 요청 | 동일 `orderId`에 대한 중복 가상 계좌 발급 또는 입금 시도 (멱등성 위반) |
| `PaymentMismatchException` | `400 Bad Request` | `PAY_002` | 입금 금액 불일치 | 요청 금액과 실제 입금 금액이 다를 경우 |
| `UnauthorizedAccessException` | `403 Forbidden` | `SEC_001` | 접근 권한 없음 | RBAC 정책 위반 또는 인증 실패 |
| `InvalidApiKeyException` | `401 Unauthorized` | `SEC_002` | 유효하지 않은 API Key | Webhook 호출 시 잘못된 API Key 사용 |
| `InvalidIpAddressException` | `403 Forbidden` | `SEC_003` | 허용되지 않은 IP 주소 | Webhook 호출 시 IP Whitelist에 없는 IP 사용 |

**구현 방안**:
- 각 비즈니스 예외 클래스는 `ErrorCode` Enum을 포함하여 구체적인 에러 코드를 제공합니다.
- `CustomException` 추상 클래스를 만들어 공통 필드(예: `errorCode`, `message`)를 관리합니다.

### 3.2. 기술적 예외 (Technical Exceptions)

**설명**: 시스템 내부의 문제(DB 연결 오류, 외부 API 호출 실패 등)로 인해 발생하는 예외입니다. 클라이언트에게는 상세 정보를 노출하지 않고, 서버 로그에 기록하여 관리자가 처리하도록 합니다.

| 예외 유형 (예시) | HTTP Status | 에러 코드 | 설명 | 처리 방안 |
|---|---|---|---|---|
| `DataAccessException` (Spring Data JPA) | `500 Internal Server Error` | `SYS_001` | 데이터베이스 접근 오류 | 로그 기록 후 일반적인 서버 에러 응답 |
| `RestClientException` (Spring RestTemplate/WebClient) | `500 Internal Server Error` | `SYS_002` | 외부 API 호출 실패 | 로그 기록 후 일반적인 서버 에러 응답 |
| `IOException` | `500 Internal Server Error` | `SYS_003` | I/O 작업 오류 | 로그 기록 후 일반적인 서버 에러 응답 |
| `IllegalArgumentException` / `IllegalStateException` | `400 Bad Request` / `500 Internal Server Error` | `GEN_001` / `GEN_002` | 잘못된 인자 / 유효하지 않은 상태 | 상황에 따라 400 또는 500 응답, 로그 기록 |

**구현 방안**:
- `@ControllerAdvice`를 통해 각 기술적 예외에 대한 `@ExceptionHandler`를 정의합니다.
- 예외 발생 시 스택 트레이스 및 관련 컨텍스트 정보를 상세히 로깅합니다.
- 클라이언트에게는 `Internal Server Error` (500) 또는 `Bad Request` (400)와 같은 일반적인 HTTP 상태 코드와 추상화된 에러 메시지를 반환합니다.

## 4. 에러 응답 형식 (ErrorResponse DTO)

클라이언트에게 반환될 표준화된 에러 응답 형식입니다.

| 필드명 | 타입 | 설명 | 비고 |
|---|---|---|---|
| `timestamp` | `LocalDateTime` | 에러 발생 시각 | |
| `status` | `int` | HTTP 상태 코드 | |
| `error` | `String` | HTTP 상태 메시지 (예: "Not Found", "Bad Request") | |
| `code` | `String` | 애플리케이션 정의 에러 코드 (예: `VA_001`) | 비즈니스 예외 시 제공 |
| `message` | `String` | 사용자에게 보여줄 에러 메시지 | |
| `path` | `String` | 요청된 API 경로 | |

## 5. 논의 사항

- **에러 코드 정책**: 에러 코드의 명명 규칙(예: `VA_001`, `PAY_001`) 및 관리 방안을 확정합니다.
- **다국어 처리**: 에러 메시지의 다국어(i18n) 지원 여부 및 구현 방안을 고려합니다.
- **로깅 정책**: 예외 발생 시 어느 수준의 정보를 로깅할 것인지 (스택 트레이스 전체, 특정 필드만 등) 구체적인 정책을 수립합니다.
- **트랜잭션 롤백**: 비즈니스 예외 발생 시 트랜잭션 롤백 정책을 명확히 합니다. (기본적으로 `RuntimeException`은 롤백)