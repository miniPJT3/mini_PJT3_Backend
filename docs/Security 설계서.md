# Security 설계서

## 1. 개요

본 시스템의 보안 설계는 "보안 격리 기반 일회용 가상 계좌 결제 시스템" 기획서의 핵심 가치인 '데이터 휘발성', '네트워크 격리', '결제 정합성'을 바탕으로, 고객의 민감 정보 보호 및 시스템 무결성 유지를 목표로 합니다. Spring Security를 중심으로 인증(Authentication), 인가(Authorization) 및 기타 보안 메커니즘을 정의합니다.

## 2. 보안 목표 및 원칙

- **기밀성(Confidentiality)**: 고객의 민감 정보(계좌번호 등) 유출 방지. 암호화, 접근 제어, 데이터 마스킹.
- **무결성(Integrity)**: 데이터의 위변조 방지. 멱등성 설계, Webhook 보안(API Key, IP 화이트리스트).
- **가용성(Availability)**: 서비스의 안정적인 제공. Rate Limiting, 분산 락.
- **최소 권한 원칙**: 모든 사용자 및 시스템 구성 요소는 자신의 기능 수행에 필요한 최소한의 권한만 가져야 합니다.
- **심층 방어(Defense in Depth)**: 단일 보안 메커니즘의 실패가 전체 시스템의 보안 침해로 이어지지 않도록 여러 계층에 보안을 적용합니다.

## 3. 인증 (Authentication)

- **일반 사용자 (구매자/판매자)**:
    - **JWT (JSON Web Token) 기반 인증**:
        - 로그인 성공 시 Access Token 및 Refresh Token 발급.
        - Access Token은 단기 유효 기간, Refresh Token은 장기 유효 기간.
        - 매 요청 시 Access Token을 HTTP Header (`Authorization: Bearer <token>`)에 포함하여 전송.
        - Refresh Token은 Redis에 저장하여 관리 (탈취 시 무효화 가능).
- **외부 은행 Webhook**:
    - **API Key 인증**: `DepositNotificationRequest`에 포함된 `apiKey`를 사용하여 유효성 검증.
    - **IP Whitelist**: 사전에 정의된 IP 주소(들)에서만 요청을 허용하여 접근 제어. (API Gateway 또는 Spring Security Filter에서 처리)

## 4. 인가 (Authorization)

- **RBAC (Role Based Access Control) 기반**:
    - **역할 (Role)**: `USER`, `SELLER`, `ADMIN` 등으로 구분.
    - **권한 (Authority)**: 각 역할에 따라 접근 가능한 리소스 및 기능 정의.
    - **Spring Security 활용**:
        - URL 패턴 기반 권한 설정 (`.antMatchers("/api/v1/admin/**").hasRole("ADMIN")`)
        - 메서드 수준 권한 설정 (`@PreAuthorize("hasRole('ADMIN')")`)
- **API Gateway 연동**: API Gateway 단에서 1차적인 인가 처리를 수행하고, 백엔드 서비스에서는 최종 검증을 수행합니다.

## 5. 주요 보안 메커니즘 및 상세 설계

### 5.1. 가상 계좌 민감 정보 보호

- **DB 암호화**: `VirtualAccount` 엔티티의 `account_number` 필드는 DB 저장 시 암호화하여 저장합니다. (예: Jasypt, AWS KMS 연동 등)
- **런타임 복호화**: 필요 시(예: 가상 계좌 발급 직후) 복호화하여 사용하되, 메모리 상에서 최소 시간만 유지 후 파기.
- **데이터 마스킹**: 결제 완료 시 `account_number`는 마스킹 처리되고 (`masked_account_number` 필드 활용), 원본은 소프트 삭제 처리됩니다.
- **로그 마스킹**: Logback 등의 로깅 프레임워크 설정을 통해 로그 파일에 계좌 번호 패턴이 포함될 경우 자동으로 마스킹 처리합니다.

### 5.2. 분산 락 (Distributed Lock)

- **목적**: 동일 주문에 대한 중복 가상 계좌 발급 요청 방지.
- **구현**: Redis를 활용한 분산 락 구현. `Redisson` 라이브러리 사용 검토.
- **동작 방식**: 가상 계좌 발급 전 `orderId`를 키로 락을 획득하고, 발급 완료 후 락을 해제. 락 획득 실패 시 중복 요청으로 간주하여 에러 처리.

### 5.3. 멱등성 (Idempotency)

- **목적**: 동일 트랜잭션에 대한 중복 결제 승인 방지.
- **구현**: `PaymentHistory` 엔티티의 `transaction_id` 필드에 Unique 인덱스 적용 및 저장 전 존재 여부 확인. (Service 계층에서 처리)
- **동작 방식**: 외부 은행 Webhook 수신 시 `transaction_id`를 사용하여 이미 처리된 요청인지 확인.

### 5.4. Rate Limiting (비정상적인 접근 탐지)

- **목적**: 서비스 거부 공격(DoS) 방지 및 비정상적인 요청 패턴 탐지.
- **구현**:
    - API Gateway 단에서 Nginx 또는 전용 솔루션을 통해 요청 속도 제한.
    - 백엔드 서비스에서는 Spring Cloud Gateway 또는 `Bucket4j`와 같은 라이브러리를 활용하여 특정 IP 또는 사용자별 API 호출 횟수 제한.
- **탐지 및 알림**: 비정상적인 Rate Limiting 위반 IP를 탐지하여 관리자에게 알림.

### 5.5. 시크릿 관리 (Secrets Management)

- **목적**: DB 패스워드, API Key 등 민감 정보를 안전하게 관리하고 런타임에 주입.
- **구현**: AWS Secrets Manager 활용.
- **동작 방식**: 애플리케이션 시작 시 Secrets Manager에서 필요한 시크릿을 조회하여 환경 변수 또는 애플리케이션 설정으로 주입. 코드 내에 하드코딩 방지.

## 6. 인프라 보안 (Infra-level Security)

기획서에 명시된 인프라 보안 시나리오를 따릅니다.

- **망 분리**: 결제 핵심 서버를 Private Subnet에 배치하여 외부 인터넷 직접 접속 차단.
- **통로 단일화**: 모든 트래픽은 API Gateway → Load Balancer를 거친 인가된 요청만 허용.
- **단방향 통신**: 서버는 NAT Gateway를 통해 외부 은행 API로 아웃바운드 통신만 허용.

## 7. 논의 사항

- **인증 공급자**: 사용자 인증 시 소셜 로그인(OAuth2) 또는 기존 인증 시스템과의 연동 여부.
- **취약점 분석**: 정적/동적 코드 분석 도구(SAST/DAST) 도입 및 정기적인 보안 취약점 점검.
- **보안 감사 로깅**: 모든 보안 관련 이벤트(로그인 실패, 권한 없는 접근 시도 등)에 대한 상세 로깅 및 모니터링 시스템 구축.
- **세션 관리**: JWT 토큰 만료 및 갱신 전략, Refresh Token 탈취 시 대응 방안 구체화.
- **API Key 관리**: 외부 서비스 연동 시 사용되는 API Key의 생성, 폐기, 순환 주기 등 관리 정책 수립.