package cc.kertaskerja.pengajuan_kta.controller;

import cc.kertaskerja.pengajuan_kta.dto.ApiResponse;
import cc.kertaskerja.pengajuan_kta.dto.FormPengajuanDTO;
import cc.kertaskerja.pengajuan_kta.service.FormPengajuanService;
import cc.kertaskerja.pengajuan_kta.service.global.CloudStorage.R2StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ApiResponse<?>> saveData(@Valid @RequestBody FormPengajuanDTO dto,
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

        FormPengajuanDTO saved = formPengajuanService.saveData(dto);

        String message = "Pengajuan KTA has been saved successfully with uuid: " + saved.getUuid();

        ApiResponse<String> response = ApiResponse.<String>builder()
              .success(true)
              .statusCode(201)
              .message(message)
              .timestamp(LocalDateTime.now())
              .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/upload-and-save")
    @Operation(summary = "Upload file ke R2 storage dan simpan metadata ke database")
    public ResponseEntity<ApiResponse<?>> uploadAndSaveFile(
          @RequestParam("file") MultipartFile file,
          @RequestParam("form_uuid") String formUuid,
          @RequestParam(value = "nama_file", required = false) String namaFile) {

        try {
            // 1. Upload file ke R2
            String fileUrl = r2StorageService.upload(file);

            // 2. Gunakan namaFile dari request atau fallback ke original filename
            String finalNamaFile = namaFile != null ? namaFile : file.getOriginalFilename();

            // 3. Buat DTO untuk metadata file
            FormPengajuanDTO.FilePendukung uploadDto = FormPengajuanDTO.FilePendukung.builder()
                  .form_uuid(formUuid)
                  .file_url(fileUrl)
                  .nama_file(finalNamaFile)
                  .build();

            // 4. Simpan metadata ke DB
            FormPengajuanDTO.FilePendukung result = formPengajuanService.uploadFile(uploadDto);

            // ✅ Success response
            return ResponseEntity.ok(ApiResponse.created(result));

        } catch (Exception e) {
            // Print full stacktrace to logs
            e.printStackTrace();

            // Debug-friendly response: return actual error message
            ApiResponse<String> errorResponse = ApiResponse.<String>builder()
                  .success(false)
                  .statusCode(500)
                  .message("Failed to upload and save file")
                  .errors(List.of(e.getClass().getName() + ": " + e.getMessage()))
                  .timestamp(LocalDateTime.now())
                  .build();

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "Ambil data pengajuan KTA berdasarkan uuid")
    public ResponseEntity<ApiResponse<FormPengajuanDTO>> getFormByUuid(@PathVariable UUID uuid) {
        FormPengajuanDTO result = formPengajuanService.findByUuidWithFiles(uuid);
        ApiResponse<FormPengajuanDTO> response = ApiResponse.success(result, "Retrieved 1 data successfully");

        return ResponseEntity.ok(response);
    }
}