package cc.kertaskerja.pengajuan_kta.entity;

import cc.kertaskerja.pengajuan_kta.common.BaseAuditable;
import cc.kertaskerja.pengajuan_kta.dto.Pengajuan.TertandaDTO;
import cc.kertaskerja.pengajuan_kta.enums.StatusPengajuanEnum;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "surat_rekomendasi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class SuratRekomendasi extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nik", referencedColumnName = "nik", nullable = false)
    @JsonBackReference
    private Account account;

    @Builder.Default
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid = UUID.randomUUID();

    @Column(name = "nomor_surat", unique = true, nullable = false)
    private String nomorSurat;

    @Column(name = "nomor_induk", nullable = false)
    private String nomorInduk;

    @Column(name = "tujuan", nullable = false)
    private String tujuan;

    @Column(name = "tanggal", nullable = false)
    private Date tanggal;

    @Column(name = "tempat", nullable = false)
    private String tempat;

    @Column(name = "tanggal_berlaku", nullable = false)
    private Date tanggalBerlaku;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private StatusPengajuanEnum status;

    @Column(name = "keterangan")
    private String keterangan;

    @Column(name = "catatan")
    private String catatan;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tertanda", columnDefinition = "jsonb", nullable = false)
    private TertandaDTO tertanda;

    @Column(name = "tanggal_surat")
    private LocalDateTime tanggalSurat;

    @OneToMany(mappedBy = "suratRekomendasi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<FilePendukung> filePendukung;
}
