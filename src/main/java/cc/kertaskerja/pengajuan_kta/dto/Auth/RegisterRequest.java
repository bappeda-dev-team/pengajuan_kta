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

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Harap isi nama lengkap Anda")
    private String nama;

    @NotBlank(message = "Username tidak boleh kosong")
    private String username;

    @NotBlank(message = "Email tidak boleh kosong")
    @Email(message = "Format email tidak valid")
    private String email;

    @NotBlank(message = "Password tidak boleh kosong")
    @Pattern(
          regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$",
          message = "Password harus minimal 8 karakter, mengandung huruf kapital dan simbol"
    )
    private String password;

    @NotBlank(message = "Harap masukkan nomor whatsapp yang masih aktif")
    @Size(min = 10, max = 16, message = "Nomor HP harus antara 10 sampai 16 digit")
    @Pattern(
          regexp = "^62\\d{8,14}$",
          message = "Nomor HP harus dimulai dengan 62 dan diikuti 8–14 digit angka"
    )
    private String nomor_telepon;

//    private String otp_code;

    @NotNull(message = "Harap masukkan tipe akun yang Anda daftarkan")
    private TipeAkunEnum tipe_akun;

//    private String captcha_token;

//    private String captcha_code;

    // ====================================
    // Inner class for sending OTP
    // ====================================
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendOtp {

        @NotBlank(message = "Harap isi nama lengkap Anda")
        private String nama;

        @NotBlank(message = "Username tidak boleh kosong")
        private String username;

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
}
