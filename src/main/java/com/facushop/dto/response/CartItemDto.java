package com.facushop.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class CartItemDto {
    private Long productId;
    private String productTitle;
    private String productImageUrl;
    private Integer quantity;
    private BigDecimal priceAtPurchase;
    private BigDecimal subtotal; // (quantity * price)
}
