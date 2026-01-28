package cc.kertaskerja.pengajuan_kta.service.organisasi;

import cc.kertaskerja.pengajuan_kta.dto.Organisasi.OrganisasiReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.Organisasi.OrganisasiResDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public interface OrganisasiService {
    List<OrganisasiResDTO> findAllOrganisasi(String authHeader);

    OrganisasiResDTO.DetailResponse detailOrganisasi(String authHeader, UUID uuid);

    OrganisasiResDTO.SaveResponse saveData(OrganisasiReqDTO.SaveData dto);

    OrganisasiResDTO.SaveResponse editDataOrganisasi(String authHeader, UUID uuid, OrganisasiReqDTO.SaveData dto);

    OrganisasiResDTO.FilePendukung uploadFilePendukung(MultipartFile file, String organisasiUuid, String namaFile);

    void deleteData(String uuid);

    void deleteFilePendukung(String authHeader, Long id);
}
