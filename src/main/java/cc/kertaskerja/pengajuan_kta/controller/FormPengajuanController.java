package cc.kertaskerja.pengajuan_kta.controller;

import cc.kertaskerja.pengajuan_kta.dto.ApiResponse;
import cc.kertaskerja.pengajuan_kta.dto.DokumenPendukungDTO;
import cc.kertaskerja.pengajuan_kta.dto.FormPengajuanDTO;
import cc.kertaskerja.pengajuan_kta.dto.TertandaDTO;
import cc.kertaskerja.pengajuan_kta.service.CloudStorage.R2StorageService;
import cc.kertaskerja.pengajuan_kta.service.FormPengajuan.FormPengajuanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/pengajuan")
@RequiredArgsConstructor
@Tag(name = "Form Pengajuan KTA")
public class FormPengajuanController {

    private final FormPengajuanService formPengajuanService;
    private final R2StorageService r2StorageService;
    private final ObjectMapper objectMapper;

    @GetMapping
    @Operation(summary = "Tampilkan semua data pengajuan")
    public ResponseEntity<ApiResponse<List<FormPengajuanDTO>>> getAllData() {
        List<FormPengajuanDTO> dtoList = formPengajuanService.getAllData();
        ApiResponse<List<FormPengajuanDTO>> response = ApiResponse.success(dtoList, "Data pengajuan KTA berhasil diambil");

        return ResponseEntity
                .status(response.getStatusCode())
                .body(response);
    }

    @PostMapping
    @Operation(summary = "Simpan data pengajuan KTA")
    public ResponseEntity<ApiResponse<?>> saveData(@Valid @RequestBody FormPengajuanDTO formPengajuanDTO,
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

        FormPengajuanDTO result = formPengajuanService.saveData(formPengajuanDTO);

        return ResponseEntity.ok(ApiResponse.created(result));
    }

@PostMapping("/json-with-files")
@Operation(summary = "Simpan data pengajuan KTA dengan JSON")
public ResponseEntity<ApiResponse<?>> saveDataWithJson(@Valid @RequestBody FormPengajuanDTO formPengajuanDTO) {
    try {
        FormPengajuanDTO savedData = formPengajuanService.saveData(formPengajuanDTO);
        
        ApiResponse<FormPengajuanDTO> response = ApiResponse.success(
                savedData,
                "Data pengajuan berhasil disimpan"
        );
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    } catch (Exception e) {
        throw new RuntimeException("Error saving data: " + e.getMessage(), e);
    }
}
@PostMapping(value = "/with-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
@Operation(summary = "Simpan data pengajuan KTA dengan file upload")
public ResponseEntity<ApiResponse<?>> saveDataWithFiles(
        HttpServletRequest request,
        @RequestParam("induk_organisasi") String indukOrganisasi,
        @RequestParam("nomor_induk") String nomorInduk,
        @RequestParam("jumlah_anggota") String jumlahAnggota,
        @RequestParam("daerah") String daerah,
        @RequestParam("berlaku_dari") String berlakuDari,
        @RequestParam("berlaku_sampai") String berlakuSampai,
        @RequestParam("nama") String nama,
        @RequestParam("tanggal_lahir") String tanggalLahir,
        @RequestParam("jenis_kelamin") String jenisKelamin,
        @RequestParam("alamat") String alamat,
        @RequestParam("profesi") String profesi,
        @RequestParam("dibuat_di") String dibuatDi,
        @RequestParam("tertanda") String tertandaJson,
        @RequestParam(value = "status", defaultValue = "DRAFT") String status,
        @RequestParam(value = "keterangan", defaultValue = "-") String keterangan,
        @RequestParam(value = "files", required = false) List<MultipartFile> files) {
    
    try {
        // Enhanced logging for debugging
        System.out.println("=== REQUEST DEBUG INFO ===");
        System.out.println("Content-Type: " + request.getContentType());
        System.out.println("Content-Length: " + request.getContentLength());
        System.out.println("Files received: " + (files != null ? files.size() : 0));
        System.out.println("Induk Organisasi: " + indukOrganisasi);
        System.out.println("Tertanda JSON: " + tertandaJson);
        
        // Check if request is actually multipart
        if (request.getContentType() == null || !request.getContentType().startsWith("multipart/form-data")) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(400, "Request must be multipart/form-data")
            );
        }

        // Validate required fields with better error messages
        if (indukOrganisasi == null || indukOrganisasi.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(400, "induk_organisasi is required and cannot be empty")
            );
        }

        // Step 1: Upload files to R2 storage first
        List<DokumenPendukungDTO> dokumenList = new ArrayList<>();
        
