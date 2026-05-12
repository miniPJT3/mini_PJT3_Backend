package mini_pjt3.com.team1.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${jwt.secret:your-very-long-and-secret-key-that-should-be-at-least-32-characters}")
    private String secretKey;

    private Key key;
    private final long tokenExpiration = 3600000L; // 1시간

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String createToken(Authentication authentication) {
        String email;
        String name = "";

        // 1. [핵심] 권한 정보 추출 (ROLE_USER, ROLE_ADMIN 등을 문자열로 변환)
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 2. 구글 로그인(OAuth2User)인 경우 처리
        if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
        }
        // 3. 일반 로그인인 경우 처리
        else {
            email = authentication.getName();
            name = email.split("@")[0];
        }

        Claims claims = Jwts.claims().setSubject(email);
        claims.put("name", name);
        claims.put("auth", authorities); // 🥊 [중요] 토큰에 권한 정보 저장

        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + tokenExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 유효성 검증
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

    // 토큰에서 사용자 이메일 추출
    public String getEmail(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * 🥊 [핵심 수정] 토큰에서 권한 정보를 꺼내서 Authentication 객체 생성
     */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();

        // 1. 토큰에서 "auth" 클레임 꺼내기
        Object authClaim = claims.get("auth");

        Collection<? extends GrantedAuthority> authorities;

        if (authClaim != null && !authClaim.toString().isEmpty()) {
            // "ROLE_USER,ROLE_ADMIN" -> List 생성
            authorities = Arrays.stream(authClaim.toString().split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        } else {
            // 권한 정보가 없으면 기본 ROLE_USER 부여
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }

        // 2. 이제 하드코딩된 ROLE_USER가 아니라 실제 권한을 담은 신분증 반환
        return new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorities);
    }
}