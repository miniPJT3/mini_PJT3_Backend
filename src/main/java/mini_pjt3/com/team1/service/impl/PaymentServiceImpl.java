package mini_pjt3.com.team1.service.impl;

import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.dto.request.PaymentRequest;
import mini_pjt3.com.team1.dto.response.PaymentResponse;
import mini_pjt3.com.team1.entity.Member;
import mini_pjt3.com.team1.entity.Payment;
import mini_pjt3.com.team1.entity.PaymentHistory;
import mini_pjt3.com.team1.entity.VirtualAccount;
import mini_pjt3.com.team1.enums.BankCode;
import mini_pjt3.com.team1.enums.TransactionStatus;
import mini_pjt3.com.team1.repository.MemberRepository;
import mini_pjt3.com.team1.repository.PaymentHistoryRepository;
import mini_pjt3.com.team1.repository.PaymentRepository;
import mini_pjt3.com.team1.repository.VirtualAccountRepository;
import mini_pjt3.com.team1.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final VirtualAccountRepository virtualAccountRepository;
    private final MemberRepository memberRepository;

    @Override
    public PaymentResponse processDeposit(PaymentRequest request) {
        // 1. 멱등성 체크 (DB 레벨 Unique Index와 함께 이중 방어)
        if (paymentHistoryRepository.existsByTransactionId(request.getTransactionId())) {
            return null;
        }

        // 2. Payment 조회 (연관된 VirtualAccount까지 함께 고려)
        Payment payment = paymentRepository.findByPayUuid(request.getPayUuid())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제 요청입니다."));

        // 3. 상태 검증
        if (payment.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("결제 대기 상태가 아닙니다. 현재 상태: " + payment.getStatus());
        }

        // 4. 금액 검증
        if (!payment.getTotalAmount().equals(request.getDepositedAmount())) {
            // 정합성이 깨진 경우 로그를 남기거나 알림을 쏘는 로직이 필요할 수 있습니다.
            throw new IllegalArgumentException("입금액이 일치하지 않습니다. 요청액: " + payment.getTotalAmount());
        }

        // 5. 가상 계좌 상태 변경 및 마스킹 데이터 가져오기
        VirtualAccount va = virtualAccountRepository.findByPaymentId(payment.getId())
                .orElseThrow(() -> new IllegalStateException("연결된 가상 계좌를 찾을 수 없습니다."));

        va.completePayment(); // 여기서 status=USED 및 마스킹 로직 실행

        // 6. 상태 업데이트 및 이력 저장
        payment.updateStatus(TransactionStatus.PAID);

        PaymentHistory history = PaymentHistory.builder()
                .payment(payment)
                .transactionId(request.getTransactionId())
                .depositedAmount(request.getDepositedAmount())
                .paidAt(LocalDateTime.now())
                .build();
        paymentHistoryRepository.save(history);

        // 7. 최종 응답 DTO 생성 및 반환
        return PaymentResponse.success(
                payment.getPayUuid(),
                history.getDepositedAmount(),
                va.getMaskedAccountNumber()
        );
    }

    @Transactional
    public PaymentResponse issueVirtualAccount(Long memberId, PaymentRequest dto) {
        // 1. 데이터 유입 확인 (로그 확인용)
        System.out.println("DTO 상품명: " + dto.getProductName());
        System.out.println("DTO 입금액: " + dto.getDepositedAmount());

        // 2. 멤버 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 3. 결제 정보 생성
        Long amount = dto.getDepositedAmount();
        String pName = dto.getProductName();

        Payment payment = Payment.builder()
                .productName(pName)
                .totalAmount(amount)
                .member(member)
                .build();

        paymentRepository.save(payment);

        // 4. 가상계좌 생성 (무작위 은행 선정 + 무작위 번호 조합)
        // 4-1. Enum에 정의된 전체 은행 목록을 가져옵니다.
        BankCode[] allBanks = BankCode.values();
        // 4-2. 그중에서 하나를 무작위로 선택합니다.
        BankCode randomBank = allBanks[(int)(Math.random() * allBanks.length)];

        // 4-3. 선택된 은행의 코드(3자리) + 무작위 9자리 숫자 생성
        String prefix = randomBank.getCode();
        long randomNum = (long) (Math.random() * 900_000_000L) + 100_000_000L;
        String generatedNumber = prefix + "-" + randomNum;

        VirtualAccount vAccount = VirtualAccount.builder()
                .accountNumber(generatedNumber)
                .bankName(randomBank.getName()) // 랜덤으로 선택된 은행명 세팅
                .bankCode(randomBank)               // 랜덤으로 선택된 은행코드 세팅
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
                    // 1. 해당 결제(p)에 연결된 가상계좌를 찾습니다.
                    String accountNum = virtualAccountRepository.findByPaymentId(p.getId())
                            .map(VirtualAccount::getAccountNumber) // 마스킹된 번호 가져오기
                            .orElse("계좌 정보 없음");
                    System.out.println("결제ID: " + p.getId() + " / 찾은계좌: " + accountNum);

                    return PaymentResponse.builder()
                            .payUuid(p.getPayUuid())
                            .productName(p.getProductName())
                            .depositedAmount(p.getTotalAmount())
                            .status(p.getStatus())
                            .maskedAccount(accountNum)
                            .message(p.getCreatedAt().toString())
                            .build();
                })
                .toList();
    }
}