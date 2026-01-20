package cc.kertaskerja.pengajuan_kta.service.auth;

import cc.kertaskerja.pengajuan_kta.dto.Auth.AccountResponse;
import cc.kertaskerja.pengajuan_kta.dto.Auth.LoginRequest;
import cc.kertaskerja.pengajuan_kta.dto.Auth.LoginResponse;
import cc.kertaskerja.pengajuan_kta.dto.Auth.RegisterRequest;
import cc.kertaskerja.pengajuan_kta.entity.Account;

import java.util.List;

public interface AuthService {

    AccountResponse.SendOtp sendOTP(RegisterRequest.SendOtp request);

    AccountResponse.ResendCaptcha resendCaptcha();

    AccountResponse register(RegisterRequest request);

    AccountResponse.VerifyAccount verifyAccount(String authHeader, String nik, RegisterRequest.VerifyAccount request);

    LoginResponse login(LoginRequest request);

    List<AccountResponse> listAccount(String authHeader);

    AccountResponse.Detail detailAccount(String authHeader, String nik);

    AccountResponse.Detail profile(String authHeader);

    String sendPasswordResetPassword(RegisterRequest.SendOtpForgotPassword dto);

    String resetPassword(RegisterRequest.ResetPassword dto);
}
