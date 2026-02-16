package cc.kertaskerja.pengajuan_kta.service.external;

import cc.kertaskerja.pengajuan_kta.dto.external.FileDownloadDTO;

public interface R2FileService {
    FileDownloadDTO.DownloadRes downloadFilePendukung(String authHeader, Long fileId);
}
