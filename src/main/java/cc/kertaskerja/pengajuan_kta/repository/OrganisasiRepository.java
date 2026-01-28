package cc.kertaskerja.pengajuan_kta.repository;

import cc.kertaskerja.pengajuan_kta.entity.Organisasi;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganisasiRepository extends JpaRepository<Organisasi, Long> {
    @Query(value = """
                  SELECT EXISTS (
                    SELECT 1
                    FROM organisasi
                    WHERE UPPER(bidang_keahlian) = UPPER(:bidangKeahlian)
                  )
                """,
          nativeQuery = true
    )
    boolean existsBidangKeahlian(@org.springframework.data.repository.query.Param("bidangKeahlian") String bidangKeahlian);

    @Query(value = "SELECT * FROM organisasi WHERE uuid = :uuid", nativeQuery = true)
    Optional<Organisasi> findByUuid(@Param("uuid") UUID uuid);

    @Query(value = "SELECT o FROM Organisasi o LEFT JOIN FETCH o.filePendukung WHERE o.uuid = :uuid")
    Optional<Organisasi> findByUuidWithFiles(@Param("uuid") UUID uuid);

    @Query("""
        SELECT o
            FROM Organisasi o
                LEFT JOIN FETCH o.formPengajuan fp
                    WHERE o.uuid = :uuid
    """)
    Optional<Organisasi> findDetailWithPengajuan(@Param("uuid") UUID uuid);
}
