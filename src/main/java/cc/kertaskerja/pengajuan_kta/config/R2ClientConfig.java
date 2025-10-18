package cc.kertaskerja.pengajuan_kta.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class R2ClientConfig {
    private final CloudFlareProperties props;

    public R2ClientConfig(CloudFlareProperties props) {
        this.props = props;
    }

    @Bean
    public S3Client r2Client() {
        return S3Client.builder()
              .credentialsProvider(
                    StaticCredentialsProvider.create(
                          AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey())
                    )
              )
              .endpointOverride(URI.create(props.getEndpoint()))
              .region(Region.of("auto"))
              .serviceConfiguration(S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build())
              .build();
    }
}
