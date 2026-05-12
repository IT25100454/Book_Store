package com.pageturner.service.impl;

import com.pageturner.model.Book;
import com.pageturner.model.Order;
import com.pageturner.model.OrderItem;
import com.pageturner.model.User;
import com.pageturner.repository.BookRepository;
import com.pageturner.repository.OrderRepository;
import com.pageturner.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {
    private static final List<String> ALLOWED_STATUSES =
            Arrays.asList("Pending", "Processing", "Shipped", "Delivered", "Cancelled");

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;

    public OrderServiceImpl(OrderRepository orderRepository, BookRepository bookRepository) {
        this.orderRepository = orderRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    @Transactional
    public Order createOrder(User user, Order orderDetails) {
        orderDetails.setUser(user);
        orderDetails.setOrderNumber(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        orderDetails.setStatus("Pending");
        
        // Deduct stock
        for (OrderItem item : orderDetails.getItems()) {
            Book book = item.getBook();
            if (book.getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException("Not enough stock for book: " + book.getTitle());
            }
            book.setStockQuantity(book.getStockQuantity() - item.getQuantity());
            bookRepository.save(book);
        }

        return orderRepository.save(orderDetails);
    }

    @Override
    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserWithItemsAndBooks(user);
    }
    
    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAllWithUserAndItems();
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found with ID: " + orderId));
        String normalized = normalizeStatus(status);
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new RuntimeException("Invalid order status: " + status);
        }
        order.setStatus(normalized);
        return orderRepository.save(order);
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            throw new RuntimeException("Invalid order status: null");
        }
        String trimmed = status.trim();
        if (trimmed.isEmpty()) {
            throw new RuntimeException("Invalid order status: " + status);
        }
        String lower = trimmed.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    @Override
    public long getTotalOrdersCount() {
        return orderRepository.count();
    }

    @Override
    public BigDecimal getTotalRevenue() {
        return orderRepository.findAll().stream()
                .filter(o -> !o.getStatus().equals("Cancelled"))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
