package cc.kertaskerja.pengajuan_kta.service.auth;

import cc.kertaskerja.pengajuan_kta.dto.Auth.AccountResponse;
import cc.kertaskerja.pengajuan_kta.dto.Auth.LoginRequest;
import cc.kertaskerja.pengajuan_kta.dto.Auth.LoginResponse;
import cc.kertaskerja.pengajuan_kta.dto.Auth.RegisterRequest;
import cc.kertaskerja.pengajuan_kta.entity.Account;
import cc.kertaskerja.pengajuan_kta.enums.StatusAccountEnum;
import cc.kertaskerja.pengajuan_kta.exception.*;
import cc.kertaskerja.pengajuan_kta.repository.AccountRepository;
import cc.kertaskerja.pengajuan_kta.security.JwtTokenProvider;
import cc.kertaskerja.pengajuan_kta.service.captcha.CaptchaService;
import cc.kertaskerja.pengajuan_kta.service.external.EncryptService;
import cc.kertaskerja.pengajuan_kta.service.otp.EmailService;
import cc.kertaskerja.pengajuan_kta.service.otp.OtpService;
import cc.kertaskerja.pengajuan_kta.service.otp.SmsService;
import cc.kertaskerja.pengajuan_kta.util.AccountUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthAttemptService authAttempService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final SmsService smsService;
    private final AccountUtils accountUtils;
    private final CaptchaService captchaService;
    private final EncryptService encryptService;

    public AuthServiceImpl(AccountRepository accountRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           AuthAttemptService authAttempService,
                           OtpService otpService,
                           EmailService emailService,
                           SmsService smsService,
                           AccountUtils accountUtils,
                           CaptchaService captchaService,
                           EncryptService encryptService) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authAttempService = authAttempService;
        this.otpService = otpService;
        this.emailService = emailService;
        this.smsService = smsService;
        this.accountUtils = accountUtils;
        this.captchaService = captchaService;
        this.encryptService = encryptService;
    }

    @Override
    public AccountResponse.SendOtp sendOTP(RegisterRequest.SendOtp request) {
        if (authAttempService.sendOtpBlocked(request.getNomor_telepon())) {
            throw new RateLimitException("Terlalu banyak percobaan. Silakan coba lagi dalam 1 menit");
        }

        List<String> conflicts = new ArrayList<>();

        if (accountRepository.findByNik(encryptService.encrypt(request.getNik())).isPresent()) {
            conflicts.add("NIK sudah terdaftar.");
        }

        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            conflicts.add("Email sudah terdaftar. Silakan gunakan email lain.");
        }

        if (accountRepository.existsAccount(request.getNik())) {
            conflicts.add("NIK sudah terdaftar.");
        }

        String formattedPhone = accountUtils.formatPhoneNumber(request.getNomor_telepon());
        if (accountRepository.existsByNomorTelepon(formattedPhone)) {
            conflicts.add("Nomor WhatsApp sudah terdaftar. Silakan gunakan nomor lain.");
        }

        if (!conflicts.isEmpty()) {
            throw new ConflictException(String.join("; ", conflicts));
        }

        try {
            String captchaKey = captchaService.generateCaptchaKey();
            String captchaText = captchaService.generateCaptchaText(5);
            captchaService.saveCaptcha(captchaKey, captchaText);
            String base64Captcha = "data:image/png;base64," + captchaService.generateCaptchaImage(captchaText);

            String otp = otpService.generateOtp(request.getEmail(), formattedPhone);
//            emailService.sendOtpEmail(request.getEmail(), otp, request.getNama());
            smsService.sendOtpWhatsApp(formattedPhone, otp, request.getNama());

            AccountResponse.SendOtp response = new AccountResponse.SendOtp();
            response.setNama(request.getNama());
            response.setEmail(request.getEmail());
            response.setNomorTelepon(request.getNomor_telepon());
            response.setCaptcha(
                  new AccountResponse.SendOtp.CaptchaResponse(captchaKey, base64Captcha)
            );

            authAttempService.sendOtpSucceeded(request.getEmail());

            return response;
        } catch (Exception e) {
            authAttempService.sendOtpFailed(request.getEmail());
            throw new RuntimeException("Unexpected error occurred while registering account", e);
        }
    }

    @Override
    public AccountResponse.ResendCaptcha resendCaptcha() {
        String captchaKey = captchaService.generateCaptchaKey();
        String captchaText = captchaService.generateCaptchaText(5);

        captchaService.saveCaptcha(captchaKey, captchaText);
        String base64Captcha = "data:image/png;base64," + captchaService.generateCaptchaImage(captchaText);

        AccountResponse.ResendCaptcha response = new AccountResponse.ResendCaptcha();
        response.setCaptcha(
              new AccountResponse.ResendCaptcha.CaptchaResponse(captchaKey, base64Captcha)
        );

        return response;
    }

    @Override
    @Transactional
    public AccountResponse register(RegisterRequest request) {
        boolean validateCaptcha = captchaService.verifyCaptcha(request.getCaptcha_token(), request.getCaptcha_code());

        if (!validateCaptcha) {
            throw new BadRequestException("CAPTCHA yang Anda masukkan salah. Silakan coba lagi.");
        }

        boolean validOtpByEmail = otpService.validateOtp(request.getEmail(), request.getOtp_code());
        boolean validOtpByPhone = otpService.validateOtp(request.getNomor_telepon(), request.getOtp_code());

        if (!validOtpByEmail || !validOtpByPhone) {
            throw new BadRequestException("Kode OTP salah atau sudah kadaluarsa. Silakan coba lagi.");
        }

        try {
            Long generatedId = accountUtils.generateRandom6DigitId();

            Account account = new Account();
            account.setId(generatedId);
            account.setNama(request.getNama());
            account.setEmail(request.getEmail());
            account.setNik(encryptService.encrypt(request.getNik()));
            account.setPassword(passwordEncoder.encode(request.getPassword()));
            account.setNomorTelepon(request.getNomor_telepon());
            account.setTempatLahir(request.getTempat_lahir());
            account.setTanggalLahir(request.getTanggal_lahir());
            account.setJenisKelamin(request.getJenis_kelamin());
            account.setAlamat(request.getAlamat());
            account.setTipeAkun(request.getTipe_akun().name());
            account.setStatus(StatusAccountEnum.PENDING);
            account.setRole("USER");
            account.setIsAssigned(false);

            Account saved = accountRepository.save(account);

            AccountResponse response = new AccountResponse();
            response.setId(saved.getId());
            response.setNama(saved.getNama());
            response.setEmail(saved.getEmail());
            response.setNik(saved.getNik());
            response.setNomorTelepon(saved.getNomorTelepon());
            response.setTipeAkun(saved.getTipeAkun());
            response.setStatus(saved.getStatus().name());
            response.setRole(saved.getRole());

            otpService.deleteOtp(request.getEmail());
            otpService.deleteOtp(request.getNomor_telepon());

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
    @Transactional
    public AccountResponse.VerifyAccount verifyAccount(String authHeader, String nik, RegisterRequest.VerifyAccount request) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthenticationException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Map<String, Object> claims = jwtTokenProvider.parseToken(token);
        String role = String.valueOf(claims.get("role"));

        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ForbiddenException("Only ADMIN can verify accounts");
        }

        try {
            Account account = accountRepository.findByNik(nik)
                  .orElseThrow(() -> new ResourceNotFoundException("NIK tidak ditemukan: " + nik));

            StatusAccountEnum newStatus = (request != null && request.getStatus() != null)
                  ? StatusAccountEnum.valueOf(request.getStatus().trim().toUpperCase())
                  : StatusAccountEnum.PENDING;

            if (newStatus != StatusAccountEnum.VERIFIED && newStatus != StatusAccountEnum.REJECTED) {
                throw new BadRequestException("Invalid status. Allowed: APPROVED, REJECTED");
            }

            account.setStatus(newStatus);
            Account saved = accountRepository.save(account);

            return AccountResponse.VerifyAccount.builder()
                  .nama(saved.getNama())
                  .nik(saved.getNik())
                  .nomorTelepon(saved.getNomorTelepon())
                  .status(saved.getStatus().name())
                  .build();

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status. Allowed: APPROVED, REJECTED");
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify account: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        boolean validateCaptcha = captchaService.verifyCaptcha(request.getCaptcha_token(), request.getCaptcha_code());

        if (!validateCaptcha) {
            throw new ForbiddenException("CAPTCHA yang Anda masukkan salah. Silakan coba lagi.");
        }

        String nik = request.getNik();

        if (authAttempService.loginBlocked(nik)) {
            throw new RateLimitException("Terlalu banyak percobaan login. Silakan coba lagi dalam 1 menit");
        }

        try {
            Account account = accountRepository.findByNik(encryptService.encrypt(nik))
                  .orElseThrow(() -> {
                      authAttempService.loginFailed(nik);
                      return new ResourceNotFoundException("Akun Anda belum terdaftar. Silakan daftar terlebih dahulu.");
                  });

            if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
                authAttempService.loginFailed(nik);
                throw new ResourceNotFoundException("Password yang Anda masukkan salah. Silakan coba lagi.");
            }

            authAttempService.loginSucceeded(nik);

            String token = jwtTokenProvider.generateToken(
                  account.getId(),
                  account.getNama(),
                  account.getEmail(),
                  account.getNik(),
                  account.getNomorTelepon(),
                  account.getRole()
            );

            LoginResponse.Profile profile = new LoginResponse.Profile(
                  String.valueOf(account.getId()),
                  account.getNama(),
                  account.getEmail(),
                  account.getNik(),
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

    @Override
    public List<AccountResponse> listAccount(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthenticationException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Map<String, Object> claims = jwtTokenProvider.parseToken(token);
        String role = String.valueOf(claims.get("role"));

        if (!"ADMIN".equalsIgnoreCase(role) && !"KEPALA".equalsIgnoreCase(role)) {
            throw new ForbiddenException("You are not authorized to access this resource");
        }

        try {
            List<Account> accounts = accountRepository.findAllData();

            return accounts.stream()
                  .map(account -> AccountResponse.builder()
                        .id(account.getId())
                        .nama(account.getNama())
                        .nik(account.getNik())
                        .email(account.getEmail())
                        .nomorTelepon(account.getNomorTelepon())
                        .tipeAkun(account.getTipeAkun())
                        .status(account.getStatus() != null ? account.getStatus().name() : null)
                        .createdAt(account.getCreatedAt())
                        .updatedAt(account.getUpdatedAt())
                        .build())
                  .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to get all account: " + e.getMessage(), e);
        }
    }

    @Override
    public AccountResponse.Detail detailAccount(String authHeader, String nik) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthenticationException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Map<String, Object> claims = jwtTokenProvider.parseToken(token);
        String role = String.valueOf(claims.get("role"));

        if (!"ADMIN".equalsIgnoreCase(role) && !"KEPALA".equalsIgnoreCase(role)) {
            throw new ForbiddenException("You are not authorized to access this resource");
        }

        try {
            Account account = accountRepository.findByNik(nik)
                  .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + nik));

            return AccountResponse.Detail.builder()
                  .id(account.getId())
                  .nama(account.getNama())
                  .nik(account.getNik())
                  .email(account.getEmail())
                  .tempatLahir(account.getTempatLahir())
                  .tanggalLahir(account.getTanggalLahir())
                  .alamat(account.getAlamat())
                  .jenisKelamin(account.getJenisKelamin())
                  .nomorTelepon(account.getNomorTelepon())
                  .tipeAkun(account.getTipeAkun())
                  .status(account.getStatus() != null ? account.getStatus().name() : null)
                  .is_assigned(account.getIsAssigned())
                  .createdAt(account.getCreatedAt())
                  .updatedAt(account.getUpdatedAt())
                  .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get data account: " + e.getMessage());
        }
    }

    @Override
    public AccountResponse.Detail profile(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthenticationException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Map<String, Object> claims = jwtTokenProvider.parseToken(token);

        String nikFromToken = String.valueOf(claims.get("sub"));

        Account account = accountRepository.findByNik(nikFromToken)
              .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        return AccountResponse.Detail.builder()
              .id(account.getId())
              .nama(account.getNama())
              .nik(account.getNik())
              .email(account.getEmail())
              .nomorTelepon(account.getNomorTelepon())
              .tempatLahir(account.getTempatLahir())
              .tanggalLahir(account.getTanggalLahir())
              .jenisKelamin(account.getJenisKelamin())
              .alamat(account.getAlamat())
              .tipeAkun(account.getTipeAkun())
              .status(account.getStatus() != null ? account.getStatus().name() : null)
              .role(account.getRole())
              .is_assigned(account.getIsAssigned())
              .createdAt(account.getCreatedAt())
              .updatedAt(account.getUpdatedAt())
              .build();
    }

    @Override
    public String sendPasswordResetPassword(RegisterRequest.SendOtpForgotPassword dto) {
        boolean validateCaptcha = captchaService.verifyCaptcha(dto.getCaptcha_token(), dto.getCaptcha_code());

        if (!validateCaptcha) {
            throw new BadRequestException("CAPTCHA yang Anda masukkan salah. Silakan coba lagi.");
        }

        Account account = accountRepository.findByNik(encryptService.encrypt(dto.getNik()))
              .orElseThrow(() -> new ResourceNotFoundException("NIK tidak ditemukan: " + dto.getNik()));

        if (authAttempService.sendOtpBlocked(account.getNomorTelepon())) {
            throw new RateLimitException("Terlalu banyak percobaan. Silakan coba lagi dalam 1 menit");
        }

        String otp = otpService.generateOtp(account.getEmail(), account.getNomorTelepon());
        smsService.sendOtpWhatsApp(account.getNomorTelepon(), otp, account.getNama());

        authAttempService.sendOtpSucceeded(account.getNomorTelepon());

        return "Success sent OTP";
    }

    @Override
    @Transactional
    public String resetPassword(RegisterRequest.ResetPassword dto) {
        Account account = accountRepository.findByNik(encryptService.encrypt(dto.getNik()))
              .orElseThrow(() -> new ResourceNotFoundException("NIK tidak ditemukan: " + dto.getNik()));

        boolean validOtpByEmail = otpService.validateOtp(account.getEmail(), dto.getOtp_code());
        boolean validOtpByPhone = otpService.validateOtp(account.getNomorTelepon(), dto.getOtp_code());

        if (!validOtpByEmail || !validOtpByPhone) {
            throw new BadRequestException("Kode OTP salah atau sudah kadaluarsa. Silakan coba lagi.");
        }

        try {
            account.setPassword(passwordEncoder.encode(dto.getPassword()));

            accountRepository.save(account);

            return "Success reset password";
        } catch (Exception e) {
            throw new RuntimeException("Failed to change password: " + e.getMessage());
        }
    }
}