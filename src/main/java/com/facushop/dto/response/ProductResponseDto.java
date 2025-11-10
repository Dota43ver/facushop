package com.facushop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private CategoryDto category;
    private SellerDto seller; // <-- ¡Usamos nuestro DTO, no la Entidad!
}
