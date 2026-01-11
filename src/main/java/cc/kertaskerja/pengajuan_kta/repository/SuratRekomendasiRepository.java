package cc.kertaskerja.pengajuan_kta.repository;

import cc.kertaskerja.pengajuan_kta.entity.SuratRekomendasi;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SuratRekomendasiRepository extends JpaRepository<SuratRekomendasi, Long> {

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
            "FROM surat_rekomendasi " +
            "WHERE nomor_surat = :nomorSurat",
            nativeQuery = true)
    boolean existsByNomorSurat(@Param("nomorSurat") String nomorSurat);
}
