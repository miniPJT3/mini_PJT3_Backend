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

### 3.1. AuthController

**설명**: 사용자 인증 및 회원가입 기능을 제공합니다.

| 엔드포인트 | HTTP 메서드 | 설명 | 요청 DTO | 응답 DTO | 비고 |
|---|---|---|---|---|---|
| `/api/v1/auth/signup` | POST | 새로운 회원을 등록합니다. | `MemberJoinRequest` | `MemberResponse` | |
| `/api/v1/auth/login` | POST | 사용자 로그인을 처리하고 JWT 토큰을 발급합니다. | `LoginRequest` | `TokenResponse` | |

### 3.2. PaymentController

**설명**: 결제 생성 및 조회 기능을 제공합니다.

| 엔드포인트 | HTTP 메서드 | 설명 | 요청 DTO | 응답 DTO | 비고 |
|---|---|---|---|---|---|
| `/api/v1/payments` | POST | 새로운 결제를 생성합니다. | `PaymentRequest` | `PaymentResponse` | 인증 필요 |
| `/api/v1/payments/{paymentId}` | GET | `paymentId`에 해당하는 결제 정보를 조회합니다. | (없음) | `PaymentResponse` | 인증 필요 |
| `/api/v1/payments/{paymentId}/status` | GET | `paymentId`에 해당하는 결제의 상태를 조회합니다. | (없음) | `PaymentResponse` | 인증 필요 |

### 3.3. DashboardController (관리자/판매자 대시보드)

**설명**: 판매자 통계 및 매출 조회 기능을 제공합니다.

| 엔드포인트 | HTTP 메서드 | 설명 | 요청 DTO | 응답 DTO | 비고 |
|---|---|---|---|---|---|
| `/api/v1/admin/statistics/{sellerId}` | GET | 특정 판매자의 통계 정보를 조회합니다. | (없음) | `StatResponse` | 관리자/판매자 권한 필요 |
| `/api/v1/admin/sales/{sellerId}` | GET | 특정 판매자의 매출 정보를 조회합니다. | (없음) | `StatResponse` | 관리자/판매자 권한 필요 |

### 3.4. PaymentHistoryController

**설명**: 사용자의 결제 이력 조회 기능을 제공합니다.

| 엔드포인트 | HTTP 메서드 | 설명 | 요청 DTO | 응답 DTO | 비고 |
|---|---|---|---|---|---|
| `/api/v1/payment-history` | GET | 모든 결제 이력을 조회합니다. | (없음) | `List<PaymentResponse>` | 인증 필요 |
| `/api/v1/payment-history/{paymentId}` | GET | `paymentId`에 해당하는 결제 이력 상세 정보를 조회합니다. | (없음) | `PaymentResponse` | 인증 필요 |

## 4. 논의 사항

- **인증/인가 방식**: JWT (JSON Web Token) 또는 OAuth2 등의 인증 프로토콜을 어떤 방식으로 적용할 것인지 구체화가 필요합니다.
- **Rate Limiting**: 각 API 엔드포인트에 대한 요청 제한(Rate Limiting)을 어떻게 적용할지 (예: Spring Cloud Gateway, Bucket4j 등) 결정해야 합니다.
- **API 문서화**: Swagger/OpenAPI를 활용하여 API 문서를 자동 생성하고 관리하는 방안을 고려합니다.
- **CORS 설정**: Frontend와의 연동을 위해 필요한 CORS(Cross-Origin Resource Sharing) 설정을 정의합니다.
- **예외 처리 상세**: 각 예외 상황에 대한 구체적인 HTTP 상태 코드 및 에러 메시지 형식을 정의합니다.