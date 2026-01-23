package cc.kertaskerja.pengajuan_kta.repository;

import cc.kertaskerja.pengajuan_kta.entity.Organisasi;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganisasiRepository extends JpaRepository<Organisasi, Long> {

    List<Organisasi> findByAccountNik(String nik);

    @Query(value = "SELECT * FROM organisasi WHERE uuid = :uuid", nativeQuery = true)
    Optional<Organisasi> findByUuid(@Param("uuid") UUID uuid);

    @Query("""
        SELECT o 
            FROM Organisasi o
                JOIN FETCH o.account a
                    LEFT JOIN FETCH o.filePendukung fp
                        WHERE o.uuid = :uuid
    """)
    Optional<Organisasi> findByUuidWithFilesAndAccount(@Param("uuid") UUID uuid);
}
