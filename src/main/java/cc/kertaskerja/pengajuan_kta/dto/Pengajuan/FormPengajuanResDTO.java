package cc.kertaskerja.pengajuan_kta.dto.Pengajuan;

import cc.kertaskerja.pengajuan_kta.dto.Auth.AccountResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

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
public class FormPengajuanResDTO {
    private UUID uuid;
    private String nama;
    private String nik;
    private String email;
    private String profesi;
    private String tipe_akun;
    private String induk_organisasi;
    private String bidang_keahlian;
    private String nomor_induk;
    private String nama_ketua;
    private String nik_ketua;
    private String nomor_telepon;
    private String jumlah_anggota;
    private String status;
    private LocalDateTime created_at;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PengajuanResponse {
        private UUID uuid;
        private String profesi;
        private String induk_organisasi;
        private String bidang_keahlian;
        private String nomor_induk;
        private String alamat;
        private String nama_ketua;
        private String nik_ketua;
        private String nomor_telepon;
        private String jumlah_anggota;
        private String daerah;
        private Date berlaku_dari;
        private Date berlaku_sampai;
        private String keterangan;
        private String status;
        private String catatan;
        private String tambahan;
        private TertandaDTO tertanda;
        private List<FilePendukung> file_pendukung;
        private List<FileOrganisasi> file_organisasi;
        private LocalDateTime status_tanggal;
        private LocalDateTime created_at;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaveDataResponse {
        private UUID uuid;
        private String nomor_induk;
        private String nama_ketua;
        private String nik_ketua;
        private String nomor_telepon;
        private Integer jumlah_anggota;
        private String daerah;
        private String profesi;
        private String status;
        private String keterangan;
        private String tambahan;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilePendukung {
        private Long id;
        private String form_uuid;
        private String file_url;
        private String nama_file;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileOrganisasi {
        private Long id;
        private String organisasi_uuid;
        private String file_url;
        private String nama_file;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyData{
        private String nomor_induk;
        private Date berlaku_dari;
        private Date berlaku_sampai;
        private String status;
        private TertandaDTO tertanda;
        private String catatan;
        private LocalDateTime status_tanggal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PengajuanWithProfileResponse {
        private PengajuanResponse pengajuan;
        private AccountResponse.Detail profile;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PengajuanBulananResponse {
        private String month;
        private Long total;
    }
}