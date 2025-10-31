package cc.kertaskerja.pengajuan_kta.service.otp;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;

    public String generateOtp(String email, String msisdn) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        redisTemplate.opsForValue().set("OTP:" + email, otp, Duration.ofMinutes(3));
        redisTemplate.opsForValue().set("OTP:" + msisdn, otp, Duration.ofMinutes(3));
        return otp;
    }

    public boolean validateOtp(String key, String otp) {
        String storedOtp = redisTemplate.opsForValue().get("OTP:" + key);
        return storedOtp != null && storedOtp.equals(otp);
    }

    public void deleteOtp(String key) {
        redisTemplate.delete("OTP:" + key);
    }
}
