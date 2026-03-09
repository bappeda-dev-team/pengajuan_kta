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
@Table(name = "izin_operasional")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class IzinOperasional extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nik", referencedColumnName = "nik", nullable = false)
    @JsonBackReference
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_uuid", referencedColumnName = "uuid", nullable = false)
    @JsonBackReference
    private FormPengajuan formPengajuan;

    @Builder.Default
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid = UUID.randomUUID();

    @Column(name = "tanggal_pendirian", nullable = false)
    private Date tanggalPendirian;

    @Column(name = "berlaku_dari", nullable = false)
    private Date berlakuDari;

    @Column(name = "berlaku_sampai", nullable = false)
    private Date berlakuSampai;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private StatusPengajuanEnum status;

    @Column(name = "keterangan", nullable = false)
    private String keterangan;

    @Column(name = "catatan")
    private String catatan;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tertanda", columnDefinition = "jsonb", nullable = false)
    private TertandaDTO tertanda;

    @Column(name = "nomor_surat")
    private String nomorSurat;

    @Column(name = "status_tanggal")
    private LocalDateTime statusTanggal;

    @OneToMany(mappedBy = "operasional", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<FilePendukung> filePendukung;
}
