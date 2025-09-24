package cc.kertaskerja.pengajuan_kta.service;

import cc.kertaskerja.pengajuan_kta.dto.FormPengajuanDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface FormPengajuanService {
    FormPengajuanDTO saveData(FormPengajuanDTO formPengajuanDTO);

    FormPengajuanDTO.FilePendukung uploadFile(FormPengajuanDTO.FilePendukung dto);

    FormPengajuanDTO findByUuidWithFiles(UUID uuid);
}
