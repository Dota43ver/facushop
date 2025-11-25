package com.facushop.service;

import com.facushop.domain.*;
import com.facushop.dto.AddItemRequest;
import com.facushop.dto.response.OrderItemResponseDto;
import com.facushop.dto.response.OrderResponseDto;
import com.facushop.repository.OrderItemRepository;
import com.facushop.repository.OrderRepository;
import com.facushop.repository.ProductRepository;
import com.facushop.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    public OrderResponseDto getCart(UserDetails loggedInUser) {
        User user = findUser(loggedInUser);
        Order cart = findOrCreateCart(user);
        return mapToCartDto(cart);
    }

    /**
     * Añade un item al carrito.
     */
    @Transactional
    public OrderResponseDto addItemToCart(AddItemRequest request, UserDetails loggedInUser) {
        User user = findUser(loggedInUser);
        Order cart = findOrCreateCart(user);

        // 1. Buscar el producto
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (product.getStock() <= 0) {
            throw new RuntimeException("No hay stock para: " + product.getTitle());
        }

        // 2. Revisar si el producto ya está en el carrito
        Optional<OrderItem> existingItemOpt = orderItemRepository.findByOrderAndProduct(cart, product);

        if (existingItemOpt.isPresent()) {
            OrderItem item = existingItemOpt.get();
            int newQuantity = item.getQuantity() + request.getQuantity();

            // Revisa si la *nueva* cantidad supera el stock
            if (newQuantity > product.getStock()) {
                throw new RuntimeException("Stock insuficiente. Solo quedan " + product.getStock() + " unidades.");
            }
            item.setQuantity(newQuantity);
            orderItemRepository.save(item);
        } else {
            // Revisa si la cantidad *inicial* supera el stock
            if (request.getQuantity() > product.getStock()) {
                throw new RuntimeException("Stock insuficiente. Solo quedan " + product.getStock() + " unidades.");
            }

            OrderItem newItem = OrderItem.builder()
                    .order(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .priceAtPurchase(product.getPrice()) // Guarda el precio actual del producto
                    .build();
            cart.getItems().add(newItem);
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
    public OrderResponseDto removeItemFromCart(Long productId, UserDetails loggedInUser) {
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

    private OrderResponseDto mapToCartDto(Order cart) {
        List<OrderItemResponseDto> itemDtos = cart.getItems().stream()
                .map(item -> OrderItemResponseDto.builder()
                        .productId(item.getProduct().getId())
                        .productTitle(item.getProduct().getTitle())
                        .productImageUrl(item.getProduct().getImageUrl())
                        .quantity(item.getQuantity())
                        .priceAtPurchase(item.getPriceAtPurchase())
                        .subtotal(item.getPriceAtPurchase().multiply(new BigDecimal(item.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        return OrderResponseDto.builder()
                .orderId(cart.getId())
                .items(itemDtos)
                .totalAmount(cart.getTotalAmount())
                .build();
    }
}