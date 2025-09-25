package cc.kertaskerja.pengajuan_kta.service;

import cc.kertaskerja.pengajuan_kta.dto.FilePendukungDTO;
import cc.kertaskerja.pengajuan_kta.dto.FormPengajuanReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.FormPengajuanResDTO;
import cc.kertaskerja.pengajuan_kta.dto.TertandaDTO;
import cc.kertaskerja.pengajuan_kta.entity.FilePendukung;
import cc.kertaskerja.pengajuan_kta.entity.FormPengajuan;
import cc.kertaskerja.pengajuan_kta.enums.StatusEnum;
import cc.kertaskerja.pengajuan_kta.repository.FilePendukungRepository;
import cc.kertaskerja.pengajuan_kta.repository.FormPengajuanRepository;
import cc.kertaskerja.pengajuan_kta.service.global.CloudStorage.R2StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FormPengajuanServiceImpl implements FormPengajuanService {
    private final FormPengajuanRepository formPengajuanRepository;
    private final FilePendukungRepository filePendukungRepository;
    private final R2StorageService r2StorageService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public FormPengajuanResDTO saveData(FormPengajuanReqDTO dto) {
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
              .status(StatusEnum.PENDING)
              .keterangan(dto.getKeterangan() != null ? dto.getKeterangan() : "-")
              .build();

        FormPengajuan saved = formPengajuanRepository.save(entity);

        return objectMapper.convertValue(saved, FormPengajuanResDTO.class);
    }

    @Override
    @Transactional
    public FilePendukungDTO uploadAndSaveFile(MultipartFile file, String formUuid, String namaFile) {
        try {
            // 1. Upload file ke R2
            String fileUrl = r2StorageService.upload(file);

            // 2. Gunakan namaFile dari request atau fallback ke original filename
            String finalNamaFile = namaFile != null ? namaFile : file.getOriginalFilename();

            // 3. Cari FormPengajuan berdasarkan UUID
            UUID formUuidParsed = UUID.fromString(formUuid);
            FormPengajuan formPengajuan = formPengajuanRepository.findByUuid(formUuidParsed)
                  .orElseThrow(() -> new RuntimeException("Form with UUID " + formUuid + " not found"));

            // 4. Simpan metadata ke database
            FilePendukung filePendukung = FilePendukung.builder()
                  .fileUrl(fileUrl)
                  .namaFile(finalNamaFile)
                  .formPengajuan(formPengajuan)
                  .build();

            FilePendukung savedFile = filePendukungRepository.save(filePendukung);

            // 5. Return DTO
            return FilePendukungDTO.builder()
                  .form_uuid(formUuid)
                  .file_url(savedFile.getFileUrl())
                  .nama_file(savedFile.getNamaFile())
                  .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload and save file: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FormPengajuanResDTO findByUuidWithFiles(UUID uuid) {
        FormPengajuan formPengajuan = formPengajuanRepository.findByUuidWithFiles(uuid)
              .orElseThrow(() -> new RuntimeException("Form with UUID " + uuid + " not found"));

        return FormPengajuanResDTO.builder()
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
              .status(formPengajuan.getStatus() != null ? formPengajuan.getStatus().name() : null)
              .keterangan(formPengajuan.getKeterangan())
              .tertanda(TertandaDTO.builder()
                    .nama(formPengajuan.getTertanda().getNama())
                    .tanda_tangan(formPengajuan.getTertanda().getTanda_tangan())
                    .jabatan(formPengajuan.getTertanda().getJabatan())
                    .nip(formPengajuan.getTertanda().getNip())
                    .pangkat(formPengajuan.getTertanda().getPangkat())
                    .build())
              .file_pendukung(formPengajuan.getFilePendukung().stream()
                    .map(file -> FormPengajuanResDTO.FilePendukung.builder()
                          .form_uuid(file.getFormPengajuan().getUuid().toString())
                          .file_url(file.getFileUrl())
                          .nama_file(file.getNamaFile())
                          .build())
                    .toList())
              .build();
    }

}
