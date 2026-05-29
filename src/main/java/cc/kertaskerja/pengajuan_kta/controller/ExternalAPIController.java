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
            FileDownloadDTO.DownloadRes fileData = r2FileService.downloadFilePendukung(token, fileId);
            ByteArrayResource resource = new ByteArrayResource(fileData.getData());

            // FIX: Deteksi paksa tipe file berdasarkan ekstensi nama file agar terbuka di new tab
            String contentTypeString = getContentTypeByFilename(fileData.getContentType(), fileData.getFilename());

            return ResponseEntity.ok()
                  .contentType(MediaType.parseMediaType(contentTypeString))
                  .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileData.getFilename() + "\"")
                  .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> uploadFile(
          @RequestHeader(name = "Authorization") String token,
          @RequestParam("file") MultipartFile file
    ) {
        try {
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

            String fileUrl = r2FileService.uploadFile(token, file);
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
            FileDownloadDTO.DownloadRes fileData = r2FileService.downloadFileByUrl(token, fileUrl);
            ByteArrayResource resource = new ByteArrayResource(fileData.getData());

            // FIX: Deteksi paksa tipe file berdasarkan ekstensi nama file agar terbuka di new tab
            String contentTypeString = getContentTypeByFilename(fileData.getContentType(), fileData.getFilename());

            return ResponseEntity.ok()
                  .contentType(MediaType.parseMediaType(contentTypeString))
                  .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileData.getFilename() + "\"")
                  .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // --- HELPER FUNCTION ---
    private String getContentTypeByFilename(String currentContentType, String filename) {
        if (currentContentType == null || currentContentType.isBlank() || currentContentType.equals("application/octet-stream")) {
            if (filename == null) return "application/octet-stream";

            String lowerCaseFilename = filename.toLowerCase();
            if (lowerCaseFilename.endsWith(".pdf")) {
                return "application/pdf";
            } else if (lowerCaseFilename.endsWith(".jpg") || lowerCaseFilename.endsWith(".jpeg")) {
                return "image/jpeg";
            } else if (lowerCaseFilename.endsWith(".png")) {
                return "image/png";
            }
        }
        return currentContentType != null ? currentContentType : "application/octet-stream";
    }
}