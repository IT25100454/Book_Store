package com.pageturner.repository;

import com.pageturner.model.Order;
import com.pageturner.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.book WHERE o.user = :user ORDER BY o.createdAt DESC")
    List<Order> findByUserWithItemsAndBooks(@Param("user") User user);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.user LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.book ORDER BY o.createdAt DESC")
    List<Order> findAllWithUserAndItems();
}
