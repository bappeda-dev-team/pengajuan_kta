package cc.kertaskerja.pengajuan_kta.service.global.CloudStorage;

import cc.kertaskerja.pengajuan_kta.config.CloudFlareProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

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

        return props.getEndpoint() + "/" + props.getBucket() + "/" + key;
    }
}
