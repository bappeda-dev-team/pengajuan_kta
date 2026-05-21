package cc.kertaskerja.pengajuan_kta.service.jenisOrganisasi;

import cc.kertaskerja.pengajuan_kta.dto.JenisOrganisasi.JenisReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.JenisOrganisasi.JenisResDTO;
import cc.kertaskerja.pengajuan_kta.entity.JenisOrganisasi;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface JenisOrganisasiService {
    List<JenisResDTO> findAll(String authHeader);
    JenisResDTO findById(String authHeader, Long id);
    JenisResDTO saveData(JenisReqDTO.SaveData dto);
    JenisResDTO updateData(String authHeader, Long id, JenisReqDTO.SaveData dto);
    void deleteData(String authHeader, Long id);
}
