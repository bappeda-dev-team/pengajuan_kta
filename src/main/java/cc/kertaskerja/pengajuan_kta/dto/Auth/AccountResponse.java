package cc.kertaskerja.pengajuan_kta.dto.Auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detail {
        private Long id;
        private String nama;
        private String email;

        @JsonProperty("nik")
        private String nik;

        @JsonProperty("nomor_telepon")
        private String nomorTelepon;

        @JsonProperty("tempat_lahir")
        private String tempatLahir;

        @JsonProperty("tanggal_lahir")
        private Date tanggalLahir;

        @JsonProperty("jenis_kelamin")
        private String jenisKelamin;

        @JsonProperty("alamat")
        private String alamat;

        @JsonProperty("tipe_akun")
        private String tipeAkun;

        @JsonProperty("nip")
        private String nip;

        @JsonProperty("pangkat")
        private String pangkat;

        @JsonProperty("jabatan")
        private String jabatan;

        private String status;

        private Boolean is_assigned;
        private String role;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterAdminResponse {
        private Long id;
        private String nama;
        private String nip;
        private String pangkat;
        private String nik;
        private String jabatan;
        private String email;
        private String role;
    }
}


