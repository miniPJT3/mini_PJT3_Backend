# Service 설계서

## 1. 개요

Service 계층은 핵심 비즈니스 로직을 구현하고, 트랜잭션을 관리하며, Controller 계층과 Repository 계층 사이의 중개자 역할을 수행합니다. 기획서에 명시된 `Service A`, `Service B`, `Service C`의 기능을 중심으로 설계합니다.

## 2. 설계 원칙

- **비즈니스 로직 캡슐화**: 도메인 객체를 활용하여 비즈니스 규칙을 구현하고, 복잡한 로직을 Service 내에 캡슐화합니다.
- **트랜잭션 관리**: `@Transactional` 어노테이션을 사용하여 비즈니스 메서드의 ACID(원자성, 일관성, 고립성, 지속성) 특성을 보장합니다.
- **DTO 변환 책임**: Repository에서 조회한 엔티티를 Controller로 전달하기 전에 DTO로 변환하여 반환합니다.
- **예외 처리**: 비즈니스 규칙 위반 또는 예상치 못한 상황에 대해 의미 있는 비즈니스 예외를 발생시키고 적절하게 처리합니다.
- **의존성 주입**: 생성자 주입을 통해 의존성을 관리하며, 단일 책임 원칙(SRP)을 준수하도록 설계합니다.

## 3. 주요 Service 목록 및 상세 설계

### 3.1. AuthService

**설명**: 회원가입, 로그인 등 인증 관련 비즈니스 로직을 처리합니다.

| 메소드 시그니처 | 설명 | 비고 |
|---|---|---|
| `MemberResponse signup(MemberJoinRequest request)` | 새로운 회원을 가입시킵니다. 이메일 중복 검증 및 비밀번호 암호화를 포함합니다. | |
| `TokenResponse login(LoginRequest request)` | 회원 로그인 요청을 처리하고, 유효한 경우 Access Token과 Refresh Token을 발급합니다. | |

### 3.2. CustomOAuth2UserService

**설명**: OAuth2 로그인 처리 및 사용자 정보 로드 로직을 제공합니다. 구글과 같은 외부 인증 공급자를 통해 사용자를 인증하고, 필요한 경우 회원 정보를 자동 저장합니다.

| 메소드 시그니처 | 설명 | 비고 |
|---|---|---|
| `OAuth2User loadUser(OAuth2UserRequest request)` | OAuth2 사용자 정보를 로드하고, 이메일을 기반으로 회원을 조회하거나 새로 저장합니다. | |

### 3.3. PaymentService

**설명**: 결제 생성, 조회, 확정, 취소 및 가상 계좌 발급 관련 비즈니스 로직을 처리합니다.

| 메소드 시그니처 | 설명 | 비고 |
|---|---|---|
| `PaymentResponse createPayment(PaymentRequest request, Authentication authentication)` | 새로운 결제를 생성하고, 가상 계좌를 발급합니다. (분산 락을 통한 중복 발급 방지는 현재 미구현) | 가상 계좌 번호 생성 로직 포함 |
| `PaymentResponse getPayment(Long paymentId)` | 특정 결제 ID로 결제 정보를 조회합니다. | |
| `PaymentResponse getPaymentStatus(Long paymentId)` | 특정 결제 ID로 결제 상태 정보를 조회합니다. | |
| `void confirmPayment(Long paymentId)` | 결제를 확정 상태로 변경하고, 결제 내역을 기록합니다. (멱등성 검증은 현재 미구현) | |
| `List<PaymentResponse> getMyPayments(Authentication authentication)` | 현재 인증된 사용자의 모든 결제 내역을 조회합니다. | |
| `List<PaymentResponse> getPaymentHistories()` | 모든 결제 내역을 조회합니다. | |
| `void cancelPayment(Long paymentId)` | 특정 결제를 취소 상태로 변경합니다. | |

### 3.4. StatService

**설명**: 판매자 매출 통계 정보를 조회하고 업데이트하는 비즈니스 로직을 처리합니다.

| 메소드 시그니처 | 설명 | 비고 |
|---|---|---|
| `StatResponse getSellerStat(Long sellerId)` | 특정 판매자의 오늘 날짜 매출 통계 정보를 조회합니다. (캐싱은 현재 미구현) | |
| `void updateDailyStat(Long sellerId, Long amount)` | 특정 판매자의 일일 매출 통계에 금액을 추가하거나 초기화합니다. | |
| `StatResponse initializeStat(Long sellerId)` | 특정 판매자의 오늘 날짜 매출 통계가 없는 경우 초기화합니다. | |

### 3.5. VaExpireScheduler

**설명**: 만료 시간이 지난 가상 계좌를 찾아 `EXPIRED` 상태로 변경하는 스케줄러 로직을 담당합니다. `VirtualAccount` 엔티티의 `expiredAt` 필드를 기준으로 동작합니다.

| 메소드 시그니처 | 설명 | 비고 |
|---|---|---|
| `void expireVirtualAccounts()` | 5분마다 실행되며, 만료 시간이 지난 활성 상태의 가상 계좌들을 찾아 만료 처리합니다. | `@Scheduled` (cron = "0 */5 * * * *") |

## 4. 논의 사항

- **비동기 처리**: 입금 알림 처리(`processDepositNotification`)와 같이 외부 시스템과의 연동이 포함되거나 시간이 오래 걸리는 작업은 비동기 처리를 고려하여 응답 지연을 최소화합니다. (메시지 큐 도입 검토)
- **이벤트 기반 아키텍처**: 결제 완료, 계좌 만료 등 핵심 비즈니스 이벤트 발생 시 후속 처리를 위해 이벤트 발행-구독 모델을 도입할 수 있습니다.
- **보안 로직 분리**: Webhook 인증 (API Key, IP 화이트리스트), RBAC 접근 제어와 같은 보안 로직은 Service 계층보다는 Controller Filter 또는 Spring Security를 활용하여 분리하는 것을 권장합니다.
- **테스트 용이성**: 각 Service는 Mock 객체를 활용한 단위 테스트가 용이하도록 의존성을 최소화하고 인터페이스 기반으로 설계합니다.