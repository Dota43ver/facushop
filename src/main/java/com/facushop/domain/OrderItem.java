package com.facushop.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // El pedido al que pertenece este item
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // El producto que se está comprando
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Cantidad (ej. 2 unidades)
    @Column(nullable = false)
    private Integer quantity;

    // El precio del producto *en el momento de la compra*
    // Es importante guardarlo aquí por si el precio del producto cambia en el futuro
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtPurchase;
}
