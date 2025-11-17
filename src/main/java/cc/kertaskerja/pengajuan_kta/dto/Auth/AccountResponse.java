package cc.kertaskerja.pengajuan_kta.dto.Auth;

import java.time.LocalDateTime;

public class AccountResponse {

    private Long id;
    private String nama;
    private String email;
    private String username;
    private String nomor_telepon;
    private String tipe_akun;
    private String status;
    private String role;

    // =====================
    // Constructors
    // =====================

    public AccountResponse() {
    }

    public AccountResponse(
          Long id,
          String nama,
          String email,
          String username,
          String nomor_telepon,
          String tipe_akun,
          String status,
          String role
    ) {
        this.id = id;
        this.nama = nama;
        this.email = email;
        this.username = username;
        this.nomor_telepon = nomor_telepon;
        this.tipe_akun = tipe_akun;
        this.status = status;
        this.role = role;
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

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    // =====================
    // Manual Builder Pattern
    // =====================

    public static class Builder {
        private Long id;
        private String nama;
        private String email;
        private String username;
        private String nomor_telepon;
        private String tipe_akun;
        private String status;
        private String role;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder nama(String nama) { this.nama = nama; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder nomor_telepon(String nomor_telepon) { this.nomor_telepon = nomor_telepon; return this; }
        public Builder tipe_akun(String tipe_akun) { this.tipe_akun = tipe_akun; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder role(String role) { this.role = role; return this; }

        public AccountResponse build() {
            return new AccountResponse(
                  id, nama, email, username, nomor_telepon, tipe_akun, status, role
            );
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // =====================
    // Inner class for Send OTP
    // =====================
    public static class SendOtp {

        private String nama;
        private String email;
        private String nomor_telepon;
        private CaptchaResponse captcha;

        public SendOtp() {}

        public SendOtp(String nama, String email, String nomor_telepon, CaptchaResponse captcha) {
            this.nama = nama;
            this.email = email;
            this.nomor_telepon = nomor_telepon;
            this.captcha = captcha;
        }

        public String getNama() { return nama; }
        public void setNama(String nama) { this.nama = nama; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getNomor_telepon() { return nomor_telepon; }
        public void setNomor_telepon(String nomor_telepon) { this.nomor_telepon = nomor_telepon; }

        public CaptchaResponse getCaptcha() {
            return captcha;
        }

        public void setCaptcha(CaptchaResponse captcha) {
            this.captcha = captcha;
        }

        public static class CaptchaResponse {
            private String key;
            private String img;

            public CaptchaResponse() {}

            public CaptchaResponse(String key, String img) {
                this.key = key;
                this.img = img;
            }

            public String getKey() {
                return key;
            }

            public void setKey(String key) {
                this.key = key;
            }

            public String getImg() {
                return img;
            }

            public void setImg(String img) {
                this.img = img;
            }
        }
    }

    // Resend Captcha
    public static class ResendCaptcha {
        private CaptchaResponse captcha;

        public ResendCaptcha() {}

        public ResendCaptcha(CaptchaResponse captcha) {
            this.captcha = captcha;
        }

        public CaptchaResponse getCaptcha() {
            return captcha;
        }

        public void setCaptcha(CaptchaResponse captcha) {
            this.captcha = captcha;
        }

        public static class CaptchaResponse {
            private String key;
            private String img;

            public CaptchaResponse() {}

            public CaptchaResponse(String key, String img) {
                this.key = key;
                this.img = img;
            }

            public String getKey() {
                return key;
            }

            public void setKey(String key) {
                this.key = key;
            }

            public String getImg() {
                return img;
            }

            public void setImg(String img) {
                this.img = img;
            }
        }
    }
}


