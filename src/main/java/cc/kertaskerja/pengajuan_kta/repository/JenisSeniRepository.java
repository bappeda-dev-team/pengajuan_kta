package cc.kertaskerja.pengajuan_kta.repository;

import cc.kertaskerja.pengajuan_kta.entity.JenisSeni;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JenisSeniRepository extends JpaRepository<JenisSeni, Long> {

    boolean existsByKodeJenisSeni(String kodeJenisSeni);

    Optional<JenisSeni> findByKodeJenisSeni(String kodeJenisSeni);
}