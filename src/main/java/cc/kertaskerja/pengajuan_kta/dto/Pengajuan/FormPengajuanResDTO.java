package cc.kertaskerja.pengajuan_kta.dto.Pengajuan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import jakarta.validation.constraints.NotBlank;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormPengajuanResDTO {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PengajuanResponse {
        private UUID uuid;
        private String induk_organisasi;
        private String nomor_induk;
        private String jumlah_anggota;
        private String daerah;
        private String nama;
        private String tempat_lahir;
        private Date tanggal_lahir;
        private String jenis_kelamin;
        private String alamat;
        private String profesi;
        private String dibuat_di;
        private String status;
        private String keterangan;
        private List<FilePendukung> file_pendukung;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaveDataResponse {
        private UUID uuid;
        private String induk_organisasi;
        private String nomor_induk;
        private String jumlah_anggota;
        private String daerah;
        private String nama;
        private String tempat_lahir;
        private Date tanggal_lahir;
        private String jenis_kelamin;
        private String alamat;
        private String profesi;
        private String dibuat_di;
        private String status;
        private String keterangan;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilePendukung {
        @NotBlank(message = "form_uuid tidak boleh kosong!")
        private String form_uuid;

        @NotBlank(message = "Url File wajib diisi")
        private String file_url;

        @NotBlank(message = "Nama file tidak boleh kosong!")
        private String nama_file;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyData{
        private Date berlaku_dari;
        private Date berlaku_sampai;
        private String status;
        private TertandaDTO tertanda;
        private String catatan;
    }
}