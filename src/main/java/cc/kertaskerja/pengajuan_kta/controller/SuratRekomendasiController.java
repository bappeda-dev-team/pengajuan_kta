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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<ApiResponse<List<RekomendasiResDTO.RekomendasiResponse>>> findAllData(
          @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        var result = rekomendasiService.findAll(authHeader);

        return ResponseEntity.ok(ApiResponse.success(result, "Retrieved " + result.size() + " data surat rekomendasi successfully"));
    }

    @PostMapping
    @Operation(summary = "Simpan data pengajuan surat rekomendasi")
    public ResponseEntity<ApiResponse<RekomendasiResDTO.SaveDataResponse>> saveData(
          @Valid @RequestBody RekomendasiReqDTO.SaveData dto) {
        RekomendasiResDTO.SaveDataResponse saved = rekomendasiService.saveData(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(saved));
    }

    @PostMapping("/upload-file")
    @Operation(summary = "Upload file")
    public ResponseEntity<ApiResponse<FilePendukungDTO>> uploadAndSave(
          @RequestParam("file") MultipartFile file,
          @RequestParam("rekom_uuid") String formUuid,
          @RequestParam(value = "nama_file", required = false) String namaFile) {
        FilePendukungDTO result = rekomendasiService.uploadAndSaveFile(file, formUuid, namaFile);

        return ResponseEntity.ok(ApiResponse.created(result));
    }

    @GetMapping("/detail-with-profile/{uuid}")
    @Operation(summary = "Ambil data pengajuan Surat Rekomendasi berdasarkan uuid")
    public ResponseEntity<ApiResponse<RekomendasiResDTO.RekomendasiWithProfileResponse>> getRekomByUuidWithProfile(
          @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
          @PathVariable UUID uuid) {
        RekomendasiResDTO.RekomendasiWithProfileResponse result =
              rekomendasiService.findByUuidWithFilesAndProfile(authHeader, uuid);

        return ResponseEntity.ok(ApiResponse.success(result, "Retrieved 1 data successfully"));
    }

    @PutMapping("/update/{uuid}")
    @Operation(summary = "Ubah data permohonan surat rekomendasi")
    public ResponseEntity<ApiResponse<RekomendasiResDTO.SaveDataResponse>> updateRekomendasi(
          @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
          @PathVariable UUID uuid,
          @RequestBody RekomendasiReqDTO.SaveData dto) {
        RekomendasiResDTO.SaveDataResponse updated = rekomendasiService.editDataRekomendasi(authHeader, uuid, dto);

        return ResponseEntity.ok(ApiResponse.updated(updated));
    }

    @PutMapping("/verify/{uuid}")
    @Operation(summary = "Verifikasi data permohonan surat rekomendasi dan update status")
    public ResponseEntity<ApiResponse<RekomendasiResDTO.VerifyData>> verifyData(
          @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
          @PathVariable UUID uuid,
          @Valid @RequestBody RekomendasiReqDTO.Verify dto) {
        RekomendasiResDTO.VerifyData result = rekomendasiService.verifyDataRekomendasi(authHeader, uuid, dto);

        return ResponseEntity.ok(ApiResponse.updated(result));
    }

    @DeleteMapping("/delete/{uuid}")
    @Operation(summary = "Hapus data permohonan surat rekomendasi")
    public ResponseEntity<ApiResponse<Void>> deleteRekomendasi(@PathVariable UUID uuid) {
        rekomendasiService.deleteData(uuid);

        return ResponseEntity.ok(ApiResponse.success(null, "Permohonan surat rekomendasi berhasil dihapus"));
    }

    @DeleteMapping("/file-pendukung/{id}")
    @Operation(summary = "Hapus 1 file pendukung berdasarkan ID (hapus juga di R2)")
    public ResponseEntity<ApiResponse<Void>> deleteOne(
          @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
          @PathVariable Long id) {
        rekomendasiService.deleteFilePendukung(authHeader, id);

        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
