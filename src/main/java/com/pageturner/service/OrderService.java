package com.pageturner.service;

import com.pageturner.model.Order;
import com.pageturner.model.User;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {
    Order createOrder(User user, Order orderDetails);
    List<Order> getUserOrders(User user);
    List<Order> getAllOrders();
    Order getOrderById(Long id);
    Order updateOrderStatus(Long orderId, String status);
    long getTotalOrdersCount();
    BigDecimal getTotalRevenue();
}
