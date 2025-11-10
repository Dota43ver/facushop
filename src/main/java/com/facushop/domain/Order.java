package com.facushop.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders") // "order" es una palabra reservada de SQL, "orders" es más seguro
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // El usuario que realiza el pedido
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Los productos dentro del pedido
    // CascadeType.ALL: Si borro un pedido, se borran sus items.
    // mappedBy: Le dice a JPA que la entidad "hija" (OrderItem) gestiona la relación.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default // Asegura que la lista se inicialice
    private List<OrderItem> items = new ArrayList<>();

    // El estado actual del pedido (CART, PAID, etc.)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    // El costo total del pedido
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // Fecha de creación o actualización
    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // --- Métodos de Ayuda (Buena práctica) ---

    // Sincroniza las fechas antes de guardar/actualizar
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
