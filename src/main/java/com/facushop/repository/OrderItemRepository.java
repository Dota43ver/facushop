package com.facushop.repository;

import com.facushop.domain.Order;
import com.facushop.domain.OrderItem;
import com.facushop.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Método para buscar si un producto ya existe en un pedido
    Optional<OrderItem> findByOrderAndProduct(Order order, Product product);
}
