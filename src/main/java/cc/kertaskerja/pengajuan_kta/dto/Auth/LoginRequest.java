package cc.kertaskerja.pengajuan_kta.dto.Auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String nik;

    @NotBlank
    private String password;

    private String captcha_token;

    private String captcha_code;
}
