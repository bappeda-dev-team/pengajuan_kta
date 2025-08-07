package cc.kertaskerja.pengajuan_kta.config;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
public class R2ClientConfig {

    @Bean
    public S3Client s3Client() {
        String accessKey = "28f20a0e4545504c62097e0b6610ff46";
        String secretKey = "458651d94bc92e54f6d2b62b5dd33b9b2c8f22b51f945ab3fa873fb1ce7518aa";

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .endpointOverride(URI.create("https://7aa9bbf17ef2077fc32fdadafbb1e76d.r2.cloudflarestorage.com"))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of("auto"))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }
}
