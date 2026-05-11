# Repository 설계서

## 1. 개요

Repository 계층은 데이터 영속성(Persistence)을 담당하며, 데이터베이스와의 상호작용을 추상화합니다. JPA(Java Persistence API)를 활용하여 엔티티 객체를 관리하고, 비즈니스 로직 계층(Service Layer)에 데이터를 제공합니다.

## 2. 설계 원칙

- **JPA 활용**: Spring Data JPA를 사용하여 기본적인 CRUD(Create, Read, Update, Delete) 연산을 제공하고, 복잡한 쿼리는 QueryDSL 또는 JPQL을 활용합니다.
- **도메인 객체 반환**: Repository는 엔티티 객체를 반환하며, DTO 변환은 Service 계층에서 담당합니다.
- **트랜잭션 관리**: Repository 계층은 주로 단일 작업 단위의 트랜잭션을 보장하며, 복합적인 비즈니스 트랜잭션은 Service 계층에서 관리합니다.
- **예외 처리**: 데이터베이스 접근 중 발생하는 예외는 Spring의 `DataAccessException`으로 추상화되며, 필요에 따라 Service 계층에서 비즈니스 예외로 전환합니다.
- **성능 최적화**: 대량 조회 시 N+1 문제를 방지하기 위한 페치 조인(Fetch Join)이나 배치 사이즈(Batch Size) 설정 등을 고려합니다.

## 3. 주요 Repository 목록 및 상세 설계

### 3.1. VirtualAccountRepository

**설명**: `VirtualAccount` 엔티티의 영속성을 관리합니다. 가상 계좌 발급, 조회, 만료 처리 관련 기능을 제공합니다.

| 메소드 시그니처 | 설명 | 비고 |
|---|---|---|
| `VirtualAccount save(VirtualAccount virtualAccount)` | 새로운 가상 계좌를 저장하거나 기존 계좌를 업데이트합니다. | (JpaRepository 기본 제공) |
| `Optional<VirtualAccount> findByAccountNumber(String accountNumber)` | 계좌 번호로 가상 계좌를 조회합니다. | 암호화된 계좌 번호 조회에 대한 고려 필요. JPA 기본 제공 기능으로 불가할 시 QueryDSL 등 고려 |
| `List<VirtualAccount> findAllByStatusAndExpiredAtBefore(AccountStatus status, LocalDateTime expiredAt)` | 특정 시간 이전에 만료되는 활성 상태의 가상 계좌 목록을 조회합니다. (스케줄러용) | `IDX_VA_EXPIRED_AT` 활용 |

### 3.2. PaymentRepository

**설명**: `Payment` 엔티티의 영속성을 관리합니다. 결제 요청 및 상태 변경 관련 기능을 제공합니다.

| 메소드 시그니처 | 설명 | 비고 |
|---|---|---|
| `Payment save(Payment payment)` | 새로운 결제 정보를 저장하거나 기존 결제 정보를 업데이트합니다. | (JpaRepository 기본 제공) |
| `Optional<Payment> findByPayUuid(String payUuid)` | `payUuid`로 결제 정보를 조회합니다. | |
| `List<Payment> findAllByMember(Member member)` | 특정 회원에 해당하는 모든 결제 정보를 조회합니다. | |
| `List<Payment> findByStatusAndCreatedAtBetween(TransactionStatus status, LocalDateTime start, LocalDateTime end)` | 특정 상태와 기간에 해당하는 결제 목록을 조회합니다. (판매자 대시보드 통계용) | `IDX_ORDER_STATUS_DATE` 활용 (Payment에 직접 status, created_at이 있다고 가정) |

### 3.3. PaymentHistoryRepository

**설명**: `PaymentHistory` 엔티티의 영속성을 관리합니다. 현재는 JpaRepository의 기본 기능만을 활용합니다.

| 메소드 시그니처 | 설명 | 비고 |
|---|---|---|
| `PaymentHistory save(PaymentHistory paymentHistory)` | 새로운 입금 내역을 저장합니다. | (JpaRepository 기본 제공) |

### 3.4. MemberRepository

**설명**: `Member` 엔티티의 영속성을 관리합니다. 사용자(회원) 정보를 조회, 저장, 업데이트하는 기능을 제공합니다.

| 메소드 시그니처 | 설명 | 비고 |
|---|---|---|
| `Optional<Member> findByEmail(String email)` | 이메일로 회원 정보를 조회합니다. | |
| `boolean existsByEmail(String email)` | 이메일로 회원 존재 여부를 확인합니다. | |

### 3.5. SellerSalesStatRepository

**설명**: `SellerSalesStat` 엔티티의 영속성을 관리합니다. 판매자 매출 통계 정보를 조회하고 존재 여부를 확인하는 기능을 제공합니다.

| 메소드 시그니처 | 설명 | 비고 |
|---|---|---|
| `SellerSalesStat save(SellerSalesStat sellerSalesStat)` | 새로운 판매자 매출 통계 정보를 저장하거나 업데이트합니다. | (JpaRepository 기본 제공) |
| `Optional<SellerSalesStat> findBySellerId(Long sellerId)` | 판매자 ID로 판매자 매출 통계 정보를 조회합니다. | |
| `Optional<SellerSalesStat> findBySellerIdAndStatDate(Long sellerId, String statDate)` | 판매자 ID와 통계 날짜로 판매자 매출 통계 정보를 조회합니다. | |
| `boolean existsBySellerId(Long sellerId)` | 판매자 ID로 판매자 매출 통계 정보의 존재 여부를 확인합니다. | |
| `boolean existsBySellerIdAndStatDate(Long sellerId, String statDate)` | 판매자 ID와 통계 날짜로 판매자 매출 통계 정보의 존재 여부를 확인합니다. | |

## 4. 논의 사항

- **쿼리 복잡성**: 복잡한 통계 쿼리나 조건부 검색 쿼리가 필요한 경우 QueryDSL 도입을 고려합니다.
- **조회 성능**: `@EntityGraph` 또는 DTO Projection을 활용하여 N+1 문제와 같은 지연 로딩으로 인한 성능 저하를 방지합니다.
- **테스트 전략**: 각 Repository 인터페이스에 대한 단위 테스트 및 통합 테스트 전략을 수립합니다.
- **Auditing**: `BaseEntity`를 상속받아 `created_at`, `updated_at` 필드가 자동으로 관리되도록 설정합니다.