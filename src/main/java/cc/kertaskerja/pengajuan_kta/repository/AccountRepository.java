package cc.kertaskerja.pengajuan_kta.repository;

import cc.kertaskerja.pengajuan_kta.entity.Account;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query(value = "SELECT * FROM account WHERE nik = :nik LIMIT 1", nativeQuery = true)
    Optional<Account> findByNik(@Param("nik") String nik);

    @Query(value = "SELECT COUNT(*) > 0 FROM account WHERE nik = :nik", nativeQuery = true)
    boolean existsAccount(@Param("nik") String nik);

    @Query(value = "SELECT * FROM account WHERE email = :email LIMIT 1", nativeQuery = true)
    Optional<Account> findByEmail(@Param("email") String email);

    @Query(
          value = "SELECT * FROM account WHERE nik = :nik OR email = :email LIMIT 1",
          nativeQuery = true
    )
    Optional<Account> findByNikOrEmail(
          @Param("nik") String nik,
          @Param("email") String email
    );

    @Query("""
        SELECT a
        FROM Account a
        WHERE a.nip IS NOT NULL
        ORDER BY a.createdAt DESC
    """)
    List<Account> findAllAdminAccount();

    @Query("""
        SELECT a
        FROM Account a
        WHERE a.nip IS NULL
        ORDER BY a.createdAt DESC
    """)
    List<Account> findAllUser();

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO account (id, nama, nip, pangkat, jabatan, nik, email, nomor_telepon, password, role)
        SELECT :id, :nama, :nip, :pangkat, :jabatan, :nik, :email, :nomor_telepon, :password, :role
        WHERE
            :id IS NOT NULL
            OR :nama IS NOT NULL
            OR :nip IS NOT NULL
            OR :pangkat IS NOT NULL
            OR :jabatan IS NOT NULL
            OR :nip IS NOT NULL
            OR :email IS NOT NULL
            OR :nomor_telepon IS NOT NULL
            OR :password IS NOT NULL
            OR :role IS NOT NULL
    """, nativeQuery = true)
    int insertIfAnyPresent(
          @Param("id") Long id,
          @Param("nama") String nama,
          @Param("nip") String nip,
          @Param("pangkat") String pangkat,
          @Param("jabatan") String jabatan,
          @Param("nik") String nik,
          @Param("email") String email,
          @Param("nomor_telepon") String nomor_telepon,
          @Param("password") String password,
          @Param("role") String role
    );

    @Query(value = "SELECT COUNT(*) > 0 FROM account WHERE email = :email", nativeQuery = true)
    boolean existsByEmail(@Param("email") String email);

    @Query(value = "SELECT COUNT(*) > 0 FROM account WHERE nip = :nip", nativeQuery = true)
    boolean existsByNip(@Param("nip") String nip);

    boolean existsByNomorTelepon(String nomorTelepon);

    @Query(value = "SELECT * FROM account", nativeQuery = true)
    List<Account> findAllData();
}
