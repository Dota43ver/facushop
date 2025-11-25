package com.facushop.repository;

import com.facushop.domain.Order;
import com.facushop.domain.OrderStatus;
import com.facushop.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Método clave para buscar el "carrito" activo de un usuario
    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<Order> findByUserAndStatus(User user, OrderStatus status);

    // Método para buscar el historial de pedidos (todo menos el carrito)
    @EntityGraph(attributePaths = {"items", "items.product"})
    List<Order> findByUserAndStatusNot(User user, OrderStatus status);
}
