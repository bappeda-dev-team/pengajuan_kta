package cc.kertaskerja.pengajuan_kta.repository;

import cc.kertaskerja.pengajuan_kta.entity.FormPengajuan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FormPengajuanRepository extends JpaRepository<FormPengajuan, Long> {

    @Query(value = "SELECT * FROM form_pengajuan", nativeQuery = true)
    List<FormPengajuan> findAllData();

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
          "FROM form_pengajuan " +
          "WHERE nomor_induk = :nomorInduk",
          nativeQuery = true)
    boolean existsByNomorInduk(@Param("nomorInduk") String nomorInduk);

    @Query(value = "SELECT * FROM form_pengajuan WHERE nik = :nik", nativeQuery = true)
    List<FormPengajuan> findByAccId(@Param("nik") String nik);

    @Query("""
              SELECT f
              FROM FormPengajuan f
              JOIN FETCH f.account a
          """)
    List<FormPengajuan> findAllWithAccount();

    @Query(value = "SELECT * FROM form_pengajuan WHERE uuid = :uuid", nativeQuery = true)
    Optional<FormPengajuan> findByUuid(@Param("uuid") UUID uuid);

    @Query("SELECT f FROM FormPengajuan f LEFT JOIN FETCH f.filePendukung WHERE f.uuid = :uuid")
    Optional<FormPengajuan> findByUuidWithFiles(@Param("uuid") UUID uuid);

    @Query("""
              SELECT f
              FROM FormPengajuan f
              JOIN FETCH f.account a
              LEFT JOIN FETCH f.filePendukung fp
              WHERE f.uuid = :uuid
          """)
    Optional<FormPengajuan> findByUuidWithFilesAndAccount(@Param("uuid") UUID uuid);

    @Query(value = """
              SELECT 
                EXTRACT(MONTH FROM created_at) AS month,
                COUNT(*) AS total
              FROM form_pengajuan
              WHERE EXTRACT(YEAR FROM created_at) = :year
              GROUP BY month
              ORDER BY month
          """, nativeQuery = true)
    List<Object[]> countPengajuanPerMonth(@Param("year") int year);

}
