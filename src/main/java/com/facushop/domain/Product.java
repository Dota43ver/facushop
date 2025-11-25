package com.facushop.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.facushop.domain.User;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob // Indica que puede ser un texto largo
    private String description;

    @Column(nullable = false, precision = 10, scale = 2) // Ej. 12345678.90
    private BigDecimal price;

    @Column(name = "image_url") // Aquí guardamos la URL de la foto del bucket
    private String imageUrl;

    // Relacionamos el producto con su categoría
    @ManyToOne(fetch = FetchType.LAZY) // "Muchos" productos pueden tener "Una" categoría
    @JoinColumn(name = "category_id") // La llave foránea
    private Category category;

    // Opcional: Podríamos relacionarlo con el usuario que lo creó
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "user_id")
    // private User seller;

    @ManyToOne(fetch = FetchType.LAZY) // LAZY = No cargar el usuario a menos que se pida
    @JoinColumn(name = "seller_id", nullable = false) // No puede haber un producto sin vendedor
    private User seller;

    @Column(nullable = false)
    private Integer stock;
}
