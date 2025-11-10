package com.facushop.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartResponseDto {
    private Long orderId;
    private List<CartItemDto> items;
    private BigDecimal totalAmount;
}