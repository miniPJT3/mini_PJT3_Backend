package mini_pjt3.com.team1.entity;

import jakarta.persistence.*;
import lombok.*;
import mini_pjt3.com.team1.enums.Role;

@Entity
@Getter
@Setter 
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 일반 로그인 ID (소셜 로그인의 경우 이메일이나 고유 식별값으로 대체 가능하도록 nullable 처리 고려)
    @Column(unique = true, nullable = true)
    private String loginId; 

    private String password; // 소셜 로그인 사용자는 null 가능

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone; // 최초 구글 로그인 시 null, 추가 정보 입력 후 업데이트

    // 소셜 로그인 제공자 (google 등)
    private String provider;

    // 소셜 로그인 제공자로부터 받는 고유 식별자 (재로그인 식별을 위해 필수)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // GUEST(최초소셜), USER, SELLER, ADMIN

    @Builder
    public Member(String loginId, String password, String name, String email, String phone, 
                  String provider, String providerId, Role role) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.provider = provider;
        this.providerId = providerId;
        this.role = role;
    }

    /**
     * 추가 정보(전화번호, 권한) 업데이트를 위한 비즈니스 메서드
     * 최초 구글 로그인 후 호출됨
     */
    public void updateAdditionalInfo(String phone, Role role) {
        this.phone = phone;
        this.role = role;
    }

    /**
     * 소셜 사용자 정보 업데이트 (이름 변경 등 대응)
     */
    public Member update(String name) {
        this.name = name;
        return this;
    }
}