package cc.kertaskerja.pengajuan_kta.dto.Auth;

public class LoginResponse {

    private String access_token;
    private Profile profile;

    public LoginResponse() {}

    public LoginResponse(String access_token, Profile profile) {
        this.access_token = access_token;
        this.profile = profile;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public static class Profile {
        private String id;
        private String nama;
        private String email;
        private String username;
        private String nomor_telepon;
        private String tipe_akun;
        private String status;
        private String role;

        public Profile() {}

        public Profile(String id, String nama, String email, String username,
                       String nomor_telepon, String tipe_akun, String status, String role) {
            this.id = id;
            this.nama = nama;
            this.email = email;
            this.username = username;
            this.nomor_telepon = nomor_telepon;
            this.tipe_akun = tipe_akun;
            this.status = status;
            this.role = role;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
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
    }
}
