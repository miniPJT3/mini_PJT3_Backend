package mini_pjt3.com.team1.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

@Component
public class JwtUtil {

    
    @Value("${jwt.secret:your-very-long-and-secret-key-that-should-be-at-least-32-characters}")
    private String secretKey;

    private Key key;
    private final long tokenExpiration = 3600000L; // 1시간 (밀리초 단위)

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String createToken(Authentication authentication) {
        String email;
        String name = "";

        // 1. 구글 로그인(OAuth2User)인 경우 처리
        if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
        }
        // 2. 일반 로그인(UsernamePasswordAuthenticationToken)인 경우 처리
        else {
            email = authentication.getName(); // 우리가 AuthController에서 넘겨준 이메일
            // 일반 로그인 시에는 이름이 없을 수 있으므로 이메일을 이름 대용으로 쓰거나 빈 값 처리
            name = email.split("@")[0];
        }

        Claims claims = Jwts.claims().setSubject(email);
        claims.put("name", name);

        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + tokenExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    //토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            System.out.println("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            System.out.println("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            System.out.println("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            System.out.println("JWT 토큰이 비어있습니다.");
        }
        return false;
    }

    //토큰에서 사용자 이메일 추출
    public String getEmail(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public Authentication getAuthentication(String token) {
        String email = getEmail(token);
        // 별도의 UserDetails 서비스가 없다면 우선 기본 정보로 생성
        // 만약 DB에서 권한을 가져와야 한다면 이 부분을 수정해야 함
        return new UsernamePasswordAuthenticationToken(email, "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }
}