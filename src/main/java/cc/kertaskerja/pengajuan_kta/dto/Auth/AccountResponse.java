package cc.kertaskerja.pengajuan_kta.dto.Auth;

import java.time.LocalDateTime;

public class AccountResponse {

    private Long id;
    private String username;
    private String nomor_telepon;
    private String tipe_akun;
    private String status;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // =====================
    // Constructors
    // =====================

    public AccountResponse() {
    }

    public AccountResponse(
          Long id,
          String username,
          String nomor_telepon,
          String tipe_akun,
          String status,
          String role,
          LocalDateTime createdAt,
          LocalDateTime updatedAt
    ) {
        this.id = id;
        this.username = username;
        this.nomor_telepon = nomor_telepon;
        this.tipe_akun = tipe_akun;
        this.status = status;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // =====================
    // Getters and Setters
    // =====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNomor_telepon() {
        return nomor_telepon;
    }

    public void setNomor_telepon(String nomor_telepon) {
        this.nomor_telepon = nomor_telepon;
    }

    public String getTipe_akun() {
        return tipe_akun;
    }

    public void setTipe_akun(String tipe_akun) {
        this.tipe_akun = tipe_akun;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // =====================
    // Manual Builder Pattern
    // =====================

    public static class Builder {
        private Long id;
        private String username;
        private String nomor_telepon;
        private String tipe_akun;
        private String status;
        private String role;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder nomor_telepon(String nomor_telepon) {
            this.nomor_telepon = nomor_telepon;
            return this;
        }

        public Builder tipe_akun(String tipe_akun) {
            this.tipe_akun = tipe_akun;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public AccountResponse build() {
            return new AccountResponse(
                  id,
                  username,
                  nomor_telepon,
                  tipe_akun,
                  status,
                  role,
                  createdAt,
                  updatedAt
            );
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
