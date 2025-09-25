package cc.kertaskerja.pengajuan_kta.service.external;

import org.springframework.stereotype.Service;

@Service
public interface EncryptService {
    String encrypt(String plainText);
    String decrypt(String encryptedText);
}
