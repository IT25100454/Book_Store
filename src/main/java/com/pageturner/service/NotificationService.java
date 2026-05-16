package com.pageturner.service;

import com.pageturner.model.Book;
import com.pageturner.model.Notification;
import com.pageturner.model.Order;
import com.pageturner.model.User;

import java.util.List;

public interface NotificationService {
    Notification createNotification(User user, String title, String message, String type, String link);
    void notifyOrderStatusChange(Order order);
    void notifyWelcome(User user);
    void notifyOrderPlaced(Order order);
    void notifyAdminNewOrder(Order order, User adminUser);
    void notifyAdminLowStock(Book book, User adminUser);
    void markAsRead(Long notificationId);
    void markAllAsRead(User user);
    long getUnreadCount(User user);
    List<Notification> getUserNotifications(User user);
}
