package cc.kertaskerja.pengajuan_kta.service.rekomendasi;

import cc.kertaskerja.pengajuan_kta.dto.Auth.AccountResponse;
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
import cc.kertaskerja.pengajuan_kta.exception.ForbiddenException;
import cc.kertaskerja.pengajuan_kta.exception.ResourceNotFoundException;
import cc.kertaskerja.pengajuan_kta.exception.UnauthorizedException;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    public List<RekomendasiResDTO.RekomendasiResponse> findAll(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);
            Map<String, Object> claims = jwtTokenProvider.parseToken(token);

            String role = (String) claims.get("role");
            String nik = String.valueOf(claims.get("sub"));

            List<SuratRekomendasi> suratRekomendasi;
            if ("ADMIN".equalsIgnoreCase(role)) {
                suratRekomendasi = repository.findAllByStatusInWithAccount(
                      List.of(
                            StatusPengajuanEnum.APPROVED,
                            StatusPengajuanEnum.REJECTED,
                            StatusPengajuanEnum.PENDING_VERIFICATOR
                      )
                );
            } else if ("KEPALA".equalsIgnoreCase(role)) {
                suratRekomendasi = repository.findAllByStatusInWithAccount(
                      List.of(
                            StatusPengajuanEnum.APPROVED,
                            StatusPengajuanEnum.REJECTED,
                            StatusPengajuanEnum.PENDING_APPROVAL
                      )
                );
            } else {
                suratRekomendasi = repository.findByAccId(nik);
            }

            return suratRekomendasi.stream()
                  .map(rekom -> RekomendasiResDTO.RekomendasiResponse.builder()
                        .uuid(rekom.getUuid())
                        .nama(rekom.getAccount().getNama())
                        .nomor_induk(rekom.getNomorInduk())
                        .tempat(rekom.getTempat())
                        .tujuan(rekom.getTujuan())
                        .tanggal(rekom.getTanggal())
                        .status(rekom.getStatus() != null ? rekom.getStatus().name() : null)
                        .build()
                  ).toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get all pengajuan: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public RekomendasiResDTO.SaveDataResponse saveData(RekomendasiReqDTO.SaveData dto) {
        Account account = accountRepository.findByNik(encryptService.encrypt(dto.getNik()))
              .orElseThrow(() -> new ResourceNotFoundException("NIK not found: " + encryptService.decrypt(dto.getNik())));

        if (repository.existsByNomorSurat(dto.getNomor_induk())) {
            throw new ConflictException("Nomor induk '" + dto.getNomor_induk() + "' sudah terdaftar.");
        }

        try {
            SuratRekomendasi entity = SuratRekomendasi.builder()
                  .account(account)
                  .uuid(UUID.randomUUID())
                  .nomorInduk(dto.getNomor_induk())
                  .tujuan(dto.getTujuan())
                  .tanggal(dto.getTanggal())
                  .tempat(dto.getTempat())
                  .status(StatusPengajuanEnum.PENDING_VERIFICATOR)
                  .keterangan(dto.getKeterangan())
                  .build();

            SuratRekomendasi saved = repository.save(entity);

            return RekomendasiResDTO.SaveDataResponse.builder()
                  .uuid(saved.getUuid())
                  .nomor_induk(saved.getNomorInduk())
                  .tujuan(saved.getTujuan())
                  .tanggal(saved.getTanggal())
                  .tempat(saved.getTempat())
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
    public RekomendasiResDTO.SaveDataResponse editDataRekomendasi(String authHeader, UUID uuid, RekomendasiReqDTO.SaveData dto) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Map<String, Object> claims = jwtTokenProvider.parseToken(token);
        String userId = String.valueOf(claims.get("uid"));

        SuratRekomendasi rekomendasi = repository.findByUuid(uuid)
              .orElseThrow(() -> new ResourceNotFoundException("Data rekomendasi with UUID " + uuid + " is not found"));

        if (!userId.equals(rekomendasi.getAccount().getId().toString())) {
            throw new ForbiddenException("Data rekomendasi that has been changed is not yours.");
        }

        if (rekomendasi.getStatus() == StatusPengajuanEnum.APPROVED) {
            throw new ConflictException("Data rekomendasi that has been approved cannot be edited.");
        }

        try {
            rekomendasi
                  .setNomorInduk(dto.getNomor_induk())
                  .setTujuan(dto.getTujuan())
                  .setTanggal(dto.getTanggal())
                  .setTempat(dto.getTempat())
                  .setStatus(StatusPengajuanEnum.PENDING_VERIFICATOR)
                  .setKeterangan(dto.getKeterangan() != null ? dto.getKeterangan() : "-");

            repository.save(rekomendasi);

            return RekomendasiResDTO.SaveDataResponse.builder()
                  .uuid(rekomendasi.getUuid())
                  .nomor_induk(rekomendasi.getNomorInduk())
                  .tujuan(rekomendasi.getTujuan())
                  .tanggal(rekomendasi.getTanggal())
                  .tempat(rekomendasi.getTempat())
                  .status(rekomendasi.getStatus().name())
                  .keterangan(rekomendasi.getKeterangan())
                  .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to change data pengajuan: " + e.getMessage());
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
    public RekomendasiResDTO.RekomendasiWithProfileResponse findByUuidWithFilesAndProfile(String authHeader, UUID uuid) {
        SuratRekomendasi suratRekomendasi = repository.findByUuidWithFilesAndAccount(uuid)
              .orElseThrow(() -> new ResourceNotFoundException("Rekomendasi with UUID " + uuid + " not found"));

        Account owner = suratRekomendasi.getAccount();

        String token = authHeader.substring(7);
        Map<String, Object> claims = jwtTokenProvider.parseToken(token);
        String nik = String.valueOf(claims.get("sub"));
        String role = String.valueOf(claims.get("role"));

        if (!owner.getNik().equals(nik) && !role.equals("ADMIN")) {
            throw new ForbiddenException("Data pengajuan is not yours.");
        };

        AccountResponse.Detail profile = AccountResponse.Detail.builder()
              .id(owner.getId())
              .nama(owner.getNama())
              .email(owner.getEmail())
              .nik(owner.getNik())
              .nomorTelepon(owner.getNomorTelepon())
              .tempatLahir(owner.getTempatLahir())
              .tanggalLahir(owner.getTanggalLahir())
              .jenisKelamin(owner.getJenisKelamin())
              .alamat(owner.getAlamat())
              .tipeAkun(owner.getTipeAkun())
              .status(owner.getStatus() != null ? owner.getStatus().name() : null)
              .role(owner.getRole())
              .is_assigned(owner.getIsAssigned())
              .createdAt(owner.getCreatedAt())
              .updatedAt(owner.getUpdatedAt())
              .build();

        RekomendasiResDTO.RekomendasiResponse rekomendasi = RekomendasiResDTO.RekomendasiResponse.builder()
              .uuid(suratRekomendasi.getUuid())
              .nomor_surat(suratRekomendasi.getNomorSurat())
              .nomor_induk(suratRekomendasi.getNomorInduk())
              .tujuan(suratRekomendasi.getTujuan())
              .tanggal(suratRekomendasi.getTanggal())
              .tempat(suratRekomendasi.getTempat())
              .tanggal_berlaku(suratRekomendasi.getTanggalBerlaku())
              .tanggal_surat(suratRekomendasi.getTanggalSurat())
              .status(suratRekomendasi.getStatus() != null ? suratRekomendasi.getStatus().name() : null)
              .tertanda(suratRekomendasi.getTertanda())
              .keterangan(suratRekomendasi.getKeterangan())
              .file_pendukung(suratRekomendasi.getFilePendukung().stream()
                    .map(file -> RekomendasiResDTO.FilePendukung.builder()
                          .rekom_uuid(file.getSuratRekomendasi().getUuid().toString())
                          .file_url(file.getFileUrl())
                          .nama_file(file.getNamaFile())
                          .build())
                    .toList())
              .created_at(suratRekomendasi.getCreatedAt())
              .build();

        return RekomendasiResDTO.RekomendasiWithProfileResponse.builder()
              .rekomendasi(rekomendasi)
              .profile(profile)
              .build();
    }

    @Override
    @Transactional
    public RekomendasiResDTO.VerifyData verifyDataRekomendasi(String authHeader, UUID uuid, RekomendasiReqDTO.Verify dto) {
        try {
            String token = authHeader.substring(7);
            Map<String, Object> claims = jwtTokenProvider.parseToken(token);
            String role = String.valueOf(claims.get("role"));

            Set<String> allowedRoles = Set.of("ADMIN", "KEPALA");

            if (!allowedRoles.contains(role)) {
                throw new ForbiddenException("You are not allowed to verify surat rekomendasi.");
            }

            SuratRekomendasi rekomendasi = repository.findByUuid(uuid)
                  .orElseThrow(() -> new ResourceNotFoundException("Data rekomendasi with UUID " + uuid + " is not found"));

            rekomendasi.setNomorSurat(dto.getNomor_surat());
            rekomendasi.setStatus(dto.getStatus() != null ? StatusPengajuanEnum.valueOf(dto.getStatus()) : StatusPengajuanEnum.PENDING_VERIFICATOR);
            rekomendasi.setTertanda(dto.getTertanda());
            rekomendasi.setTanggalBerlaku(dto.getTanggal_berlaku());
            rekomendasi.setCatatan(dto.getCatatan());
            rekomendasi.setTanggalSurat(LocalDateTime.now());

            repository.save(rekomendasi);

            return RekomendasiResDTO.VerifyData.builder()
                  .nomor_surat(rekomendasi.getNomorSurat())
                  .nomor_induk(rekomendasi.getNomorInduk())
                  .status(rekomendasi.getStatus().name())
                  .tanggal_berlaku(rekomendasi.getTanggalBerlaku())
                  .tertanda(rekomendasi.getTertanda())
                  .catatan(rekomendasi.getCatatan())
                  .tanggal_surat(rekomendasi.getTanggalSurat())
                  .build();

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to verified data pengajuan: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteData(UUID uuid) {
        SuratRekomendasi suratRekomendasi = repository.findByUuidWithFiles(uuid)
              .orElseThrow(() -> new ResourceNotFoundException(
                    "Surat rekomendasi with UUID " + uuid + " not found"));

        if (suratRekomendasi.getStatus() == StatusPengajuanEnum.APPROVED) {
            throw new ConflictException("Data rekomendasi that has been approved cannot be deleted.");
        }

        // 1️⃣ Delete file in storage
        if (suratRekomendasi.getFilePendukung() != null) {
            for (FilePendukung file : suratRekomendasi.getFilePendukung()) {
                if (file.getFileUrl() != null && !file.getFileUrl().isBlank()) {
                    r2StorageService.delete(file.getFileUrl());
                }
            }
        }

        // 2️⃣ Delete child records (FK safe)
        filePendukungRepository.deleteBySuratRekomendasi(suratRekomendasi);
        filePendukungRepository.flush();

        // ✅ 3️⃣ Delete parent (FIX)
        repository.delete(suratRekomendasi);
    }
}
