package com.facushop.service;

import com.facushop.dto.response.OrderResponseDto;
import com.facushop.dto.response.CheckoutResponseDto;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.mercadopago.resources.preference.Preference;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MercadoPagoService {

    private final CartService cartService; // Usamos el servicio que ya tienes

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Value("${mercadopago.redirect.success-url}")
    private String successUrl;
    @Value("${mercadopago.redirect.failure-url}")
    private String failureUrl;
    @Value("${mercadopago.redirect.pending-url}")
    private String pendingUrl;

    @Value("${mercadopago.webhook-url}")
    private String webhookUrl;

    @PostConstruct
    public void init() {
        // Inicializa el SDK de MP con tu Access Token
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    public CheckoutResponseDto createCheckoutPreference(UserDetails loggedInUser) throws MPException, MPApiException {
        // 1. Obtener el carrito del usuario
        OrderResponseDto cart = cartService.getCart(loggedInUser);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            // Lanza una excepción clara en lugar de llamar a MP
            throw new IllegalStateException("No se puede crear un checkout con un carrito vacío.");
        }

        // 2. Crear la lista de items para Mercado Pago
        List<PreferenceItemRequest> items = new ArrayList<>();
        cart.getItems().forEach(itemDto -> {
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .id(itemDto.getProductId().toString())
                    .title(itemDto.getProductTitle())
                    .quantity(itemDto.getQuantity())
                    .unitPrice(itemDto.getPriceAtPurchase())
                    .currencyId("ARS") // Moneda (Ajusta a tu país)
                    .build();
            items.add(itemRequest);
        });

        // 3. Crear las URLs de redirección
        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success("http://localhost:4200/pago-exitoso")
                .failure("http://localhost:4200/pago-fallido")
                .pending("http://localhost:4200/pago-pendiente")
                .build();

        // 4. Crear la "Preferencia de Pago"
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .backUrls(backUrls)
                //.autoReturn("approved") // Redirige automáticamente solo si el pago es aprobado
                .externalReference(cart.getOrderId().toString()) // ¡Guarda el ID de tu pedido!
                .notificationUrl(webhookUrl)
                .build();

        // 5. Crear el cliente y generar la preferencia
        PreferenceClient client = new PreferenceClient();
        Preference preference = client.create(preferenceRequest);

        // 6. Devolver la URL de pago
        return CheckoutResponseDto.builder()
                .checkoutUrl(preference.getInitPoint())
                .build();
    }
}
