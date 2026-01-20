package cc.kertaskerja.pengajuan_kta.repository;

import cc.kertaskerja.pengajuan_kta.entity.SuratRekomendasi;
import cc.kertaskerja.pengajuan_kta.enums.StatusPengajuanEnum;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SuratRekomendasiRepository extends JpaRepository<SuratRekomendasi, Long> {

    @Query("""
        SELECT rk
            FROM SuratRekomendasi rk
                JOIN FETCH rk.account a
    """)
    List<SuratRekomendasi> findAllWithAccount();

    @Query("""
        SELECT DISTINCT rk
            FROM SuratRekomendasi rk
                JOIN FETCH rk.account a
                    WHERE rk.status IN :statuses
                        ORDER BY rk.createdAt DESC
    """)
    List<SuratRekomendasi> findAllByStatusInWithAccount(@Param("statuses") List<StatusPengajuanEnum> statuses);

    @Query(value = "SELECT * FROM surat_rekomendasi WHERE nik = :nik", nativeQuery = true)
    List<SuratRekomendasi> findByAccId(@Param("nik") String nik);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
            "FROM surat_rekomendasi " +
            "WHERE nomor_surat = :nomorSurat",
            nativeQuery = true)
    boolean existsByNomorSurat(@Param("nomorSurat") String nomorSurat);

    @Query(value = "SELECT * FROM surat_rekomendasi WHERE uuid = :uuid", nativeQuery = true)
    Optional<SuratRekomendasi> findByUuid(@Param("uuid") UUID uuid);

    @Query("SELECT rk FROM SuratRekomendasi rk LEFT JOIN FETCH rk.filePendukung WHERE rk.uuid = :uuid")
    Optional<SuratRekomendasi> findByUuidWithFiles(@Param("uuid") UUID uuid);

    @Query("""
        SELECT rk
            FROM SuratRekomendasi rk
                JOIN FETCH rk.account a
                    LEFT JOIN FETCH rk.filePendukung fp
                        WHERE rk.uuid = :uuid
    """)
    Optional<SuratRekomendasi> findByUuidWithFilesAndAccount(@Param("uuid") UUID uuid);
}
