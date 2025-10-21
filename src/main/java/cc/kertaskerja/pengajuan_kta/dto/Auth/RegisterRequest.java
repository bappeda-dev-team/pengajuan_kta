package cc.kertaskerja.pengajuan_kta.dto.Auth;

import cc.kertaskerja.pengajuan_kta.enums.TipeAkunEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

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

    private String otp_code;

    @NotNull(message = "Harap masukkan tipe akun yang Anda daftarkan")
    private TipeAkunEnum tipe_akun;

    public RegisterRequest() {}

    public RegisterRequest(String nama, String username, String password, String nomor_telepon,
                           TipeAkunEnum tipe_akun, String email) {
        this.nama = nama;
        this.username = username;
        this.password = password;
        this.nomor_telepon = nomor_telepon;
        this.tipe_akun = tipe_akun;
        this.email = email;
    }

    // =====================
    // Getters and Setters
    // =====================

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNomor_telepon() {
        return nomor_telepon;
    }

    public void setNomor_telepon(String nomor_telepon) {
        this.nomor_telepon = nomor_telepon;
    }

    public TipeAkunEnum getTipe_akun() {
        return tipe_akun;
    }

    public void setTipe_akun(TipeAkunEnum tipe_akun) {
        this.tipe_akun = tipe_akun;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp_code() {
        return otp_code;
    }

    public void setOtp_code(String otp_code) {
        this.otp_code = otp_code;
    }

    // =====================
    // Inner class for sending OTP
    // =====================
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

        public SendOtp() {}

        public SendOtp(String nama, String username, String email, String nomor_telepon) {
            this.nama = nama;
            this.username = username;
            this.email = email;
            this.nomor_telepon = nomor_telepon;
        }

        public String getNama() {
            return nama;
        }

        public void setNama(String nama) {
            this.nama = nama;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getNomor_telepon() {
            return nomor_telepon;
        }

        public void setNomor_telepon(String nomor_telepon) {
            this.nomor_telepon = nomor_telepon;
        }
    }
}
