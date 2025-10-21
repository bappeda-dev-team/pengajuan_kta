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
import cc.kertaskerja.pengajuan_kta.service.otp.EmailService;
import cc.kertaskerja.pengajuan_kta.service.otp.OtpService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthAttemptService authAttempService;
    private final OtpService otpService;
    private final EmailService emailService;

    public AuthServiceImpl(AccountRepository accountRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           AuthAttemptService authAttempService,
                           OtpService otpService,
                           EmailService emailService) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authAttempService = authAttempService;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    private Long generateRandom6DigitId() {
        long min = 100000L;
        long max = 999999L;
        long randomId;

        do {
            randomId = (long) (Math.random() * (max - min + 1)) + min;
        } while (accountRepository.existsById(randomId));

        return randomId;
    }

    @Override
    public AccountResponse.SendOtp sendOTP(RegisterRequest.SendOtp request) {
        if (authAttempService.sendOtpBlocked(request.getEmail())) {
            throw new BadRequestException("TOO_MANY_ATTEMPTS. Please wait 1 minute before sending OTP again");
        }

        List<String> conflicts = new ArrayList<>();

        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            conflicts.add("Email has been registered. Please try another email.");
        }

        if (accountRepository.existsByUsername(request.getUsername())) {
            conflicts.add("Username has been registered. Please try another username.");
        }

        if (!conflicts.isEmpty()) {
            throw new ConflictException(String.join("; ", conflicts));
        }

       try {
           String otp = otpService.generateOtp(request.getEmail());
           emailService.sendOtpEmail(request.getEmail(), otp, request.getNama());

           AccountResponse.SendOtp response = new AccountResponse.SendOtp();
           response.setNama(request.getNama());
           response.setEmail(request.getEmail());
           response.setNomor_telepon(request.getNomor_telepon());
           response.setMessage("OTP code has been sent to your email and whatsapp");

           authAttempService.sendOtpSucceeded(request.getEmail());

           return response;
       } catch (Exception e) {
           authAttempService.sendOtpFailed(request.getEmail());
           throw new RuntimeException("Unexpected error occurred while registering account", e);
       }
    }

    @Override
    @Transactional
    public AccountResponse register(RegisterRequest request) {
        boolean valid = otpService.validateOtp(request.getEmail(), request.getOtp_code());

        if (!valid) {
            throw new BadRequestException("Invalid OTP code");
        }

        try {
            Long generatedId = generateRandom6DigitId();

            Account account = new Account();
            account.setId(generatedId);
            account.setNama(request.getNama());
            account.setEmail(request.getEmail());
            account.setUsername(request.getUsername());
            account.setPassword(passwordEncoder.encode(request.getPassword()));
            account.setNomorTelepon(request.getNomor_telepon());
            account.setTipeAkun(request.getTipe_akun().name());
            account.setStatus(StatusEnum.PENDING);
            account.setRole("USER");

            Account saved = accountRepository.save(account);

            AccountResponse response = new AccountResponse();
            response.setId(saved.getId());
            response.setNama(saved.getNama());
            response.setEmail(saved.getEmail());
            response.setUsername(saved.getUsername());
            response.setNomor_telepon(saved.getNomorTelepon());
            response.setTipe_akun(saved.getTipeAkun());
            response.setStatus(saved.getStatus().name());
            response.setRole(saved.getRole());

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

        if (authAttempService.loginBlocked(username)) {
            throw new BadRequestException("TOO_MANY_ATTEMPTS. Please wait 1 minute before retrying");
        }

        try {
            Account account = accountRepository.findByUsername(username)
                  .orElseThrow(() -> {
                      authAttempService.loginFailed(username);
                      return new ResourceNotFoundException("Invalid username or password");
                  });

            if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
                authAttempService.loginFailed(username);
                throw new ResourceNotFoundException("Invalid username or password");
            }

            authAttempService.loginSucceeded(username);

            String token = jwtTokenProvider.generateToken(
                  account.getId(),
                  account.getNama(),
                  account.getEmail(),
                  account.getUsername(),
                  account.getNomorTelepon(),
                  account.getRole()
            );

            LoginResponse.Profile profile = new LoginResponse.Profile(
                  String.valueOf(account.getId()),
                  account.getNama(),
                  account.getEmail(),
                  account.getUsername(),
                  account.getNomorTelepon(),
                  account.getTipeAkun(),
                  account.getStatus().name(),
                  account.getRole()
            );

            return new LoginResponse(token, profile);

        } catch (BadRequestException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error occurred during login process", e);
        }
    }
}