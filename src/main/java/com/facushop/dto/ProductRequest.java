package com.facushop.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductRequest {
    private String title;
    private String description;
    private BigDecimal price;
    private Long categoryId;
    // El MultipartFile vendrá por separado
}
