package cc.kertaskerja.pengajuan_kta.service.rekomendasi;

import cc.kertaskerja.pengajuan_kta.dto.Pengajuan.FormPengajuanResDTO;
import cc.kertaskerja.pengajuan_kta.dto.Rekomendasi.FilePendukungDTO;
import cc.kertaskerja.pengajuan_kta.dto.Rekomendasi.RekomendasiReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.Rekomendasi.RekomendasiResDTO;
import cc.kertaskerja.pengajuan_kta.entity.Account;
import cc.kertaskerja.pengajuan_kta.entity.FilePendukung;
import cc.kertaskerja.pengajuan_kta.entity.FormPengajuan;
import cc.kertaskerja.pengajuan_kta.entity.SuratRekomendasi;
import cc.kertaskerja.pengajuan_kta.enums.StatusPengajuanEnum;
import cc.kertaskerja.pengajuan_kta.exception.ConflictException;
import cc.kertaskerja.pengajuan_kta.exception.ResourceNotFoundException;
import cc.kertaskerja.pengajuan_kta.repository.AccountRepository;
import cc.kertaskerja.pengajuan_kta.repository.FilePendukungRepository;
import cc.kertaskerja.pengajuan_kta.repository.SuratRekomendasiRepository;
import cc.kertaskerja.pengajuan_kta.security.JwtTokenProvider;
import cc.kertaskerja.pengajuan_kta.service.external.EncryptService;
import cc.kertaskerja.pengajuan_kta.service.global.R2StorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RekomendasiServiceImpl implements RekomendasiService {
    private final AccountRepository accountRepository;
    private final SuratRekomendasiRepository repository;
    private final FilePendukungRepository filePendukungRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final EncryptService encryptService;
    private final R2StorageService r2StorageService;

    @Override
    @Transactional
    public RekomendasiResDTO.SaveDataResponse saveData(RekomendasiReqDTO.SaveData dto) {
        Account account = accountRepository.findByNik(dto.getNik())
              .orElseThrow(() -> new ResourceNotFoundException("NIK not found: " + encryptService.decrypt(dto.getNik())));

        if (repository.existsByNomorSurat(dto.getNomor_surat())) {
            throw new ConflictException("Nomor surat '" + dto.getNomor_surat() + "' sudah terdaftar.");
        }

        try {
            SuratRekomendasi entity = SuratRekomendasi.builder()
                  .account(account)
                  .uuid(UUID.randomUUID())
                  .nomorSurat(dto.getNomor_surat())
                  .nomorInduk(dto.getNomor_induk())
                  .tujuan(dto.getTujuan())
                  .tanggal(dto.getTanggal())
                  .tempat(dto.getTempat())
                  .tanggalBerlaku(dto.getTanggal_berlaku())
                  .status(StatusPengajuanEnum.PENDING)
                  .keterangan(dto.getKeterangan())
                  .build();

            SuratRekomendasi saved = repository.save(entity);

            return RekomendasiResDTO.SaveDataResponse.builder()
                  .uuid(saved.getUuid())
                  .nomor_surat(saved.getNomorSurat())
                  .nomor_induk(saved.getNomorInduk())
                  .tujuan(saved.getTujuan())
                  .tanggal(saved.getTanggal())
                  .tempat(saved.getTempat())
                  .tanggal_berlaku(saved.getTanggalBerlaku())
                  .status(saved.getStatus().name())
                  .keterangan(saved.getKeterangan())
                  .build();
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Data integrity violation. Please check NOT NULL, UNIQUE, or foreign key constraints.", e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error occurred while saving surat rekomendasi", e);
        }
    }

    @Override
    @Transactional
    public FilePendukungDTO uploadAndSaveFile(MultipartFile file, String rekomUuid, String namaFile) {
        try {
            String fileUrl = r2StorageService.upload(file);
            String finalNamaFile = namaFile != null ? namaFile : file.getOriginalFilename();

            UUID rekomUuidParsed = UUID.fromString(rekomUuid);
            SuratRekomendasi rekomendasi = repository.findByUuid(rekomUuidParsed)
                  .orElseThrow(() -> new ResourceNotFoundException("Form with UUID " + rekomUuid + " not found"));

            FilePendukung filePendukung = FilePendukung.builder()
                  .fileUrl(fileUrl)
                  .namaFile(finalNamaFile)
                  .suratRekomendasi(rekomendasi)
                  .build();

            FilePendukung savedFile = filePendukungRepository.save(filePendukung);

            return FilePendukungDTO.builder()
                  .rekom_uuid(rekomUuid)
                  .file_url(savedFile.getFileUrl())
                  .nama_file(savedFile.getNamaFile())
                  .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload and save file: " + e.getMessage(), e);
        }
    }

    @Override
    public RekomendasiResDTO.RekomendasiResponse findByUuidWithFiles(UUID uuid) {
        SuratRekomendasi rekomendasi = repository.findByUuidWithFiles(uuid)
              .orElseThrow(() -> new ResourceNotFoundException("Rekomendasi with UUID " + uuid + " not found"));

        return RekomendasiResDTO.RekomendasiResponse.builder()
              .uuid(rekomendasi.getUuid())
              .nomor_surat(rekomendasi.getNomorSurat())
              .nomor_induk(rekomendasi.getNomorInduk())
              .tujuan(rekomendasi.getTujuan())
              .tanggal(rekomendasi.getTanggal())
              .tempat(rekomendasi.getTempat())
              .tanggal_berlaku(rekomendasi.getTanggalBerlaku())
              .status(rekomendasi.getStatus() != null ? rekomendasi.getStatus().name() : null)
              .keterangan(rekomendasi.getKeterangan())
              .file_pendukung(rekomendasi.getFilePendukung().stream()
                    .map(file -> RekomendasiResDTO.FilePendukung.builder()
                          .rekom_uuid(file.getSuratRekomendasi().getUuid().toString())
                          .file_url(file.getFileUrl())
                          .nama_file(file.getNamaFile())
                          .build())
                    .toList())
              .build();
    }
}
