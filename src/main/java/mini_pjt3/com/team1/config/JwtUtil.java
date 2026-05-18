package mini_pjt3.com.team1.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j // 시스템 아웃 대신 로그를 사용하기 위해 추가
@Component
public class JwtUtil {

    // 배포 환경에서는 반드시 환경변수나 application.yml에 실제 키를 설정해야 합니다.
    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;
    private final long tokenExpiration = 3600000L; // 1시간

    @PostConstruct
    public void init() {
        // secretKey가 너무 짧으면 에러가 발생할 수 있으므로, 바이트 배열로 변환하여 키 생성
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * 토큰 생성
     */
    public String createToken(Authentication authentication) {
        String email;
        String name = "";

        // 권한 정보 추출
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 구글 로그인(OAuth2User)과 일반 로그인 분기 처리
        if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
        } else {
            email = authentication.getName();
            // 이름 정보가 따로 없다면 이메일 앞부분 사용
            name = email.contains("@") ? email.split("@")[0] : email;
        }

        Claims claims = Jwts.claims().setSubject(email);
        claims.put("name", name);
        claims.put("auth", authorities); 

        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + tokenExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 비어있습니다.");
        }
        return false;
    }

    /**
     * 토큰에서 사용자 이메일 추출
     */
    public String getEmail(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * 토큰에서 Authentication 객체 생성
     */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();

        Object authClaim = claims.get("auth");

        Collection<? extends GrantedAuthority> authorities;

        if (authClaim != null && !authClaim.toString().isEmpty()) {
            authorities = Arrays.stream(authClaim.toString().split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        } else {
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorities);
    }
}