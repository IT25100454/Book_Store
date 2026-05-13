package com.pageturner.service.impl;

import com.pageturner.model.Book;
import com.pageturner.model.Notification;
import com.pageturner.model.Order;
import com.pageturner.model.User;
import com.pageturner.repository.NotificationRepository;
import com.pageturner.service.EmailNotificationService;
import com.pageturner.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailNotificationService emailNotificationService;

    public NotificationServiceImpl(NotificationRepository notificationRepository, EmailNotificationService emailNotificationService) {
        this.notificationRepository = notificationRepository;
        this.emailNotificationService = emailNotificationService;
    }

    @Override
    public Notification createNotification(User user, String title, String message, String type, String link) {
        Notification n = new Notification();
        n.setUser(user);
        n.setTitle(title);
        n.setMessage(message);
        n.setType(type);
        n.setLink(link);
        return notificationRepository.save(n);
    }

    @Override
    public void notifyOrderStatusChange(Order order) {
        String title = "";
        String message = "";
        switch (order.getStatus()) {
            case "Processing" -> {
                title = "Order Being Processed 📦";
                message = "Your order #" + order.getOrderNumber() + " is now being processed. We'll ship it soon!";
            }
            case "Shipped" -> {
                title = "Order Shipped 🚚";
                message = "Great news! Your order #" + order.getOrderNumber() + " has been shipped and is on its way to you.";
            }
            case "Delivered" -> {
                title = "Order Delivered ✅";
                message = "Your order #" + order.getOrderNumber() + " has been delivered. Enjoy your books!";
            }
            case "Cancelled" -> {
                title = "Order Cancelled ❌";
                message = "Your order #" + order.getOrderNumber() + " has been cancelled. Contact us if you have questions.";
            }
        }
        if (!title.isEmpty()) {
            createNotification(order.getUser(), title, message, "ORDER_STATUS", "/orders/" + order.getId());
            emailNotificationService.sendOrderStatusEmail(order, title, message);
        }
    }

    @Override
    public void notifyWelcome(User user) {
        createNotification(user,
            "Welcome to PageTurner! 📚",
            "Hi " + user.getName() + "! Your account is ready. Start exploring our collection!",
            "WELCOME", "/books");
    }

    @Override
    public void notifyOrderPlaced(Order order) {
        createNotification(order.getUser(),
            "Order Placed Successfully! 🎉",
            "Your order #" + order.getOrderNumber() + " for Rs. " + order.getTotalAmount() + " has been placed. We'll process it shortly!",
            "NEW_ORDER", "/orders/" + order.getId());
    }

    @Override
    public void notifyAdminNewOrder(Order order, User adminUser) {
        createNotification(adminUser,
            "New Order Received 🛒",
            "Order #" + order.getOrderNumber() + " placed by " + order.getUser().getName() + " for Rs. " + order.getTotalAmount(),
            "NEW_ORDER", "/admin/orders");
    }

    @Override
    public void notifyAdminLowStock(Book book, User adminUser) {
        createNotification(adminUser,
            "Low Stock Alert ⚠️",
            "'" + book.getTitle() + "' only has " + book.getStockQuantity() + " copies left. Consider restocking soon.",
            "LOW_STOCK", "/admin/books");
    }

    @Override
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Override
    public void markAllAsRead(User user) {
        List<Notification> unread = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Override
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Override
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }
}
