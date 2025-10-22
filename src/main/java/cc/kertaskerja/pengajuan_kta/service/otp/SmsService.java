package cc.kertaskerja.pengajuan_kta.service.otp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor // ✅ hanya menyuntikkan bean non-@Value
public class SmsService {

    private final RestTemplate restTemplate;

    @Value("${whatsapp.api.url:https://api.fonnte.com/api/send}")
    private String whatsAppApiUrl;

    @Value("${whatsapp.api.token:DEFAULT_TOKEN}")
    private String whatsAppApiToken;

    public void sendOtpWhatsApp(String nomorTujuan, String otpCode, String nama) {
        try {
            String message = String.format(
                        "Halo *%s* 👋,\n\n" +
                        "Gunakan kode OTP berikut untuk memverifikasi akun Anda:\n\n" +
                        "🔹 *%s*\n\n" +
                        "Kode ini berlaku selama *3 menit*.\n" +
                        "_Jangan bagikan kode ini kepada siapa pun._ 🚫\n\n" +
                        "Terima kasih 🙏\n" +
                        "*Dinas Komunikasi, Informatika, Statistik dan Persandian Kabupaten Ngawi*\n" +
                        "📍Jl. Teuku Umar No.43, Ngawi\n" +
                        "🌐 www.kominfo.ngawikab.go.id\n" +
                        "✉️ kominfo@ngawikab.go.id",
                  nama, otpCode
            );

            Map<String, Object> body = new HashMap<>();
            body.put("target", nomorTujuan);
            body.put("message", message);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", whatsAppApiToken);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            restTemplate.exchange(
                  whatsAppApiUrl,
                  HttpMethod.POST,
                  entity,
                  String.class
            );

            log.info("Success: OTP WhatsApp sent to {}", nomorTujuan);
        } catch (Exception e) {
            log.error("Failed to send WhatsApp OTP to {} | Error: {}", nomorTujuan, e.getMessage());
            throw new RuntimeException("Gagal mengirim OTP via WhatsApp");
        }
    }
}

