package cc.kertaskerja.pengajuan_kta.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileStorageService {
    String upload(MultipartFile file);
    byte[] download(String fileUrl);
    void delete(String fileUrl);
    Path getRootLocation();
}
