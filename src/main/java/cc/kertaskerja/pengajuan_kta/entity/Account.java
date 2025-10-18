package cc.kertaskerja.pengajuan_kta.entity;

import cc.kertaskerja.pengajuan_kta.common.BaseAuditable;
import cc.kertaskerja.pengajuan_kta.enums.StatusEnum;
import jakarta.persistence.*;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "account")
public class Account extends BaseAuditable {

    @Id
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nomor_telepon", nullable = false)
    private String nomorTelepon;

    @Column(name = "tipe_akun", nullable = false)
    private String tipeAkun;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusEnum status;

    @Column(name = "role", nullable = false)
    private String role;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    //@JsonBackReference
    private List<FormPengajuan> formPengajuan;

    // =====================
    // Constructors
    // =====================

    public Account() {}

    public Account(Long id, String username, String password, String nomorTelepon,
                   String tipeAkun, StatusEnum status, String role,
                   List<FormPengajuan> formPengajuan) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nomorTelepon = nomorTelepon;
        this.tipeAkun = tipeAkun;
        this.status = status;
        this.role = role;
        this.formPengajuan = formPengajuan;
    }

    // =====================
    // Builder Pattern
    // =====================

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String username;
        private String password;
        private String nomorTelepon;
        private String tipeAkun;
        private StatusEnum status;
        private String role;
        private List<FormPengajuan> formPengajuan;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder nomorTelepon(String nomorTelepon) {
            this.nomorTelepon = nomorTelepon;
            return this;
        }

        public Builder tipeAkun(String tipeAkun) {
            this.tipeAkun = tipeAkun;
            return this;
        }

        public Builder status(StatusEnum status) {
            this.status = status;
            return this;
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public Builder formPengajuan(List<FormPengajuan> formPengajuan) {
            this.formPengajuan = formPengajuan;
            return this;
        }

        public Account build() {
            return new Account(id, username, password, nomorTelepon, tipeAkun, status, role, formPengajuan);
        }
    }

    // =====================
    // Getters and Setters
    // =====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNomorTelepon() { return nomorTelepon; }
    public void setNomorTelepon(String nomorTelepon) { this.nomorTelepon = nomorTelepon; }

    public String getTipeAkun() { return tipeAkun; }
    public void setTipeAkun(String tipeAkun) { this.tipeAkun = tipeAkun; }

    public StatusEnum getStatus() { return status; }
    public void setStatus(StatusEnum status) { this.status = status; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public List<FormPengajuan> getFormPengajuan() { return formPengajuan; }
    public void setFormPengajuan(List<FormPengajuan> formPengajuan) { this.formPengajuan = formPengajuan; }

    // =====================
    // equals & hashCode
    // =====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        if (!super.equals(o)) return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id) &&
              Objects.equals(username, account.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, username);
    }

    // =====================
    // toString
    // =====================

    @Override
    public String toString() {
        return "Account{" +
              "id=" + id +
              ", username='" + username + '\'' +
              ", nomorTelepon='" + nomorTelepon + '\'' +
              ", tipeAkun='" + tipeAkun + '\'' +
              ", status=" + status +
              ", role='" + role + '\'' +
              '}';
    }
}


