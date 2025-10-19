package cc.kertaskerja.pengajuan_kta.dto.Auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenRequest {
    @NotBlank(message = "Access token tidak boleh kosong")
    @JsonProperty("access_token")
    private String accessToken;
}