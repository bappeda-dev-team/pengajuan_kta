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
    @Operation(summary = "Kirim OTP ke email pengguna untuk verifikasi")
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
            AccountResponse.SendOtp response = authService.sendOTP(request);

            return ResponseEntity.ok(
                  ApiResponse.success(response, response.getMessage())
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

    @PostMapping("/verify-otp-and-signup")
    @Operation(summary = "Daftar akun baru")
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

    @PostMapping("/login")
    @Operation(summary = "Login akun")
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


