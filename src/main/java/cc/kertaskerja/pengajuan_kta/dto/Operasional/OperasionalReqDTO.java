package cc.kertaskerja.pengajuan_kta.dto.Operasional;

import cc.kertaskerja.pengajuan_kta.dto.Pengajuan.TertandaDTO;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperasionalReqDTO {

    @Getter
    @Setter
    public static class SaveData {
        @NotNull(message = "Tanggal pendirian organisasi kesenian tidak boleh kosong!")
        @JsonProperty("tanggal_pendirian")
        private Date tanggal_pendirian;

        @NotBlank(message = "NIK wajib diisi!")
        @JsonProperty("nik")
        private String nik;

        @NotNull(message = "form_uuid wajib diisi!")
        @JsonProperty("form_uuid")
        private UUID form_uuid;

        @JsonProperty("keterangan")
        private String keterangan;
    }

    @Getter
    @Setter
    public static class Verify {
        @NotBlank(message = "NIK wajib diisi!")
        @JsonProperty("nik")
        private String nik;

        @JsonProperty("nomor_surat")
        private String nomor_surat;

        @JsonProperty("berlaku_dari")
        private Date berlaku_dari;

        @JsonProperty("berlaku_sampai")
        private Date berlaku_sampai;

        @Valid
        @JsonAlias("terdanda")
        private TertandaDTO tertanda;

        @NotBlank(message = "Status harap diisi!")
        @JsonProperty("status")
        private String status;

        @JsonProperty("catatan")
        private String catatan;
    }
}
