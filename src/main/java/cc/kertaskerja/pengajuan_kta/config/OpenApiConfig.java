package cc.kertaskerja.pengajuan_kta.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
              .info(new Info()
                    .title("KTA API Documentation")
                    .version("1.0.0")
                    .description("API documentation for Kertaskerja Pengajuan KTA"))
              .servers(List.of(
                    new Server()
                          .url("http://localhost:8080/kta/api")
                          .description("Development server")
              ))
              .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
              .components(new Components()
                    .addSecuritySchemes("basicAuth",
                          new SecurityScheme()
                                .type(Type.HTTP)
                                .scheme("basic")
                                .in(In.HEADER)
                                .name("Authorization")
                                .description("Enter username and password for Swagger UI access")
                    )
              );
    }
}
