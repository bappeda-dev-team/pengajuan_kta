package cc.kertaskerja.pengajuan_kta.dto.Auth;

import cc.kertaskerja.pengajuan_kta.enums.TipeAkunEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Username tidak boleh kosong")
    private String username;

    @NotBlank(message = "Password tidak boleh kosong")
    @Pattern(
          regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$",
          message = "Password harus minimal 8 karakter, mengandung huruf kapital dan simbol"
    )
    private String password;

    @NotBlank(message = "Harap masukkan nomor whatsapp yang masih aktif")
    @Size(min = 10, max = 16, message = "Nomor HP harus antara 10 sampai 16 digit")
    @Pattern(regexp = "^62\\d{8,14}$", message = "Nomor HP harus dimulai dengan 62 dan diikuti 8–14 digit angka")
    private String nomor_telepon;

    @NotNull(message = "Harap masukkan tipe akun yang Anda daftarkan")
    private TipeAkunEnum tipe_akun;

    public RegisterRequest() {}

    public RegisterRequest(String username, String password, String nomor_telepon, TipeAkunEnum tipe_akun) {
        this.username = username;
        this.password = password;
        this.nomor_telepon = nomor_telepon;
        this.tipe_akun = tipe_akun;
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
}
