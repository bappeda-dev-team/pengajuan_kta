package cc.kertaskerja.pengajuan_kta.controller;

import cc.kertaskerja.pengajuan_kta.service.CloudStorage.R2StorageService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/r2")
public class R2FileController {

    private final R2StorageService r2Service;

    public R2FileController(R2StorageService r2Service) {
        this.r2Service = r2Service;
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam String key, @RequestParam String content) {
        r2Service.upload(key, content);
        return "Uploaded successfully!";
    }

    @GetMapping("/download")
    public String downloadFile(@RequestParam String key) {
        return r2Service.download(key);
    }
}