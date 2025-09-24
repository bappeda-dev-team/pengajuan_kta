package cc.kertaskerja.pengajuan_kta.repository;

import cc.kertaskerja.pengajuan_kta.entity.FormPengajuan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface FormPengajuanRepository extends JpaRepository<FormPengajuan, Long> {

    Optional<FormPengajuan> findByUuid(UUID uuid);

    @Query("SELECT f FROM FormPengajuan f LEFT JOIN FETCH f.filePendukung WHERE f.uuid = :uuid")
    Optional<FormPengajuan> findByUuidWithFiles(@Param("uuid") UUID uuid);

}
