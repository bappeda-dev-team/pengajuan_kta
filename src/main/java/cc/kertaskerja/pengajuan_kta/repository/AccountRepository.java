package cc.kertaskerja.pengajuan_kta.repository;

import cc.kertaskerja.pengajuan_kta.entity.Account;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query(value = "SELECT * FROM account WHERE username = :username LIMIT 1", nativeQuery = true)
    Optional<Account> findByUsername(@Param("username") String username);

    @Query(value = "SELECT COUNT(*) > 0 FROM account WHERE username = :username", nativeQuery = true)
    boolean existsByUsername(@Param("username") String username);

    @Query(value = "SELECT * FROM account WHERE email = :email LIMIT 1", nativeQuery = true)
    Optional<Account> findByEmail(@Param("email") String email);

    @Query(value = "SELECT COUNT(*) > 0 FROM account WHERE email = :email", nativeQuery = true)
    boolean existsByEmail(@Param("email") String email);


}
