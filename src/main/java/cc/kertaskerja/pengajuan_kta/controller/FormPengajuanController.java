package cc.kertaskerja.pengajuan_kta.controller;

import cc.kertaskerja.pengajuan_kta.dto.ApiResponse;
import cc.kertaskerja.pengajuan_kta.dto.Pengajuan.FilePendukungDTO;
import cc.kertaskerja.pengajuan_kta.dto.Pengajuan.FormPengajuanReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.Pengajuan.FormPengajuanResDTO;
import cc.kertaskerja.pengajuan_kta.service.global.R2StorageService;
import cc.kertaskerja.pengajuan_kta.service.pengajuan.FormPengajuanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pengajuan")
@RequiredArgsConstructor
@Tag(name = "Form Pengajuan KTA")
public class FormPengajuanController {

    private final FormPengajuanService formPengajuanService;
    private final R2StorageService r2StorageService;

    @GetMapping
    @Operation(summary = "Ambil semua data pengajuan KTA berdasarkan role & token JWT")
    public ResponseEntity<ApiResponse<?>> findAllPengajuan(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false)
                                                           String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        try {
            var result = formPengajuanService.findAllDataPengajuan(authHeader);
            return ResponseEntity.ok(ApiResponse.success(result, "Retrieved " + result.size() + " data pengajuan successfully"));
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
    @Operation(summary = "Simpan data pengajuan KTA")
    public ResponseEntity<ApiResponse<?>> saveData(@Valid @RequestBody FormPengajuanReqDTO.SavePengajuan dto,
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

        FormPengajuanResDTO.SaveDataResponse saved = formPengajuanService.saveData(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(saved));
    }

    @PostMapping("/upload-file")
    @Operation(summary = "Upload file")
    public ResponseEntity<ApiResponse<?>> uploadAndSaveFile(@RequestParam("file") MultipartFile file,
                                                            @RequestParam("form_uuid") String formUuid,
                                                            @RequestParam(value = "nama_file", required = false) String namaFile) {
        FilePendukungDTO result = formPengajuanService.uploadAndSaveFile(file, formUuid, namaFile);

        return ResponseEntity.ok(ApiResponse.created(result));
    }

    @GetMapping("/detail/{uuid}")
    @Operation(summary = "Ambil data pengajuan KTA berdasarkan uuid")
    public ResponseEntity<ApiResponse<FormPengajuanResDTO.PengajuanResponse>> getFormByUuid(@PathVariable UUID uuid) {
        FormPengajuanResDTO.PengajuanResponse result = formPengajuanService.findByUuidWithFiles(uuid);
        ApiResponse<FormPengajuanResDTO.PengajuanResponse> response = ApiResponse.success(result, "Retrieved 1 data successfully");

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{uuid}")
    @Operation(summary = "Ubah data pengajuan KTA")
    public ResponseEntity<ApiResponse<?>> updatePengajuan(@Valid @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
                                                          @PathVariable UUID uuid,
                                                          @RequestBody FormPengajuanReqDTO.SavePengajuan dto,
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

        FormPengajuanResDTO.SaveDataResponse updated = formPengajuanService.editDataPengajuan(authHeader, uuid, dto);

        return ResponseEntity.ok(ApiResponse.updated(updated));
    }

    @PutMapping("/verify/{uuid}")
    @Operation(summary = "Verifikasi data pengajuan KTA dan update status")
    public ResponseEntity<ApiResponse<?>> verifyData(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
                                                     @PathVariable UUID uuid,
                                                     @Valid @RequestBody FormPengajuanReqDTO.VerifyPengajuan dto,
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
            FormPengajuanResDTO.VerifyData result = formPengajuanService.verifyDataPengajuan(authHeader, dto, uuid);

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

    @GetMapping("/detail-with-profile/{uuid}")
    @Operation(summary = "Ambil detail pengajuan + profile pemilik (account) berdasarkan uuid")
    public ResponseEntity<ApiResponse<FormPengajuanResDTO.PengajuanWithProfileResponse>> getDetailWithProfile(
          @Valid @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
          @PathVariable UUID uuid
    ) {
        FormPengajuanResDTO.PengajuanWithProfileResponse result =
              formPengajuanService.findByUuidWithFilesAndProfile(authHeader, uuid);

        return ResponseEntity.ok(ApiResponse.success(result, "Retrieved 1 data successfully"));
    }

    @PutMapping("/setassigned/{nik}")
    @Operation(summary = "Set Assigned ke akun yang sudah buat form pengajuan")
    public ResponseEntity<ApiResponse<String>> setIsAssigned(@PathVariable String nik) {
        String result = formPengajuanService.editIsAssignedInAccount(nik);

        return ResponseEntity.ok(ApiResponse.success(result, "Set assigned successfully"));
    }

    @DeleteMapping("/delete/{uuid}")
    @Operation(summary = "Hapus Data Pengajuan via uuid")
    public ResponseEntity<ApiResponse<Void>> deletePengajuan(@PathVariable UUID uuid) {
        try {
            formPengajuanService.deleteData(uuid.toString());

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

    @DeleteMapping("/file-pendukung/{id}")
    @Operation(summary = "Hapus 1 file pendukung berdasarkan ID (hapus juga di R2)")
    public ResponseEntity<ApiResponse<Void>> deleteOne(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
                                                       @PathVariable Long id) {
        formPengajuanService.deleteFilePendukung(authHeader, id);

        return ResponseEntity.ok(ApiResponse.deleted());
    }

    @GetMapping("/image/{key}")
    public ResponseEntity<byte[]> getImage(@PathVariable String key) {
        byte[] bytes = r2StorageService.getObject(key);

        MediaType mediaType = key.endsWith(".jpg") || key.endsWith(".jpeg")
              ? MediaType.IMAGE_JPEG
              : MediaType.IMAGE_PNG;

        return ResponseEntity.ok()
              .contentType(mediaType)
              .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
              .body(bytes);
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<List<FormPengajuanResDTO.PengajuanBulananResponse>>> getPengajuanBulanan(@RequestParam(required = false) Integer tahun) {
        int year = tahun != null ? tahun : LocalDate.now().getYear();

        return ResponseEntity.ok(
              ApiResponse.success(
                    formPengajuanService.getStatisticsPerMonth(year),
                    "Statistik pengajuan tahun " + year
              )
        );
    }

}