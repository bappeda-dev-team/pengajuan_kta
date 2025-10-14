package cc.kertaskerja.pengajuan_kta.service.auth;

import cc.kertaskerja.pengajuan_kta.dto.Auth.AccountResponse;
import cc.kertaskerja.pengajuan_kta.dto.Auth.LoginRequest;
import cc.kertaskerja.pengajuan_kta.dto.Auth.LoginResponse;
import cc.kertaskerja.pengajuan_kta.dto.Auth.RegisterRequest;
import cc.kertaskerja.pengajuan_kta.entity.Account;
import cc.kertaskerja.pengajuan_kta.enums.StatusEnum;
import cc.kertaskerja.pengajuan_kta.exception.BadRequestException;
import cc.kertaskerja.pengajuan_kta.exception.ResourceNotFoundException;
import cc.kertaskerja.pengajuan_kta.repository.AccountRepository;
import cc.kertaskerja.pengajuan_kta.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    private Long generateRandom6DigitId() {
        long min = 100000L;
        long max = 999999L;
        long randomId;

        do {
            randomId = (long) (Math.random() * (max - min + 1)) + min;
        } while (accountRepository.existsById(randomId)); // ensure uniqueness

        return randomId;
    }

    @Override
    @Transactional
    public AccountResponse register(RegisterRequest request) {
        try {
            if (accountRepository.existsByUsername(request.getUsername())) {
                throw new BadRequestException("Username already exists");
            }

            Long generatedId = generateRandom6DigitId();

            Account account = Account.builder()
                  .id(generatedId)
                  .username(request.getUsername())
                  .password(passwordEncoder.encode(request.getPassword()))
                  .nomorTelepon(request.getNomor_telepon())
                  .tipeAkun(request.getTipe_akun())
                  .status(StatusEnum.PENDING)
                  .role("USER")
                  .build();

            Account saved = accountRepository.save(account);

            return AccountResponse.builder()
                  .id(saved.getId())
                  .username(saved.getUsername())
                  .nomor_telepon(saved.getNomorTelepon())
                  .tipe_akun(saved.getTipeAkun())
                  .status("PENDING")
                  .role(saved.getRole())
                  .createdAt(saved.getCreatedAt())
                  .updatedAt(saved.getUpdatedAt())
                  .build();

        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Data integrity violation: please check unique or not-null constraints.");
        } catch (Exception e) {
            System.out.println("Error: " + e);
            throw new RuntimeException("Unexpected error occurred while registering account", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        try {
            Account account = accountRepository.findByUsername(request.getUsername())
                  .orElseThrow(() -> new ResourceNotFoundException("Invalid username or password"));

            if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
                throw new ResourceNotFoundException("Invalid username or password");
            }

            String token = jwtTokenProvider.generateToken(account.getId(), account.getUsername(), account.getTipeAkun());

            return LoginResponse.builder()
                  .access_token(token)
                  .build();

        } catch (Exception e) {
            throw new RuntimeException("Error occurred during login process", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse validateToken(String token) {
        try {
            Map<String, Object> claims = jwtTokenProvider.parseToken(token);

            Long userId = ((Number) claims.get("uid")).longValue();
            String username = (String) claims.get("sub");

            Account account = accountRepository.findById(userId)
                  .filter(acc -> acc.getUsername().equals(username))
                  .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired token"));

            return AccountResponse.builder()
                  .id(account.getId())
                  .username(account.getUsername())
                  .nomor_telepon(account.getNomorTelepon())
                  .tipe_akun(account.getTipeAkun())
                  .status(account.getStatus().name())
                  .role(account.getRole())
                  .createdAt(account.getCreatedAt())
                  .updatedAt(account.getUpdatedAt())
                  .build();

        } catch (Exception e) {
            throw new RuntimeException("Token validation failed: " + e.getMessage(), e);
        }
    }
}
