package cc.kertaskerja.pengajuan_kta.service.pengajuan;

import cc.kertaskerja.pengajuan_kta.dto.Pengajuan.FilePendukungDTO;
import cc.kertaskerja.pengajuan_kta.dto.Pengajuan.FormPengajuanReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.Pengajuan.FormPengajuanResDTO;
import cc.kertaskerja.pengajuan_kta.entity.Account;
import cc.kertaskerja.pengajuan_kta.entity.FilePendukung;
import cc.kertaskerja.pengajuan_kta.entity.FormPengajuan;
import cc.kertaskerja.pengajuan_kta.enums.StatusEnum;
import cc.kertaskerja.pengajuan_kta.exception.*;
import cc.kertaskerja.pengajuan_kta.repository.AccountRepository;
import cc.kertaskerja.pengajuan_kta.repository.FilePendukungRepository;
import cc.kertaskerja.pengajuan_kta.repository.FormPengajuanRepository;
import cc.kertaskerja.pengajuan_kta.security.JwtTokenProvider;
import cc.kertaskerja.pengajuan_kta.service.global.R2StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormPengajuanServiceImpl implements FormPengajuanService {
    private final AccountRepository accountRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final FormPengajuanRepository formPengajuanRepository;
    private final FilePendukungRepository filePendukungRepository;
    private final R2StorageService r2StorageService;
    private final ObjectMapper objectMapper;

    @Override
    public List<FormPengajuanResDTO.PengajuanResponse> findAllDataPengajuan(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);
            Map<String, Object> claims = jwtTokenProvider.parseToken(token);

            String role = (String) claims.get("role");
            Long userId = ((Number) claims.get("uid")).longValue();

            List<FormPengajuan> forms;
            if ("ADMIN".equalsIgnoreCase(role)) {
                forms = formPengajuanRepository.findAllData();
            } else {
                forms = formPengajuanRepository.findByAccId(userId);
            }

            // Mapping entity ke response DTO
            return forms.stream()
                  .map(form -> FormPengajuanResDTO.PengajuanResponse.builder()
                        .uuid(form.getUuid())
                        .induk_organisasi(form.getIndukOrganisasi())
                        .nomor_induk(form.getNomorInduk())
                        .jumlah_anggota(form.getJumlahAnggota() != null ? form.getJumlahAnggota().toString() : "0")
                        .daerah(form.getDaerah())
                        .berlaku_dari(form.getBerlakuDari())
                        .berlaku_sampai(form.getBerlakuSampai())
                        .nama(form.getNama())
                        .tempat_lahir(form.getTempatLahir())
                        .tanggal_lahir(form.getTanggalLahir())
                        .jenis_kelamin(form.getJenisKelamin())
                        .alamat(form.getAlamat())
                        .profesi(form.getProfesi())
                        .dibuat_di(form.getDibuatDi())
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
        try {
            Account account = accountRepository.findById(dto.getUser_id())
                  .orElseThrow(() -> new ResourceNotFoundException("Account not found for user_id: " + dto.getUser_id()));

            FormPengajuan entity = FormPengajuan.builder()
                  .account(account)
                  .uuid(UUID.randomUUID())
                  .indukOrganisasi(dto.getInduk_organisasi())
                  .nomorInduk(dto.getNomor_induk())
                  .jumlahAnggota(Integer.parseInt(dto.getJumlah_anggota()))
                  .daerah(dto.getDaerah())
                  .nama(dto.getNama())
                  .tempatLahir(dto.getTempat_lahir())
                  .tanggalLahir(dto.getTanggal_lahir())
                  .jenisKelamin(dto.getJenis_kelamin())
                  .alamat(dto.getAlamat())
                  .profesi(dto.getProfesi())
                  .dibuatDi(dto.getDibuat_di())
                  .status(StatusEnum.PENDING)
                  .keterangan(dto.getKeterangan() != null ? dto.getKeterangan() : "-")
                  .build();

            FormPengajuan saved = formPengajuanRepository.save(entity);

            return FormPengajuanResDTO.SaveDataResponse.builder()
                  .uuid(saved.getUuid())
                  .induk_organisasi(saved.getIndukOrganisasi())
                  .nomor_induk(saved.getNomorInduk())
                  .jumlah_anggota(String.valueOf(saved.getJumlahAnggota()))
                  .daerah(saved.getDaerah())
                  .nama(saved.getNama())
                  .tempat_lahir(saved.getTempatLahir())
                  .tanggal_lahir(saved.getTanggalLahir())
                  .jenis_kelamin(saved.getJenisKelamin())
                  .alamat(saved.getAlamat())
                  .profesi(saved.getProfesi())
                  .dibuat_di(saved.getDibuatDi())
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
            // 1. Upload file ke R2
            String fileUrl = r2StorageService.upload(file);

            // 2. Gunakan namaFile dari request atau fallback ke original filename
            String finalNamaFile = namaFile != null ? namaFile : file.getOriginalFilename();

            // 3. Cari FormPengajuan berdasarkan UUID
            UUID formUuidParsed = UUID.fromString(formUuid);
            FormPengajuan formPengajuan = formPengajuanRepository.findByUuid(formUuidParsed)
                  .orElseThrow(() -> new ResourceNotFoundException("Form with UUID " + formUuid + " not found"));

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
    public FormPengajuanResDTO.PengajuanResponse findByUuidWithFiles(UUID uuid) {
        FormPengajuan formPengajuan = formPengajuanRepository.findByUuidWithFiles(uuid)
              .orElseThrow(() -> new RuntimeException("Form with UUID " + uuid + " not found"));

        return FormPengajuanResDTO.PengajuanResponse.builder()
              .uuid(formPengajuan.getUuid())
              .induk_organisasi(formPengajuan.getIndukOrganisasi())
              .nomor_induk(formPengajuan.getNomorInduk())
              .jumlah_anggota(formPengajuan.getJumlahAnggota() != null
                    ? formPengajuan.getJumlahAnggota().toString()
                    : "0")
              .daerah(formPengajuan.getDaerah())
              .nama(formPengajuan.getNama())
              .tempat_lahir(formPengajuan.getTempatLahir())
              .tanggal_lahir(formPengajuan.getTanggalLahir())
              .jenis_kelamin(formPengajuan.getJenisKelamin())
              .alamat(formPengajuan.getAlamat())
              .profesi(formPengajuan.getProfesi())
              .dibuat_di(formPengajuan.getDibuatDi())
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
    @Transactional
    public FormPengajuanResDTO.SaveDataResponse editDataPengajuan(String authHeader, UUID uuid, FormPengajuanReqDTO.SavePengajuan dto) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Map<String, Object> claims = jwtTokenProvider.parseToken(token);
        String userId = String.valueOf(claims.get("uid"));

        FormPengajuan formPengajuan = formPengajuanRepository.findByUuid(uuid)
              .orElseThrow(() -> new ResourceNotFoundException("Data pengajuan dengan UUID " + uuid + " tidak ditemukan"));

        if (!userId.equals(formPengajuan.getAccount().getId().toString())) {
            throw new ForbiddenException("Data pengajuan yang diubah bukan milik Anda.");
        }

        if (formPengajuan.getStatus() == StatusEnum.APPROVED) {
            throw new ConflictException("Data pengajuan yang sudah disetujui tidak dapat diubah.");
        }

        try {
            formPengajuan
                  .setIndukOrganisasi(dto.getInduk_organisasi())
                  .setNomorInduk(dto.getNomor_induk())
                  .setJumlahAnggota(Integer.parseInt(dto.getJumlah_anggota()))
                  .setDaerah(dto.getDaerah())
                  .setNama(dto.getNama())
                  .setTempatLahir(dto.getTempat_lahir())
                  .setTanggalLahir(dto.getTanggal_lahir())
                  .setJenisKelamin(dto.getJenis_kelamin())
                  .setAlamat(dto.getAlamat())
                  .setProfesi(dto.getProfesi())
                  .setDibuatDi(dto.getDibuat_di())
                  .setKeterangan(dto.getKeterangan());

            formPengajuanRepository.save(formPengajuan);

            return FormPengajuanResDTO.SaveDataResponse.builder()
                  .uuid(formPengajuan.getUuid())
                  .induk_organisasi(formPengajuan.getIndukOrganisasi())
                  .nomor_induk(formPengajuan.getNomorInduk())
                  .jumlah_anggota(String.valueOf(formPengajuan.getJumlahAnggota()))
                  .daerah(formPengajuan.getDaerah())
                  .nama(formPengajuan.getNama())
                  .tempat_lahir(formPengajuan.getTempatLahir())
                  .tanggal_lahir(formPengajuan.getTanggalLahir())
                  .jenis_kelamin(formPengajuan.getJenisKelamin())
                  .alamat(formPengajuan.getAlamat())
                  .profesi(formPengajuan.getProfesi())
                  .dibuat_di(formPengajuan.getDibuatDi())
                  .status(formPengajuan.getStatus().toString())
                  .keterangan(formPengajuan.getKeterangan())
                  .build();

        } catch (Exception e) {
            throw new RuntimeException("Gagal mengubah data pengajuan: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public FormPengajuanResDTO.VerifyData verifyDataPengajuan(FormPengajuanReqDTO.VerifyPengajuan dto, UUID uuid) {
        try {
            FormPengajuan form = formPengajuanRepository.findByUuid(uuid)
                  .orElseThrow(() -> new ResourceNotFoundException("Form pengajuan tidak ditemukan"));

            form.setBerlakuDari(dto.getBerlaku_dari());
            form.setBerlakuSampai(dto.getBerlaku_sampai());
            form.setStatus(dto.getStatus() != null ? StatusEnum.valueOf(dto.getStatus()) : StatusEnum.PENDING);
            form.setTertanda(dto.getTertanda());
            form.setCatatan(dto.getCatatan());

            formPengajuanRepository.save(form);

            return FormPengajuanResDTO.VerifyData.builder()
                  .berlaku_dari(form.getBerlakuDari())
                  .berlaku_sampai(form.getBerlakuSampai())
                  .status(form.getStatus().name())
                  .tertanda(dto.getTertanda())
                  .catatan(form.getCatatan())
                  .build();

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Gagal memverifikasi data pengajuan: " + e.getMessage(), e);
        }
    }
}
