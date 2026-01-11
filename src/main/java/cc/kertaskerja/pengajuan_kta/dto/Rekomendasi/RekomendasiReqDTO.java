package cc.kertaskerja.pengajuan_kta.dto.Rekomendasi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RekomendasiReqDTO {

    @Getter
    @Setter
    public static class SaveData {
        @NotNull(message = "NIK tidak boleh kosong!")
        @JsonProperty("nik")
        private String nik;

        @NotNull(message = "Nomor surat tidak boleh kosong!")
        @JsonProperty("nomor_surat")
        private String nomor_surat;

        @NotNull(message = "Nomor induk tidak boleh kosong!")
        @JsonProperty("nomor_induk")
        private String nomor_induk;

        @NotNull(message = "Tujuan tidak boleh kosong!")
        @JsonProperty("tujuan")
        private String tujuan;

        @NotNull(message = "Tanggal tidak boleh kosong!")
        @JsonProperty("tanggal")
        private Date tanggal;

        @NotNull(message = "Tempat tidak boleh kosong!")
        @JsonProperty("tempat")
        private String tempat;

        @NotNull(message = "Tanggal berlaku wajib diisi!")
        @JsonProperty("tanggal_berlaku")
        private Date tanggal_berlaku;

        private String keterangan;
    }
}
