package com.pageturner.repository;

import com.pageturner.model.Feedback;
import com.pageturner.model.Feedback.FeedbackStatus;
import com.pageturner.model.Feedback.FeedbackType;
import com.pageturner.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByUserOrderByCreatedAtDesc(User user);

    Page<Feedback> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Feedback> findByStatusOrderByCreatedAtDesc(FeedbackStatus status, Pageable pageable);

    Page<Feedback> findByTypeOrderByCreatedAtDesc(FeedbackType type, Pageable pageable);

    long countByStatus(FeedbackStatus status);

    @Query("SELECT f FROM Feedback f WHERE f.book.id = :bookId ORDER BY f.createdAt DESC")
    List<Feedback> findByBookId(@Param("bookId") Long bookId);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.book.id = :bookId AND f.rating IS NOT NULL")
    Double avgRatingByBookId(@Param("bookId") Long bookId);

    @Query("SELECT f FROM Feedback f WHERE " +
           "(:status IS NULL OR f.status = :status) AND " +
           "(:type IS NULL OR f.type = :type) " +
           "ORDER BY f.createdAt DESC")
    Page<Feedback> findFiltered(
            @Param("status") FeedbackStatus status,
            @Param("type") FeedbackType type,
            Pageable pageable);
}
