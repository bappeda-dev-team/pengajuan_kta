package cc.kertaskerja.pengajuan_kta.entity;

import cc.kertaskerja.pengajuan_kta.dto.DokumenPendukungDTO;
import cc.kertaskerja.pengajuan_kta.dto.TertandaDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "form_pengajuan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormPengajuan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "induk_organisasi", nullable = false)
    private String indukOrganisasi;

    @Column(name = "nomor_induk", length = 100, nullable = false)
    private String nomorInduk;

    @Column(name = "jumlah_anggota", nullable = false)
    private Integer jumlahAnggota;

    @Column(name = "daerah", length = 50, nullable = false)
    private String daerah;

    @Column(name = "berlaku_dari", nullable = false)
    private Date berlakuDari;

    @Column(name = "berlaku_sampai", nullable = false)
    private Date berlakuSampai;

    @Column(name = "nama", length = 100, nullable = false)
    private String nama;

    @Column(name = "tanggal_lahir", length = 100, nullable = false)
    private String tanggalLahir;

    @Column(name = "jenis_kelamin", length = 100, nullable = false)
    private String jenisKelamin;

    @Column(name = "alamat", columnDefinition = "TEXT")
    private String alamat;

    @Column(name = "profesi", nullable = false)
    private String profesi;

    @Column(name = "dibuat_di", length = 50, nullable = false)
    private String dibuatDi;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dokumen_pendukung", columnDefinition = "jsonb")
    private List<DokumenPendukungDTO> dokumenPendukung;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tertanda", columnDefinition = "jsonb", nullable = false)
    private TertandaDTO tertanda;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "keterangan", nullable = false)
    private String keterangan;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}