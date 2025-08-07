package cc.kertaskerja.pengajuan_kta.service.CloudStorage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class R2StorageService {

    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket:}")
    private String bucketName;

    @Value("${cloudflare.r2.base-url:}")
    private String baseUrl;

    public R2StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void upload(String key, String content) {
        validateConfiguration();
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.putObject(putRequest, RequestBody.fromString(content));
    }

    public String uploadFile(MultipartFile file) throws IOException {
        validateConfiguration();
        
        String key = generateUniqueKey(file.getOriginalFilename());
        
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        
        return baseUrl + "/" + key;
    }

    private void validateConfiguration() {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new RuntimeException("R2 bucket name is not configured. Please set cloudflare.r2.bucket property.");
        }
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new RuntimeException("R2 base URL is not configured. Please set cloudflare.r2.base-url property.");
        }
    }

    private String generateUniqueKey(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return "dokumen-pendukung/" + UUID.randomUUID().toString() + extension;
    }

    public String download(String key) {
        validateConfiguration();
        
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try (var response = s3Client.getObject(getRequest)) {
            return new String(response.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}