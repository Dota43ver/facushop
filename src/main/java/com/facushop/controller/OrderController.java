package com.facushop.controller;

import com.facushop.dto.response.OrderResponseDto;
import com.facushop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/history")
    public ResponseEntity<List<OrderResponseDto>> getMyOrderHistory(
            @AuthenticationPrincipal UserDetails loggedInUser
    ) {
        return ResponseEntity.ok(orderService.getOrderHistory(loggedInUser));
    }
}