package com.facushop.service;

import com.facushop.domain.Order;
import com.facushop.domain.OrderStatus;
import com.facushop.domain.User;
import com.facushop.dto.response.OrderItemResponseDto;
import com.facushop.dto.response.OrderResponseDto;
import com.facushop.repository.OrderRepository;
import com.facushop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public List<OrderResponseDto> getOrderHistory(UserDetails loggedInUser) {
        User user = userRepository.findByEmail(loggedInUser.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Busca todos los pedidos que NO sean 'CART'
        List<Order> orders = orderRepository.findByUserAndStatusNot(user, OrderStatus.CART);

        // Mapea a DTOs
        return orders.stream()
                .map(this::mapToOrderDto)
                .collect(Collectors.toList());
    }

    // Método mapeador (lo movimos de CartService aquí)
    private OrderResponseDto mapToOrderDto(Order order) {
        List<OrderItemResponseDto> itemDtos = order.getItems().stream()
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
                .orderId(order.getId())
                .items(itemDtos)
                .totalAmount(order.getTotalAmount())
                // Podrías añadir más campos aquí, como estado y fecha
                // .status(order.getStatus().name())
                // .createdAt(order.getCreatedAt())
                .build();
    }
}