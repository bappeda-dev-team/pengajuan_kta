package cc.kertaskerja.pengajuan_kta.service.FormPengajuan;

import cc.kertaskerja.pengajuan_kta.dto.FormPengajuanDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FormPengajuanService {
    List<FormPengajuanDTO> getAllData();
    FormPengajuanDTO saveData(FormPengajuanDTO formPengajuanDTO);
}
