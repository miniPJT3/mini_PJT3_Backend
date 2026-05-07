# Redis 설계서

## 1. 개요

Redis는 인메모리 데이터 구조 스토어로서, 고성능의 키-값 저장소로 활용됩니다. 본 시스템에서는 기획서에 명시된 '분산 락(Redis)' 및 'JWT Refresh Token 관리'와 같은 빠른 읽기/쓰기 성능과 데이터 휘발성이 요구되는 영역에 Redis를 도입합니다.

## 2. 설계 원칙

- **성능 최적화**: 인메모리 특성을 활용하여 데이터 접근 속도를 극대화합니다.
- **데이터 휘발성**: 캐싱, 분산 락, 세션 관리 등 영속성이 필수는 아니지만 빠른 접근이 필요한 데이터에 주로 사용합니다.
- **고가용성 및 확장성**: Redis Cluster 또는 Sentinel 모드를 고려하여 서비스의 고가용성과 수평 확장을 지원합니다.
- **적절한 TTL(Time To Live) 설정**: Redis에 저장되는 데이터의 유효 기간을 명확히 설정하여 메모리 효율성을 관리합니다.

## 3. 주요 Redis 활용 방안 및 상세 설계

### 3.1. 분산 락 (Distributed Lock)

- **목적**: 동일 `orderId`에 대한 가상 계좌 중복 발급 요청을 방지하여 데이터 일관성을 유지합니다.
- **구현 기술**: Spring Data Redis의 `DistributedLock` 또는 `Redisson` 라이브러리 활용. `Redisson`은 복잡한 락 시나리오(예: Redlock) 및 편의 기능(예: Watchdog)을 제공하여 더 강력한 분산 락 구현이 가능합니다.
- **데이터 구조**: `String` 타입.
    - **Key**: `lock:virtual-account:order:{orderId}`
    - **Value**: 락을 획득한 인스턴스의 고유 ID (예: UUID 또는 Thread ID)
    - **TTL**: 락 획득 시 락의 유효 시간을 설정합니다. (예: 5초) 작업 완료 전에 락이 만료되지 않도록 Redisson의 Watchdog 기능을 활용하거나, 수동으로 락 갱신 로직을 구현합니다.
- **흐름**:
    1. 가상 계좌 발급 요청 시 `orderId`를 기반으로 Redis 락 획득 시도.
    2. 락 획득 성공 시 비즈니스 로직 수행 후 락 해제.
    3. 락 획득 실패 시 (이미 다른 요청이 락을 보유 중) 중복 요청으로 간주하여 `DuplicateOrderPaymentException` 발생.

### 3.2. JWT Refresh Token 관리

- **목적**: JWT Access Token 만료 시 새로운 Access Token을 발급하기 위한 Refresh Token을 안전하게 저장하고 관리합니다. Refresh Token 탈취 시 즉시 무효화할 수 있는 메커니즘을 제공합니다.
- **데이터 구조**: `String` 또는 `Hash` 타입.
    - **Key**: `refresh_token:{userId}` 또는 `refresh_token:{refreshTokenValue}`
    - **Value**: Refresh Token 자체 또는 사용자 정보 (예: `userId`, `roles`)
    - **TTL**: Refresh Token의 유효 기간과 동일하게 설정합니다.
- **흐름**:
    1. 로그인 성공 시 Refresh Token을 발급하고, 해당 Refresh Token (또는 userId)을 Redis에 저장하고 TTL 설정.
    2. 클라이언트로부터 Access Token 갱신 요청 시, Redis에 저장된 Refresh Token의 유효성을 검증.
    3. 유효성 검증 성공 시 새로운 Access Token 및 Refresh Token 발급 후 기존 Redis Refresh Token 무효화 및 새 Refresh Token 저장.
    4. 로그아웃 시 Redis에서 해당 Refresh Token 삭제하여 즉시 무효화.

### 3.3. 캐싱 (Caching)

- **목적**: 자주 조회되거나 계산 비용이 높은 데이터에 대한 접근 속도를 향상시킵니다. (예: 판매자 매출 통계)
- **구현 기술**: Spring Cache Abstraction (`@Cacheable`, `@CachePut`, `@CacheEvict`)과 Spring Data Redis 연동.
- **데이터 구조**: `Hash` 또는 `String` 타입.
    - **Key**: `{cacheName}::{key}` (예: `sales::statistics:2026-05-07`)
    - **Value**: 캐싱할 데이터 (JSON 형태로 직렬화하여 저장)
    - **TTL**: 데이터의 최신성 요구사항에 맞춰 적절히 설정합니다.
- **흐름**:
    1. `SalesService`의 `getDailySalesStatistics` 메서드에 `@Cacheable` 적용.
    2. 첫 호출 시 데이터베이스에서 조회 후 Redis에 저장.
    3. 이후 호출 시 Redis에서 데이터를 가져와 반환.
    4. 데이터 변경 시 `@CacheEvict`를 통해 캐시 무효화 또는 `@CachePut`으로 갱신.

## 4. Redis 구성 및 운영 방안

- **배포 모델**:
    - **개발/테스트 환경**: 단일 Redis 인스턴스 또는 Docker Compose를 통한 구성.
    - **운영 환경**: 고가용성 및 성능을 위해 Redis Cluster (샤딩 및 복제) 또는 Sentinel (자동 장애 조치) 모드 고려.
- **영속성**: RDB 스냅샷 및 AOF(Append Only File) 설정을 통해 Redis 데이터의 영속성 확보 여부 결정. (분산 락, JWT 등 휘발성 데이터는 필수 아님)
- **모니터링**: Redis 지표(메모리 사용량, 연결 수, 초당 명령 처리량 등)를 모니터링하여 성능 병목 현상 및 문제점을 조기에 감지합니다.
- **보안**: Redis 서버 접근 제어 (ACL, 방화벽), Redis 설정 파일(예: `requirepass`를 통한 비밀번호 설정) 보안 강화.

## 5. 논의 사항

- **데이터 직렬화**: Java 객체를 Redis에 저장할 때 사용할 직렬화 방식 (JDK Serialization, Jackson2JsonRedisSerializer 등) 결정.
- **연결 풀 관리**: Lettuce 또는 Jedis와 같은 Redis 클라이언트 라이브러리를 사용하여 효율적인 연결 풀 관리 전략 수립.
- **Prefix 전략**: Redis Key 관리를 위한 Prefix 규칙 정의. (예: `app-name:feature:key`)
- **장애 복구**: Redis 장애 발생 시 시스템 전체에 미치는 영향 분석 및 복구 전략 구체화.