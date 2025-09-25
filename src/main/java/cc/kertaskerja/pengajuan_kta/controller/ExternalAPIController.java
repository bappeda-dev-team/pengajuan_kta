package cc.kertaskerja.pengajuan_kta.controller;

import cc.kertaskerja.pengajuan_kta.dto.ApiResponse;
import cc.kertaskerja.pengajuan_kta.dto.EncryptDTO;
import cc.kertaskerja.pengajuan_kta.service.external.EncryptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/external")
public class ExternalAPIController {

    private final EncryptService encryptService;

    @PostMapping("/encrypt")
    public ResponseEntity<ApiResponse<?>> encrypt(@Valid @RequestBody EncryptDTO request,
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

        String encrypted = encryptService.encrypt(request.getData());

        return ResponseEntity.ok(ApiResponse.success(encrypted, "Encrypted successfully"));
    }

    @PostMapping("/decrypt")
    public ResponseEntity<ApiResponse<?>> decrypt(@Valid @RequestBody EncryptDTO request,
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

        String decrypted = encryptService.decrypt(request.getData());

        return ResponseEntity.ok(ApiResponse.success(decrypted, "Decrypted successfully"));
    }
}
