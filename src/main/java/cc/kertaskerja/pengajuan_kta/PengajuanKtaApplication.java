package cc.kertaskerja.pengajuan_kta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PengajuanKtaApplication {

	public static void main(String[] args) {
		SpringApplication.run(PengajuanKtaApplication.class, args);
	}

}
