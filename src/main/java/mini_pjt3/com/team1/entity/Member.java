package mini_pjt3.com.team1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mini_pjt3.com.team1.enums.Role;

@Entity
@Getter @NoArgsConstructor
public class Member extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String loginId; // 일반 로그인용 ID

    private String password; // 암호화된 비밀번호
    private String name;
    private String email;
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role; // USER, SELLER, ADMIN

    @Builder // 빌더 패턴으로 가독성 확보
    public Member(String loginId, String password, String name, String email, String phone, Role role) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }
}
