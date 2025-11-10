package com.facushop.service;

import com.facushop.domain.*;
import com.facushop.dto.AddItemRequest;
import com.facushop.dto.response.CartItemDto;
import com.facushop.dto.response.CartResponseDto;
import com.facushop.repository.OrderItemRepository;
import com.facushop.repository.OrderRepository;
import com.facushop.repository.ProductRepository;
import com.facushop.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * Obtiene el carrito del usuario logueado o crea uno nuevo si no existe.
     */
    @Transactional
    public CartResponseDto getCart(UserDetails loggedInUser) {
        User user = findUser(loggedInUser);
        Order cart = findOrCreateCart(user);
        return mapToCartDto(cart);
    }

    /**
     * Añade un item al carrito.
     */
    @Transactional
    public CartResponseDto addItemToCart(AddItemRequest request, UserDetails loggedInUser) {
        User user = findUser(loggedInUser);
        Order cart = findOrCreateCart(user);

        // 1. Buscar el producto
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // 2. Revisar si el producto ya está en el carrito
        Optional<OrderItem> existingItemOpt = orderItemRepository.findByOrderAndProduct(cart, product);

        if (existingItemOpt.isPresent()) {
            // Si existe, actualiza la cantidad
            OrderItem item = existingItemOpt.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            orderItemRepository.save(item);
        } else {
            // Si no existe, crea un nuevo OrderItem
            OrderItem newItem = OrderItem.builder()
                    .order(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .priceAtPurchase(product.getPrice()) // Guarda el precio actual
                    .build();
            cart.getItems().add(newItem); // Añade el item a la lista del pedido
            orderItemRepository.save(newItem);
        }

        // 3. Recalcular el total y guardar
        recalculateCartTotal(cart);
        Order savedCart = orderRepository.save(cart);

        return mapToCartDto(savedCart);
    }

    /**
     * Quita un item del carrito por completo.
     */
    @Transactional
    public CartResponseDto removeItemFromCart(Long productId, UserDetails loggedInUser) {
        User user = findUser(loggedInUser);
        Order cart = findOrCreateCart(user);

        // 1. Buscar el producto
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // 2. Buscar el item en el carrito
        OrderItem itemToRemove = orderItemRepository.findByOrderAndProduct(cart, product)
                .orElseThrow(() -> new RuntimeException("Item no encontrado en el carrito"));

        // 3. Quitarlo (orphanRemoval=true en Order.java se encarga de borrarlo)
        cart.getItems().remove(itemToRemove);
        orderItemRepository.delete(itemToRemove);

        // 4. Recalcular total y guardar
        recalculateCartTotal(cart);
        Order savedCart = orderRepository.save(cart);

        return mapToCartDto(savedCart);
    }


    // --- MÉTODOS PRIVADOS DE AYUDA ---

    private User findUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    private Order findOrCreateCart(User user) {
        return orderRepository.findByUserAndStatus(user, OrderStatus.CART)
                .orElseGet(() -> createNewCart(user));
    }

    private Order createNewCart(User user) {
        Order newCart = Order.builder()
                .user(user)
                .status(OrderStatus.CART)
                .totalAmount(BigDecimal.ZERO)
                .build();
        return orderRepository.save(newCart);
    }

    private void recalculateCartTotal(Order cart) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : cart.getItems()) {
            BigDecimal subtotal = item.getPriceAtPurchase().multiply(new BigDecimal(item.getQuantity()));
            total = total.add(subtotal);
        }
        cart.setTotalAmount(total);
    }

    private CartResponseDto mapToCartDto(Order cart) {
        List<CartItemDto> itemDtos = cart.getItems().stream()
                .map(item -> CartItemDto.builder()
                        .productId(item.getProduct().getId())
                        .productTitle(item.getProduct().getTitle())
                        .productImageUrl(item.getProduct().getImageUrl())
                        .quantity(item.getQuantity())
                        .priceAtPurchase(item.getPriceAtPurchase())
                        .subtotal(item.getPriceAtPurchase().multiply(new BigDecimal(item.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        return CartResponseDto.builder()
                .orderId(cart.getId())
                .items(itemDtos)
                .totalAmount(cart.getTotalAmount())
                .build();
    }
}