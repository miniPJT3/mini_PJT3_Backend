# Controller/API 설계서

## 1. 개요

Controller/API 계층은 클라이언트의 요청을 받아 Service 계층으로 전달하고, Service 계층의 처리 결과를 클라이언트에게 응답하는 역할을 합니다. RESTful API 디자인 원칙을 따르며, 일관된 응답 형식과 적절한 HTTP 상태 코드를 사용합니다.

## 2. 설계 원칙

- **RESTful API**: 리소스 기반의 명확한 URI 설계, HTTP 메서드(GET, POST, PUT, DELETE)의 적절한 활용을 통해 RESTful 원칙을 준수합니다.
- **요청/응답 일관성**: 모든 API는 표준화된 요청 및 응답 형식을 따르도록 설계합니다. (예: JSON)
- **유효성 검증**: DTO에 정의된 `@Valid` 어노테이션과 `@RequestBody`, `@RequestParam` 등의 파라미터 유효성 검증을 Controller에서 수행합니다.
- **인증/인가**: Spring Security를 활용하여 각 API 엔드포인트에 대한 인증 및 인가 처리를 적용합니다. (필터 체인 또는 메서드 수준 보안)
- **예외 처리**: ControllerAdvice를 통해 전역적인 예외 처리를 구현하여 일관된 에러 응답을 제공합니다.
- **버전 관리**: API 변경 시 호환성 유지를 위해 URI 기반 또는 Header 기반의 API 버전 관리를 고려합니다. (예: `/api/v1/virtual-accounts`)

## 3. 주요 Controller/API 목록 및 상세 설계

### 3.1. VirtualAccountController (Service A 관련)

**설명**: 가상 계좌 발급, 조회 기능을 제공합니다.

| 엔드포인트 | HTTP 메서드 | 설명 | 요청 DTO | 응답 DTO | 비고 |
|---|---|---|---|---|---|
| `/api/v1/virtual-accounts` | POST | 새로운 가상 계좌를 발급합니다. | `VirtualAccountCreateRequest` | `VirtualAccountCreateResponse` | 인증 필요 (구매자) |
| `/api/v1/virtual-accounts/{orderId}` | GET | `orderId`에 해당하는 가상 계좌 정보를 조회합니다. | (없음) | `VirtualAccountCreateResponse` | 인증 필요 (구매자) |

### 3.2. DepositController (Service B 관련)

**설명**: 외부 은행으로부터의 입금 알림(Webhook)을 수신하고, 결제 상태를 조회하는 기능을 제공합니다.

| 엔드포인트 | HTTP 메서드 | 설명 | 요청 DTO | 응답 DTO | 비고 |
|---|---|---|---|---|---|
| `/api/v1/deposits/webhook` | POST | 외부 은행으로부터 입금 알림을 수신합니다. | `DepositNotificationRequest` | (없음, HTTP 200 OK) | Webhook 보안 (API Key, IP 화이트리스트 검증) |
| `/api/v1/payments/{payUuid}/status` | GET | `payUuid`를 통해 결제 상태를 조회합니다. | (없음) | `PaymentStatusResponse` | 인증 필요 (구매자/판매자) |
| `/api/v1/admin/deposits/simulate` | POST | 입금 시뮬레이션을 수행합니다. (관리자용) | `DepositNotificationRequest` | (없음, HTTP 200 OK) | 관리자 권한 필요 |

### 3.3. SalesController (Service C 관련)

**설명**: 판매자 관제 페이지를 위한 매출 통계 및 보안 로그 조회 기능을 제공합니다.

| 엔드포인트 | HTTP 메서드 | 설명 | 요청 DTO | 응답 DTO | 비고 |
|---|---|---|---|---|---|
| `/api/v1/admin/sales/statistics` | GET | 특정 날짜의 매출 통계를 조회합니다. | `@RequestParam LocalDate date` | `SalesStatisticsResponse` | 관리자 권한 필요 |
| `/api/v1/admin/security/logs` | GET | 보안 로그 목록을 조회합니다. | `@RequestParam int page, @RequestParam int size` | `List<SecurityLogDto>` | 관리자 권한 필요 |

### 3.4. UserController

**설명**: 사용자 관련 API를 제공합니다. (예: 사용자 정보 조회, 회원가입 등)

| 엔드포인트 | HTTP 메서드 | 설명 | 요청 DTO | 응답 DTO | 비고 |
|---|---|---|---|---|---|
| `/api/v1/users/{userId}` | GET | 사용자 정보를 조회합니다. | (없음) | `UserDto` | 인증 필요 (본인 또는 관리자) |
| `/api/v1/users` | POST | 새로운 사용자를 생성합니다. (회원가입) | `UserCreateRequest` | `UserDto` | |

## 4. 논의 사항

- **인증/인가 방식**: JWT (JSON Web Token) 또는 OAuth2 등의 인증 프로토콜을 어떤 방식으로 적용할 것인지 구체화가 필요합니다.
- **Rate Limiting**: 각 API 엔드포인트에 대한 요청 제한(Rate Limiting)을 어떻게 적용할지 (예: Spring Cloud Gateway, Bucket4j 등) 결정해야 합니다.
- **API 문서화**: Swagger/OpenAPI를 활용하여 API 문서를 자동 생성하고 관리하는 방안을 고려합니다.
- **CORS 설정**: Frontend와의 연동을 위해 필요한 CORS(Cross-Origin Resource Sharing) 설정을 정의합니다.
- **예외 처리 상세**: 각 예외 상황에 대한 구체적인 HTTP 상태 코드 및 에러 메시지 형식을 정의합니다.