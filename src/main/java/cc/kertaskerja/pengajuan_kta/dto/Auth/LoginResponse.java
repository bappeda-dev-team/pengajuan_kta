package cc.kertaskerja.pengajuan_kta.dto.Auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String access_token;
    private Profile profile;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Profile {
        private String id;
        private String nama;
        private String email;
        private String nik;
        private String nomor_telepon;
        private String tipe_akun;
        private String status;
        private String role;
    }
}