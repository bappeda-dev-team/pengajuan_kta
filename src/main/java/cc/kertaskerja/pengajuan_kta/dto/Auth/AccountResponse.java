package cc.kertaskerja.pengajuan_kta.dto.Auth;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AccountResponse {
    private Long id;
    private String username;
    private String nomor_telepon;
    private String tipe_akun;
    private String status;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
