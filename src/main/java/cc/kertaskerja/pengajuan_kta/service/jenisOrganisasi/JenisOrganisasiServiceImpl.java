package cc.kertaskerja.pengajuan_kta.service.jenisOrganisasi;

import cc.kertaskerja.pengajuan_kta.dto.JenisOrganisasi.JenisReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.JenisOrganisasi.JenisResDTO;
import cc.kertaskerja.pengajuan_kta.entity.JenisOrganisasi;
import cc.kertaskerja.pengajuan_kta.exception.*;
import cc.kertaskerja.pengajuan_kta.repository.JenisOrganisasiRepository;
import cc.kertaskerja.pengajuan_kta.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JenisOrganisasiServiceImpl implements JenisOrganisasiService {

    private final JenisOrganisasiRepository jenisOrganisasiRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public List<JenisResDTO> findAll(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Missing or invalid Authorization header");
            }

            List<JenisOrganisasi> jenisOrganisasiList = jenisOrganisasiRepository.findAll();

            if (jenisOrganisasiList == null || jenisOrganisasiList.isEmpty()) {
                return Collections.emptyList();
            }

            return jenisOrganisasiList.stream()
                  .map(entity -> JenisResDTO.builder()
                        .id(entity.getId())
                        .kode_jenis_organisasi(entity.getKodeJenisOrganisasi())
                        .nama_jenis_organisasi(entity.getNamaJenisOrganisasi())
                        .build())
                  .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Error fetching jenis organisasi list: " + e.getMessage(), e);
        }
    }

    @Override
    public JenisResDTO findById(String authHeader, Long id) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Map<String, Object> claims = jwtTokenProvider.parseToken(token);
        String userId = String.valueOf(claims.get("uid")); // Parsing token sama seperti contoh Anda

        JenisOrganisasi jenisOrganisasi = jenisOrganisasiRepository.findById(id)
              .orElseThrow(() -> new ResourceNotFoundException("Jenis Organisasi with ID " + id + " not found"));

        return JenisResDTO.builder()
              .id(jenisOrganisasi.getId())
              .kode_jenis_organisasi(jenisOrganisasi.getKodeJenisOrganisasi())
              .nama_jenis_organisasi(jenisOrganisasi.getNamaJenisOrganisasi())
              .build();
    }

    @Override
    @Transactional
    public JenisResDTO saveData(JenisReqDTO.SaveData dto) {
        // Cek data duplikat (pastikan Anda menambahkan method existsByKodeJenisOrganisasi di Repository)
        boolean isExist = jenisOrganisasiRepository.existsByKodeJenisOrganisasi(dto.getKode_jenis_organisasi());

        if (isExist) {
            throw new BadRequestException("Kode Jenis Organisasi sudah terdaftar");
        }

        try {
            JenisOrganisasi entity = JenisOrganisasi.builder()
                  .kodeJenisOrganisasi(dto.getKode_jenis_organisasi())
                  .namaJenisOrganisasi(dto.getNama_jenis_organisasi())
                  .build();

            JenisOrganisasi saved = jenisOrganisasiRepository.save(entity);

            return JenisResDTO.builder()
                  .id(saved.getId())
                  .kode_jenis_organisasi(saved.getKodeJenisOrganisasi())
                  .nama_jenis_organisasi(saved.getNamaJenisOrganisasi())
                  .build();
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Data integrity violation. Please check NOT NULL, UNIQUE, or foreign key constraints.", e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error occurred while saving jenis_organisasi", e);
        }
    }

    @Override
    @Transactional
    public JenisResDTO updateData(String authHeader, Long id, JenisReqDTO.SaveData dto) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Map<String, Object> claims = jwtTokenProvider.parseToken(token);
        String userId = String.valueOf(claims.get("uid"));

        JenisOrganisasi jenisOrganisasi = jenisOrganisasiRepository.findById(id)
              .orElseThrow(() -> new ResourceNotFoundException("Jenis Organisasi with ID " + id + " not found"));

        try {
            jenisOrganisasi
                  .setKodeJenisOrganisasi(dto.getKode_jenis_organisasi())
                  .setNamaJenisOrganisasi(dto.getNama_jenis_organisasi());

            JenisOrganisasi saved = jenisOrganisasiRepository.save(jenisOrganisasi);

            return JenisResDTO.builder()
                  .id(saved.getId())
                  .kode_jenis_organisasi(saved.getKodeJenisOrganisasi())
                  .nama_jenis_organisasi(saved.getNamaJenisOrganisasi())
                  .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to change data jenis organisasi: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteData(String authHeader, Long id) {
        // Cek auth sama seperti deleteFilePendukung di contoh Anda
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        JenisOrganisasi jenisOrganisasi = jenisOrganisasiRepository.findById(id)
              .orElseThrow(() -> new ResourceNotFoundException("Jenis Organisasi with ID " + id + " not found"));

        jenisOrganisasiRepository.delete(jenisOrganisasi);
    }
}