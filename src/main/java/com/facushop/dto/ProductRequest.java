package com.facushop.dto;

import java.math.BigDecimal;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class ProductRequest {
    @NotBlank(message = "El título no puede estar vacío")
    private String title;
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser un número positivo")
    private BigDecimal price;
    @NotNull(message = "La categoría es obligatoria")
    private Long categoryId;

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Integer stock;
}
