package cc.kertaskerja.pengajuan_kta.service.pengajuan;

import cc.kertaskerja.pengajuan_kta.dto.Auth.AccountResponse;
import cc.kertaskerja.pengajuan_kta.dto.Pengajuan.FilePendukungDTO;
import cc.kertaskerja.pengajuan_kta.dto.Pengajuan.FormPengajuanReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.Pengajuan.FormPengajuanResDTO;
import cc.kertaskerja.pengajuan_kta.entity.Account;
import cc.kertaskerja.pengajuan_kta.entity.FilePendukung;
import cc.kertaskerja.pengajuan_kta.entity.FormPengajuan;
import cc.kertaskerja.pengajuan_kta.entity.Organisasi;
import cc.kertaskerja.pengajuan_kta.enums.StatusPengajuanEnum;
import cc.kertaskerja.pengajuan_kta.exception.*;
import cc.kertaskerja.pengajuan_kta.repository.AccountRepository;
import cc.kertaskerja.pengajuan_kta.repository.FilePendukungRepository;
import cc.kertaskerja.pengajuan_kta.repository.FormPengajuanRepository;
import cc.kertaskerja.pengajuan_kta.repository.OrganisasiRepository;
import cc.kertaskerja.pengajuan_kta.security.JwtTokenProvider;
import cc.kertaskerja.pengajuan_kta.service.external.EncryptService;
import cc.kertaskerja.pengajuan_kta.service.global.R2StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormPengajuanServiceImpl implements FormPengajuanService {

    private final AccountRepository accountRepository;
    private final OrganisasiRepository organisasiRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final FormPengajuanRepository formPengajuanRepository;
    private final FilePendukungRepository filePendukungRepository;
    private final R2StorageService r2StorageService;
    private final EncryptService encryptService;

    @Override
    public List<FormPengajuanResDTO> getAllPengajuan(String authHeader) { // Added method signature assumption
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);
            Map<String, Object> claims = jwtTokenProvider.parseToken(token);

            String role = (String) claims.get("role");
            String nik = String.valueOf(claims.get("sub"));

            List<FormPengajuan> result = new ArrayList<>();

            if ("ADMIN".equalsIgnoreCase(role)) {
                List<FormPengajuan> byStatus = formPengajuanRepository.findAllByStatusInWithAccount(
                      List.of(
                            StatusPengajuanEnum.APPROVED,
                            StatusPengajuanEnum.REJECTED,
                            StatusPengajuanEnum.PENDING_VERIFICATOR
                      )
                );
                List<FormPengajuan> byNik = formPengajuanRepository.findByAccIdWithAccount(nik);
                result.addAll(byStatus);
                result.addAll(byNik);

            } else if ("KEPALA".equalsIgnoreCase(role)) {
                List<FormPengajuan> byStatus = formPengajuanRepository.findAllByStatusInWithAccount(
                      List.of(
                            StatusPengajuanEnum.APPROVED,
                            StatusPengajuanEnum.REJECTED,
                            StatusPengajuanEnum.PENDING_APPROVAL
                      )
                );
                List<FormPengajuan> byNik = formPengajuanRepository.findByAccIdWithAccount(nik);
                result.addAll(byStatus);
                result.addAll(byNik);

            } else {
                result = formPengajuanRepository.findByAccIdWithAccount(nik);
            }

            // 🔹 Remove duplicate by UUID
            Map<UUID, FormPengajuan> uniqueMap = result.stream()
                  .collect(Collectors.toMap(
                        FormPengajuan::getUuid,
                        f -> f,
                        (existing, replacement) -> existing
                  ));

            return uniqueMap.values().stream()
                  .sorted(Comparator.comparing(FormPengajuan::getCreatedAt).reversed())
                  .map(form -> FormPengajuanResDTO.builder()
                        .uuid(form.getUuid())
                        .nama(form.getAccount().getNama())
                        .nik(form.getAccount().getNik())
                        .email(form.getAccount().getEmail())
                        .profesi(form.getProfesi())
                        .tipe_akun(form.getAccount().getTipeAkun())
                        .nomor_induk(form.getNomorInduk())
                        .status(form.getStatus() != null ? form.getStatus().name() : null)
                        .nama_ketua(form.getAccount() != null ? form.getAccount().getNama() : null)
                        .nomor_induk(form.getNomorInduk())
                        .jumlah_anggota(
                              form.getJumlahAnggota() != null
                                    ? form.getJumlahAnggota().toString()
                                    : "0"
                        )
                        .created_at(form.getCreatedAt())
                        .build()
                  )
                  .collect(Collectors.toList()); // <--- FIXED HERE

        } catch (Exception e) {
            throw new RuntimeException("Failed to get all pengajuan: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public FormPengajuanResDTO.SaveDataResponse saveData(FormPengajuanReqDTO.SavePengajuan dto) {
        Account account = accountRepository.findByNik(encryptService.encrypt(dto.getNik()))
              .orElseThrow(() -> new ResourceNotFoundException("NIK not found: " + dto.getNik()));

        Organisasi organisasi = null;

        if ("Organisasi".equalsIgnoreCase(account.getTipeAkun()) && dto.getOrganisasi_uuid() != null) {
            organisasi = organisasiRepository.findByUuid(dto.getOrganisasi_uuid())
                  .orElseThrow(() -> new ResourceNotFoundException("Organisasi not found"));
        }

        try {
            FormPengajuan entity;

            if ("Organisasi".equalsIgnoreCase(account.getTipeAkun())) {
                entity = FormPengajuan.builder()
                      .account(account)
                      .uuid(UUID.randomUUID())
                      .organisasi(organisasi)
                      .namaKetua(dto.getNama_ketua())
                      .nikKetua(dto.getNik_ketua())
                      .nomorTelepon(dto.getNomor_telepon())
                      .jumlahAnggota(dto.getJumlah_anggota())
                      .daerah(dto.getDaerah())
                      .status(StatusPengajuanEnum.PENDING_VERIFICATOR)
                      .tambahan(dto.getTambahan())
                      .keterangan(dto.getKeterangan() != null ? dto.getKeterangan() : "-")
                      .build();
            } else {
                entity = FormPengajuan.builder()
                      .account(account)
                      .uuid(UUID.randomUUID())
                      .namaKetua(dto.getNama_ketua())
                      .nikKetua(dto.getNik_ketua())
                      .nomorTelepon(dto.getNomor_telepon())
                      .profesi(dto.getProfesi())
                      .status(StatusPengajuanEnum.PENDING_VERIFICATOR)
                      .keterangan(dto.getKeterangan() != null ? dto.getKeterangan() : "-")
                      .tambahan(dto.getTambahan())
                      .build();
            }
            FormPengajuan saved = formPengajuanRepository.save(entity);

            return FormPengajuanResDTO.SaveDataResponse.builder()
                  .uuid(saved.getUuid())
                  .nomor_induk(saved.getNomorInduk())
                  .nama_ketua(saved.getNamaKetua())
                  .nik_ketua(saved.getNikKetua())
                  .nomor_telepon(saved.getNomorTelepon())
                  .jumlah_anggota(saved.getJumlahAnggota())
                  .daerah(saved.getDaerah())
                  .profesi(saved.getProfesi())
                  .status(saved.getStatus().name())
                  .keterangan(saved.getKeterangan())
                  .tambahan(saved.getTambahan())
                  .build();

        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Data integrity violation. Please check NOT NULL, UNIQUE, or foreign key constraints.", e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error occurred while saving form_pengajuan", e);
        }
    }

    @Override
    @Transactional
    public FilePendukungDTO uploadAndSaveFile(MultipartFile file, String formUuid, String namaFile) {
        try {
            String fileUrl = r2StorageService.upload(file);
            String finalNamaFile = namaFile != null ? namaFile : file.getOriginalFilename();

            UUID formUuidParsed = UUID.fromString(formUuid);
            FormPengajuan formPengajuan = formPengajuanRepository.findByUuid(formUuidParsed)
                  .orElseThrow(() -> new ResourceNotFoundException("Form with UUID " + formUuid + " not found"));

            FilePendukung filePendukung = FilePendukung.builder()
                  .fileUrl(fileUrl)
                  .namaFile(finalNamaFile)
                  .formPengajuan(formPengajuan)
                  .build();

            FilePendukung savedFile = filePendukungRepository.save(filePendukung);

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
    public FormPengajuanResDTO.PengajuanResponse findByUuidWithFiles(String authHeader, UUID uuid) {
        String token = authHeader.substring(7);
        String tipeAkun = (String) jwtTokenProvider.parseToken(token).get("tipe_akun");

        FormPengajuan form = formPengajuanRepository.findByUuidWithFiles(uuid)
              .orElseThrow(() -> new ResourceNotFoundException("Form with UUID " + uuid + " not found"));

        List<FormPengajuanResDTO.FilePendukung> filePendukungList = form.getFilePendukung().stream()
              .map(file -> FormPengajuanResDTO.FilePendukung.builder()
                    .id(file.getId())
                    .form_uuid(form.getUuid().toString())
                    .file_url(file.getFileUrl())
                    .nama_file(file.getNamaFile())
                    .build())
              .toList();

        var responseBuilder = FormPengajuanResDTO.PengajuanResponse.builder()
              .uuid(form.getUuid())
              .nomor_induk(form.getNomorInduk())
              .nama_ketua(form.getNamaKetua())
              .nik_ketua(form.getNikKetua())
              .nomor_telepon(form.getNomorTelepon())
              .jumlah_anggota(form.getJumlahAnggota() != null ? form.getJumlahAnggota().toString() : "0")
              .daerah(form.getDaerah())
              .status(form.getStatus() != null ? form.getStatus().name() : null)
              .keterangan(form.getKeterangan())
              .tambahan(form.getTambahan())
              .file_pendukung(filePendukungList);

        if ("Organisasi".equalsIgnoreCase(tipeAkun)) {
            Organisasi org = organisasiRepository.findByUuidWithFiles(form.getOrganisasi().getUuid())
                  .orElseThrow(() -> new ResourceNotFoundException("Organisasi is not found"));

            List<FormPengajuanResDTO.FileOrganisasi> fileOrganisasiList = org.getFilePendukung().stream()
                  .map(file -> FormPengajuanResDTO.FileOrganisasi.builder()
                        .id(file.getId())
                        .organisasi_uuid(org.getUuid().toString())
                        .file_url(file.getFileUrl())
                        .nama_file(file.getNamaFile())
                        .build())
                  .toList();

            responseBuilder
                  .induk_organisasi(org.getBidangKeahlian())
                  .alamat(org.getAlamat())
                  .file_organisasi(fileOrganisasiList)
                  .file_pendukung(filePendukungList);
        }

        return responseBuilder.build();
    }

    @Override
    @Transactional(readOnly = true)
    public FormPengajuanResDTO.PengajuanWithProfileResponse findByUuidWithFilesAndProfile(String authHeader, UUID uuid) {
        String token = authHeader.substring(7);
        Map<String, Object> claims = jwtTokenProvider.parseToken(token);
        String requesterNik = String.valueOf(claims.get("sub"));
        String requesterRole = String.valueOf(claims.get("role"));

        // 1. Basic Existence Check
        if (formPengajuanRepository.findByUuid(uuid).isEmpty()) {
            throw new ResourceNotFoundException("Data not found");
        }

        // 2. Fetch Form with Files
        FormPengajuan form = formPengajuanRepository.findByUuidWithFilesAndAccount(uuid)
              .orElseThrow(() -> new ResourceNotFoundException("Pengajuan with UUID " + uuid + " not found"));

        Account owner = form.getAccount();

        // 3. Authorization Check
        Set<String> allowedRoles = Set.of("ADMIN", "KEPALA");
        if (!owner.getNik().equals(requesterNik) && !allowedRoles.contains(requesterRole)) {
            throw new ForbiddenException("Data pengajuan is not yours.");
        }

        // 4. Build Profile
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

        // 5. Build File List (Common)
        List<FormPengajuanResDTO.FilePendukung> filePendukungList = (form.getFilePendukung() == null) ? List.of() :
              form.getFilePendukung().stream()
                    .map(file -> FormPengajuanResDTO.FilePendukung.builder()
                          .id(file.getId())
                          .form_uuid(form.getUuid().toString())
                          .file_url(file.getFileUrl())
                          .nama_file(file.getNamaFile())
                          .build())
                    .toList();

        // 6. Initialize Builder with COMMON Data (Used by both Org and Pribadi)
        var pengajuanBuilder = FormPengajuanResDTO.PengajuanResponse.builder()
              .uuid(form.getUuid())
              .nomor_induk(form.getNomorInduk())
              // Common fields
              .daerah(form.getDaerah())
              .berlaku_dari(form.getBerlakuDari())
              .berlaku_sampai(form.getBerlakuSampai())
              .keterangan(form.getKeterangan())
              .catatan(form.getCatatan())
              .status(form.getStatus() != null ? form.getStatus().name() : null)
              .tertanda(form.getTertanda())
              .status_tanggal(form.getStatusTanggal())
              .created_at(form.getCreatedAt())
              .file_pendukung(filePendukungList);

        // 7. Handle Specific Account Types
        if ("Organisasi".equalsIgnoreCase(owner.getTipeAkun())) {
            if (form.getOrganisasi() == null) {
                throw new ResourceNotFoundException("Data inconsistency: Organization account missing details");
            }

            Organisasi org = organisasiRepository.findByUuidWithFiles(form.getOrganisasi().getUuid())
                  .orElseThrow(() -> new ResourceNotFoundException("Organisasi details not found"));

            List<FormPengajuanResDTO.FileOrganisasi> fileOrgList = (org.getFilePendukung() == null) ? List.of() :
                  org.getFilePendukung().stream()
                        .map(file -> FormPengajuanResDTO.FileOrganisasi.builder()
                              .id(file.getId())
                              .organisasi_uuid(org.getUuid().toString())
                              .file_url(file.getFileUrl())
                              .nama_file(file.getNamaFile())
                              .build())
                        .toList();

            // Fill Organization specific fields
            pengajuanBuilder
                  .nama_ketua(form.getNamaKetua())
                  .nik_ketua(form.getNikKetua())
                  .nomor_telepon(form.getNomorTelepon())
                  .bidang_keahlian(form.getOrganisasi().getBidangKeahlian())
                  .induk_organisasi(org.getBidangKeahlian())
                  .alamat(org.getAlamat())
                  .jumlah_anggota(form.getJumlahAnggota() != null ? form.getJumlahAnggota().toString() : "0")
                  .file_organisasi(fileOrgList);
        } else {
            // Handle PRIBADI: Just set the profession
            // The builder will leave organization fields as null automatically
            pengajuanBuilder.profesi(form.getProfesi());
        }

        // 8. Single Return Point
        return FormPengajuanResDTO.PengajuanWithProfileResponse.builder()
              .pengajuan(pengajuanBuilder.build())
              .profile(profile)
              .build();
    }

    @Override
    @Transactional
    public FormPengajuanResDTO.SaveDataResponse editDataPengajuan(String authHeader, UUID uuid, FormPengajuanReqDTO.SavePengajuan dto) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Map<String, Object> claims = jwtTokenProvider.parseToken(token);
        String userId = String.valueOf(claims.get("uid"));

        FormPengajuan formPengajuan = formPengajuanRepository.findByUuid(uuid)
              .orElseThrow(() -> new ResourceNotFoundException("Data pengajuan with UUID " + uuid + " is not found"));

        if (!userId.equals(formPengajuan.getAccount().getId().toString())) {
            throw new ForbiddenException("Data pengajuan that has been changed is not yours.");
        }

        if (formPengajuan.getStatus() == StatusPengajuanEnum.APPROVED) {
            throw new ConflictException("Data pengajuan that has been approved cannot be edited.");
        }

        try {
            formPengajuan
                  .setJumlahAnggota(dto.getJumlah_anggota())
                  .setDaerah(dto.getDaerah())
                  .setProfesi(dto.getProfesi())
                  .setStatus(StatusPengajuanEnum.PENDING_VERIFICATOR)
                  .setKeterangan(dto.getKeterangan() != null ? dto.getKeterangan() : "-")
                  .setTambahan(dto.getTambahan());

            formPengajuanRepository.save(formPengajuan);

            return FormPengajuanResDTO.SaveDataResponse.builder()
                  .uuid(formPengajuan.getUuid())
                  .nomor_induk(formPengajuan.getNomorInduk())
                  .nama_ketua(formPengajuan.getNamaKetua())
                  .nik_ketua(formPengajuan.getNikKetua())
                  .nomor_telepon(formPengajuan.getNomorTelepon())
                  .jumlah_anggota(formPengajuan.getJumlahAnggota())
                  .daerah(formPengajuan.getDaerah())
                  .profesi(formPengajuan.getProfesi())
                  .status(formPengajuan.getStatus().toString())
                  .keterangan(formPengajuan.getKeterangan())
                  .tambahan(formPengajuan.getTambahan())
                  .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to change data pengajuan: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public FormPengajuanResDTO.VerifyData verifyDataPengajuan(String authHeader, FormPengajuanReqDTO.VerifyPengajuan dto, UUID uuid) {
        try {
            String token = authHeader.substring(7);
            Map<String, Object> claims = jwtTokenProvider.parseToken(token);
            String role = String.valueOf(claims.get("role"));

            Set<String> allowedRoles = Set.of("ADMIN", "KEPALA");

            if (!allowedRoles.contains(role)) {
                throw new ForbiddenException("You are not allowed to verify data pengajuan.");
            }

            FormPengajuan form = formPengajuanRepository.findByUuid(uuid)
                  .orElseThrow(() -> new ResourceNotFoundException("Form pengajuan is not found"));

            Account account = accountRepository.findByNik(dto.getNik())
                  .orElseThrow(() -> new ResourceNotFoundException("NIK not found: " + encryptService.decrypt(dto.getTertanda().getNip())));

            form.setNomorInduk(dto.getNomor_induk());
            form.setBerlakuDari(dto.getBerlaku_dari());
            form.setBerlakuSampai(dto.getBerlaku_sampai());
            form.setStatus(dto.getStatus() != null ? StatusPengajuanEnum.valueOf(dto.getStatus()) : StatusPengajuanEnum.PENDING_VERIFICATOR);
            form.setTertanda(dto.getTertanda());
            form.setCatatan(dto.getCatatan());
            form.setStatusTanggal(LocalDateTime.now());

            formPengajuanRepository.save(form);

            if ("KEPALA".equalsIgnoreCase(role)) {
                account.setIsAssigned(false);
            }

            return FormPengajuanResDTO.VerifyData.builder()
                  .nomor_induk(form.getNomorInduk())
                  .berlaku_dari(form.getBerlakuDari())
                  .berlaku_sampai(form.getBerlakuSampai())
                  .status(form.getStatus().name())
                  .tertanda(dto.getTertanda())
                  .catatan(form.getCatatan())
                  .status_tanggal(form.getStatusTanggal())
                  .build();

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to verified data pengajuan: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public String editIsAssignedInAccount(String nik) {
        try {
            Account account = accountRepository.findByNik(nik)
                  .orElseThrow(() -> new ResourceNotFoundException("NIK not found: " + nik));

            account.setIsAssigned(true);

            return "Success";
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to set assigned: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteData(String uuid) {
        UUID formUuid;

        try {
            formUuid = UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid UUID format: " + uuid);
        }

        FormPengajuan form = formPengajuanRepository.findByUuidWithFiles(formUuid)
              .orElseThrow(() ->
                    new ResourceNotFoundException("Form pengajuan not found: " + uuid)
              );

        // ❌ Safety rule
        if (form.getStatus() == StatusPengajuanEnum.APPROVED) {
            throw new ConflictException("Approved pengajuan cannot be deleted");
        }

        // 1️⃣ Delete files from R2
        if (form.getFilePendukung() != null && !form.getFilePendukung().isEmpty()) {
            for (FilePendukung file : form.getFilePendukung()) {
                if (file.getFileUrl() != null && !file.getFileUrl().isBlank()) {
                    r2StorageService.delete(file.getFileUrl());
                }
            }
        }

        // 2️⃣ Delete child records (FK SAFE)
        filePendukungRepository.deleteByFormPengajuan(form);

        // 3️⃣ Flush to enforce DB execution order
        filePendukungRepository.flush();

        // 4️⃣ Delete parent
        formPengajuanRepository.delete(form);
    }

    @Override
    @Transactional
    public void deleteFilePendukung(String authHeader, Long id) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthenticationException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Map<String, Object> claims = jwtTokenProvider.parseToken(token);

        String nikFromToken = String.valueOf(claims.get("sub"));
        String role = String.valueOf(claims.get("role"));

        Set<String> allowedRoles = Set.of("ADMIN", "KEPALA");

        FilePendukung file = filePendukungRepository.findById(id)
              .orElseThrow(() -> new ResourceNotFoundException("File pendukung not found: " + id));

        if (file.getFormPengajuan() != null) {
            String ownerNik = file.getFormPengajuan().getAccount() != null ? file.getFormPengajuan().getAccount().getNik() : null;

            if (ownerNik != null && !ownerNik.equals(nikFromToken) && !allowedRoles.contains(role)) {
                throw new ForbiddenException("File pendukung is not yours.");
            }

            if (file.getFormPengajuan().getStatus() == StatusPengajuanEnum.APPROVED) {
                throw new ConflictException("File pendukung cannot be deleted because pengajuan is APPROVED.");
            }
        }

        if (file.getFileUrl() != null && !file.getFileUrl().isBlank()) {
            r2StorageService.delete(file.getFileUrl());
        }

        filePendukungRepository.delete(file);
    }

    @Override
    public List<FormPengajuanResDTO.PengajuanBulananResponse> getStatisticsPerMonth(int year) {
        List<Object[]> results = formPengajuanRepository.countPengajuanPerMonth(year);

        Map<Integer, Long> monthMap = new HashMap<>();
        for (Object[] row : results) {
            Integer month = ((Number) row[0]).intValue();
            Long total = ((Number) row[1]).longValue();
            monthMap.put(month, total);
        }

        String[] months = {
              "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
              "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        };

        List<FormPengajuanResDTO.PengajuanBulananResponse> response = new ArrayList<>();

        for (int i = 1; i <= 12; i++) {
            response.add(
                  new FormPengajuanResDTO.PengajuanBulananResponse(
                        months[i - 1],
                        monthMap.getOrDefault(i, 0L)
                  )
            );
        }

        return response;
    }
}
