package cc.kertaskerja.pengajuan_kta.dto.Operasional;

import cc.kertaskerja.pengajuan_kta.dto.Auth.AccountResponse;
import cc.kertaskerja.pengajuan_kta.dto.Pengajuan.FormPengajuanResDTO;
import cc.kertaskerja.pengajuan_kta.dto.Pengajuan.TertandaDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperasionalResDTO {
    private UUID uuid;
    private String nama;
    private String nik;
    private String email;
    private String bidang_keahlian;
    private String nomor_induk;
    private String status;
    private LocalDateTime created_at;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailResponse {
        private UUID uuid;
        private AccountResponse.Detail profile;
        private String induk_organisasi;
        private String bidang_keahlian;
        private String nomor_induk;
        private String alamat;
        private String nama_ketua;
        private String nik_ketua;
        private String nomor_telepon;
        private Integer jumlah_anggota;
        private Date tanggal_pendirian;
        private Date berlaku_dari;
        private Date berlaku_sampai;
        private String keterangan;
        private String status;
        private String nomor_surat;
        private String catatan;
        private TertandaDTO tertanda;
        private List<FilePendukung> file_pendukung;
        private LocalDateTime status_tanggal;
        private LocalDateTime created_at;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilePendukung {
        private Long id;
        private String operasional_uuid;
        private String file_url;
        private String nama_file;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyData{
        private String nomor_surat;
        private Date berlaku_dari;
        private Date berlaku_sampai;
        private String status;
        private TertandaDTO tertanda;
        private String catatan;
        private LocalDateTime status_tanggal;
    }

}
