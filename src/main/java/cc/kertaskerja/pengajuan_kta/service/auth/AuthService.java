package cc.kertaskerja.pengajuan_kta.service.auth;

import cc.kertaskerja.pengajuan_kta.dto.Auth.AccountResponse;
import cc.kertaskerja.pengajuan_kta.dto.Auth.LoginRequest;
import cc.kertaskerja.pengajuan_kta.dto.Auth.LoginResponse;
import cc.kertaskerja.pengajuan_kta.dto.Auth.RegisterRequest;

public interface AuthService {

    AccountResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    AccountResponse validateToken(String token);
}
