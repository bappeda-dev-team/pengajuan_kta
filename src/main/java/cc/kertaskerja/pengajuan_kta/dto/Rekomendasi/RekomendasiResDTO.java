package cc.kertaskerja.pengajuan_kta.dto.Rekomendasi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RekomendasiResDTO {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaveDataResponse {
        private UUID uuid;
        private String nomor_surat;
        private String nomor_induk;
        private String tujuan;
        private Date tanggal;
        private String tempat;
        private Date tanggal_berlaku;
        private String status;
        private String keterangan;
    }
}
