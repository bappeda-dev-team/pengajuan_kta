package cc.kertaskerja.pengajuan_kta.dto.Auth;

import cc.kertaskerja.pengajuan_kta.enums.TipeAkunEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Harap isi nama lengkap Anda")
    private String nama;

    @NotBlank
    @Size(min = 16, max = 16)
    @Pattern(regexp = "\\d{16}", message = "NIK must be exactly 16 digits")
    private String nik;

    @NotBlank(message = "Email tidak boleh kosong")
    @Email(message = "Format email tidak valid")
    private String email;

    @NotBlank(message = "Password tidak boleh kosong")
    private String password;

    @NotBlank(message = "Harap masukkan nomor whatsapp yang masih aktif")
    @Size(min = 10, max = 16, message = "Nomor HP harus antara 10 sampai 16 digit")
    @Pattern(
          regexp = "^62\\d{8,14}$",
          message = "Nomor HP harus dimulai dengan 62 dan diikuti 8–14 digit angka"
    )
    private String nomor_telepon;

    @NotBlank(message = "Tempat lahir wajib diisi")
    private String tempat_lahir;

    @NotNull(message = "Tanggal lahir wajib diisi")
    private Date tanggal_lahir;

    @NotBlank(message = "Alamat wajib diisi")
    private String alamat;

    @NotBlank(message = "Jenis kelamin wajib diisi")
    private String jenis_kelamin;

    private String otp_code;

    @NotNull(message = "Harap masukkan tipe akun yang Anda daftarkan")
    private TipeAkunEnum tipe_akun;

    private String captcha_token;

    private String captcha_code;

    // ====================================
    // Inner class for sending OTP
    // ====================================
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendOtp {

        @NotBlank(message = "Harap isi nama lengkap Anda")
        private String nama;

        @NotBlank(message = "NIK tidak boleh kosong")
        private String nik;

        @NotBlank(message = "Email tidak boleh kosong")
        @Email(message = "Format email tidak valid")
        private String email;

        @NotBlank(message = "Harap masukkan nomor whatsapp yang masih aktif")
        @Size(min = 10, max = 16, message = "Nomor HP harus antara 10 sampai 16 digit")
        @Pattern(
              regexp = "^62\\d{8,14}$",
              message = "Nomor HP harus dimulai dengan 62 dan diikuti 8–14 digit angka"
        )
        private String nomor_telepon;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendOtpForgotPassword {
        @NotBlank(message = "NIK tidak boleh kosong")
        private String nik;
        private String captcha_token;
        private String captcha_code;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResetPassword {
        @NotBlank(message = "NIK tidak boleh kosong!")
        private String nik;

        @NotBlank(message = "Password baru tidak boleh kosong")
        private String password;

        @NotBlank(message = "Kode OTP tidak boleh kosong")
        private String otp_code;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyAccount {
        @NotNull(message = "Status tidak boleh kosong")
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterAdmin {
        @NotBlank(message = "Nama tidak boleh kosong!")
        private String nama;

        @NotBlank(message = "NIP tidak boleh kosong!")
        private String nip;

        @NotBlank(message = "Pangkat tidak boleh kosong!")
        private String pangkat;

        @NotBlank(message = "Jabatan tidak boleh kosong!")
        private String jabatan;

        @NotBlank(message = "NIK tidak boleh kosong!")
        private String nik;

        @NotBlank(message = "Harap masukkan alamat email!")
        private String email;

        @Size(min = 10, max = 16, message = "Nomor HP harus antara 10 sampai 16 digit")
        @Pattern(
              regexp = "^62\\d{8,14}$",
              message = "Nomor HP harus dimulai dengan 62 dan diikuti 8–14 digit angka"
        )
        private String nomor_telepon;

        private String password;

        @NotBlank(message = "Role harus diisi!")
        private String role;
    }
}
