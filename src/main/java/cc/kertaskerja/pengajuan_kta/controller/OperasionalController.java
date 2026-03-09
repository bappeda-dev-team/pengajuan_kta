package cc.kertaskerja.pengajuan_kta.controller;

import cc.kertaskerja.pengajuan_kta.dto.ApiResponse;
import cc.kertaskerja.pengajuan_kta.dto.Operasional.OperasionalReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.Operasional.OperasionalResDTO;
import cc.kertaskerja.pengajuan_kta.service.operasional.OperasionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/operasional")
@RequiredArgsConstructor
@Tag(name = "Izin Operasional")
public class OperasionalController {

    private final OperasionalService operasionalService;

    @GetMapping
    @Operation(summary = "Lihat semua permohonan izin operasional")
    public ResponseEntity<ApiResponse<?>> findAllOperasional(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        try {
            var result = operasionalService.getAllData(authHeader);
            return ResponseEntity.ok(ApiResponse.success(result, "Retrieved " + result.size() + " permohonan izin operasional successfully"));
        } catch (RuntimeException e) {
            var error = ApiResponse.builder()
                  .success(false)
                  .statusCode(400)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now())
                  .build();
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/detail-with-profile/{uuid}")
    @Operation(summary = "Ambil detail izin operasional + profile pemilik (account) berdasarkan uuid")
    public ResponseEntity<ApiResponse<OperasionalResDTO.DetailResponse>> getDetailWithProfile(@Valid @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
                                                                                              @PathVariable UUID uuid) {
        OperasionalResDTO.DetailResponse result = operasionalService.findByUuidWithFiledAndProfile(authHeader, uuid);

        return ResponseEntity.ok(ApiResponse.success(result, "Retrieved 1 data successfully"));
    }

    @PostMapping
    @Operation(summary = "Simpan data permohonan izin operasional")
    public ResponseEntity<ApiResponse<?>> saveData(@Valid @RequestBody OperasionalReqDTO.SaveData dto,
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

        OperasionalResDTO saved = operasionalService.saveData(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(saved));
    }

    @PostMapping("/upload-file")
    @Operation(summary = "Upload file pendukung")
    public ResponseEntity<ApiResponse<?>> uploadFile(@RequestParam("file") MultipartFile file,
                                                     @RequestParam("operasional_uuid") String operasionalUuid,
                                                     @RequestParam(value = "nama_file", required = false) String namaFile) {
        OperasionalResDTO.FilePendukung result = operasionalService.uploadFilePendukung(file, operasionalUuid, namaFile);

        return ResponseEntity.ok(ApiResponse.created(result));

    }

    @PutMapping("/verify/{uuid}")
    @Operation(summary = "Verifikasi permohonan izin operasional")
    public ResponseEntity<ApiResponse<?>> verifyData(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
                                                     @PathVariable UUID uuid,
                                                     @Valid @RequestBody OperasionalReqDTO.Verify dto,
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

        try {
            OperasionalResDTO.VerifyData result = operasionalService.verify(authHeader, dto, uuid);

            return ResponseEntity.ok(ApiResponse.updated(result));
        } catch (RuntimeException e) {
            ApiResponse<Object> errorResponse = ApiResponse.builder()
                  .success(false)
                  .statusCode(400)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now())
                  .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/delete/{uuid}")
    @Operation(summary = "Hapus data permohonan izin operasional via uuid")
    public ResponseEntity<ApiResponse<Void>> deleteOperasional(@PathVariable UUID uuid) {
        try {
            operasionalService.deleteData(uuid.toString());

            return ResponseEntity.ok(
                  ApiResponse.success(null, "Data pengajuan berhasil dihapus")
            );
        } catch (RuntimeException e) {
            ApiResponse<Void> errorResponse = ApiResponse.<Void>builder()
                  .success(false)
                  .statusCode(400)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now())
                  .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
