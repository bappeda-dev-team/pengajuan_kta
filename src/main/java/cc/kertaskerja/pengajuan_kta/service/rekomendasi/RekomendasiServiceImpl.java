package cc.kertaskerja.pengajuan_kta.service.rekomendasi;

import cc.kertaskerja.pengajuan_kta.dto.Rekomendasi.RekomendasiReqDTO;
import cc.kertaskerja.pengajuan_kta.dto.Rekomendasi.RekomendasiResDTO;
import cc.kertaskerja.pengajuan_kta.entity.Account;
import cc.kertaskerja.pengajuan_kta.entity.SuratRekomendasi;
import cc.kertaskerja.pengajuan_kta.enums.StatusPengajuanEnum;
import cc.kertaskerja.pengajuan_kta.exception.ConflictException;
import cc.kertaskerja.pengajuan_kta.exception.ResourceNotFoundException;
import cc.kertaskerja.pengajuan_kta.repository.AccountRepository;
import cc.kertaskerja.pengajuan_kta.repository.SuratRekomendasiRepository;
import cc.kertaskerja.pengajuan_kta.security.JwtTokenProvider;
import cc.kertaskerja.pengajuan_kta.service.external.EncryptService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RekomendasiServiceImpl implements RekomendasiService {
    private final AccountRepository accountRepository;
    private final SuratRekomendasiRepository repository;
    private final JwtTokenProvider jwtTokenProvider;
    private final EncryptService encryptService;

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
}
