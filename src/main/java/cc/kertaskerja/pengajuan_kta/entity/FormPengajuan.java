package cc.kertaskerja.pengajuan_kta.entity;

import cc.kertaskerja.pengajuan_kta.dto.TertandaDTO;
import cc.kertaskerja.pengajuan_kta.enums.StatusEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
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
public class FormPengajuan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    @Column(name = "tertanda", columnDefinition = "jsonb", nullable = false)
    private TertandaDTO tertanda;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private StatusEnum status;

    @Column(name = "keterangan", nullable = false)
    private String keterangan;

    // Changed from @OneToOne to @OneToMany to match the @ManyToOne in FilePendukung
    @OneToMany(mappedBy = "formPengajuan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<FilePendukung> filePendukung;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}