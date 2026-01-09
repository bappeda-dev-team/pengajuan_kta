package cc.kertaskerja.pengajuan_kta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PengajuanKtaApplication {

	public static void main(String[] args) {
		// Disable spring-dotenv to avoid startup crash when dotenv-java is not on the classpath.
		// Prefer standard Spring Boot config (application.properties/yml) or OS environment variables.
		System.setProperty("spring.dotenv.enabled", "false");

		SpringApplication.run(PengajuanKtaApplication.class, args);
	}

}
