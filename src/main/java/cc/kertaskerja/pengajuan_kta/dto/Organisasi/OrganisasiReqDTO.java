package cc.kertaskerja.pengajuan_kta.dto.Organisasi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class OrganisasiReqDTO {

    @Getter
    @Setter
    public static class SaveData {
        @NotBlank(message = "Bidang keahlian tidak boleh kosong!")
        @JsonProperty("bidang_keahlian")
        private String bidang_keahlian;

        @NotBlank(message = "Nama ketua organisasi harap diisi!")
        @JsonProperty("nama_ketua")
        private String nama_ketua;

        @NotBlank(message = "Nomor telepon tidak boleh kosong!")
        @JsonProperty("nomor_telepon")
        private String nomor_telepon;

        @NotBlank(message = "Alamat harap diisi!")
        @JsonProperty("alamat")
        private String alamat;

        @NotNull(message = "NIK tidak boleh kosong!")
        @JsonProperty("nik")
        private String nik;
    }

    @Getter
    @Setter
    public static class UploadFilePendukung {
        @NotBlank(message = "uuid tidak boleh kosong")
        private String organisasi_uuid;

        @NotBlank(message = "Url File wajib diisi")
        private String file_url;

        @NotBlank(message = "Nama file tidak boleh kosong!")
        private String nama_file;
    }
}
