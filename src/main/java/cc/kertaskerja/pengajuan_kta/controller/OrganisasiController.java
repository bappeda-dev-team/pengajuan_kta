package cc.kertaskerja.pengajuan_kta.controller;

import cc.kertaskerja.pengajuan_kta.dto.ApiResponse;
import cc.kertaskerja.pengajuan_kta.dto.Organisasi.OrganisasiReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.Organisasi.OrganisasiResDTO;
import cc.kertaskerja.pengajuan_kta.service.organisasi.OrganisasiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
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
@RequestMapping("/organisasi")
@RequiredArgsConstructor
@Tag(name = "Pengajuan Nama Organisasi Kesenian Baru")
public class OrganisasiController {

    private final OrganisasiService organisasiService;

    @GetMapping
    @Operation(summary = "Lihat semua organisasi kesenian")
    public ResponseEntity<ApiResponse<?>> findAllOrganisasi(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        try {
            var result = organisasiService.findAllOrganisasi(authHeader);
            return ResponseEntity.ok(ApiResponse.success(result, "Retrieved " + result.size() + " data organisasi successfully"));
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

    @GetMapping("/detail/{uuid}")
    @Operation(summary = "Ambil data organisasi seni berdasarkan uuid")
    public ResponseEntity<ApiResponse<OrganisasiResDTO.DetailResponse>> getDetail(@Valid @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
                                                                                                                            @PathVariable UUID uuid) {
        OrganisasiResDTO.DetailResponse result = organisasiService.detailOrganisasi(authHeader, uuid);
        ApiResponse<OrganisasiResDTO.DetailResponse> response = ApiResponse.success(result, "Retrieved 1 data successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Simpan pengajuan organisasi seni baru")
    public ResponseEntity<ApiResponse<?>> saveData(@Valid @RequestBody OrganisasiReqDTO.SaveData dto,
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

        OrganisasiResDTO.SaveResponse saved = organisasiService.saveData(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(saved));
    }

    @PutMapping("/update/{uuid}")
    @Operation(summary = "Ubah data pengajuan organisasi kesenian")
    public ResponseEntity<ApiResponse<?>> updateOrganisasi(@Valid @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
                                                          @PathVariable UUID uuid,
                                                          @RequestBody OrganisasiReqDTO.SaveData dto,
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

        OrganisasiResDTO.SaveResponse updated = organisasiService.editDataOrganisasi(authHeader, uuid, dto);

        return ResponseEntity.ok(ApiResponse.updated(updated));
    }

    @PostMapping("/upload-file")
    @Operation(summary = "Upload file pendukung")
    public ResponseEntity<ApiResponse<?>> uploadFile(@RequestParam("file") MultipartFile file,
                                                     @RequestParam("organisasi_uuid") String organisasiUuid,
                                                     @RequestParam(value = "nama_file", required = false) String namaFile) {
        OrganisasiResDTO.FilePendukung result = organisasiService.uploadFilePendukung(file, organisasiUuid, namaFile);

        return ResponseEntity.ok(ApiResponse.created(result));
    }

    @DeleteMapping("/delete/{uuid}")
    @Operation(summary = "Hapus data organisasi kesenian via uuid")
    public ResponseEntity<ApiResponse<Void>> deleteOrganisasi(@PathVariable UUID uuid) {
        try {
            organisasiService.deleteData(uuid.toString());

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
    @Operation(summary = "Hapus 1 file organisasi berdasarkan ID (hapus juga di R2)")
    public ResponseEntity<ApiResponse<Void>> deleteOne(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
                                                       @PathVariable Long id) {
        organisasiService.deleteFilePendukung(authHeader, id);

        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
