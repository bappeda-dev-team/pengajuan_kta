package cc.kertaskerja.pengajuan_kta.controller;

import cc.kertaskerja.pengajuan_kta.dto.ApiResponse;
import cc.kertaskerja.pengajuan_kta.dto.EncryptDTO;
import cc.kertaskerja.pengajuan_kta.dto.external.FileDownloadDTO;
import cc.kertaskerja.pengajuan_kta.service.external.EncryptService;
import cc.kertaskerja.pengajuan_kta.service.external.R2FileService;
import org.springframework.core.io.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/external")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ExternalAPIController {

    private final EncryptService encryptService;
    private final R2FileService r2FileService;

    @PostMapping("/encrypt")
    public ResponseEntity<ApiResponse<?>> encrypt(@Valid @RequestBody EncryptDTO request,
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

        String encrypted = encryptService.encrypt(request.getData());

        return ResponseEntity.ok(ApiResponse.success(encrypted, "Encrypted successfully"));
    }

    @PostMapping("/decrypt")
    public ResponseEntity<ApiResponse<?>> decrypt(@Valid @RequestBody EncryptDTO request,
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

        String decrypted = encryptService.decrypt(request.getData());

        return ResponseEntity.ok(ApiResponse.success(decrypted, "Decrypted successfully"));
    }

    @GetMapping("/file/{fileId}")
    public ResponseEntity<Resource> downloadFile(
          @RequestHeader(name = "Authorization") String token,
          @PathVariable Long fileId
    ) {
        try {
            // 1. Panggil service
            FileDownloadDTO.DownloadRes fileData = r2FileService.downloadFilePendukung(token, fileId);

            // 2. Buat Resource
            ByteArrayResource resource = new ByteArrayResource(fileData.getData());

            // 3. Handle Content Type (Jaga-jaga jika null)
            String contentTypeString = fileData.getContentType();
            if (contentTypeString == null || contentTypeString.isBlank()) {
                contentTypeString = "application/octet-stream"; // Default type
            }

            return ResponseEntity.ok()
                  .contentType(MediaType.parseMediaType(contentTypeString))
                  .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileData.getFilename() + "\"")
                  .body(resource);

        } catch (Exception e) {
            // Log error ke console server agar terbaca
            e.printStackTrace();

            // Kembalikan error yang bisa dibaca di Postman/Browser
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> uploadFile(
          @RequestHeader(name = "Authorization") String token,
          @RequestParam("file") MultipartFile file
    ) {
        try {
            // Check if file is empty
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                      ApiResponse.builder()
                            .success(false)
                            .statusCode(400)
                            .message("File is empty")
                            .timestamp(LocalDateTime.now())
                            .build()
                );
            }

            // 1. Call service to upload
            String fileUrl = r2FileService.uploadFile(token, file);

            // 2. Return the URL wrapped in ApiResponse
            return ResponseEntity.ok(ApiResponse.success(fileUrl, "File uploaded successfully"));

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.internalServerError().body(
                  ApiResponse.builder()
                        .success(false)
                        .statusCode(500)
                        .message("Failed to upload file: " + e.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build()
            );
        }
    }

    @GetMapping("/show")
    public ResponseEntity<Resource> showFileByUrl(
          @RequestHeader(name = "Authorization") String token,
          @RequestParam(name = "url") String fileUrl
    ) {
        try {
            // 1. Panggil service menggunakan URL
            FileDownloadDTO.DownloadRes fileData = r2FileService.downloadFileByUrl(token, fileUrl);

            // 2. Buat Resource
            ByteArrayResource resource = new ByteArrayResource(fileData.getData());

            // 3. Handle Content Type
            String contentTypeString = fileData.getContentType();
            if (contentTypeString == null || contentTypeString.isBlank()) {
                contentTypeString = "application/octet-stream";
            }

            // Gunakan inline agar gambar langsung tampil di browser (tidak auto-download)
            // Jika ingin auto-download, ganti "inline" menjadi "attachment"
            return ResponseEntity.ok()
                  .contentType(MediaType.parseMediaType(contentTypeString))
                  .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileData.getFilename() + "\"")
                  .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
