package cc.kertaskerja.pengajuan_kta.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
              .info(new Info()
                    .title("Pengajuan KTA API Service")
                    .version("1.0.0")
                    .description("Pejabat Pengelola Informasi dan Dokumentasi Kabupaten Ngawi")
                    .contact(new Contact()
                          .name("API Support")
                          .email("ppidkabngawi@ngawikab.go.id")));
    }
}
