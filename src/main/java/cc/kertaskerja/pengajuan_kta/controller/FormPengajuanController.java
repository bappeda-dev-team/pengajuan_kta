package cc.kertaskerja.pengajuan_kta.controller;

import cc.kertaskerja.pengajuan_kta.dto.ApiResponse;
import cc.kertaskerja.pengajuan_kta.dto.FilePendukungDTO;
import cc.kertaskerja.pengajuan_kta.dto.FormPengajuanReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.FormPengajuanResDTO;
import cc.kertaskerja.pengajuan_kta.exception.BadRequestException;
import cc.kertaskerja.pengajuan_kta.helper.Crypto;
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
    public ResponseEntity<ApiResponse<?>> saveData(@Valid @RequestBody FormPengajuanReqDTO dto,
                                                   BindingResult bindingResult) {
        if (!Crypto.isEncrypted(dto.getTertanda().getNip())) {
            throw new BadRequestException("NIP is not encrypted: " + dto.getTertanda().getNip());
        }

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

        FormPengajuanResDTO saved = formPengajuanService.saveData(dto);

        ApiResponse<Object> response = ApiResponse.builder()
              .success(true)
              .statusCode(201)
              .message("Pengajuan KTA has been saved successfully")
              .data(saved.getUuid()) // only UUID here
              .timestamp(LocalDateTime.now())
              .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/upload-and-save")
    @Operation(summary = "Upload file ke R2 storage dan simpan metadata ke database")
    public ResponseEntity<ApiResponse<?>> uploadAndSaveFile(@RequestParam("file") MultipartFile file,
                                                          @RequestParam("form_uuid") String formUuid,
                                                          @RequestParam(value = "nama_file", required = false) String namaFile) {

        FilePendukungDTO result = formPengajuanService.uploadAndSaveFile(file, formUuid, namaFile);

        return ResponseEntity.ok(ApiResponse.created(result));
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "Ambil data pengajuan KTA berdasarkan uuid")
    public ResponseEntity<ApiResponse<FormPengajuanResDTO>> getFormByUuid(@PathVariable UUID uuid) {
        FormPengajuanResDTO result = formPengajuanService.findByUuidWithFiles(uuid);
        ApiResponse<FormPengajuanResDTO> response = ApiResponse.success(result, "Retrieved 1 data successfully");

        return ResponseEntity.ok(response);
    }
}