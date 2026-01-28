package cc.kertaskerja.pengajuan_kta.service.organisasi;

import cc.kertaskerja.pengajuan_kta.dto.Organisasi.OrganisasiReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.Organisasi.OrganisasiResDTO;
import cc.kertaskerja.pengajuan_kta.entity.FilePendukung;
import cc.kertaskerja.pengajuan_kta.entity.Organisasi;
import cc.kertaskerja.pengajuan_kta.exception.*;
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

            if (organisasiList == null || organisasiList.isEmpty()) {
                return Collections.emptyList();
            }

            return organisasiList.stream()
                  .map(entity -> OrganisasiResDTO.builder()
                        .uuid(entity.getUuid())
                        .bidang_keahlian(entity.getBidangKeahlian())
                        .alamat(entity.getAlamat())
                        .build())
                  .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Error fetching organisasi list: " + e.getMessage(), e);
        }
    }

    @Override
    public OrganisasiResDTO.DetailResponse detailOrganisasi(String authHeader, UUID uuid) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Map<String, Object> claims = jwtTokenProvider.parseToken(token);
        String userId = String.valueOf(claims.get("uid"));

        Organisasi organisasi = organisasiRepository.findDetailWithPengajuan(uuid)
              .orElseThrow(() -> new ResourceNotFoundException("Organisasi with UUID " + uuid + " not found"));

        return OrganisasiResDTO.DetailResponse.builder()
              .uuid(organisasi.getUuid())
              .bidang_keahlian(organisasi.getBidangKeahlian())
              .alamat(organisasi.getAlamat())
              .pengajuan(organisasi.getFormPengajuan().stream()
                    .map(data -> OrganisasiResDTO.Pengajuan.builder()
                          .nomor_induk(data.getNomorInduk())
                          .nama_ketua(data.getNamaKetua())
                          .nik_ketua(data.getNikKetua())
                          .nomor_telepon(data.getNomorTelepon())
                          .jumlah_anggota(data.getJumlahAnggota())
                          .status(data.getStatus().name())
                          .build())
                    .toList())
              .build();
    }

    @Override
    @Transactional
    public OrganisasiResDTO.SaveResponse saveData(OrganisasiReqDTO.SaveData dto) {
        boolean isExist = organisasiRepository.existsBidangKeahlian(dto.getBidang_keahlian());

        if (isExist) {
            throw new BadRequestException("Organisasi sudah terdaftar");
        }

       try {
           Organisasi entity = Organisasi.builder()
                 .uuid(UUID.randomUUID())
                 .bidangKeahlian(dto.getBidang_keahlian())
                 .alamat(dto.getAlamat())
                 .build();

           Organisasi saved =  organisasiRepository.save(entity);

           return OrganisasiResDTO.SaveResponse.builder()
                 .uuid(saved.getUuid())
                 .bidang_keahlian(saved.getBidangKeahlian())
                 .alamat(saved.getAlamat())
                 .build();
       } catch (DataIntegrityViolationException e) {
           throw new RuntimeException("Data integrity violation. Please check NOT NULL, UNIQUE, or foreign key constraints.", e);
       } catch (Exception e) {
           throw new RuntimeException("Unexpected error occurred while saving form_pengajuan", e);
       }
    }

    @Override
    @Transactional
    public OrganisasiResDTO.SaveResponse editDataOrganisasi(String authHeader, UUID uuid, OrganisasiReqDTO.SaveData dto) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Map<String, Object> claims = jwtTokenProvider.parseToken(token);
        String userId = String.valueOf(claims.get("uid"));

        Organisasi organisasi = organisasiRepository.findByUuid(uuid)
              .orElseThrow(() -> new ResourceNotFoundException("Organisasi with UUID " + uuid + " not found"));

        try {
            organisasi
                  .setBidangKeahlian(dto.getBidang_keahlian())
                  .setAlamat(dto.getAlamat());
            Organisasi saved = organisasiRepository.save(organisasi);

            return OrganisasiResDTO.SaveResponse.builder()
                  .uuid(saved.getUuid())
                  .bidang_keahlian(saved.getBidangKeahlian())
                  .alamat(saved.getAlamat())
                  .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to change data organisasi: " + e.getMessage());
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
                  .namaFile(finalNamaFile)
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

    @Override
    @Transactional
    public void deleteData(String uuid) {
        UUID organisasiUuid;

        try {
            organisasiUuid = UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid UUID format: " + uuid);
        }

        Organisasi organisasi = organisasiRepository.findByUuidWithFiles(organisasiUuid)
              .orElseThrow(() -> new ResourceNotFoundException("Organisasi with UUID " + uuid + " not found"));

        if (organisasi.getFilePendukung() != null && !organisasi.getFilePendukung().isEmpty()) {
            for (FilePendukung file : organisasi.getFilePendukung()) {
                if (file.getFileUrl() != null && !file.getFileUrl().isBlank()) {
                    r2StorageService.delete(file.getFileUrl());
                }
            }
        }

        filePendukungRepository.deleteByOrganisasi(organisasi);
        filePendukungRepository.flush();
        organisasiRepository.delete(organisasi);
    }

    @Override
    @Transactional
    public void deleteFilePendukung(String authHeader, Long id) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthenticationException("Missing or invalid Authorization header");
        }

        FilePendukung file = filePendukungRepository.findById(id)
              .orElseThrow(() -> new ResourceNotFoundException("File pendukung not found: " + id));

        if (file.getFileUrl() != null && !file.getFileUrl().isBlank()) {
            r2StorageService.delete(file.getFileUrl());
        }

        filePendukungRepository.delete(file);
    }
}
