package com.facushop.controller;

import com.facushop.dto.AuthResponse;
import com.facushop.dto.LoginRequest;
import com.facushop.dto.RegisterRequest;
import com.facushop.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth") // Ruta base (pública, según SecurityConfiguration)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody RegisterRequest request
    ) {
        // El servicio se encarga de toda la lógica
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request
    ) {
        // El servicio se encarga de toda la lógica
        return ResponseEntity.ok(authService.login(request));
    }
}
