package mini_pjt3.com.team1;

import mini_pjt3.com.team1.dto.request.MemberJoinRequest;
import mini_pjt3.com.team1.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Team1Application {

    public static void main(String[] args) {
        SpringApplication.run(Team1Application.class, args);
    }

    /**
     * 애플리케이션 실행 시 초기 데이터를 생성하는 설정입니다.
     * DB 연결 전에도 프론트엔드 로그인/권한 테스트를 진행할 수 있습니다.
     */
    @Bean
    public CommandLineRunner initData(AuthService authService) {
        return args -> {
            // 1. 일반 사용자(USER) 테스트 계정 생성
            MemberJoinRequest user = new MemberJoinRequest();
            user.setLoginId("user01");
            user.setPassword("1234");
            user.setName("테스트유저");
            user.setEmail("user@test.com");
            user.setPhone("010-1111-2222");
            user.setRole("USER"); // 일반 사용자 권한 부여
            
            try {
                authService.join(user);
                System.out.println(">>> 테스트 계정 생성 완료: ID(user01) / PW(1234)");
            } catch (Exception e) {
                System.out.println(">>> 테스트 계정이 이미 존재하거나 생성에 실패했습니다.");
            }

            // 2. 판매자(SELLER) 테스트 계정 생성
            MemberJoinRequest seller = new MemberJoinRequest();
            seller.setLoginId("seller01");
            seller.setPassword("1234");
            seller.setName("김판매");
            seller.setEmail("seller@test.com");
            seller.setPhone("010-3333-4444");
            seller.setRole("SELLER"); // 판매자 권한 부여

            try {
                authService.join(seller);
                System.out.println(">>> 테스트 판매자 계정 생성 완료: ID(seller01) / PW(1234)");
            } catch (Exception e) {
                System.out.println(">>> 테스트 판매자 계정이 이미 존재하거나 생성에 실패했습니다.");
            }
        };
    }
}