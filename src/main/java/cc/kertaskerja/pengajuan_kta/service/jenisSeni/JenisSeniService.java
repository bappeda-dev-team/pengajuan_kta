package cc.kertaskerja.pengajuan_kta.service.jenisSeni;

import cc.kertaskerja.pengajuan_kta.dto.JenisSeni.JenisReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.JenisSeni.JenisResDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface JenisSeniService {
    List<JenisResDTO> findAll(String authHeader);
    JenisResDTO findById(String authHeader, Long id);
    JenisResDTO saveData(JenisReqDTO.SaveData dto);
    JenisResDTO updateData(String authHeader, Long id, JenisReqDTO.SaveData dto);
    void deleteData(String authHeader, Long id);
}
