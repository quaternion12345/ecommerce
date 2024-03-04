package com.example.orderservice.service;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.jpa.MySQLOrderEntity;

public interface OrderService {
    OrderDto createOrder(OrderDto orderDto);
    Iterable<OrderDto> getOrdersByUserId(String userId);

    void deleteOrdersByUserId(String userId);

    OrderDto getOrderByOrderId(String orderId);

    OrderDto updateOrderByOrderId(OrderDto orderDto);

    void deleteOrderByOrderId(String orderId);
}
