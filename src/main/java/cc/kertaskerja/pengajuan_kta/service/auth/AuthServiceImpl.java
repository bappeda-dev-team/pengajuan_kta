package cc.kertaskerja.pengajuan_kta.service.auth;

import cc.kertaskerja.pengajuan_kta.dto.Auth.AccountResponse;
import cc.kertaskerja.pengajuan_kta.dto.Auth.LoginRequest;
import cc.kertaskerja.pengajuan_kta.dto.Auth.LoginResponse;
import cc.kertaskerja.pengajuan_kta.dto.Auth.RegisterRequest;
import cc.kertaskerja.pengajuan_kta.entity.Account;
import cc.kertaskerja.pengajuan_kta.enums.StatusEnum;
import cc.kertaskerja.pengajuan_kta.exception.BadRequestException;
import cc.kertaskerja.pengajuan_kta.exception.ConflictException;
import cc.kertaskerja.pengajuan_kta.exception.ResourceNotFoundException;
import cc.kertaskerja.pengajuan_kta.repository.AccountRepository;
import cc.kertaskerja.pengajuan_kta.security.JwtTokenProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginAttemptService loginAttemptService;

    public AuthServiceImpl(AccountRepository accountRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           LoginAttemptService loginAttemptService) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.loginAttemptService = loginAttemptService;
    }

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
            if (accountRepository.existsByUsername(request.getUsername())) {
                throw new ConflictException("Username has been registered. Please try another username.");
            }

        try {

            Long generatedId = generateRandom6DigitId();

            Account account = new Account();
            account.setId(generatedId);
            account.setUsername(request.getUsername());
            account.setPassword(passwordEncoder.encode(request.getPassword()));
            account.setNomorTelepon(request.getNomor_telepon());
            account.setTipeAkun(request.getTipe_akun().name());
            account.setStatus(StatusEnum.PENDING);
            account.setRole("USER");

            Account saved = accountRepository.save(account);

            AccountResponse response = new AccountResponse();
            response.setId(saved.getId());
            response.setUsername(saved.getUsername());
            response.setNomor_telepon(saved.getNomorTelepon());
            response.setTipe_akun(saved.getTipeAkun());
            response.setStatus(saved.getStatus().name());
            response.setRole(saved.getRole());
            response.setCreatedAt(saved.getCreatedAt());
            response.setUpdatedAt(saved.getUpdatedAt());

            return response;

        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().toLowerCase().contains("unique") || e.getMessage().toLowerCase().contains("duplicate")) {
                throw new ConflictException("Username has been registered. Please try another username.");
            }
            throw new BadRequestException("Data integrity violation: please check unique or not-null constraints.");
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error occurred while registering account", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();

        if (loginAttemptService.isBlocked(username)) {
            throw new BadRequestException("TOO_MANY_ATTEMPTS");
        }

        try {
            Account account = accountRepository.findByUsername(username)
                  .orElseThrow(() -> {
                      loginAttemptService.loginFailed(username);
                      return new ResourceNotFoundException("Invalid username or password");
                  });

            if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
                loginAttemptService.loginFailed(username);
                throw new ResourceNotFoundException("Invalid username or password");
            }

            loginAttemptService.loginSucceeded(username);

            String token = jwtTokenProvider.generateToken(
                  account.getId(),
                  account.getUsername(),
                  account.getNomorTelepon(),
                  account.getRole()
            );

            LoginResponse response = new LoginResponse();
            response.setAccess_token(token);

            return response;

        } catch (BadRequestException | ResourceNotFoundException e) {
            throw e;
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

            AccountResponse response = new AccountResponse();
            response.setId(account.getId());
            response.setUsername(account.getUsername());
            response.setNomor_telepon(account.getNomorTelepon());
            response.setTipe_akun(account.getTipeAkun());
            response.setStatus(account.getStatus().name());
            response.setRole(account.getRole());
            response.setCreatedAt(account.getCreatedAt());
            response.setUpdatedAt(account.getUpdatedAt());

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Token validation failed: " + e.getMessage(), e);
        }
    }
}