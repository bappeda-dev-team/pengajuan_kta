package cc.kertaskerja.pengajuan_kta.service.auth;

import cc.kertaskerja.pengajuan_kta.dto.Auth.AccountResponse;
import cc.kertaskerja.pengajuan_kta.dto.Auth.LoginRequest;
import cc.kertaskerja.pengajuan_kta.dto.Auth.LoginResponse;
import cc.kertaskerja.pengajuan_kta.dto.Auth.RegisterRequest;
import cc.kertaskerja.pengajuan_kta.entity.Account;

public interface AuthService {

    AccountResponse.SendOtp sendOTP(RegisterRequest.SendOtp request);

    AccountResponse.ResendCaptcha resendOtp();

    AccountResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}
