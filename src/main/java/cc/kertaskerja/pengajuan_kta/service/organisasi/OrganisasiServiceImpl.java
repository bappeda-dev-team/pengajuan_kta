package cc.kertaskerja.pengajuan_kta.service.organisasi;

import cc.kertaskerja.pengajuan_kta.dto.Auth.AccountResponse;
import cc.kertaskerja.pengajuan_kta.dto.Organisasi.OrganisasiReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.Organisasi.OrganisasiResDTO;
import cc.kertaskerja.pengajuan_kta.entity.Account;
import cc.kertaskerja.pengajuan_kta.entity.FilePendukung;
import cc.kertaskerja.pengajuan_kta.entity.Organisasi;
import cc.kertaskerja.pengajuan_kta.enums.StatusPengajuanEnum;
import cc.kertaskerja.pengajuan_kta.exception.ForbiddenException;
import cc.kertaskerja.pengajuan_kta.exception.ResourceNotFoundException;
import cc.kertaskerja.pengajuan_kta.repository.AccountRepository;
import cc.kertaskerja.pengajuan_kta.repository.FilePendukungRepository;
import cc.kertaskerja.pengajuan_kta.repository.OrganisasiRepository;
import cc.kertaskerja.pengajuan_kta.security.JwtTokenProvider;
import cc.kertaskerja.pengajuan_kta.service.external.EncryptService;
import cc.kertaskerja.pengajuan_kta.service.global.R2StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganisasiServiceImpl implements OrganisasiService {
    private final OrganisasiRepository organisasiRepository;
    private final AccountRepository accountRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final FilePendukungRepository filePendukungRepository;
    private final R2StorageService r2StorageService;
    private final EncryptService encryptService;

    @Override
    public List<OrganisasiResDTO> findAllOrganisasi(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Missing or invalid Authorization header");
            }

            List<Organisasi> organisasiList = organisasiRepository.findAll();

            return organisasiList.stream()
                  .map(entity -> OrganisasiResDTO.builder()
                        .uuid(entity.getUuid())
                        .bidang_keahlian(entity.getBidangKeahlian())
                        .nama_ketua(entity.getNamaKetua())
                        .nomor_telepon(entity.getNomorTelepon())
                        .alamat(entity.getAlamat())
                        .status(entity.getStatus().name())
                        .build())
                  .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Error fetching organisasi list: " + e.getMessage(), e);
        }
    }

    @Override
    public OrganisasiResDTO.OrganisasiDetailWithProfileResponse detailWithProfile(String authHeader, UUID uuid) {
        Organisasi entity = organisasiRepository.findByUuidWithFilesAndAccount(uuid)
              .orElseThrow(() -> new ResourceNotFoundException("Rekomendasi with UUID " + uuid + " not found"));

        Account owner = entity.getAccount();

        String token = authHeader.substring(7);
        Map<String, Object> claims = jwtTokenProvider.parseToken(token);
        String nik = String.valueOf(claims.get("sub"));
        String role = String.valueOf(claims.get("role"));

        Set<String> allowedRoles = Set.of("ADMIN", "KEPALA");

        if (!owner.getNik().equals(nik) && !allowedRoles.contains(role)) {
            throw new ForbiddenException("You are not allowed to verify data pengajuan.");
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
              .build();

        OrganisasiResDTO.DetailResponse organisasi = OrganisasiResDTO.DetailResponse.builder()
              .bidang_keahlian(entity.getBidangKeahlian())
              .nama_ketua(entity.getNamaKetua())
              .nomor_telepon(entity.getNomorTelepon())
              .alamat(entity.getAlamat())
              .status(entity.getStatus() != null ? entity.getStatus().name() : null)
              .catatan(entity.getCatatan())
              .file_pendukung(entity.getFilePendukung().stream()
                    .map(file -> OrganisasiResDTO.FilePendukung.builder()
                          .id(file.getId())
                          .file_url(file.getFileUrl())
                          .nama_file(file.getNamaFile())
                          .build())
                    .toList())
              .created_at(entity.getCreatedAt())
              .build();

        return OrganisasiResDTO.OrganisasiDetailWithProfileResponse.builder()
              .organisasi(organisasi)
              .profile(profile)
              .build();
    }

    @Override
    @Transactional
    public OrganisasiResDTO.SaveResponse saveData(OrganisasiReqDTO.SaveData dto) {
        Account account = accountRepository.findByNik(encryptService.encrypt(dto.getNik()))
              .orElseThrow(() -> new ResourceNotFoundException("NIK not found: " + dto.getNik()));
       try {
           Organisasi entity = Organisasi.builder()
                 .account(account)
                 .uuid(UUID.randomUUID())
                 .bidangKeahlian(dto.getBidang_keahlian())
                 .namaKetua(dto.getNama_ketua())
                 .nomorTelepon(dto.getNomor_telepon())
                 .alamat(dto.getAlamat())
                 .status(StatusPengajuanEnum.PENDING_VERIFICATOR)
                 .build();

           Organisasi saved =  organisasiRepository.save(entity);

           return OrganisasiResDTO.SaveResponse.builder()
                 .uuid(saved.getUuid())
                 .bidang_keahlian(saved.getBidangKeahlian())
                 .nama_ketua(saved.getNamaKetua())
                 .nomor_telepon(saved.getNomorTelepon())
                 .alamat(saved.getAlamat())
                 .status(saved.getStatus().name())
                 .build();
       } catch (DataIntegrityViolationException e) {
           throw new RuntimeException("Data integrity violation. Please check NOT NULL, UNIQUE, or foreign key constraints.", e);
       } catch (Exception e) {
           throw new RuntimeException("Unexpected error occurred while saving form_pengajuan", e);
       }
    }

    @Override
    @Transactional
    public OrganisasiResDTO.FilePendukung uploadFilePendukung(MultipartFile file, String organisasiUuid, String namaFile) {
        try {
            String fileUrl = r2StorageService.upload(file);
            String finalNamaFile = namaFile != null ? namaFile : file.getOriginalFilename();

            UUID rekomUuidParsed = UUID.fromString(organisasiUuid);
            Organisasi organisasi = organisasiRepository.findByUuid(rekomUuidParsed)
                  .orElseThrow(() -> new ResourceNotFoundException("Organisasi with UUID " + rekomUuidParsed + " not found"));

            FilePendukung filePendukung = FilePendukung.builder()
                  .fileUrl(fileUrl)
                  .namaFile(namaFile)
                  .organisasi(organisasi)
                  .build();

            FilePendukung savedFile = filePendukungRepository.save(filePendukung);

            return OrganisasiResDTO.FilePendukung.builder()
                  .organisasi_uuid(savedFile.getOrganisasiUuid())
                  .file_url(savedFile.getFileUrl())
                  .nama_file(savedFile.getNamaFile())
                  .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload and save file: " + e.getMessage(), e);
        }
    }
}
