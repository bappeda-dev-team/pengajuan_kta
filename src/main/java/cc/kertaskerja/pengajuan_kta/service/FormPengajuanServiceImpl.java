package cc.kertaskerja.pengajuan_kta.service;

import cc.kertaskerja.pengajuan_kta.dto.FormPengajuanDTO;
import cc.kertaskerja.pengajuan_kta.dto.TertandaDTO;
import cc.kertaskerja.pengajuan_kta.entity.FilePendukung;
import cc.kertaskerja.pengajuan_kta.entity.FormPengajuan;
import cc.kertaskerja.pengajuan_kta.repository.FilePendukungRepository;
import cc.kertaskerja.pengajuan_kta.repository.FormPengajuanRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FormPengajuanServiceImpl implements FormPengajuanService {
    private final FormPengajuanRepository formPengajuanRepository;
    private final FilePendukungRepository filePendukungRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public FormPengajuanDTO saveData(FormPengajuanDTO dto) {
        FormPengajuan entity = FormPengajuan.builder()
              .uuid(UUID.randomUUID())
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
              .tertanda(dto.getTertanda())
              .status(dto.getStatus() != null ? dto.getStatus() : "DRAFT")
              .keterangan(dto.getKeterangan() != null ? dto.getKeterangan() : "-")
              .build();

        FormPengajuan saved = formPengajuanRepository.save(entity);

        return objectMapper.convertValue(saved, FormPengajuanDTO.class);
    }

    @Override
    @Transactional
    public FormPengajuanDTO.FilePendukung uploadFile(FormPengajuanDTO.FilePendukung dto) {
        try {
            // 1. Find the FormPengajuan by UUID
            UUID formUuidParsed = UUID.fromString(dto.getForm_uuid());
            FormPengajuan formPengajuan = formPengajuanRepository.findByUuid(formUuidParsed)
                  .orElseThrow(() -> new RuntimeException("Form with UUID " + dto.getForm_uuid() + " not found"));

            // 2. Create and save FilePendukung entity
            FilePendukung filePendukung = FilePendukung.builder()
                  .fileUrl(dto.getFile_url())
                  .namaFile(dto.getNama_file())
                  .formPengajuan(formPengajuan)
                  .build();

            FilePendukung savedFile = filePendukungRepository.save(filePendukung);

            // 3. Return the DTO
            return FormPengajuanDTO.FilePendukung.builder()
                  .form_uuid(dto.getForm_uuid())
                  .file_url(savedFile.getFileUrl())
                  .nama_file(savedFile.getNamaFile())
                  .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload and save file: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FormPengajuanDTO findByUuidWithFiles(UUID uuid) {
        FormPengajuan formPengajuan = formPengajuanRepository.findByUuidWithFiles(uuid)
              .orElseThrow(() -> new RuntimeException("Form with UUID " + uuid + " not found"));

        return FormPengajuanDTO.builder()
              .uuid(formPengajuan.getUuid())
              .induk_organisasi(formPengajuan.getIndukOrganisasi())
              .nomor_induk(formPengajuan.getNomorInduk())
              .jumlah_anggota(formPengajuan.getJumlahAnggota() != null ? formPengajuan.getJumlahAnggota().toString () : String.valueOf (0L))
              .daerah(formPengajuan.getDaerah())
              .berlaku_dari(formPengajuan.getBerlakuDari())
              .berlaku_sampai(formPengajuan.getBerlakuSampai())
              .nama(formPengajuan.getNama())
              .tanggal_lahir(formPengajuan.getTanggalLahir())
              .jenis_kelamin(formPengajuan.getJenisKelamin())
              .alamat(formPengajuan.getAlamat())
              .profesi(formPengajuan.getProfesi())
              .dibuat_di(formPengajuan.getDibuatDi())
              .status(formPengajuan.getStatus())
              .keterangan(formPengajuan.getKeterangan())
              .tertanda(TertandaDTO.builder()
                    .nama(formPengajuan.getTertanda().getNama())
                    .tanda_tangan(formPengajuan.getTertanda().getTanda_tangan())
                    .jabatan(formPengajuan.getTertanda().getJabatan())
                    .nip(formPengajuan.getTertanda().getNip())
                    .pangkat(formPengajuan.getTertanda().getPangkat())
                    .build())
              .file_pendukung(formPengajuan.getFilePendukung().stream()
                    .map(file -> FormPengajuanDTO.FilePendukung.builder()
                          .form_uuid(file.getFormPengajuan().getUuid().toString())
                          .file_url(file.getFileUrl())
                          .nama_file(file.getNamaFile())
                          .build())
                    .toList())
              .build();
    }

}
