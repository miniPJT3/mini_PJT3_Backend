package mini_pjt3.com.team1.service;

import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.dto.request.PaymentRequest;
import mini_pjt3.com.team1.dto.response.PaymentResponse;
import mini_pjt3.com.team1.entity.Payment;
import mini_pjt3.com.team1.entity.Member;
import mini_pjt3.com.team1.entity.PaymentHistory;
import mini_pjt3.com.team1.entity.VirtualAccount;
import mini_pjt3.com.team1.enums.BankCode;
import mini_pjt3.com.team1.enums.TransactionStatus;
import mini_pjt3.com.team1.repository.MemberRepository;
import mini_pjt3.com.team1.repository.PaymentHistoryRepository;
import mini_pjt3.com.team1.repository.PaymentRepository;
import mini_pjt3.com.team1.repository.VirtualAccountRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository historyRepository;
    private final VirtualAccountRepository virtualAccountRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public PaymentResponse createPayment(
            PaymentRequest request,
            Authentication authentication
    ) {

        Long memberId = Long.parseLong(authentication.getName());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        // 은행 코드 검증
        BankCode bankCode = BankCode.fromCode(request.getBankCode());
        // 가상계좌 생성
        VirtualAccount virtualAccount = VirtualAccount.builder()
                .accountNumber(generateAccountNumber(bankCode))
                .bankCode(bankCode)
                .member(member)
                .build();

        virtualAccountRepository.save(virtualAccount);

        // 2. 결제 생성
        Payment payment = Payment.builder()
                .amount(request.getAmount())
                .member(member)
                .build();

        paymentRepository.save(payment);

        virtualAccount.setPayment(payment);

        return PaymentResponse.from(payment);
    }

    private String generateAccountNumber(BankCode bankCode) {
        return "VA" + System.currentTimeMillis();
    }

    @Transactional
    public PaymentResponse getPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제 없음"));

        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentStatus(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제 없음"));

        return PaymentResponse.from(payment);
    }

    @Transactional
    public void confirmPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제 없음"));

        payment.confirm();

        PaymentHistory history = PaymentHistory.builder()
                .payment(payment)
                .status(TransactionStatus.SUCCESS)
                .finalAmount(payment.getAmount()) // Assuming finalAmount is payment amount
                .build();

        historyRepository.save(history);
    }

    public List<PaymentResponse> getMyPayments(Authentication authentication) {

        Long memberId = Long.parseLong(authentication.getName());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        List<Payment> payments = paymentRepository.findAllByMember(member);

        return payments.stream()
                .map(PaymentResponse::from)
                .toList();
        }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentHistories() {
        return paymentRepository.findAll().stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제 없음"));

        payment.cancel();
    }

}