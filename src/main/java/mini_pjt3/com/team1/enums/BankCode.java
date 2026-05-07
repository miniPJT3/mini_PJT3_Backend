package mini_pjt3.com.team1.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BankCode {
    SHINHAN("088", "신한은행", "시중은행"),
    KOOKMIN("004", "KB국민은행", "시중은행"),
    WOORI("020", "우리은행", "시중은행"),
    HANA("081", "하나은행", "시중은행"),
    NH("011", "NH농협은행", "특수은행"),
    IBK("003", "IBK기업은행", "특수은행"),
    KAKAO("090", "카카오뱅크", "인터넷전문은행"),
    TOSS("092", "토스뱅크", "인터넷전문은행"),
    K_BANK("089", "케이뱅크", "인터넷전문은행"),
    CITY("027", "한국씨티은행", "시중은행"),
    SC("023", "SC제일은행", "시중은행"),
    POST("071", "우체국", "공공기관"),
    MG("045", "새마을금고", "상호금융"),
    SUHYUP("007", "수협은행", "특수은행"),
    BUSAN("032", "부산은행", "지방은행"),
    DAEGU("031", "iM뱅크(대구)", "지방은행");

    private final String code;     // 은행 코드 (088 등)
    private final String name;     // 은행 명칭
    private final String category; // 비고 (지방, 인터넷 등)
}