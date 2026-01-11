package cc.kertaskerja.pengajuan_kta.dto.Pengajuan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS) // always show fields
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormPengajuanReqDTO {

    @Getter
    @Setter
    public static class SavePengajuan {
        @NotNull(message = "NIK tidak boleh kosong!")
        @JsonProperty("nik")
        private String nik;

        @JsonProperty("induk_organisasi")
        private String induk_organisasi;

        @NotBlank(message = "Harap masukkan Nomor Induk")
        @JsonProperty("nomor_induk")
        private String nomor_induk;
        
        @JsonProperty("jumlah_anggota")
        private String jumlah_anggota;

        @NotBlank(message = "Nama daerah (kab/kota) wajib diisi")
        @JsonProperty("daerah")
        private String daerah;

        @JsonProperty("profesi")
        private String profesi;

        private String keterangan;
    }

    @Getter
    @Setter
    public static class VerifyPengajuan {
        @NotNull(message = "Tanggal berlaku dari wajib diisi")
        @JsonProperty("berlaku_dari")
        private Date berlaku_dari;

        @NotNull(message = "Tanggal berlaku sampai wajib diisi")
        @JsonProperty("berlaku_sampai")
        private Date berlaku_sampai;

        @Valid
        @NotNull(message = "Kolom tertanda tidak boleh kosong!")
        @JsonAlias("terdanda")
        private TertandaDTO tertanda;

        @NotBlank(message = "Status harap diisi!")
        @JsonProperty("status")
        private String status;

        @JsonProperty("catatan")
        private String catatan;
    }
}