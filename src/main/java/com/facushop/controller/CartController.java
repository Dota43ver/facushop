package com.facushop.controller;

import com.facushop.dto.AddItemRequest;
import com.facushop.dto.response.CartResponseDto;
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
    public ResponseEntity<CartResponseDto> getMyCart(@AuthenticationPrincipal UserDetails loggedInUser) {
        return ResponseEntity.ok(cartService.getCart(loggedInUser));
    }

    /**
     * Añade un item al carrito.
     */
    @PostMapping("/items")
    public ResponseEntity<CartResponseDto> addItemToMyCart(
            @RequestBody AddItemRequest request,
            @AuthenticationPrincipal UserDetails loggedInUser
    ) {
        return ResponseEntity.ok(cartService.addItemToCart(request, loggedInUser));
    }

    /**
     * Elimina un item del carrito.
     */
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponseDto> removeItemFromMyCart(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails loggedInUser
    ) {
        return ResponseEntity.ok(cartService.removeItemFromCart(productId, loggedInUser));
    }
}