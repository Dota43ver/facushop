package com.facushop.domain;

public enum OrderStatus {
    CART,        // El pedido es un carrito activo
    PAID,        // El pedido ha sido pagado
    SHIPPED,     // El pedido ha sido enviado
    DELIVERED,   // El pedido ha sido entregado
    CANCELLED    // El pedido fue cancelado
}
