package cc.kertaskerja.pengajuan_kta.controller;

import cc.kertaskerja.pengajuan_kta.dto.ApiResponse;
import cc.kertaskerja.pengajuan_kta.dto.JenisSeni.JenisReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.JenisSeni.JenisResDTO;
import cc.kertaskerja.pengajuan_kta.service.jenisSeni.JenisSeniService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jenis-seni")
@RequiredArgsConstructor
@Tag(name = "Master Jenis Seni")
public class JenisSeniController {

    private final JenisSeniService jenisSeniService;

    @GetMapping
    @Operation(summary = "Lihat semua data jenis seni")
    public ResponseEntity<ApiResponse<List<JenisResDTO>>> findAll(
          @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        List<JenisResDTO> result = jenisSeniService.findAll(authHeader);

        return ResponseEntity.ok(ApiResponse.success(result, "Retrieved " + result.size() + " data successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ambil detail data jenis seni berdasarkan ID")
    public ResponseEntity<ApiResponse<JenisResDTO>> findById(
          @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
          @PathVariable Long id) {
        JenisResDTO result = jenisSeniService.findById(authHeader, id);

        return ResponseEntity.ok(ApiResponse.success(result, "Retrieved 1 data successfully"));
    }

    @PostMapping
    @Operation(summary = "Simpan data jenis seni baru")
    public ResponseEntity<ApiResponse<JenisResDTO>> saveData(
          @Valid @RequestBody JenisReqDTO.SaveData dto) {
        JenisResDTO saved = jenisSeniService.saveData(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Ubah data jenis seni berdasarkan ID")
    public ResponseEntity<ApiResponse<JenisResDTO>> updateData(
          @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
          @PathVariable Long id,
          @Valid @RequestBody JenisReqDTO.SaveData dto) {
        JenisResDTO updated = jenisSeniService.updateData(authHeader, id, dto);

        return ResponseEntity.ok(ApiResponse.updated(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Hapus data jenis seni berdasarkan ID")
    public ResponseEntity<ApiResponse<Void>> deleteData(
          @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
          @PathVariable Long id) {
        jenisSeniService.deleteData(authHeader, id);

        return ResponseEntity.ok(ApiResponse.success(null, "Data berhasil dihapus"));
    }
}