package cc.kertaskerja.pengajuan_kta.dto.JenisOrganisasi;

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
        @NotBlank(message = "Kode jenis organisasi wajib diisi!")
        @JsonProperty("kode_jenis_organisasi")
        private String kode_jenis_organisasi;

        @NotBlank(message = "Nama jenis organisasi wajib diisi!")
        @JsonProperty("nama_jenis_organisasi")
        private String nama_jenis_organisasi;
    }
}
