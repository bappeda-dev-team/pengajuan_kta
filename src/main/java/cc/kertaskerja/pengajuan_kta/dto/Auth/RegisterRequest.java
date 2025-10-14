package cc.kertaskerja.pengajuan_kta.dto.Auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterRequest {

    @JsonProperty("username")
    @NotBlank(message = "Username tidak boleh kosong")
    private String username;

    @NotBlank(message = "Password tidak boleh kosong")
    @JsonProperty("password")
    private String password;

    @NotBlank(message = "Harap masukkan nomor whatsapp yang masih aktif")
    @Size(min = 10, max = 16, message = "Nomor HP harus antara 10 sampai 16 digit")
    @JsonProperty("nomor_telepon")
    private String nomor_telepon;

    @NotBlank(message = "Harap masukkan tipe akun yang Anda daftarkan")
    @JsonProperty("tipe_akun")
    private String tipe_akun;
}
