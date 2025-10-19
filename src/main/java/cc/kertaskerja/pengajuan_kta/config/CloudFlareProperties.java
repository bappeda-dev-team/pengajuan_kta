package cc.kertaskerja.pengajuan_kta.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cloudflare.r2")
public class CloudFlareProperties {
    private String endpoint;
    private String bucket;
    private String accessKey;
    private String secretKey;
    private String baseUrl;
}
