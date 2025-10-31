package cc.kertaskerja.pengajuan_kta.util;

import cc.kertaskerja.pengajuan_kta.repository.AccountRepository;
import org.springframework.stereotype.Component;

@Component
public class AccountUtils {

    private final AccountRepository accountRepository;

    public AccountUtils(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Long generateRandom6DigitId() {
        long min = 100000L;
        long max = 999999L;
        long randomId;

        do {
            randomId = (long) (Math.random() * (max - min + 1)) + min;
        } while (accountRepository.existsById(randomId));

        return randomId;
    }

    public String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return phoneNumber;
        }

        // Hapus semua karakter non-digit dan tanda +
        String cleaned = phoneNumber.replaceAll("[^\\d+]", "");

        if (cleaned.startsWith("0")) {
            cleaned = "62" + cleaned.substring(1);
        } else if (cleaned.startsWith("+62")) {
            cleaned = cleaned.substring(1);
        } else if (!cleaned.startsWith("62")) {
            cleaned = "62" + cleaned;
        }

        return cleaned;
    }
}
