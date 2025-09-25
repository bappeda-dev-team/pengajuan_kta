package cc.kertaskerja.pengajuan_kta.service;

import cc.kertaskerja.pengajuan_kta.dto.FilePendukungDTO;
import cc.kertaskerja.pengajuan_kta.dto.FormPengajuanReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.FormPengajuanResDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public interface FormPengajuanService {
    FormPengajuanResDTO saveData(FormPengajuanReqDTO formPengajuanDTO);

    FilePendukungDTO uploadAndSaveFile(MultipartFile file, String formUuid, String namaFile);

    FormPengajuanResDTO findByUuidWithFiles(UUID uuid);
}
