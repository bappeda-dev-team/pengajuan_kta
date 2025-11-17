package cc.kertaskerja.pengajuan_kta.service.captcha;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

@Service
public class CaptchaService {

    private final StringRedisTemplate redisTemplate;

    public CaptchaService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final Duration CAPTCHA_TTL = Duration.ofSeconds(60);
    private static final String CAPTCHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    public String generateCaptchaText(int length) {
        Random random = new Random();
        StringBuilder captcha = new StringBuilder();
        for (int i = 0; i < length; i++) {
            captcha.append(CAPTCHA_CHARS.charAt(random.nextInt(CAPTCHA_CHARS.length())));
        }
        return captcha.toString();
    }

    // 🔹 Generate CAPTCHA key (UUID)
    public String generateCaptchaKey() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid.substring(0, 12);
    }

    // 🔹 Save captcha text ke Redis
    public void saveCaptcha(String key, String text) {
        redisTemplate.opsForValue().set("captcha:" + key, text, CAPTCHA_TTL);
    }

    public String generateCaptchaImage(String captchaText) {
        int width = 180;
        int height = 60;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        Random rand = new Random();

        // Background warna terang
        g2d.setColor(new Color(240, 240, 240));
        g2d.fillRect(0, 0, width, height);

        // Tambahkan titik-titik warna random (noise)
        for (int i = 0; i < 200; i++) {
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            int r = rand.nextInt(255);
            int g = rand.nextInt(255);
            int b = rand.nextInt(255);
            g2d.setColor(new Color(r, g, b));
            g2d.fillRect(x, y, 1, 1);
        }

        // Garis noise (opsional)
        for (int i = 0; i < 8; i++) {
            int x1 = rand.nextInt(width);
            int y1 = rand.nextInt(height);
            int x2 = rand.nextInt(width);
            int y2 = rand.nextInt(height);
            g2d.setColor(new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Gambar teks CAPTCHA dengan posisi dan warna acak
        Font[] fonts = {
              new Font("Arial", Font.BOLD, 32),
              new Font("Verdana", Font.BOLD, 34),
              new Font("Georgia", Font.BOLD, 30),
              new Font("Tahoma", Font.BOLD, 36)
        };

        int x = 15;
        for (char c : captchaText.toCharArray()) {
            g2d.setFont(fonts[rand.nextInt(fonts.length)]);

            // Warna acak untuk setiap huruf
            g2d.setColor(new Color(rand.nextInt(150), rand.nextInt(150), rand.nextInt(150)));

            // Rotasi kecil (antara -25 sampai 25 derajat)
            double angle = Math.toRadians(rand.nextInt(50) - 25);
            g2d.rotate(angle, x, 40);

            // Gambar huruf
            g2d.drawString(String.valueOf(c), x, 40);

            // Reset rotasi
            g2d.rotate(-angle, x, 40);

            // Geser posisi X
            x += 30 + rand.nextInt(5);
        }

        g2d.dispose();

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CAPTCHA image", e);
        }
    }


    public String generateCaptcha() {
        String text = generateCaptchaText(5);
        String key = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("captcha:" + key, text, CAPTCHA_TTL);
        return key;
    }

    public boolean verifyCaptcha(String key, String userInput) {
        String stored = redisTemplate.opsForValue().get("captcha:" + key);
        return stored != null && stored.equalsIgnoreCase(userInput);
    }
}
