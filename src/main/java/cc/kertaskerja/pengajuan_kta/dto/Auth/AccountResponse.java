package cc.kertaskerja.pengajuan_kta.dto.Auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private Long id;
    private String nama;
    private String email;

    @JsonProperty("nik")
    private String nik;

    @JsonProperty("nomor_telepon")
    private String nomorTelepon;

    @JsonProperty("tipe_akun")
    private String tipeAkun;

    private String status;
    private String role;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendOtp {
        private String nama;
        private String email;

        @JsonProperty("nomor_telepon")
        private String nomorTelepon;

        private CaptchaResponse captcha;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class CaptchaResponse {
            private String key;
            private String img;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResendCaptcha {
        private CaptchaResponse captcha;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class CaptchaResponse {
            private String key;
            private String img;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyAccount {
        private String nama;
        private String email;

        @JsonProperty("nik")
        private String nik;

        @JsonProperty("nomor_telepon")
        private String nomorTelepon;

        @JsonProperty("tipe_akun")
        private String tipeAkun;

        @JsonProperty("role")
        private String role;

        private String status;
    }
}


