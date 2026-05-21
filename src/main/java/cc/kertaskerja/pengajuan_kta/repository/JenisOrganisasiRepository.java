package cc.kertaskerja.pengajuan_kta.repository;

import cc.kertaskerja.pengajuan_kta.entity.JenisOrganisasi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JenisOrganisasiRepository extends JpaRepository<JenisOrganisasi, Long> {

    boolean existsByKodeJenisOrganisasi(String kodeJenisOrganisasi);

    Optional<JenisOrganisasi> findByKodeJenisOrganisasi(String kodeJenisOrganisasi);
}