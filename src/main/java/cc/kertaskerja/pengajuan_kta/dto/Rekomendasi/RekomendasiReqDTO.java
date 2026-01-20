package cc.kertaskerja.pengajuan_kta.dto.Rekomendasi;

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

        private String keterangan;
    }

    @Getter
    @Setter
    public static class Verify {
        @JsonProperty("nomor_surat")
        private String nomor_surat;

        @Valid
        @JsonAlias("terdanda")
        private TertandaDTO tertanda;

        @NotBlank(message = "Status harap diisi!")
        @JsonProperty("status")
        private String status;

        @JsonProperty("tanggal_berlaku")
        private Date tanggal_berlaku;

        @JsonProperty("catatan")
        private String catatan;
    }
}
