package cc.kertaskerja.pengajuan_kta.dto.JenisSeni;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JenisReqDTO {

    @Getter
    @Setter
    public static class SaveData {
        @NotBlank(message = "Kode jenis seni wajib diisi!")
        @JsonProperty("kode_jenis_seni")
        private String kode_jenis_seni;

        @NotBlank(message = "Nama jenis seni wajib diisi!")
        @JsonProperty("jenis_seni")
        private String jenis_seni;
    }
}
