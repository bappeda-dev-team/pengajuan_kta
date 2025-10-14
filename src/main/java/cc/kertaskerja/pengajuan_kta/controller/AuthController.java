package cc.kertaskerja.pengajuan_kta.controller;

import cc.kertaskerja.pengajuan_kta.dto.ApiResponse;
import cc.kertaskerja.pengajuan_kta.dto.Auth.*;
import cc.kertaskerja.pengajuan_kta.security.JwtTokenProvider;
import cc.kertaskerja.pengajuan_kta.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Endpoint Authentication")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    @Operation(summary = "Register a new account")
    public ResponseEntity<ApiResponse<?>> register(@Valid @RequestBody RegisterRequest request, BindingResult bindingResult) {
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

        try {
            AccountResponse created = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                  .body(ApiResponse.created(created));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                  .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Login and get JWT access token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Signin successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                  .body(ApiResponse.error(401, e.getMessage()));
        }
    }

    @PostMapping("/check-account")
    @Operation(summary = "Validate access token and get account data")
    public ResponseEntity<ApiResponse<AccountResponse>> checkAccount(@Valid @RequestBody TokenRequest tokenRequest) {
        try {
            AccountResponse account = authService.validateToken(tokenRequest.getAccessToken());
            return ResponseEntity.ok(ApiResponse.success(account, "Account validated"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                  .body(ApiResponse.error(401, e.getMessage()));
        }
    }
}


