package cc.kertaskerja.pengajuan_kta.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // Remove the webMultipartResolver bean - it's conflicting with the other one
}