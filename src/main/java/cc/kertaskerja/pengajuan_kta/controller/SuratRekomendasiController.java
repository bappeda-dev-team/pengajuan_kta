package cc.kertaskerja.pengajuan_kta.controller;

import cc.kertaskerja.pengajuan_kta.dto.ApiResponse;
import cc.kertaskerja.pengajuan_kta.dto.Rekomendasi.FilePendukungDTO;
import cc.kertaskerja.pengajuan_kta.dto.Rekomendasi.RekomendasiReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.Rekomendasi.RekomendasiResDTO;
import cc.kertaskerja.pengajuan_kta.service.rekomendasi.RekomendasiService;
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
@RequestMapping("/rekomendasi")
@RequiredArgsConstructor
@Tag(name = "Form Pengajuan Surat Rekomendasi")
public class SuratRekomendasiController {

    private final RekomendasiService rekomendasiService;

    @GetMapping
    @Operation(summary = "Ambil semua data permohonan surat rekomendasi berdasarkan role & token JWT")
    public ResponseEntity<ApiResponse<?>> findAllData(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false)
                                                          String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        try {
            var result = rekomendasiService.findAll(authHeader);
            return ResponseEntity.ok(ApiResponse.success(result, "Retrieved " + result.size() + " data surat rekomendasi successfully"));
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

    @PostMapping
    @Operation(summary = "Simpan data pengajuan surat rekomendasi")
    public ResponseEntity<ApiResponse<?>> saveData(@Valid @RequestBody RekomendasiReqDTO.SaveData dto,
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

        RekomendasiResDTO.SaveDataResponse saved = rekomendasiService.saveData(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(saved));
    }

    @PostMapping("/upload-file")
    @Operation(summary = "Upload file")
    public ResponseEntity<ApiResponse<?>> uploadAndSave(@RequestParam("file") MultipartFile file,
                                                        @RequestParam("rekom_uuid") String formUuid,
                                                        @RequestParam(value = "nama_file", required = false) String namaFile) {
        FilePendukungDTO result = rekomendasiService.uploadAndSaveFile(file, formUuid, namaFile);

        return ResponseEntity.ok(ApiResponse.created(result));
    }

    @GetMapping("/detail-with-profile/{uuid}")
    @Operation(summary = "Ambil data pengajuan Surat Rekomendasi berdasarkan uuid")
    public ResponseEntity<ApiResponse<RekomendasiResDTO.RekomendasiWithProfileResponse>> getRekomByUuidWithProfile(@Valid @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
                                                                                             @PathVariable UUID uuid) {
        RekomendasiResDTO.RekomendasiWithProfileResponse result = rekomendasiService.findByUuidWithFilesAndProfile(authHeader, uuid);
        ApiResponse<RekomendasiResDTO.RekomendasiWithProfileResponse> response = ApiResponse.success(result, "Retrieved 1 data successfully");

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{uuid}")
    @Operation(summary = "Ubah data permohonan surat rekomendasi")
    public ResponseEntity<ApiResponse<?>> updateRekomendasi(@Valid @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
                                                            @PathVariable UUID uuid,
                                                            @RequestBody RekomendasiReqDTO.SaveData dto,
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

        RekomendasiResDTO.SaveDataResponse updated = rekomendasiService.editDataRekomendasi(authHeader, uuid, dto);

        return ResponseEntity.ok(ApiResponse.updated(updated));
    }

    @PutMapping("/verify/{uuid}")
    @Operation(summary = "Verifikasi data permohonan surat rekomendasi dan update status")
    public ResponseEntity<ApiResponse<?>> verifyData(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
                                                     @PathVariable UUID uuid,
                                                     @Valid @RequestBody RekomendasiReqDTO.Verify dto,
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
            RekomendasiResDTO.VerifyData result = rekomendasiService.verifyDataRekomendasi(authHeader, uuid, dto);

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
    @Operation(summary = "Hapus data permohonan surat rekomendasi")
    public ResponseEntity<ApiResponse<?>> deleteRekomendasi(@PathVariable UUID uuid) {
        try {
            rekomendasiService.deleteData(uuid);

            return ResponseEntity.ok(
                  ApiResponse.success(null, "Permohonan surat rekomendasi berhasil dihapus")
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

    @DeleteMapping("/file-pendukung/{id}")
    @Operation(summary = "Hapus 1 file pendukung berdasarkan ID (hapus juga di R2)")
    public ResponseEntity<ApiResponse<Void>> deleteOne(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
                                                       @PathVariable Long id) {
        rekomendasiService.deleteFilePendukung(authHeader, id);

        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
