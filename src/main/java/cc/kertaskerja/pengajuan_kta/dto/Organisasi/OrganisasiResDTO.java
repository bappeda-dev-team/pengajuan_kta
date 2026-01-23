package cc.kertaskerja.pengajuan_kta.dto.Organisasi;

import cc.kertaskerja.pengajuan_kta.dto.Auth.AccountResponse;
import cc.kertaskerja.pengajuan_kta.entity.FilePendukung;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganisasiResDTO {
    private UUID uuid;
    private String bidang_keahlian;
    private String nama_ketua;
    private String nomor_telepon;
    private String alamat;
    private String status;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailResponse {
        private String bidang_keahlian;
        private String nama_ketua;
        private String nomor_telepon;
        private String alamat;
        private String status;
        private String catatan;
        private List<FilePendukung> file_pendukung;
        private LocalDateTime status_tanggal;
        private LocalDateTime created_at;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaveResponse {
        private UUID uuid;
        private String bidang_keahlian;
        private String nama_ketua;
        private String nomor_telepon;
        private String alamat;
        private String status;
        private LocalDateTime status_tanggal;
        private LocalDateTime created_at;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilePendukung {
        private Long id;
        private UUID organisasi_uuid;
        private String file_url;
        private String nama_file;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganisasiDetailWithProfileResponse {
        private OrganisasiResDTO.DetailResponse organisasi;
        private AccountResponse.Detail profile;
    }
}
