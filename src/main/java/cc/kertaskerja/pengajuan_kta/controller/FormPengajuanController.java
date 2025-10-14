package cc.kertaskerja.pengajuan_kta.controller;

import cc.kertaskerja.pengajuan_kta.dto.ApiResponse;
import cc.kertaskerja.pengajuan_kta.dto.Pengajuan.FilePendukungDTO;
import cc.kertaskerja.pengajuan_kta.dto.Pengajuan.FormPengajuanReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.Pengajuan.FormPengajuanResDTO;
import cc.kertaskerja.pengajuan_kta.service.pengajuan.FormPengajuanService;
import cc.kertaskerja.pengajuan_kta.service.global.CloudStorage.R2StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

        try {
            FormPengajuanResDTO.SaveDataResponse saved = formPengajuanService.saveData(dto);

            return ResponseEntity
                  .status(201)
                  .body(ApiResponse.created(saved.getUuid()));

        } catch (RuntimeException e) {
            ApiResponse<Object> error = ApiResponse.builder()
                  .success(false)
                  .statusCode(400)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now())
                  .build();

            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/upload/save")
    @Operation(summary = "Upload file ke R2 storage dan simpan metadata ke database")
    public ResponseEntity<ApiResponse<?>> uploadAndSaveFile(@RequestParam("file") MultipartFile file,
                                                          @RequestParam("form_uuid") String formUuid,
                                                          @RequestParam(value = "nama_file", required = false) String namaFile) {

        FilePendukungDTO result = formPengajuanService.uploadAndSaveFile(file, formUuid, namaFile);

        return ResponseEntity.ok(ApiResponse.created(result));
    }

    @GetMapping("/file/{uuid}")
    @Operation(summary = "Ambil data pengajuan KTA berdasarkan uuid")
    public ResponseEntity<ApiResponse<FormPengajuanResDTO.PengajuanResponse>> getFormByUuid(@PathVariable UUID uuid) {
        FormPengajuanResDTO.PengajuanResponse result = formPengajuanService.findByUuidWithFiles(uuid);
        ApiResponse<FormPengajuanResDTO.PengajuanResponse> response = ApiResponse.success(result, "Retrieved 1 data successfully");

        return ResponseEntity.ok(response);
    }

    @PutMapping("/verify/{uuid}")
    @Operation(summary = "Verifikasi data pengajuan KTA dan update status")
    public ResponseEntity<ApiResponse<?>> verifyData(@PathVariable UUID uuid,
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
            FormPengajuanResDTO.VerifyData result = formPengajuanService.verifyDataPengajuan(dto, uuid);

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
}