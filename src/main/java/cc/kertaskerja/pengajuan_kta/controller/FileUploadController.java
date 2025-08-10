package cc.kertaskerja.pengajuan_kta.controller;

import cc.kertaskerja.pengajuan_kta.service.CloudStorage.R2StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/r2")
public class FileUploadController {
    private final R2StorageService storageService;

    public FileUploadController(R2StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) throws Exception {
        String url = storageService.upload(file);
            return ResponseEntity.ok(url);
    }
}
