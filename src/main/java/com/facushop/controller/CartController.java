package com.facushop.controller;

import com.facushop.dto.AddItemRequest;
import com.facushop.dto.response.OrderResponseDto;
import com.facushop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * Obtiene el carrito del usuario actual.
     */
    @GetMapping
    public ResponseEntity<OrderResponseDto> getMyCart(@AuthenticationPrincipal UserDetails loggedInUser) {
        return ResponseEntity.ok(cartService.getCart(loggedInUser));
    }

    /**
     * Añade un item al carrito.
     */
    @PostMapping("/items")
    public ResponseEntity<OrderResponseDto> addItemToMyCart(
            @RequestBody AddItemRequest request,
            @AuthenticationPrincipal UserDetails loggedInUser
    ) {
        return ResponseEntity.ok(cartService.addItemToCart(request, loggedInUser));
    }

    /**
     * Elimina un item del carrito.
     */
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<OrderResponseDto> removeItemFromMyCart(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails loggedInUser
    ) {
        return ResponseEntity.ok(cartService.removeItemFromCart(productId, loggedInUser));
    }
}