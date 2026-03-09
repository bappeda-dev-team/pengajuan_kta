package cc.kertaskerja.pengajuan_kta.repository;

import cc.kertaskerja.pengajuan_kta.entity.IzinOperasional;
import cc.kertaskerja.pengajuan_kta.enums.StatusPengajuanEnum;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IzinOperasionalRepository extends JpaRepository<IzinOperasional, Long> {
    @Query("""
        SELECT DISTINCT io
        FROM IzinOperasional io 
        JOIN FETCH io.account a 
        WHERE io.status IN :statuses
        ORDER BY io.createdAt DESC          
    """)
    List<IzinOperasional> findAllByStatusInWithAccount(@Param("statuses") List<StatusPengajuanEnum> statuses);

    @Query("""
        SELECT io
        FROM IzinOperasional io
        JOIN FETCH io.account a
        WHERE a.nik = :nik
        ORDER BY io.createdAt DESC
    """)
    List<IzinOperasional> findByAccIdWithAccount(@Param("nik") String nik);

    @Query("""
        SELECT io
        FROM IzinOperasional io 
            JOIN FETCH io.account a
                JOIN FETCH io.formPengajuan p
                    LEFT JOIN io.filePendukung fp
                        WHERE io.uuid = :uuid
    """)
    Optional<IzinOperasional> findByUuidWithFilesAndAccount(@Param("uuid") UUID uuid);

    @Query(value = "SELECT * FROM izin_operasional WHERE uuid = :uuid", nativeQuery = true)
    Optional<IzinOperasional> findByUuid(@Param("uuid") UUID uuid);

    @Query(value = "SELECT io FROM IzinOperasional io LEFT JOIN FETCH io.filePendukung WHERE io.uuid = :uuid")
    Optional<IzinOperasional> findByUuidWithFiles(@Param("uuid") UUID uuid);
}
