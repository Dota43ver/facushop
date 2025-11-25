package com.facushop.config;

import com.facushop.config.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity // Opcional: para usar @PreAuthorize en el futuro
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter; // Nuestro filtro
    private final AuthenticationProvider authenticationProvider; // El @Bean de ApplicationConfig

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desactivamos CSRF (común en APIs stateless)
                .csrf(csrf -> csrf.disable())

                // Definimos las reglas de autorización
                .authorizeHttpRequests(auth -> auth
                        // Permisos públicos:
                        .requestMatchers("/api/auth/**") // Todo lo que sea /api/auth/...
                        .permitAll()                     // (login, registro) es público.
                        .requestMatchers(HttpMethod.GET, "/api/products/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**")
                        .permitAll()
                        .requestMatchers("/api/webhooks/**") // Permite el Webhook de MP
                        .permitAll()

                        // Opcional: Si quieres que CUALQUIERA vea las categorías
                        // .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()

                        // Todo lo demás...
                        .anyRequest()
                        .authenticated() // ...requiere autenticación.
                )

                // Le decimos a Spring que no cree sesiones (somos stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Usamos el proveedor de autenticación que definimos
                .authenticationProvider(authenticationProvider)

                // ¡IMPORTANTE! Añadimos nuestro filtro JWT ANTES del filtro estándar
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
