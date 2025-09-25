package cc.kertaskerja.pengajuan_kta.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS) // always show fields
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormPengajuanResDTO {
    private UUID uuid;
    private String induk_organisasi;
    private String nomor_induk;
    private String jumlah_anggota;
    private String daerah;
    private Date berlaku_dari;
    private Date berlaku_sampai;
    private String nama;
    private String tanggal_lahir;
    private String jenis_kelamin;
    private String alamat;
    private String profesi;
    private String dibuat_di;
    private TertandaDTO tertanda;
    private String status;
    private String keterangan;

    private List<FilePendukung> file_pendukung;

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
}