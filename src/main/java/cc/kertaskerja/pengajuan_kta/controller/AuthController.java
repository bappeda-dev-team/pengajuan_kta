package cc.kertaskerja.pengajuan_kta.controller;

import cc.kertaskerja.pengajuan_kta.dto.ApiResponse;
import cc.kertaskerja.pengajuan_kta.dto.Auth.*;
import cc.kertaskerja.pengajuan_kta.exception.BadRequestException;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Endpoint Authentication")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/signup")
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
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Signin successfully"));
        } catch (BadRequestException e) {
            String message = e.getMessage();

            if ("TOO_MANY_ATTEMPTS".equalsIgnoreCase(message)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                      .body(ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .statusCode(HttpStatus.FORBIDDEN.value())
                            .message("Failed to login for more than 3 times. Please wait 1 minute before retrying")
                            .data(Map.of("countdown", Instant.now().plus(Duration.ofMinutes(1))))
                            .timestamp(LocalDateTime.now())
                            .build());
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                  .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), message));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                  .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), e.getMessage()));
        }
    }

    @PostMapping("/check-account")
    @Operation(summary = "Cek dan validasi data akun yang baru login")
    public ResponseEntity<ApiResponse<AccountResponse>> checkAccount(@Valid @RequestBody TokenRequest tokenRequest) {
        try {
            AccountResponse account = authService.validateToken(tokenRequest.getAccessToken());
            return ResponseEntity.ok(ApiResponse.success(account, "Account validated"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                  .body(ApiResponse.error(401, e.getMessage()));
        }
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


