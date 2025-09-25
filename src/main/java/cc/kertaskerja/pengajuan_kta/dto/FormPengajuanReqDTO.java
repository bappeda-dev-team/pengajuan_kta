package cc.kertaskerja.pengajuan_kta.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS) // always show fields
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormPengajuanReqDTO {
    @NotBlank(message = "Nama Induk Organisasi harus diisi")
    @JsonProperty("induk_organisasi")
    private String induk_organisasi;

    @NotBlank(message = "Harap masukkan Nomor Induk")
    @JsonProperty("nomor_induk")
    private String nomor_induk;

    @NotBlank(message = "Masukkan jumlah anggota")
    @JsonProperty("jumlah_anggota")
    private String jumlah_anggota;

    @NotBlank(message = "Nama daerah (kab/kota) wajib diisi")
    @JsonProperty("daerah")
    private String daerah;

    @NotNull(message = "Tanggal berlaku dari wajib diisi")
    @JsonProperty("berlaku_dari")
    private Date berlaku_dari;

    @NotNull(message = "Tanggal berlaku sampai wajib diisi")
    @JsonProperty("berlaku_sampai")
    private Date berlaku_sampai;

    @NotBlank(message = "Nama wajib diisi")
    @JsonProperty("nama")
    private String nama;

    @NotBlank(message = "Wajib mengisi tanggal lahir")
    @JsonProperty("tanggal_lahir")
    private String tanggal_lahir;

    @NotBlank(message = "Wajib mengisi jenis kelamin")
    @JsonProperty("jenis_kelamin")
    private String jenis_kelamin;

    @NotBlank(message = "Alamat wajib diisi")
    @JsonProperty("alamat")
    private String alamat;

    @NotBlank(message = "Profesi tidak boleh kosong")
    @JsonProperty("profesi")
    private String profesi;

    @NotBlank(message = "Wajib mengisi tempat daerah dibuat")
    @JsonProperty("dibuat_di")
    private String dibuat_di;

    @Valid
    @NotNull(message = "Pegawai tidak boleh kosong!")
    private TertandaDTO tertanda;

    private String keterangan;
}
