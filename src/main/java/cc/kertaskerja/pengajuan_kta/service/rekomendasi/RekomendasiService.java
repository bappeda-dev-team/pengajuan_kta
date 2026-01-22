package cc.kertaskerja.pengajuan_kta.service.rekomendasi;

import cc.kertaskerja.pengajuan_kta.dto.Rekomendasi.FilePendukungDTO;
import cc.kertaskerja.pengajuan_kta.dto.Rekomendasi.RekomendasiReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.Rekomendasi.RekomendasiResDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface RekomendasiService {
    List<RekomendasiResDTO.RekomendasiResponse> findAll(String authHeader);

    RekomendasiResDTO.SaveDataResponse saveData(RekomendasiReqDTO.SaveData dto);

    RekomendasiResDTO.SaveDataResponse editDataRekomendasi(String authHeader, UUID uuid, RekomendasiReqDTO.SaveData dto);

    FilePendukungDTO uploadAndSaveFile(MultipartFile file, String rekomUuid, String namaFile);

    RekomendasiResDTO.RekomendasiWithProfileResponse findByUuidWithFilesAndProfile(String authHeader, UUID uuid);

    RekomendasiResDTO.VerifyData verifyDataRekomendasi(String authHeader, UUID uuid, RekomendasiReqDTO.Verify dto);

    void deleteData(UUID uuid);

    void deleteFilePendukung(String authHeader, Long id);
}
