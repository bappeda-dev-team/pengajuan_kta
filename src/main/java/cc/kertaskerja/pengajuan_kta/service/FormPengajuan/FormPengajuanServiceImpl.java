package cc.kertaskerja.pengajuan_kta.service.FormPengajuan;

import cc.kertaskerja.pengajuan_kta.dto.FormPengajuanDTO;
import cc.kertaskerja.pengajuan_kta.entity.FormPengajuan;
import cc.kertaskerja.pengajuan_kta.repository.FormPengajuanRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormPengajuanServiceImpl implements FormPengajuanService{

    private final FormPengajuanRepository formPengajuanRepository;
    private final ObjectMapper objectMapper;

    private FormPengajuanDTO toDTO(FormPengajuan entity) {
        if (entity == null) return null;

        return FormPengajuanDTO.builder()
                .id(entity.getId())
                .induk_organisasi(entity.getIndukOrganisasi())
                .nomor_induk(entity.getNomorInduk())
                .jumlah_anggota(entity.getJumlahAnggota() != null ? entity.getJumlahAnggota().toString() : null)
                .daerah(entity.getDaerah())
                .berlaku_dari(entity.getBerlakuDari())
                .berlaku_sampai(entity.getBerlakuSampai())
                .nama(entity.getNama())
                .tanggal_lahir(entity.getTanggalLahir())
                .jenis_kelamin(entity.getJenisKelamin())
                .alamat(entity.getAlamat())
                .profesi(entity.getProfesi())
                .dibuat_di(entity.getDibuatDi())
                .dokumen_pendukung(entity.getDokumenPendukung())
                .tertanda(entity.getTertanda())
                .status(entity.getStatus())
                .keterangan(entity.getKeterangan())
                .build();
    }

    @Override
    public List<FormPengajuanDTO> getAllData() {
        return formPengajuanRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FormPengajuanDTO saveData(FormPengajuanDTO dto) {
        try {
            FormPengajuan entity = FormPengajuan.builder()
                    .indukOrganisasi(dto.getInduk_organisasi())
                    .nomorInduk(dto.getNomor_induk())
                    .jumlahAnggota(Integer.parseInt(dto.getJumlah_anggota()))
                    .daerah(dto.getDaerah())
                    .berlakuDari(new Date(dto.getBerlaku_dari().getTime()))
                    .berlakuSampai(new Date(dto.getBerlaku_sampai().getTime()))
                    .nama(dto.getNama())
                    .tanggalLahir(dto.getTanggal_lahir())
                    .jenisKelamin(dto.getJenis_kelamin())
                    .alamat(dto.getAlamat())
                    .profesi(dto.getProfesi())
                    .dibuatDi(dto.getDibuat_di())
                    .dokumenPendukung(dto.getDokumen_pendukung())
                    .tertanda(dto.getTertanda())
                    .status(dto.getStatus() != null ? dto.getStatus() : "DRAFT")
                    .keterangan(dto.getKeterangan() != null ? dto.getKeterangan() : "-")
                    .build();

            FormPengajuan saved = formPengajuanRepository.save(entity);

            return toDTO(saved);
        } catch (Exception e) {
            throw new RuntimeException("Error saving form pengajuan: " + e.getMessage(), e);
        }
    }
}