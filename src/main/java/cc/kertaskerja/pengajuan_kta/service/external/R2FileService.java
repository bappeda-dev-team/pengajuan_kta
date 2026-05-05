package cc.kertaskerja.pengajuan_kta.service.external;

import cc.kertaskerja.pengajuan_kta.dto.external.FileDownloadDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface R2FileService {
    String uploadFile(String authHeader, MultipartFile file) throws IOException;

    FileDownloadDTO.DownloadRes downloadFilePendukung(String authHeader, Long fileId);

    FileDownloadDTO.DownloadRes downloadFileByUrl(String authHeader, String fileUrl);
}
