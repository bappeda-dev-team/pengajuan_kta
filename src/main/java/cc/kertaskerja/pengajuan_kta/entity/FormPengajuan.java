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
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "form_pengajuan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class FormPengajuan extends BaseAuditable {
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

    @Column(name = "profesi", nullable = false)
    private String profesi;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private StatusPengajuanEnum status;

    @Column(name = "keterangan", nullable = false)
    private String keterangan;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tertanda", columnDefinition = "jsonb", nullable = false)
    private TertandaDTO tertanda;

    @Column(name = "catatan")
    private String catatan;

    @OneToMany(mappedBy = "formPengajuan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<FilePendukung> filePendukung;
}
