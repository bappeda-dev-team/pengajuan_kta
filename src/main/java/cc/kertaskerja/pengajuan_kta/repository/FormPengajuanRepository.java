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

    @Query(value = "SELECT * FROM form_pengajuan WHERE user_id = :accountId", nativeQuery = true)
    List<FormPengajuan> findByAccId(@Param("accountId") Long accountId);

    @Query(value = "SELECT * FROM form_pengajuan WHERE uuid = :uuid", nativeQuery = true)
    Optional<FormPengajuan> findByUuid(@Param("uuid") UUID uuid);

    @Query(value = """
        SELECT f.* 
        FROM form_pengajuan f
        LEFT JOIN file_pendukung fp ON f.id = fp.form_pengajuan_id
        WHERE f.uuid = :uuid
        """, nativeQuery = true)
    Optional<FormPengajuan> findByUuidWithFiles(@Param("uuid") UUID uuid);

}
