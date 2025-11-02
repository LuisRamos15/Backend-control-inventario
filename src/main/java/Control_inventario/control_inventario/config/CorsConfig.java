package Control_inventario.control_inventario.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();

        // IMPORTANTES: credenciales y orígenes explícitos (no "*")
        c.setAllowCredentials(true);
        c.setAllowedOrigins(List.of(
                "http://localhost:4200"
                // agrega aquí otros orígenes válidos (tu dominio en producción)
        ));

        c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        c.setExposedHeaders(List.of("Authorization", "Content-Type"));
        c.setMaxAge(3600L); // cache del preflight

        UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/**", c);
        return s;
    }
}