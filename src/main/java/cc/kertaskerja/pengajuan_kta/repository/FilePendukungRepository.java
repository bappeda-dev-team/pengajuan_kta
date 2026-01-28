package cc.kertaskerja.pengajuan_kta.repository;

import cc.kertaskerja.pengajuan_kta.entity.FilePendukung;
import cc.kertaskerja.pengajuan_kta.entity.FormPengajuan;
import cc.kertaskerja.pengajuan_kta.entity.Organisasi;
import cc.kertaskerja.pengajuan_kta.entity.SuratRekomendasi;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface FilePendukungRepository extends JpaRepository<FilePendukung, Long> {
    @Modifying
    @Transactional
    void deleteByOrganisasi(Organisasi organisasi);

    @Modifying
    @Transactional
    void deleteByFormPengajuan(FormPengajuan form);

    @Modifying
    @Transactional
    void deleteBySuratRekomendasi(SuratRekomendasi rekomendasi);

}
