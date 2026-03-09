package cc.kertaskerja.pengajuan_kta.service.operasional;

import cc.kertaskerja.pengajuan_kta.dto.Operasional.OperasionalReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.Operasional.OperasionalResDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public interface OperasionalService {
    List<OperasionalResDTO> getAllData(String authHeader);

    OperasionalResDTO.DetailResponse findByUuidWithFiledAndProfile(String authHeader, UUID uuid);

    OperasionalResDTO saveData(OperasionalReqDTO.SaveData dto);

    OperasionalResDTO.FilePendukung uploadFilePendukung(MultipartFile file, String operasionalUuid, String namaFile);

    OperasionalResDTO.VerifyData verify(String authHeader, OperasionalReqDTO.Verify dto, UUID uuid);

    void deleteData(String uuid);
}
