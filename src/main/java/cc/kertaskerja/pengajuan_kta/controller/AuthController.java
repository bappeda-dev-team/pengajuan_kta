package cc.kertaskerja.pengajuan_kta.controller;

import cc.kertaskerja.pengajuan_kta.dto.ApiResponse;
import cc.kertaskerja.pengajuan_kta.dto.Auth.*;
import cc.kertaskerja.pengajuan_kta.service.auth.AuthService;
import cc.kertaskerja.pengajuan_kta.service.auth.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Endpoint Authentication")
public class AuthController {

    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/auth/send-otp")
    @Operation(summary = "[1] - Kirim OTP ke email pengguna untuk verifikasi")
    public ResponseEntity<ApiResponse<AccountResponse.SendOtp>> sendOtp(@Valid @RequestBody RegisterRequest.SendOtp request) {
        AccountResponse.SendOtp response = authService.sendOTP(request);

        return ResponseEntity.ok(ApiResponse.success(response, "OTP sent successfully! Please check your email and WhatsApp."));
    }

    @GetMapping("/auth/resend-captcha")
    @Operation(summary = "Kirim ulang captcha")
    public ResponseEntity<ApiResponse<AccountResponse.ResendCaptcha>> resendOtp() {
        AccountResponse.ResendCaptcha response = authService.resendCaptcha();

        return ResponseEntity.ok(ApiResponse.success(response, "Captcha resent successfully"));
    }

    @PostMapping("/auth/verify-otp-and-signup")
    @Operation(summary = "[2] - Daftar akun baru")
    public ResponseEntity<ApiResponse<AccountResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AccountResponse created = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(created));
    }

    @PutMapping("/verify-account/{nik}")
    @Operation(summary = "Verifikasi akun oleh ADMIN")
    public ResponseEntity<ApiResponse<AccountResponse.VerifyAccount>> verifyAccount(
          @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
          @PathVariable String nik,
          @Valid @RequestBody RegisterRequest.VerifyAccount request) {
        AccountResponse.VerifyAccount verified = authService.verifyAccount(authHeader, nik, request);

        return ResponseEntity.ok(ApiResponse.success(verified, "Account verified successfully"));
    }

    @PostMapping("/auth/login")
    @Operation(summary = "[3] - Login akun")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success(response, "Signin successfully"));
    }

    @GetMapping("/get-all-admin-account")
    @Operation(summary = "Ambil data akun Admin")
    public ResponseEntity<ApiResponse<List<AccountResponse.AdminResponse>>> getAllAdminAcc(
          @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        List<AccountResponse.AdminResponse> response = authService.listAdmin(authHeader);

        return ResponseEntity.ok(ApiResponse.success(response, "List admin account successfully"));
    }

    @PostMapping("/register/account-admin")
    @Operation(summary = "Daftar akun ADMIN / KEPALA OPD")
    public ResponseEntity<ApiResponse<AccountResponse.AdminResponse>> registerAccountAdmin(
          @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
          @Valid @RequestBody RegisterRequest.RegisterAdmin request) {
        AccountResponse.AdminResponse created = authService.registerAdmin(authHeader, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(created));
    }

    @PutMapping("/edit-admin/{nik}")
    @Operation(summary = "Edit akun ADMIN")
    public ResponseEntity<ApiResponse<AccountResponse.AdminResponse>> editAdmin(
          @PathVariable String nik,
          @Valid @RequestBody RegisterRequest.EditAdmin request) {
        AccountResponse.AdminResponse updated = authService.editAdmin(nik, request);

        return ResponseEntity.ok(ApiResponse.success(updated, "Admin account updated successfully"));
    }

    @PutMapping("/edit-user/{nik}")
    @Operation(summary = "Edit akun ADMIN")
    public ResponseEntity<ApiResponse<AccountResponse.UserResponse>> editUser(
          @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
          @PathVariable String nik,
          @Valid @RequestBody RegisterRequest.EditUser request) {
        AccountResponse.UserResponse updated = authService.editUser(authHeader, nik, request);

        return ResponseEntity.ok(ApiResponse.success(updated, "User account updated successfully"));
    }

    @GetMapping("/accounts")
    @Operation(summary = "List akun terdaftar")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> listAccount(
          @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        List<AccountResponse> response = authService.listAccount(authHeader);

        return ResponseEntity.ok(ApiResponse.success(response, "List account successfully"));
    }

    @GetMapping("/account/{nik}")
    @Operation(summary = "Lihat detail akun")
    public ResponseEntity<ApiResponse<AccountResponse.Detail>> detailAccount(
          @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
          @PathVariable String nik) {
        AccountResponse.Detail response = authService.detailAccount(authHeader, nik);

        return ResponseEntity.ok(ApiResponse.success(response, "Detail account successfully"));
    }

    @DeleteMapping("/account/{nik}")
    @Operation(summary = "Hapus akun berdasarkan NIK")
    public ResponseEntity<ApiResponse<String>> deleteAccount(
          @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
          @PathVariable String nik) {
        String result = authService.deleteAccountByNik(authHeader, nik);

        return ResponseEntity.ok(ApiResponse.success(result, "Account deleted successfully"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and blacklist token")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        String token = authHeader.substring(7);
        tokenBlacklistService.blacklist(token);

        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }

    @GetMapping("/account/profile")
    @Operation(summary = "Lihat profile")
    public ResponseEntity<ApiResponse<AccountResponse.Detail>> myDetailAccount(
          @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        AccountResponse.Detail response = authService.profile(authHeader);

        return ResponseEntity.ok(ApiResponse.success(response, "My account detail retrieved successfully"));
    }

    @PostMapping("/auth/send-otp-forgot-password")
    @Operation(summary = "Kirim OTP untuk reset password")
    public ResponseEntity<ApiResponse<String>> sendOtpResetPassword(
          @Valid @RequestBody RegisterRequest.SendOtpForgotPassword request) {
        String result = authService.sendPasswordResetPassword(request);

        return ResponseEntity.ok(ApiResponse.success(result, "OTP reset password berhasil dikirim"));
    }

    @PutMapping("/auth/reset-password")
    @Operation(summary = "Reset password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody RegisterRequest.ResetPassword request) {
        String result = authService.resetPassword(request);

        return ResponseEntity.ok(ApiResponse.success(result, "Password berhasil diubah"));
    }
}
