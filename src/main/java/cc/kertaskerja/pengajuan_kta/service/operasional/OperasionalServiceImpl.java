package cc.kertaskerja.pengajuan_kta.service.operasional;

import cc.kertaskerja.pengajuan_kta.dto.Auth.AccountResponse;
import cc.kertaskerja.pengajuan_kta.dto.Operasional.OperasionalReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.Operasional.OperasionalResDTO;
import cc.kertaskerja.pengajuan_kta.dto.Organisasi.OrganisasiResDTO;
import cc.kertaskerja.pengajuan_kta.dto.Pengajuan.FormPengajuanResDTO;
import cc.kertaskerja.pengajuan_kta.entity.*;
import cc.kertaskerja.pengajuan_kta.enums.StatusPengajuanEnum;
import cc.kertaskerja.pengajuan_kta.exception.BadRequestException;
import cc.kertaskerja.pengajuan_kta.exception.ForbiddenException;
import cc.kertaskerja.pengajuan_kta.exception.ResourceNotFoundException;
import cc.kertaskerja.pengajuan_kta.exception.UnauthorizedException;
import cc.kertaskerja.pengajuan_kta.repository.AccountRepository;
import cc.kertaskerja.pengajuan_kta.repository.FilePendukungRepository;
import cc.kertaskerja.pengajuan_kta.repository.FormPengajuanRepository;
import cc.kertaskerja.pengajuan_kta.repository.IzinOperasionalRepository;
import cc.kertaskerja.pengajuan_kta.security.JwtTokenProvider;
import cc.kertaskerja.pengajuan_kta.service.external.EncryptService;
import cc.kertaskerja.pengajuan_kta.service.global.R2StorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OperasionalServiceImpl implements OperasionalService {
    private final IzinOperasionalRepository operasionalRepository;
    private final AccountRepository accountRepository;
    private final FormPengajuanRepository formPengajuanRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final FilePendukungRepository filePendukungRepository;
    private final R2StorageService r2StorageService;
    private final EncryptService encryptService;

    @Override
    public List<OperasionalResDTO> getAllData(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new UnauthorizedException("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);
            Map<String, Object> claims = jwtTokenProvider.parseToken(token);

            String role = String.valueOf(claims.get("role"));
            String nik = String.valueOf(claims.get("sub"));

            List<IzinOperasional> result = new java.util.ArrayList<>();

            if ("ADMIN".equalsIgnoreCase(role)) {
                result.addAll(
                      operasionalRepository.findAllByStatusInWithAccount(
                            List.of(
                                  StatusPengajuanEnum.APPROVED,
                                  StatusPengajuanEnum.REJECTED,
                                  StatusPengajuanEnum.PENDING_VERIFICATOR
                            )
                      )
                );
                result.addAll(operasionalRepository.findByAccIdWithAccount(nik));

            } else if ("KEPALA".equalsIgnoreCase(role)) {
                result.addAll(
                      operasionalRepository.findAllByStatusInWithAccount(
                            List.of(
                                  StatusPengajuanEnum.APPROVED,
                                  StatusPengajuanEnum.REJECTED,
                                  StatusPengajuanEnum.PENDING_APPROVAL
                            )
                      )
                );
                result.addAll(operasionalRepository.findByAccIdWithAccount(nik));

            } else {
                result = operasionalRepository.findByAccIdWithAccount(nik);
            }

            Map<UUID, IzinOperasional> uniqueMap = result.stream()
                  .collect(java.util.stream.Collectors.toMap(
                        IzinOperasional::getUuid,
                        r -> r,
                        (existing, replacement) -> existing
                  ));

            return uniqueMap.values().stream()
                  .sorted(java.util.Comparator.comparing(IzinOperasional::getCreatedAt).reversed())
                  .map(opr -> OperasionalResDTO.builder()
                        .uuid(opr.getUuid())
                        .bidang_keahlian(opr.getFormPengajuan().getOrganisasi().getBidangKeahlian())
                        .nomor_induk(opr.getFormPengajuan().getNomorInduk())
                        .nama(opr.getAccount().getNama())
                        .status(opr.getStatus().name())
                        .created_at(opr.getCreatedAt())
                        .build()
                  ).toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get all izin operasional: " + e.getMessage(), e);
        }
    }

    @Override
    public OperasionalResDTO.DetailResponse findByUuidWithFiledAndProfile(String authHeader, UUID uuid) {
        String token = authHeader.substring(7);
        Map<String, Object> claims = jwtTokenProvider.parseToken(token);
        String requesterNik = String.valueOf(claims.get("sub"));
        String requesterRole = String.valueOf(claims.get("role"));

        if (operasionalRepository.findByUuid(uuid).isEmpty()) {
            throw new ResourceNotFoundException("No izin operasional found with uuid: " + uuid);
        }

        IzinOperasional operasional = operasionalRepository.findByUuidWithFilesAndAccount(uuid)
            .orElseThrow(() -> new ResourceNotFoundException("Data Izin Operasional with UUID " + uuid + " not found"));

        Account owner = operasional.getAccount();

        Set<String> allowedRoles = Set.of("ADMIN", "KEPALA");
        if (!owner.getNik().equals(requesterNik) && !allowedRoles.contains(requesterRole)) {
            throw new ForbiddenException("Data pengajuan is not yours.");
        }

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

        List<OperasionalResDTO.FilePendukung> filePendukungList = (operasional.getFilePendukung() == null) ? List.of() :
              operasional.getFilePendukung().stream()
                    .map(file -> OperasionalResDTO.FilePendukung.builder()
                          .id(file.getId())
                          .operasional_uuid(file.getOperasionalUuid().toString())
                          .file_url(file.getFileUrl())
                          .nama_file(file.getNamaFile())
                          .build()
                    ).toList();

        return OperasionalResDTO.DetailResponse.builder()
              .uuid(operasional.getUuid())
              .profile(profile)
              .induk_organisasi(operasional.getFormPengajuan().getOrganisasi().getBidangKeahlian())
              .nomor_induk(operasional.getFormPengajuan().getNomorInduk())
              .alamat(operasional.getFormPengajuan().getOrganisasi().getAlamat())
              .nama_ketua(operasional.getFormPengajuan().getNamaKetua())
              .nik_ketua(operasional.getFormPengajuan().getNikKetua())
              .nomor_telepon(operasional.getFormPengajuan().getNomorTelepon())
              .jumlah_anggota(operasional.getFormPengajuan().getJumlahAnggota())
              .tanggal_pendirian(operasional.getTanggalPendirian())
              .berlaku_dari(operasional.getBerlakuDari())
              .berlaku_sampai(operasional.getBerlakuSampai())
              .keterangan(operasional.getKeterangan())
              .status(operasional.getStatus() != null ? operasional.getStatus().name() : null)
              .status_tanggal(operasional.getStatusTanggal())
              .nomor_surat(operasional.getNomorSurat())
              .catatan(operasional.getCatatan())
              .tertanda(operasional.getTertanda())
              .file_pendukung(filePendukungList)
              .created_at(operasional.getCreatedAt())
              .build();
    }

    @Override
    @Transactional
    public OperasionalResDTO saveData(OperasionalReqDTO.SaveData dto) {
        Account account = accountRepository.findByNik(encryptService.encrypt(dto.getNik()))
              .orElseThrow(() -> new ResourceNotFoundException("NIK not found: " + dto.getNik()));

        FormPengajuan form = formPengajuanRepository.findByUuid(dto.getForm_uuid())
              .orElseThrow(() -> new ResourceNotFoundException("Form with UUID " + dto.getForm_uuid() + " not found"));

        try {
            IzinOperasional entity = IzinOperasional.builder()
                  .uuid(UUID.randomUUID())
                  .account(account)
                  .formPengajuan(form)
                  .tanggalPendirian(dto.getTanggal_pendirian())
                  .status(StatusPengajuanEnum.PENDING_VERIFICATOR)
                  .keterangan(dto.getKeterangan() != null ? dto.getKeterangan() : "-")
                  .build();

            return OperasionalResDTO.builder()
                  .uuid(operasionalRepository.save(entity).getUuid())
                  .build();
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Data integrity violation. Please check NOT NULL, UNIQUE, or foreign key constraints.", e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error occurred while saving form_pengajuan", e);
        }
    }

    @Override
    @Transactional
    public OperasionalResDTO.FilePendukung uploadFilePendukung(MultipartFile file, String operasionalUuid, String namaFile) {
        try {
            String fileUrl = r2StorageService.upload(file);
            String finalNamaFile = namaFile != null ? namaFile : file.getOriginalFilename();

            UUID operasionalUuidParsed = UUID.fromString(operasionalUuid);
            IzinOperasional operasional = operasionalRepository.findByUuid(operasionalUuidParsed)
                  .orElseThrow(() -> new ResourceNotFoundException("Operasional with UUID " + operasionalUuidParsed + " not found"));

            FilePendukung filePendukung = FilePendukung.builder()
                  .fileUrl(fileUrl)
                  .namaFile(finalNamaFile)
                  .operasional(operasional)
                  .build();

            FilePendukung savedFile = filePendukungRepository.save(filePendukung);

            return OperasionalResDTO.FilePendukung.builder()
                  .operasional_uuid(operasionalUuid)
                  .file_url(savedFile.getFileUrl())
                  .nama_file(savedFile.getNamaFile())
                  .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload and save file: " + e.getMessage(), e);
        }
    }

    @Override
    public OperasionalResDTO.VerifyData verify(String authHeader, OperasionalReqDTO.Verify dto, UUID uuid) {
        try {
            String token = authHeader.substring(7);
            Map<String, Object> claims = jwtTokenProvider.parseToken(token);
            String role = String.valueOf(claims.get("role"));

            Set<String> allowedRoles = Set.of("ADMIN", "KEPALA");

            if (!allowedRoles.contains(role)) {
                throw new ForbiddenException("You are not allowed to verify data pengajuan.");
            }

            IzinOperasional operasional = operasionalRepository.findByUuid(uuid)
                  .orElseThrow(() -> new ResourceNotFoundException("Operasional with UUID " + uuid + " not found"));

            Account account = accountRepository.findByNik(dto.getNik())
                  .orElseThrow(() -> new ResourceNotFoundException("NIK not found: " + encryptService.decrypt(dto.getTertanda().getNip())));

            operasional.setNomorSurat(dto.getNomor_surat());
            operasional.setBerlakuDari(dto.getBerlaku_dari());
            operasional.setBerlakuSampai(dto.getBerlaku_sampai());
            operasional.setStatus(dto.getStatus() != null ? StatusPengajuanEnum.valueOf(dto.getStatus()) : StatusPengajuanEnum.PENDING_VERIFICATOR);
            operasional.setTertanda(dto.getTertanda());
            operasional.setCatatan(dto.getCatatan());
            operasional.setStatusTanggal(LocalDateTime.now());

            operasionalRepository.save(operasional);

            if ("KEPALA".equalsIgnoreCase(role)) {
                account.setIsAssigned(false);
            }

            return OperasionalResDTO.VerifyData.builder()
                  .nomor_surat(operasional.getNomorSurat())
                  .berlaku_dari(operasional.getBerlakuDari())
                  .berlaku_sampai(operasional.getBerlakuSampai())
                  .status(operasional.getStatus().name())
                  .tertanda(dto.getTertanda())
                  .catatan(operasional.getCatatan())
                  .status_tanggal(operasional.getStatusTanggal())
                  .build();
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to verified data izin operasional: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteData(String uuid) {
        UUID operasionalUuid;

        try {
            operasionalUuid = UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid UUID format: " + uuid);
        }

        IzinOperasional operasional = operasionalRepository.findByUuidWithFiles(operasionalUuid)
              .orElseThrow(() -> new ResourceNotFoundException("Operasional with UUID " + operasionalUuid + " not found"));

        if (operasional.getFilePendukung() != null && !operasional.getFilePendukung().isEmpty()) {
            for (FilePendukung file : operasional.getFilePendukung()) {
                if (file.getFileUrl() != null && !file.getFileUrl().isBlank()) {
                    r2StorageService.delete(file.getFileUrl());
                }
            }
        }

        filePendukungRepository.deleteByOperasional(operasional);
        filePendukungRepository.flush();
        operasionalRepository.delete(operasional);
    }
}
