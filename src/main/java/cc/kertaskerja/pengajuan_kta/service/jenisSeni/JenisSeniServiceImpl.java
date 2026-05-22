package cc.kertaskerja.pengajuan_kta.service.jenisSeni;

import cc.kertaskerja.pengajuan_kta.dto.JenisSeni.JenisReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.JenisSeni.JenisResDTO;
import cc.kertaskerja.pengajuan_kta.entity.JenisSeni;
import cc.kertaskerja.pengajuan_kta.exception.*;
import cc.kertaskerja.pengajuan_kta.repository.JenisSeniRepository;
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
public class JenisSeniServiceImpl implements JenisSeniService {

    private final JenisSeniRepository jenisSeniRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public List<JenisResDTO> findAll(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Missing or invalid Authorization header");
            }

            List<JenisSeni> jenisSeniList = jenisSeniRepository.findAll();

            if (jenisSeniList == null || jenisSeniList.isEmpty()) {
                return Collections.emptyList();
            }

            return jenisSeniList.stream()
                  .map(entity -> JenisResDTO.builder()
                        .id(entity.getId())
                        .kode_jenis_seni(entity.getKodeJenisSeni())
                        .jenis_seni(entity.getJenisSeni())
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

        JenisSeni jenisSeni = jenisSeniRepository.findById(id)
              .orElseThrow(() -> new ResourceNotFoundException("Jenis Organisasi with ID " + id + " not found"));

        return JenisResDTO.builder()
              .id(jenisSeni.getId())
              .kode_jenis_seni(jenisSeni.getKodeJenisSeni())
              .jenis_seni(jenisSeni.getJenisSeni())
              .build();
    }

    @Override
    @Transactional
    public JenisResDTO saveData(JenisReqDTO.SaveData dto) {
        // Cek data duplikat (pastikan Anda menambahkan method existsByKodeJenisOrganisasi di Repository)
        boolean isExist = jenisSeniRepository.existsByKodeJenisSeni(dto.getKode_jenis_seni());

        if (isExist) {
            throw new BadRequestException("Kode Jenis Organisasi sudah terdaftar");
        }

        try {
            JenisSeni entity = JenisSeni.builder()
                  .kodeJenisSeni(dto.getKode_jenis_seni())
                  .jenisSeni(dto.getJenis_seni())
                  .build();

            JenisSeni saved = jenisSeniRepository.save(entity);

            return JenisResDTO.builder()
                  .id(saved.getId())
                  .kode_jenis_seni(saved.getKodeJenisSeni())
                  .jenis_seni(saved.getJenisSeni())
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

        JenisSeni jenisSeni = jenisSeniRepository.findById(id)
              .orElseThrow(() -> new ResourceNotFoundException("Jenis Organisasi with ID " + id + " not found"));

        try {
            jenisSeni
                  .setKodeJenisSeni(dto.getKode_jenis_seni())
                  .setJenisSeni(dto.getJenis_seni());

            JenisSeni saved = jenisSeniRepository.save(jenisSeni);

            return JenisResDTO.builder()
                  .id(saved.getId())
                  .kode_jenis_seni(saved.getKodeJenisSeni())
                  .jenis_seni(saved.getJenisSeni())
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

        JenisSeni jenisSeni = jenisSeniRepository.findById(id)
              .orElseThrow(() -> new ResourceNotFoundException("Jenis Organisasi with ID " + id + " not found"));

        jenisSeniRepository.delete(jenisSeni);
    }
}