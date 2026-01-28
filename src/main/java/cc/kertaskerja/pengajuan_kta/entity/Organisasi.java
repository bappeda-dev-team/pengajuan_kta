package cc.kertaskerja.pengajuan_kta.entity;

import cc.kertaskerja.pengajuan_kta.common.BaseAuditable;
import cc.kertaskerja.pengajuan_kta.enums.StatusPengajuanEnum;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "organisasi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class Organisasi extends BaseAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid = UUID.randomUUID();

    // Field lain sesuai gambar (nama_ketua, bidang_keahlian, dll)...
    @Column(name = "alamat")
    private String alamat;

    @Column(name = "bidang_keahlian")
    private String bidangKeahlian;
    // --- RELASI KE CHILDREN ---

    @OneToMany(mappedBy = "organisasi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<FormPengajuan> formPengajuan;

    @OneToMany(mappedBy = "organisasi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<SuratRekomendasi> suratRekomendasi;

    @OneToMany(mappedBy = "organisasi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<FilePendukung> filePendukung;
}