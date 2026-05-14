package com.pageturner.controller;

import com.pageturner.model.CartItem;
import com.pageturner.model.Order;
import com.pageturner.model.OrderItem;
import com.pageturner.model.User;
import com.pageturner.service.OrderService;
import com.pageturner.service.UserService;
import jakarta.servlet.http.HttpSession;
import com.pageturner.service.EmailNotificationService;
import com.pageturner.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final EmailNotificationService emailNotificationService;

    public OrderController(OrderService orderService, UserService userService, NotificationService notificationService, EmailNotificationService emailNotificationService) {
        this.orderService = orderService;
        this.userService = userService;
        this.notificationService = notificationService;
        this.emailNotificationService = emailNotificationService;
    }

    @SuppressWarnings("unchecked")
    private List<CartItem> getCart(HttpSession session) {
        return (List<CartItem>) session.getAttribute("cart");
    }

    @GetMapping("/orders")
    public String viewOrders() {
        return "redirect:/profile";
    }

    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session, Model model, Authentication authentication) {
        List<CartItem> cart = getCart(session);
        if (cart == null || cart.isEmpty()) {
            return "redirect:/cart";
        }
        
        User user = userService.findByUsername(authentication.getName());
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        
        BigDecimal total = cart.stream()
                .map(item -> item.getBook().getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("total", total);
        
        return "orders/checkout";
    }

    @PostMapping("/checkout/process")
    public String processCheckout(@RequestParam("shippingAddress") String shippingAddress, HttpSession session, Authentication authentication, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        List<CartItem> cart = getCart(session);
        if (cart == null || cart.isEmpty()) {
            return "redirect:/cart";
        }
        
        User user = userService.findByUsername(authentication.getName());
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User account not found.");
            return "redirect:/login";
        }
        
        Order order = new Order();
        order.setShippingAddress(shippingAddress);
        
        BigDecimal total = BigDecimal.ZERO;
        
        for (CartItem item : cart) {
            OrderItem orderItem = new OrderItem();
            orderItem.setBook(item.getBook());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(item.getBook().getPrice());
            order.addItem(orderItem);
            
            total = total.add(item.getBook().getPrice().multiply(new BigDecimal(item.getQuantity())));
        }
        order.setTotalAmount(total);
        
        try {
            Order savedOrder = orderService.createOrder(user, order);
            
            // Notifications
            notificationService.notifyOrderPlaced(savedOrder);
            emailNotificationService.sendOrderConfirmationEmail(savedOrder);
            
            // Notify all admins
            userService.getAllUsers().stream()
                .filter(u -> "ROLE_ADMIN".equals(u.getRole()))
                .forEach(admin -> notificationService.notifyAdminNewOrder(savedOrder, admin));

            // Clear cart
            session.setAttribute("cart", null);
            
            return "redirect:/checkout/confirmation/" + savedOrder.getOrderNumber();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/checkout/confirmation/{orderNumber}")
    public String orderConfirmation(@PathVariable("orderNumber") String orderNumber, Model model) {
        model.addAttribute("orderNumber", orderNumber);
        return "orders/confirmation";
    }

}
