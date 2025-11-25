package com.facushop.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class OrderResponseDto {
    private Long orderId;
    private List<OrderItemResponseDto> items;
    private BigDecimal totalAmount;
}