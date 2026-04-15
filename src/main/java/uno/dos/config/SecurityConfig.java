package uno.dos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            
        
        // Desactivar CSRF para formularios simples
            .csrf(csrf -> csrf.disable())

            // Permitir acceso a todas las rutas
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/",
                            "/css/**",
                            "/js/**",
                            "/images/**"
                    ).permitAll()

                    .anyRequest().permitAll()
            )

            // Login por formulario
            .formLogin(login -> login
                    .loginPage("/login")
                    .permitAll()
            )

            // Logout
            .logout(logout -> logout
                    .logoutSuccessUrl("/login?logout")
                    .permitAll()
            );

        return http.build();
    }
}