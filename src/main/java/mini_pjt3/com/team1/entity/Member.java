package mini_pjt3.com.team1.entity;

import jakarta.persistence.*;
import lombok.*;
import mini_pjt3.com.team1.enums.Role;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String username; // 일반 로그인 아이디
    private String password; // 암호화된 비밀번호
    private String name;     // 이름 또는 닉네임
    private String provider; // google, kakao 등

    @Enumerated(EnumType.STRING)
    private Role role;       // USER, SELLER, ADMIN

    @Builder
    public Member(String email, String username, String password, String name, String provider, Role role) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.name = name;
        this.provider = provider;
        this.role = role;
    }
}