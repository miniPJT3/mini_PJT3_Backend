package mini_pjt3.com.team1.entity;

import jakarta.persistence.*;
import lombok.*;
import mini_pjt3.com.team1.enums.Role;

@Entity
@Getter
@Setter //setName, setProvider 메서드를 사용하기 위해 추가
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String loginId; // 일반 로그인용 ID

    private String password; // 암호화된 비밀번호

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    //소셜 로그인 제공자(google 등)를 저장하기 위해 추가
    private String provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // USER, SELLER, ADMIN

    // 필드명을 클래스 필드(loginId)와 일치시키고 phone도 추가했습니다.
    @Builder
    public Member(String loginId, String password, String name, String email, String phone, String provider, Role role) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.provider = provider;
        this.role = role;
    }
}