package com.facushop.config.jwt;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService; // El @Bean que creamos en ApplicationConfig

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 1. Si no hay cabecera o no empieza con "Bearer ", pasamos al siguiente filtro.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2. Extraemos el token (quitando "Bearer ")
            final String jwt = authHeader.substring(7);
            final String userEmail = jwtService.extractUsername(jwt);

            // 3. Si hay email y el usuario no está "autenticado" en esta petición...
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 4. Cargamos el usuario desde la BD
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // 5. Validamos el token contra el usuario
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // 6. Si es válido, creamos una autenticación y la guardamos
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // No usamos credenciales (contraseña) aquí
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    // 7. Guardamos la autenticación en el Contexto de Seguridad
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            // 8. Pasamos al siguiente filtro de la cadena
            filterChain.doFilter(request, response);
        } catch (JWTVerificationException e) {
            // Si el token es inválido (firma, expiración), devolvemos un error 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token JWT inválido o expirado");
        }
    }
}
