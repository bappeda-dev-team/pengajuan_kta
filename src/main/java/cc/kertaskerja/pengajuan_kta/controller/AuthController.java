package cc.kertaskerja.pengajuan_kta.controller;

import cc.kertaskerja.pengajuan_kta.dto.ApiResponse;
import cc.kertaskerja.pengajuan_kta.dto.Auth.*;
import cc.kertaskerja.pengajuan_kta.exception.BadRequestException;
import cc.kertaskerja.pengajuan_kta.exception.ConflictException;
import cc.kertaskerja.pengajuan_kta.exception.UnauthenticationException;
import cc.kertaskerja.pengajuan_kta.security.JwtTokenProvider;
import cc.kertaskerja.pengajuan_kta.service.auth.AuthService;
import cc.kertaskerja.pengajuan_kta.service.auth.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Endpoint Authentication")
public class AuthController {

    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/send-otp")
    @Operation(summary = "[1] - Kirim OTP ke email pengguna untuk verifikasi")
    public ResponseEntity<ApiResponse<?>> sendOtp(@Valid @RequestBody RegisterRequest.SendOtp request,
                                                  BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getFieldErrors().stream()
                  .map(error -> error.getField() + ": " + error.getDefaultMessage())
                  .toList();

            return ResponseEntity.badRequest()
                  .body(ApiResponse.<List<String>>builder()
                        .success(false)
                        .statusCode(400)
                        .message("Validation failed")
                        .data(errorMessages)
                        .timestamp(LocalDateTime.now())
                        .build());
        }

        try {
            // AccountResponse.SendOtp response = authService.sendOTP(request);

            var response = "SUCCESS";

            return ResponseEntity.ok(
                  ApiResponse.builder()
                        .success(true)
                        .statusCode(200)
                        .message("OTP sent successfully! Please check your email and WhatsApp.")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .build()
            );
        } catch (ConflictException ex) {
            List<String> conflicts = Arrays.asList(ex.getMessage().split("; "));
            return ResponseEntity.status(HttpStatus.CONFLICT)
                  .body(ApiResponse.<List<String>>builder()
                        .success(false)
                        .statusCode(409)
                        .message("Conflict detected")
                        .data(conflicts)
                        .timestamp(LocalDateTime.now())
                        .build());
        }
    }

    @GetMapping("/resend-captcha")
    @Operation(summary = "Kirim ulang captcha")
    public ResponseEntity<ApiResponse<AccountResponse.ResendCaptcha>> resendOtp() {
        AccountResponse.ResendCaptcha response = authService.resendOtp();

        return ResponseEntity.ok(ApiResponse.success(response, "Captcha resent successfully"));
    }

    @PostMapping("/verify-otp-and-signup")
    @Operation(summary = "[2] - Daftar akun baru")
    public ResponseEntity<ApiResponse<?>> register(@Valid @RequestBody RegisterRequest request,
                                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getFieldErrors().stream()
                  .map(error -> error.getField() + ": " + error.getDefaultMessage())
                  .toList();

            return ResponseEntity.badRequest().body(
                  ApiResponse.<List<String>>builder()
                        .success(false)
                        .statusCode(400)
                        .message("Validation failed")
                        .errors(errorMessages)
                        .timestamp(LocalDateTime.now())
                        .build()
            );
        }

        AccountResponse created = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
              .body(ApiResponse.created(created));
    }

    @PutMapping("/verify-account/{nik}")
    @Operation(summary = "Verifikasi akun oleh ADMIN")
    public ResponseEntity<ApiResponse<?>> verifyAccount(@Valid @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
                                                        @PathVariable String nik,
                                                        @Valid @RequestBody RegisterRequest.VerifyAccount request,
                                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getFieldErrors().stream()
                  .map(error -> error.getField() + ": " + error.getDefaultMessage())
                  .toList();

            ApiResponse<List<String>> errorResponse = ApiResponse.<List<String>>builder()
                  .success(false)
                  .statusCode(400)
                  .message("Validation failed")
                  .errors(errorMessages)
                  .timestamp(LocalDateTime.now())
                  .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }

        AccountResponse.VerifyAccount verified = authService.verifyAccount(authHeader, nik, request);

        return ResponseEntity.ok(ApiResponse.success(verified, "Account verified successfully"));
    }

    @PostMapping("/login")
    @Operation(summary = "[3] - Login akun")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success(response, "Signin successfully"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and blacklist token")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                  .body(ApiResponse.error(400, "No token provided"));
        }

        String token = authHeader.substring(7);
        tokenBlacklistService.blacklist(token);

        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }
}

