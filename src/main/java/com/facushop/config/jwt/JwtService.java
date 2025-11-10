package com.facushop.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.facushop.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${application.jwt.secret-key}")
    private String secretKey;

    @Value("${application.jwt.expiration}")
    private long jwtExpiration;

    // Método privado para obtener el algoritmo de firma
    // Usa la llave de application.properties
    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256(secretKey);
    }

    /**
     * Extrae el "username" (nuestro email) del token.
     */
    public String extractUsername(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        return decodedJWT.getSubject();
    }

    /**
     * Genera un token JWT para un usuario.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Genera un token con "claims" (datos extra) adicionales.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        Algorithm algorithm = getAlgorithm();

        // Añadimos info extra al token
        if (userDetails instanceof User user) {
            extraClaims.put("firstname", user.getFirstname());
            extraClaims.put("role", user.getRole().name());
        }

        return JWT.create()
                .withSubject(userDetails.getUsername()) // "subject" es el username (email)
                .withIssuedAt(new Date(System.currentTimeMillis()))
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpiration))
                // Añadimos los claims extra
                .withPayload(extraClaims)
                // Firmamos el token
                .sign(algorithm);
    }

    /**
     * Valida si un token es correcto (le pertenece al usuario y no ha expirado).
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // --- Métodos Privados de Ayuda ---

    private boolean isTokenExpired(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        return decodedJWT.getExpiresAt().before(new Date());
    }

    /**
     * Método centralizado para verificar y decodificar el token.
     */
    private DecodedJWT verifyToken(String token) {
        Algorithm algorithm = getAlgorithm();
        // Creamos el objeto verificador
        JWTVerifier verifier = JWT.require(algorithm).build();
        // Verificamos el token (esto lanza una excepción si es inválido)
        return verifier.verify(token);
    }
}