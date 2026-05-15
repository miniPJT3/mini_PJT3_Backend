package mini_pjt3.com.team1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host:127.0.0.1}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    /**
     * Redis 연결을 위한 ConnectionFactory 설정
     * Lettuce 라이브러리를 사용하여 비동기로 Redis에 연결합니다.
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    /**
     * RedisTemplate 설정: 데이터를 저장하고 조회할 때 사용하는 메인 템플릿
     * 직렬화(Serializer)를 설정하지 않으면 Redis 데스크탑 매니저 등에서 
     * 데이터가 깨져 보이므로 반드시 설정해주는 것이 좋습니다.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        
        // Redis 연결 팩토리 설정
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        // Key는 일반적인 String으로 직렬화
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // Value는 JSON 형태로 직렬화하여 저장 (객체 저장 시 유용)
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return redisTemplate;
    }
}