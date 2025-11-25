package com.facushop.controller;

import com.facushop.service.WebhookService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;

    @PostMapping("/mercadopago")
    public ResponseEntity<Void> handleMercadoPagoWebhook(
            @RequestBody JsonNode payload,
            @RequestParam("data.id") String paymentId
    ) {
        // Solo nos interesa el "paymentId" (data.id) por ahora
        System.out.println(">>> WEBHOOK RECIBIDO DE MP - Payment ID: " + paymentId);

        try {
            webhookService.processPaymentNotification(paymentId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error procesando webhook: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}