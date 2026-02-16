package cc.kertaskerja.pengajuan_kta.service.external;

import cc.kertaskerja.pengajuan_kta.dto.external.FileDownloadDTO;
import cc.kertaskerja.pengajuan_kta.entity.FilePendukung;
import cc.kertaskerja.pengajuan_kta.exception.ResourceNotFoundException;
import cc.kertaskerja.pengajuan_kta.exception.UnauthorizedException;
import cc.kertaskerja.pengajuan_kta.repository.FilePendukungRepository;
import cc.kertaskerja.pengajuan_kta.service.global.R2StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class R2FileServiceImpl implements R2FileService {
    private final FilePendukungRepository filePendukungRepository;
    private final R2StorageService r2StorageService;

    @Override
    public FileDownloadDTO.DownloadRes downloadFilePendukung(String authHeader, Long fileId) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        FilePendukung filePendukung = filePendukungRepository.findById(fileId)
              .orElseThrow(() -> new ResourceNotFoundException("File Pendukung not found with ID: " + fileId));

        return r2StorageService.download(filePendukung.getFileUrl());
    }
}
