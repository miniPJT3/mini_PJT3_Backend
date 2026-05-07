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

### 3.1. VirtualAccountService (Service A)

**설명**: 가상 계좌 발급, 상태 관리(만료, 사용 완료), 민감 정보 마스킹 및 소프트 삭제를 처리합니다.

| 메소드 시그니처 | 설명 | 비고 |
|---|---|---|
| `VirtualAccountCreateResponse issueVirtualAccount(VirtualAccountCreateRequest request)` | 주문 정보와 함께 가상 계좌를 발급하고, Redis 분산 락을 사용하여 중복 발급을 방지합니다. | 트랜잭션, Redis 분산 락 |
| `void expireVirtualAccounts()` | 만료 시간이 지난 가상 계좌를 찾아 `EXPIRED` 상태로 변경하고, 입금을 차단합니다. (스케줄러에 의해 주기적으로 호출) | 트랜잭션, 스케줄러 |
| `void maskAndSoftDeleteVirtualAccount(Long orderId)` | 결제 완료된 가상 계좌의 계좌 번호를 마스킹하고, 소프트 삭제 처리합니다. | 트랜잭션, 민감 정보 처리 |
| `VirtualAccountCreateResponse getVirtualAccountInfo(Long orderId)` | `orderId`로 가상 계좌 정보를 조회하여 DTO로 반환합니다. | |

### 3.2. DepositService (Service B)

**설명**: 외부 은행으로부터의 입금 알림을 처리하고, 결제 정합성을 검증하며, 멱등성을 보장합니다.

| 메소드 시그니처 | 설명 | 비고 |
|---|---|---|
| `void processDepositNotification(DepositNotificationRequest request)` | 외부 은행의 입금 알림(Webhook)을 수신하여 처리합니다. `transactionId`를 이용한 멱등성 검사 후 결제 상태를 업데이트하고 `PaymentHistory`를 기록합니다. | 트랜잭션, 멱등성, Webhook 보안 (API Key, IP 화이트리스트 검증은 Controller 또는 Filter에서 처리) |
| `PaymentStatusResponse getPaymentStatus(String payUuid)` | `payUuid`를 통해 결제 상태를 조회하고, 관련 가상 계좌 정보(마스킹된)를 포함하여 DTO로 반환합니다. | |
| `void simulateDeposit(DepositNotificationRequest request)` | 입금 시뮬레이터를 통해 성공/실패/위변조 시나리오를 테스트합니다. (관리자용) | |

### 3.3. SalesService (Service C)

**설명**: 판매자 관제 페이지를 위한 매출 통계 및 보안 로그 모니터링 기능을 제공합니다.

| 메소드 시그니처 | 설명 | 비고 |
|---|---|---|
| `SalesStatisticsResponse getDailySalesStatistics(LocalDate date)` | 특정 날짜의 총 매출 금액, 결제 완료/실패 건수 등 통계 정보를 조회하여 DTO로 반환합니다. | 캐싱 고려 |
| `List<SecurityLogDto> getSecurityLogs(int page, int size)` | 비정상적인 IP 접근 시도 등 보안 로그를 조회합니다. (페이징 처리) | |
| `void detectAndNotifyIntrusion(String ipAddress)` | 비정상적인 접근(Rate Limiting 위반 등) IP를 탐지하고 관리자에게 알림을 보냅니다. (별도의 스케줄러 또는 이벤트 리스너에 의해 호출) | |

### 3.4. UserService

**설명**: 사용자 관련 기본 CRUD 및 인증/인가에 필요한 사용자 정보를 제공합니다.

| 메소드 시그니처 | 설명 | 비고 |
|---|---|---|
| `UserDto getUserInfo(Long userId)` | `userId`로 사용자 정보를 조회하여 DTO로 반환합니다. | |
| `UserDto createUser(UserCreateRequest request)` | 새로운 사용자를 생성합니다. | |

## 4. 논의 사항

- **비동기 처리**: 입금 알림 처리(`processDepositNotification`)와 같이 외부 시스템과의 연동이 포함되거나 시간이 오래 걸리는 작업은 비동기 처리를 고려하여 응답 지연을 최소화합니다. (메시지 큐 도입 검토)
- **이벤트 기반 아키텍처**: 결제 완료, 계좌 만료 등 핵심 비즈니스 이벤트 발생 시 후속 처리를 위해 이벤트 발행-구독 모델을 도입할 수 있습니다.
- **보안 로직 분리**: Webhook 인증 (API Key, IP 화이트리스트), RBAC 접근 제어와 같은 보안 로직은 Service 계층보다는 Controller Filter 또는 Spring Security를 활용하여 분리하는 것을 권장합니다.
- **테스트 용이성**: 각 Service는 Mock 객체를 활용한 단위 테스트가 용이하도록 의존성을 최소화하고 인터페이스 기반으로 설계합니다.