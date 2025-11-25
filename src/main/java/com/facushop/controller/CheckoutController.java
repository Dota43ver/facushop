package com.facushop.controller;

import com.facushop.dto.response.CheckoutResponseDto;
import com.facushop.service.MercadoPagoService;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final MercadoPagoService mercadoPagoService;

    @PostMapping
    public ResponseEntity<?> createCheckout( // <-- Cambiado a ResponseEntity<?>
                                             @AuthenticationPrincipal UserDetails loggedInUser
    ) {
        try {
            CheckoutResponseDto response = mercadoPagoService.createCheckoutPreference(loggedInUser);
            return ResponseEntity.ok(response);

            // --- BLOQUE DE ERROR MEJORADO ---
        } catch (MPApiException e) {
            // Captura el error específico de la API
            System.err.println("Error de API de Mercado Pago: " + e.getApiResponse().getContent());
            e.printStackTrace();
            // Devuelve el error real de MP a Postman
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getApiResponse().getContent());

        } catch (MPException | IllegalStateException e) {
            // Captura otros errores (configuración, carrito vacío, etc.)
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
