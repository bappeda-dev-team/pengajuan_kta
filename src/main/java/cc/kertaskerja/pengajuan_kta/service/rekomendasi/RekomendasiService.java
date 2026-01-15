package cc.kertaskerja.pengajuan_kta.service.rekomendasi;

import cc.kertaskerja.pengajuan_kta.dto.Rekomendasi.FilePendukungDTO;
import cc.kertaskerja.pengajuan_kta.dto.Rekomendasi.RekomendasiReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.Rekomendasi.RekomendasiResDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface RekomendasiService {
    RekomendasiResDTO.SaveDataResponse saveData(RekomendasiReqDTO.SaveData dto);

    FilePendukungDTO uploadAndSaveFile(MultipartFile file, String rekomUuid, String namaFile);

    RekomendasiResDTO.RekomendasiResponse findByUuidWithFiles(UUID uuid);
}
