package cc.kertaskerja.pengajuan_kta.service.global;

import cc.kertaskerja.pengajuan_kta.config.CloudFlareProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;

@Service
public class R2StorageService {

    private final S3Client r2Client;
    private final CloudFlareProperties props;

    public R2StorageService(S3Client r2Client, CloudFlareProperties props) {
        this.r2Client = r2Client;
        this.props = props;
    }

    public String upload(MultipartFile file) throws IOException {
        String key = file.getOriginalFilename();

        r2Client.putObject(
              PutObjectRequest.builder()
                    .bucket(props.getBucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .build(),
              software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes())
        );

        return String.format("%s/%s", props.getBaseUrl(), key);
    }

    public byte[] getObject(String key) {
        return r2Client.getObject(
              GetObjectRequest.builder()
                    .bucket(props.getBucket())
                    .key(key)
                    .build(),
              ResponseTransformer.toBytes()
        ).asByteArray();
    }


    // ======================
    // DELETE FILE
    // ======================
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        String key = extractKeyFromUrl(fileUrl);

        r2Client.deleteObject(
              DeleteObjectRequest.builder()
                    .bucket(props.getBucket())
                    .key(key)
                    .build()
        );
    }

    // ======================
    // HELPER
    // ======================
    private String extractKeyFromUrl(String fileUrl) {
        try {
            URI uri = URI.create(fileUrl);
            String path = uri.getPath(); // /filename.jpg OR /bucket/filename.jpg

            // jika baseUrl = https://xxx.r2.dev
            return path.startsWith("/")
                  ? path.substring(1)
                  : path;

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid R2 file URL: " + fileUrl, e);
        }
    }

}