package cc.kertaskerja.pengajuan_kta.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormPengajuanDTO {
    private Long id;

    @NotBlank(message = "Nama Induk Organisasi harus diisi")
    private String induk_organisasi;

    @NotBlank(message = "Harap masukkan Nomor Induk")
    private String nomor_induk;

    @NotBlank(message = "Masukkan jumlah anggota")
    private String jumlah_anggota;

    @NotBlank(message = "Nama daerah (kab/kota) wajib diisi")
    private String daerah;

    @NotNull(message = "Tanggal berlaku dari wajib diisi")
    private Date berlaku_dari;

    @NotNull(message = "Tanggal berlaku sampai wajib diisi")
    private Date berlaku_sampai;

    @NotBlank(message = "Nama wajib diisi")
    private String nama;

    @NotBlank(message = "Wajib mengisi tanggal lahir")
    private String tanggal_lahir;

    @NotBlank(message = "Wajib mengisi jenis kelamin")
    private String jenis_kelamin;

    @NotBlank(message = "Alamat wajib diisi")
    private String alamat;

    @NotBlank(message = "Profesi tidak boleh kosong")
    private String profesi;

    @NotBlank(message = "Wajib mengisi tempat daerah dibuat")
    private String dibuat_di;

    private List<DokumenPendukungDTO> dokumen_pendukung;

    @Valid
    @NotNull(message = "Pegawai tidak boleh kosong!")
    private TertandaDTO tertanda;

    private String status;

    private String keterangan;
}