package mini_pjt3.com.team1.service.impl;

import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.dto.request.PaymentRequest;
import mini_pjt3.com.team1.dto.response.PaymentResponse;
import mini_pjt3.com.team1.entity.*;
import mini_pjt3.com.team1.enums.BankCode;
import mini_pjt3.com.team1.enums.TransactionStatus;
import mini_pjt3.com.team1.repository.*;
import mini_pjt3.com.team1.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final VirtualAccountRepository virtualAccountRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    // 1. [조회] 판매자용 대시보드 리스트 수정
    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsBySeller(Long sellerId) {

        // 해당 판매자의 '모든 결제'를 가져옴
        List<Payment> payments = paymentRepository.findAllByProduct_SellerId(sellerId);

        return payments.stream()
                .map(p -> PaymentResponse.builder()
                        .payUuid(p.getPayUuid())
                        .productName(p.getProductName())
                        .totalAmount(p.getTotalAmount())
                        .status(p.getStatus())
                        .memberName(p.getMember().getName())
                        .createdAt(p.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // 2. [승인] 판매자가 최종 승인 버튼 클릭
    @Override
    public void approvePayment(String payUuid, Long sellerId) {
        Payment payment = paymentRepository.findByPayUuid(payUuid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제 내역입니다."));

        // 안전성 체크: 내 물건이 맞는지 확인
        if (!payment.getProduct().getSellerId().equals(sellerId)) {
            throw new IllegalStateException("본인의 상품만 승인할 수 있습니다.");
        }

        // 상태 검증: 입금 보고가 된 건인지 확인
        if (payment.getStatus() != TransactionStatus.DEPOSITED) {
            throw new IllegalStateException("입금 보고가 완료된 상태가 아닙니다.");
        }

        // 가상계좌 상태 변경 (USED)
        VirtualAccount va = virtualAccountRepository.findByPaymentId(payment.getId())
                .orElseThrow(() -> new IllegalStateException("연결된 계좌가 없습니다."));
        va.completePayment();

        // 최종 상태 변경
        payment.updateStatus(TransactionStatus.PAID);

        // 이력 저장
        PaymentHistory history = PaymentHistory.builder()
                .payment(payment)
                .transactionId("APPROVE_" + payUuid) // 혹은 자동 생성
                .depositedAmount(payment.getTotalAmount())
                .paidAt(LocalDateTime.now())
                .build();
        paymentHistoryRepository.save(history);
    }

    // 3. 구매자가 "입금했어요" 클릭
    @Override
    public PaymentResponse reportDeposit(String payUuid) {
        Payment payment = paymentRepository.findByPayUuid(payUuid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제 요청입니다."));

        if (payment.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("이미 처리 중이거나 완료된 결제입니다.");
        }

        payment.updateStatus(TransactionStatus.DEPOSITED);
        return PaymentResponse.builder().payUuid(payUuid).status(TransactionStatus.DEPOSITED).build();
    }


    @Transactional
    public PaymentResponse issueVirtualAccount(Long memberId, PaymentRequest dto) {
        // 1. 멤버 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 2. 상품 조회
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        // 3. 결제 정보 생성
        Payment payment = Payment.builder()
                .productName(product.getName())
                .totalAmount(dto.getDepositedAmount())
                .member(member)
                .product(product)
                .build();

        paymentRepository.save(payment);

        // 4. 가상계좌 생성 로직
        BankCode[] allBanks = BankCode.values();
        BankCode randomBank = allBanks[(int)(Math.random() * allBanks.length)];
        String generatedNumber = randomBank.getCode() + "-" + ((long)(Math.random() * 900_000_000L) + 100_000_000L);

        VirtualAccount vAccount = VirtualAccount.builder()
                .accountNumber(generatedNumber)
                .bankName(randomBank.getName())
                .bankCode(randomBank)
                .payment(payment)
                .build();
        virtualAccountRepository.save(vAccount);

        // 5. 응답 리턴
        return PaymentResponse.builder()
                .payUuid(payment.getPayUuid())
                .productName(payment.getProductName())
                .status(payment.getStatus())
                .bankName(randomBank.getName())
                .depositedAmount(payment.getTotalAmount())
                .maskedAccount(vAccount.getAccountNumber())
                .message("가상계좌가 발급되었습니다.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getMyHistory(Long memberId) {
        List<Payment> payments = paymentRepository.findAllByMemberId(memberId);

        return payments.stream()
                .map(p -> {
                    // 1. 해당 결제(p)에 연결된 가상계좌 엔티티를 통째로 찾습니다
                    Optional<VirtualAccount> vaOptional = virtualAccountRepository.findByPaymentId(p.getId());

                    // 2. 계좌번호와 은행명을 안전하게 추출
                    String accountNum = vaOptional.map(VirtualAccount::getAccountNumber).orElse("계좌 정보 없음");
                    String bankName = vaOptional.map(VirtualAccount::getBankName).orElse("은행 정보 없음"); // ⬅️ 추가됨!

                    System.out.println("결제ID: " + p.getId() + " / 은행: " + bankName + " / 계좌: " + accountNum);

                    // 3. 응답 DTO 빌더에 bankName 추가
                    return PaymentResponse.builder()
                            .payUuid(p.getPayUuid())
                            .productName(p.getProductName())
                            .depositedAmount(p.getTotalAmount())
                            .status(p.getStatus())
                            .maskedAccount(accountNum)
                            .bankName(bankName) //
                            .message(p.getCreatedAt().toString())
                            .build();
                })
                .toList();
    }

}