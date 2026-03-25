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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<ApiResponse<List<OperasionalResDTO>>> findAllOperasional(
          @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        var result = operasionalService.getAllData(authHeader);

        return ResponseEntity.ok(ApiResponse.success(result, "Retrieved " + result.size() + " permohonan izin operasional successfully"));
    }

    @GetMapping("/detail-with-profile/{uuid}")
    @Operation(summary = "Ambil detail izin operasional + profile pemilik (account) berdasarkan uuid")
    public ResponseEntity<ApiResponse<OperasionalResDTO.DetailResponse>> getDetailWithProfile(
          @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
          @PathVariable UUID uuid) {
        OperasionalResDTO.DetailResponse result = operasionalService.findByUuidWithFiledAndProfile(authHeader, uuid);

        return ResponseEntity.ok(ApiResponse.success(result, "Retrieved 1 data successfully"));
    }

    @PostMapping
    @Operation(summary = "Simpan data permohonan izin operasional")
    public ResponseEntity<ApiResponse<OperasionalResDTO>> saveData(
          @Valid @RequestBody OperasionalReqDTO.SaveData dto) {
        OperasionalResDTO saved = operasionalService.saveData(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(saved));
    }

    @PostMapping("/upload-file")
    @Operation(summary = "Upload file pendukung")
    public ResponseEntity<ApiResponse<OperasionalResDTO.FilePendukung>> uploadFile(
          @RequestParam("file") MultipartFile file,
          @RequestParam("operasional_uuid") String operasionalUuid,
          @RequestParam(value = "nama_file", required = false) String namaFile) {
        OperasionalResDTO.FilePendukung result = operasionalService.uploadFilePendukung(file, operasionalUuid, namaFile);

        return ResponseEntity.ok(ApiResponse.created(result));
    }

    @PutMapping("/verify/{uuid}")
    @Operation(summary = "Verifikasi permohonan izin operasional")
    public ResponseEntity<ApiResponse<OperasionalResDTO.VerifyData>> verifyData(
          @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
          @PathVariable UUID uuid,
          @Valid @RequestBody OperasionalReqDTO.Verify dto) {
        OperasionalResDTO.VerifyData result = operasionalService.verify(authHeader, dto, uuid);

        return ResponseEntity.ok(ApiResponse.updated(result));
    }

    @DeleteMapping("/delete/{uuid}")
    @Operation(summary = "Hapus data permohonan izin operasional via uuid")
    public ResponseEntity<ApiResponse<Void>> deleteOperasional(@PathVariable UUID uuid) {
        operasionalService.deleteData(uuid.toString());

        return ResponseEntity.ok(ApiResponse.success(null, "Data pengajuan berhasil dihapus"));
    }
}
