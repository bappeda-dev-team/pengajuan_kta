package cc.kertaskerja.pengajuan_kta.service.pengajuan;

import cc.kertaskerja.pengajuan_kta.dto.Pengajuan.FilePendukungDTO;
import cc.kertaskerja.pengajuan_kta.dto.Pengajuan.FormPengajuanReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.Pengajuan.FormPengajuanResDTO;
import cc.kertaskerja.pengajuan_kta.entity.FormPengajuan;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public interface FormPengajuanService {
    List<FormPengajuanResDTO.PengajuanResponse> findAllDataPengajuan(String authHeader);

    FormPengajuanResDTO.SaveDataResponse saveData(FormPengajuanReqDTO.SavePengajuan formPengajuanDTO);

    FilePendukungDTO uploadAndSaveFile(MultipartFile file, String formUuid, String namaFile);

    FormPengajuanResDTO.PengajuanResponse findByUuidWithFiles(UUID uuid);

    FormPengajuanResDTO.PengajuanWithProfileResponse findByUuidWithFilesAndProfile(String authHeader, UUID uuid);

    FormPengajuanResDTO.SaveDataResponse editDataPengajuan(String authHeader, UUID uuid, FormPengajuanReqDTO.SavePengajuan dto);

    FormPengajuanResDTO.VerifyData verifyDataPengajuan(FormPengajuanReqDTO.VerifyPengajuan dto, UUID uuid);

    String editIsAssignedInAccount(String nik);

    void deleteData(String uuid);
}
