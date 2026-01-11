package cc.kertaskerja.pengajuan_kta.service.rekomendasi;

import cc.kertaskerja.pengajuan_kta.dto.Rekomendasi.RekomendasiReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.Rekomendasi.RekomendasiResDTO;

public interface RekomendasiService {
    RekomendasiResDTO.SaveDataResponse saveData(RekomendasiReqDTO.SaveData dto);
}
