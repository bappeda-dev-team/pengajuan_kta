package cc.kertaskerja.pengajuan_kta.dto.Rekomendasi;

import cc.kertaskerja.pengajuan_kta.dto.Auth.AccountResponse;
import cc.kertaskerja.pengajuan_kta.dto.Pengajuan.TertandaDTO;
import cc.kertaskerja.pengajuan_kta.entity.FilePendukung;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
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
public class RekomendasiResDTO {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RekomendasiResponse {
        private UUID uuid;

        @JsonProperty("nama")
        private String nama;

        private String nomor_surat;
        private String nomor_induk;
        private String tujuan;
        private Date tanggal;
        private String tempat;
        private Date tanggal_berlaku;
        private LocalDateTime tanggal_surat;
        private String status;
        private TertandaDTO tertanda;
        private String keterangan;
        private List<FilePendukung> file_pendukung;
        private LocalDateTime created_at;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RekomendasiWithProfileResponse {
        private RekomendasiResponse rekomendasi;
        private AccountResponse.Detail profile;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaveDataResponse {
        private UUID uuid;
        private String nomor_induk;
        private String tujuan;
        private Date tanggal;
        private String tempat;
        private String status;
        private String keterangan;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilePendukung {
        private Long id;
        private String rekom_uuid;
        private String file_url;
        private String nama_file;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyData{
        private String nomor_surat;
        private String nomor_induk;
        private Date tanggal_berlaku;
        private LocalDateTime tanggal_surat;
        private String status;
        private TertandaDTO tertanda;
        private String catatan;
    }
}
