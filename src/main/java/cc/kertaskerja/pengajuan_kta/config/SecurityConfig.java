package cc.kertaskerja.pengajuan_kta.config;

import cc.kertaskerja.pengajuan_kta.security.CustomBasicAuthenticationEntryPoint;
import cc.kertaskerja.pengajuan_kta.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomBasicAuthenticationEntryPoint customBasicAuthEntryPoint;

    @Value("${SWAGGER_USERNAME}")
    private String swaggerUsername;

    @Value("${SWAGGER_PASSWORD}")
    private String swaggerPassword;

    /**
     * 🔒 Single Security Chain - Simpler approach
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
              .csrf(csrf -> csrf.disable())
              .cors(cors -> cors.configurationSource(corsConfigurationSource()))
              .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/actuator/health", "/public/**").permitAll()
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").authenticated()
                    .anyRequest().authenticated()
              )
              .httpBasic(httpBasic -> httpBasic
                    .realmName("Swagger UI Access")
                    .authenticationEntryPoint(customBasicAuthEntryPoint)
              )
              .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
              .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
              .build();
    }

    /**
     * 🧩 CORS Configuration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
              "http://localhost:3008",
              "http://192.168.1.38:3000",
              "https://ktangawikabfe.zeabur.app"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 🧑 Basic Auth User untuk Swagger
     */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
              .username("webprogregktangawikab")
              .password(passwordEncoder().encode("xLI061@4f0#"))
              .roles("ADMIN")
              .build();

        return new InMemoryUserDetailsManager(user);
    }

    /**
     * 🔐 Password Encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}