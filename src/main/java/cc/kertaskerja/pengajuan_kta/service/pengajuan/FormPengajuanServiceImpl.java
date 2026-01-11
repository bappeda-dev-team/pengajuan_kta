package cc.kertaskerja.pengajuan_kta.service.pengajuan;

import cc.kertaskerja.pengajuan_kta.dto.Auth.AccountResponse;
import cc.kertaskerja.pengajuan_kta.dto.Pengajuan.FilePendukungDTO;
import cc.kertaskerja.pengajuan_kta.dto.Pengajuan.FormPengajuanReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.Pengajuan.FormPengajuanResDTO;
import cc.kertaskerja.pengajuan_kta.entity.Account;
import cc.kertaskerja.pengajuan_kta.entity.FilePendukung;
import cc.kertaskerja.pengajuan_kta.entity.FormPengajuan;
import cc.kertaskerja.pengajuan_kta.enums.StatusPengajuanEnum;
import cc.kertaskerja.pengajuan_kta.exception.*;
import cc.kertaskerja.pengajuan_kta.repository.AccountRepository;
import cc.kertaskerja.pengajuan_kta.repository.FilePendukungRepository;
import cc.kertaskerja.pengajuan_kta.repository.FormPengajuanRepository;
import cc.kertaskerja.pengajuan_kta.security.JwtTokenProvider;
import cc.kertaskerja.pengajuan_kta.service.external.EncryptService;
import cc.kertaskerja.pengajuan_kta.service.global.R2StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FormPengajuanServiceImpl implements FormPengajuanService {

    private final AccountRepository accountRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final FormPengajuanRepository formPengajuanRepository;
    private final FilePendukungRepository filePendukungRepository;
    private final R2StorageService r2StorageService;
    private final EncryptService encryptService;

    @Override
    public List<FormPengajuanResDTO.PengajuanResponse> findAllDataPengajuan(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);
            Map<String, Object> claims = jwtTokenProvider.parseToken(token);

            String role = (String) claims.get("role");
            String nik = String.valueOf(claims.get("sub"));

            List<FormPengajuan> forms;
            if ("ADMIN".equalsIgnoreCase(role)) {
                forms = formPengajuanRepository.findAllData();
            } else {
                forms = formPengajuanRepository.findByAccId(nik);
            }

            return forms.stream()
                  .map(form -> FormPengajuanResDTO.PengajuanResponse.builder()
                        .uuid(form.getUuid())
                        .induk_organisasi(form.getIndukOrganisasi())
                        .nomor_induk(form.getNomorInduk())
                        .jumlah_anggota(form.getJumlahAnggota() != null ? form.getJumlahAnggota().toString() : "0")
                        .daerah(form.getDaerah())
                        .berlaku_dari(form.getBerlakuDari())
                        .berlaku_sampai(form.getBerlakuSampai())
                        .profesi(form.getProfesi())
                        .keterangan(form.getKeterangan())
                        .catatan(form.getCatatan())
                        .status(form.getStatus() != null ? form.getStatus().name() : null)
                        .tertanda(form.getTertanda())
                        .file_pendukung(form.getFilePendukung().stream()
                              .map(file -> FormPengajuanResDTO.FilePendukung.builder()
                                    .form_uuid(file.getFormPengajuan().getUuid().toString())
                                    .file_url(file.getFileUrl())
                                    .nama_file(file.getNamaFile())
                                    .build())
                              .toList())
                        .build())
                  .toList();

        } catch (Exception e) {
            throw new RuntimeException("Failed to get all pengajuan: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public FormPengajuanResDTO.SaveDataResponse saveData(FormPengajuanReqDTO.SavePengajuan dto) {
        Account account = accountRepository.findByNik(encryptService.encrypt(dto.getNik()))
              .orElseThrow(() -> new ResourceNotFoundException("NIK not found: " + dto.getNik()));

        if (formPengajuanRepository.existsByNomorInduk(dto.getNomor_induk())) {
            throw new ConflictException("Nomor induk '" + dto.getNomor_induk() + "' already exists");
        }

        try {
            FormPengajuan entity = FormPengajuan.builder()
                  .account(account)
                  .uuid(UUID.randomUUID())
                  .indukOrganisasi(dto.getInduk_organisasi())
                  .nomorInduk(dto.getNomor_induk())
                  .jumlahAnggota(Integer.parseInt(dto.getJumlah_anggota()))
                  .daerah(dto.getDaerah())
                  .profesi(dto.getProfesi())
                  .status(StatusPengajuanEnum.PENDING)
                  .keterangan(dto.getKeterangan() != null ? dto.getKeterangan() : "-")
                  .build();

            FormPengajuan saved = formPengajuanRepository.save(entity);
            account.setIsAssigned(true);

            return FormPengajuanResDTO.SaveDataResponse.builder()
                  .uuid(saved.getUuid())
                  .induk_organisasi(saved.getIndukOrganisasi())
                  .nomor_induk(saved.getNomorInduk())
                  .jumlah_anggota(String.valueOf(saved.getJumlahAnggota()))
                  .daerah(saved.getDaerah())
                  .profesi(saved.getProfesi())
                  .status(saved.getStatus().name())
                  .keterangan(saved.getKeterangan())
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
            System.out.println(e.getMessage() + " ERROR");
            throw new RuntimeException("Failed to upload and save file: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FormPengajuanResDTO.PengajuanResponse findByUuidWithFiles(UUID uuid) {
        FormPengajuan formPengajuan = formPengajuanRepository.findByUuidWithFiles(uuid)
              .orElseThrow(() -> new ResourceNotFoundException("Form with UUID " + uuid + " not found"));

        return FormPengajuanResDTO.PengajuanResponse.builder()
              .uuid(formPengajuan.getUuid())
              .induk_organisasi(formPengajuan.getIndukOrganisasi())
              .nomor_induk(formPengajuan.getNomorInduk())
              .jumlah_anggota(formPengajuan.getJumlahAnggota() != null
                    ? formPengajuan.getJumlahAnggota().toString()
                    : "0")
              .daerah(formPengajuan.getDaerah())
              .profesi(formPengajuan.getProfesi())
              .status(formPengajuan.getStatus() != null ? formPengajuan.getStatus().name() : null)
              .keterangan(formPengajuan.getKeterangan())
              .file_pendukung(formPengajuan.getFilePendukung().stream()
                    .map(file -> FormPengajuanResDTO.FilePendukung.builder()
                          .form_uuid(file.getFormPengajuan().getUuid().toString())
                          .file_url(file.getFileUrl())
                          .nama_file(file.getNamaFile())
                          .build())
                    .toList())
              .build();
    }

    @Override
    public FormPengajuanResDTO.PengajuanWithProfileResponse findByUuidWithFilesAndProfile(String authHeader, UUID uuid) {
        FormPengajuan formPengajuan = formPengajuanRepository.findByUuidWithFilesAndAccount(uuid)
              .orElseThrow(() -> new ResourceNotFoundException("Form with UUID " + uuid + " not found"));

        Account owner = formPengajuan.getAccount();

        String token = authHeader.substring(7);
        Map<String, Object> claims = jwtTokenProvider.parseToken(token);
        String nik = String.valueOf(claims.get("sub"));

        if (!owner.getNik().equals(nik)) {
            throw new ForbiddenException("Data pengajuan is not yours.");
        };

        FormPengajuanResDTO.PengajuanResponse pengajuan = FormPengajuanResDTO.PengajuanResponse.builder()
              .uuid(formPengajuan.getUuid())
              .induk_organisasi(formPengajuan.getIndukOrganisasi())
              .nomor_induk(formPengajuan.getNomorInduk())
              .jumlah_anggota(formPengajuan.getJumlahAnggota() != null ? formPengajuan.getJumlahAnggota().toString() : "0")
              .daerah(formPengajuan.getDaerah())
              .berlaku_dari(formPengajuan.getBerlakuDari())
              .berlaku_sampai(formPengajuan.getBerlakuSampai())
              .profesi(formPengajuan.getProfesi())
              .keterangan(formPengajuan.getKeterangan())
              .catatan(formPengajuan.getCatatan())
              .status(formPengajuan.getStatus() != null ? formPengajuan.getStatus().name() : null)
              .tertanda(formPengajuan.getTertanda())
              .file_pendukung(formPengajuan.getFilePendukung() == null ? List.of() : formPengajuan.getFilePendukung().stream()
                    .map(file -> FormPengajuanResDTO.FilePendukung.builder()
                          .form_uuid(file.getFormPengajuan().getUuid().toString())
                          .file_url(file.getFileUrl())
                          .nama_file(file.getNamaFile())
                          .build())
                    .toList())
              .status_tanggal(formPengajuan.getStatusTanggal())
              .build();

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

        return FormPengajuanResDTO.PengajuanWithProfileResponse.builder()
              .pengajuan(pengajuan)
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
                  .setIndukOrganisasi(dto.getInduk_organisasi())
                  .setNomorInduk(dto.getNomor_induk())
                  .setJumlahAnggota(Integer.parseInt(dto.getJumlah_anggota()))
                  .setDaerah(dto.getDaerah())
                  .setProfesi(dto.getProfesi())
                  .setStatus(StatusPengajuanEnum.PENDING)
                  .setKeterangan(dto.getKeterangan() != null ? dto.getKeterangan() : "-");

            formPengajuanRepository.save(formPengajuan);

            return FormPengajuanResDTO.SaveDataResponse.builder()
                  .uuid(formPengajuan.getUuid())
                  .induk_organisasi(formPengajuan.getIndukOrganisasi())
                  .nomor_induk(formPengajuan.getNomorInduk())
                  .jumlah_anggota(String.valueOf(formPengajuan.getJumlahAnggota()))
                  .daerah(formPengajuan.getDaerah())
                  .profesi(formPengajuan.getProfesi())
                  .status(formPengajuan.getStatus().toString())
                  .keterangan(formPengajuan.getKeterangan())
                  .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to change data pengajuan: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public FormPengajuanResDTO.VerifyData verifyDataPengajuan(String authHeader, FormPengajuanReqDTO.VerifyPengajuan dto, UUID uuid) {
        try {
            FormPengajuan form = formPengajuanRepository.findByUuid(uuid)
                  .orElseThrow(() -> new ResourceNotFoundException("Form pengajuan is not found"));

            String token = authHeader.substring(7);
            Map<String, Object> claims = jwtTokenProvider.parseToken(token);
            String nik = String.valueOf(claims.get("sub"));

            Account account = accountRepository.findByNik(nik)
                  .orElseThrow(() -> new ResourceNotFoundException("NIK not found: " + encryptService.decrypt(dto.getTertanda().getNip())));

            form.setBerlakuDari(dto.getBerlaku_dari());
            form.setBerlakuSampai(dto.getBerlaku_sampai());
            form.setStatus(dto.getStatus() != null ? StatusPengajuanEnum.valueOf(dto.getStatus()) : StatusPengajuanEnum.PENDING);
            form.setTertanda(dto.getTertanda());
            form.setCatatan(dto.getCatatan());
            form.setStatusTanggal(LocalDateTime.now());

            account.setIsAssigned(false);

            formPengajuanRepository.save(form);

            return FormPengajuanResDTO.VerifyData.builder()
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
}
