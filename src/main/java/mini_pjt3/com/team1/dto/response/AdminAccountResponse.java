package mini_pjt3.com.team1.dto.response;

import mini_pjt3.com.team1.entity.Member;
import mini_pjt3.com.team1.enums.Role;

import java.time.LocalDateTime;

public record AdminAccountResponse(
        Long id,
        String email,
        String username,
        String name,
        String phone,
        String provider,
        Role role,
        String roleName,
        LocalDateTime createdAt
) {
    public static AdminAccountResponse from(Member member) {
        return new AdminAccountResponse(
                member.getId(),
                maskEmail(member.getEmail()),
                maskLoginId(member.getLoginId()),
                maskName(member.getName()),
                maskPhone(member.getPhone()),
                member.getProvider(),
                member.getRole(),
                member.getRole() == null ? null : member.getRole().getTitle(),
                member.getCreatedAt()
        );
    }

    private static String maskName(String name) {
        if (name == null || name.isBlank()) {
            return "알 수 없음";
        }

        int length = name.length();

        if (length == 1) {
            return "*";
        }

        if (length == 2) {
            return name.charAt(0) + "*";
        }

        return name.charAt(0)
                + "*".repeat(length - 2)
                + name.charAt(length - 1);
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return "전화번호 정보 없음";
        }

        // 예: 010-1234-5678 -> 010-****-5678
        // 예: 011-123-4567  -> 011-***-4567
        if (phone.contains("-")) {
            String[] parts = phone.split("-");

            if (parts.length == 3) {
                return parts[0]
                        + "-"
                        + "*".repeat(parts[1].length())
                        + "-"
                        + parts[2];
            }
        }

        String digits = phone.replaceAll("\\D", "");

        if (digits.length() <= 7) {
            return "****";
        }

        String prefix = digits.substring(0, 3);
        String suffix = digits.substring(digits.length() - 4);
        int middleLength = digits.length() - 7;

        return prefix + "-"
                + "*".repeat(middleLength)
                + "-"
                + suffix;
    }

    private static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "이메일 정보 없음";
        }

        int atIndex = email.indexOf("@");

        if (atIndex <= 0) {
            return maskLoginId(email);
        }

        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        return maskEmailLocalPart(localPart) + domain;
    }

    private static String maskLoginId(String loginId) {
        if (loginId == null || loginId.isBlank()) {
            return "아이디 정보 없음";
        }

        return maskEmailLocalPart(loginId);
    }

    private static String maskEmailLocalPart(String value) {
        int length = value.length();

        if (length <= 2) {
            return "*".repeat(length);
        }

        if (length <= 5) {
            return value.charAt(0)
                    + "*".repeat(length - 1);
        }

        return value.substring(0, 3)
                + "*".repeat(length - 5)
                + value.substring(length - 2);
    }
}