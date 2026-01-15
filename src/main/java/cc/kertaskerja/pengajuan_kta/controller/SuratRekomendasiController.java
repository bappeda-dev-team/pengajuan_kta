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

    @GetMapping("/detail/{uuid}")
    @Operation(summary = "Ambil data pengajuan Surat Rekomendasi berdasarkan uuid")
    public ResponseEntity<ApiResponse<RekomendasiResDTO.RekomendasiResponse>> getRekomByUuid(@PathVariable UUID uuid) {
        RekomendasiResDTO.RekomendasiResponse result = rekomendasiService.findByUuidWithFiles(uuid);
        ApiResponse<RekomendasiResDTO.RekomendasiResponse> response = ApiResponse.success(result, "Retrieved 1 data successfully");

        return ResponseEntity.ok(response);
    }
}
