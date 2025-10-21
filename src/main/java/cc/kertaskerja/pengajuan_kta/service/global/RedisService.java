package cc.kertaskerja.pengajuan_kta.service.global;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    private static final Duration CACHE_TTL = Duration.ofMinutes(3);

    public String getOTP(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void setOTP(String key, String value) {
        redisTemplate.opsForValue().set(key, value, CACHE_TTL);
    }
}
