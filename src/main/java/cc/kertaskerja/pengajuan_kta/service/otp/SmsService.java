package cc.kertaskerja.pengajuan_kta.service.otp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Slf4j
@Service
@RequiredArgsConstructor
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
                        "Kode ini berlaku selama *10 menit*.\n" +
                        "_Jangan bagikan kode ini kepada siapa pun._ 🚫\n\n" +
                        "Terima kasih 🙏\n" +
                        "*Dinas Komunikasi, Informatika, Statistik dan Persandian Kabupaten Ngawi*\n" +
                        "📍Jl. Teuku Umar No.43, Ngawi\n" +
                        "🌐 www.kominfo.ngawikab.go.id\n" +
                        "✉️ kominfo@ngawikab.go.id",
                  nama, otpCode
            );

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("target", nomorTujuan);
            body.add("message", message);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", whatsAppApiToken);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> resp = restTemplate.exchange(
                  whatsAppApiUrl,
                  HttpMethod.POST,
                  entity,
                  String.class
            );

            log.info("WhatsApp send response: status={} body={}", resp.getStatusCode(), resp.getBody());
        } catch (Exception e) {
            log.error("Failed to send WhatsApp OTP to {} | Error: {}", nomorTujuan, e.getMessage(), e);
            throw new RuntimeException("Gagal mengirim OTP via WhatsApp");
        }
    }
}

