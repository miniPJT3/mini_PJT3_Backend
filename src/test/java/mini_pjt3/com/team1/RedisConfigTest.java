package mini_pjt3.com.team1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RedisConfigTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    @DisplayName("Redis 연결 및 데이터 저장/조회 테스트")
    public void redisConnectionTest() {
        // Given (데이터 준비)
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        String key = "testKey";
        String value = "Hello Redis!";

        // When (데이터 저장 및 조회)
        valueOperations.set(key, value);
        String result = (String) valueOperations.get(key);

        // Then (검증)
        assertThat(result).isEqualTo(value);
        System.out.println("Redis에서 읽어온 값: " + result);
        
        // 테스트 후 데이터 삭제 (선택)
        // redisTemplate.delete(key);
    }
}