package cc.kertaskerja.pengajuan_kta.entity;

import cc.kertaskerja.pengajuan_kta.common.BaseAuditable;
import cc.kertaskerja.pengajuan_kta.enums.StatusAccountEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account extends BaseAuditable {

    @Id
    private Long id;

    @Column(name = "nama", nullable = false)
    private String nama;

    @Column(name = "nik", nullable = false, unique = true, length = 16)
    private String nik;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nomor_telepon", nullable = false)
    private String nomorTelepon;

    @Column(name = "tipe_akun", nullable = false)
    private String tipeAkun;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusAccountEnum status;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "tempat_lahir")
    private String tempatLahir;

    @Temporal(TemporalType.DATE)
    @Column(name = "tanggal_lahir")
    private Date tanggalLahir;

    @Column(name = "alamat")
    private String alamat;

    @Column(name = "jenis_kelamin")
    private String jenisKelamin;

    @Column(name = "is_assigned")
    private Boolean isAssigned;

    @Column(name = "nip")
    private String nip;

    @Column(name = "pangkat")
    private String pangkat;

    @Column(name = "jabatan")
    private String jabatan;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FormPengajuan> formPengajuan;
}