        if (files != null && !files.isEmpty()) {
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                if (file != null && !file.isEmpty()) {
                    try {
                        System.out.println("Processing file " + (i + 1) + ": " + file.getOriginalFilename());
                        System.out.println("File size: " + file.getSize() + " bytes");
                        System.out.println("Content type: " + file.getContentType());
                        
                        String url = r2StorageService.upload(file);
                        
                        DokumenPendukungDTO dokumen = DokumenPendukungDTO.builder()
                                .url(url)
                                .fileName(file.getOriginalFilename())
                                .contentType(file.getContentType())
                                .build();
                        
                        dokumenList.add(dokumen);
                        System.out.println("File uploaded successfully: " + url);
                    } catch (Exception e) {
                        System.err.println("Failed to upload file: " + file.getOriginalFilename());
                        return ResponseEntity.internalServerError().body(
                            ApiResponse.error(500, "Failed to upload file: " + file.getOriginalFilename() + ". Error: " + e.getMessage())
                        );
                    }
                }
            }
        }

        // Parse tertanda JSON with better error handling
        TertandaDTO tertanda;
        try {
            if (tertandaJson != null && !tertandaJson.trim().isEmpty()) {
                System.out.println("Parsing tertanda JSON...");
                tertanda = objectMapper.readValue(tertandaJson, TertandaDTO.class);
                System.out.println("Tertanda parsed successfully: " + tertanda.getNama());
            } else {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error(400, "tertanda is required")
                );
            }
        } catch (Exception e) {
            System.err.println("Failed to parse tertanda JSON: " + e.getMessage());
            return ResponseEntity.badRequest().body(
                ApiResponse.error(400, "Invalid tertanda JSON format: " + e.getMessage())
            );
        }

        // Parse dates with validation
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        
        Date berlakuDariDate, berlakuSampaiDate;
        try {
            if (berlakuDari == null || berlakuDari.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error(400, "berlaku_dari is required")
                );
            }
            if (berlakuSampai == null || berlakuSampai.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error(400, "berlaku_sampai is required")
                );
            }
            
            berlakuDariDate = dateFormat.parse(berlakuDari);
            berlakuSampaiDate = dateFormat.parse(berlakuSampai);
            System.out.println("Dates parsed successfully");
        } catch (ParseException e) {
            System.err.println("Date parsing error: " + e.getMessage());
            return ResponseEntity.badRequest().body(
                ApiResponse.error(400, "Invalid date format. Use yyyy-MM-dd format. Error: " + e.getMessage())
            );
        }
        
        // Step 2: Create FormPengajuanDTO with uploaded document URLs
        System.out.println("Creating FormPengajuanDTO...");
        FormPengajuanDTO formPengajuanDTO = FormPengajuanDTO.builder()
                .induk_organisasi(indukOrganisasi)
                .nomor_induk(nomorInduk)
                .jumlah_anggota(jumlahAnggota)
                .daerah(daerah)
                .berlaku_dari(berlakuDariDate)
                .berlaku_sampai(berlakuSampaiDate)
                .nama(nama)
                .tanggal_lahir(tanggalLahir)
                .jenis_kelamin(jenisKelamin)
                .alamat(alamat)
                .profesi(profesi)
                .dibuat_di(dibuatDi)
                .dokumen_pendukung(dokumenList)
                .tertanda(tertanda)
                .status(status)
                .keterangan(keterangan)
                .build();

        // Step 3: Save to database
        System.out.println("Saving to database...");
        FormPengajuanDTO result = formPengajuanService.saveData(formPengajuanDTO);
        System.out.println("Data saved successfully with ID: " + result.getId());

        return ResponseEntity.ok(ApiResponse.created(result));

    } catch (Exception e) {
        System.err.println("=== UNEXPECTED ERROR ===");
        System.err.println("Error class: " + e.getClass().getName());
        System.err.println("Error message: " + e.getMessage());

        return ResponseEntity.internalServerError().body(
            ApiResponse.error(500, "Error processing request: " + e.getMessage())
        );
    }
}

    @PostMapping(value = "/test-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Test multipart upload")
    public ResponseEntity<ApiResponse<?>> testUpload(
            @RequestParam("name") String name,
            @RequestParam(value = "file", required = false) MultipartFile file) {
    
    try {
        String message = "Name: " + name;
        if (file != null && !file.isEmpty()) {
            message += ", File: " + file.getOriginalFilename() + " (" + file.getSize() + " bytes)";
        }
        
        return ResponseEntity.ok(ApiResponse.success(message, "Test successful"));
        
    } catch (Exception e) {
        return ResponseEntity.internalServerError().body(
            ApiResponse.error(500, "Test failed: " + e.getMessage())
        );
    }
}
@PostMapping(value = "/with-files-v2", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
@Operation(summary = "Simpan data pengajuan KTA dengan file upload (Alternative)")
public ResponseEntity<ApiResponse<?>> saveDataWithFilesV2(
        @RequestPart("formData") String formDataJson,
        @RequestPart(value = "files", required = false) List<MultipartFile> files) {
    
    try {
        // Parse the form data JSON
        FormPengajuanDTO formPengajuanDTO = objectMapper.readValue(formDataJson, FormPengajuanDTO.class);
        
        // Upload files if present
        List<DokumenPendukungDTO> dokumenList = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    String url = r2StorageService.upload(file);
                    
                    DokumenPendukungDTO dokumen = DokumenPendukungDTO.builder()
                            .url(url)
                            .fileName(file.getOriginalFilename())
                            .contentType(file.getContentType())
                            .build();
                    
                    dokumenList.add(dokumen);
                }
            }
        }
        
        formPengajuanDTO.setDokumen_pendukung(dokumenList);
        
        // Save to database
        FormPengajuanDTO result = formPengajuanService.saveData(formPengajuanDTO);
        
        return ResponseEntity.ok(ApiResponse.created(result));
        
    } catch (Exception e) {
        return ResponseEntity.internalServerError().body(
            ApiResponse.error(500, "Error processing request: " + e.getMessage())
        );
    }
}
@PostMapping(value = "/simple-test", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
@Operation(summary = "Simple multipart test")
public ResponseEntity<ApiResponse<?>> simpleTest(
        @RequestParam(value = "text", required = false) String text,
        @RequestParam(value = "file", required = false) MultipartFile file) {
    
    try {
        Map<String, Object> result = new HashMap<>();
        result.put("text", text);
        result.put("hasFile", file != null && !file.isEmpty());
        
        if (file != null && !file.isEmpty()) {
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", file.getSize());
            result.put("contentType", file.getContentType());
        }
        
        return ResponseEntity.ok(ApiResponse.success(result, "Simple test successful"));
        
    } catch (Exception e) {
        return ResponseEntity.internalServerError().body(
            ApiResponse.error(500, "Simple test failed: " + e.getMessage())
        );
    }
}
}