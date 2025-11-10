package com.facushop.service;

import com.facushop.config.jwt.JwtService;
import com.facushop.domain.Role;
import com.facushop.domain.User;
import com.facushop.dto.AuthResponse;
import com.facushop.dto.LoginRequest;
import com.facushop.dto.RegisterRequest;
import com.facushop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // El Bean de ApplicationConfig
    private final JwtService jwtService;           // Nuestro servicio de JWT
    private final AuthenticationManager authenticationManager; // El Bean de ApplicationConfig

    /**
     * Lógica para registrar un nuevo usuario.
     */
    public AuthResponse register(RegisterRequest request) {
        // 1. Hasheamos la contraseña
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // 2. Creamos el objeto User
        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(hashedPassword) // Guardamos la contraseña hasheada
                .role(Role.USER) // Asignamos rol USER por defecto
                .build();

        // 3. Guardamos al usuario en la BD
        userRepository.save(user);

        // 4. Creamos un token JWT para el nuevo usuario
        String jwtToken = jwtService.generateToken(user);

        // 5. Devolvemos el token
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }

    /**
     * Lógica para autenticar (loguear) un usuario.
     */
    public AuthResponse login(LoginRequest request) {
        // 1. El AuthenticationManager verifica si el email y pass son correctos.
        //    Si no lo son, lanzará una excepción (BadCredentialsException) automáticamente.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Si llegamos aquí, el usuario es válido. Lo buscamos en la BD.
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado post-autenticación"));

        // 3. Generamos y devolvemos el token.
        String jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }
}
