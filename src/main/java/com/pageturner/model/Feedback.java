package com.pageturner.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
public class Feedback extends BaseEntity {

    public enum FeedbackType {
        GENERAL, BOOK_REVIEW, COMPLAINT, SUGGESTION, COMPLIMENT
    }

    public enum FeedbackStatus {
        PENDING, REVIEWED, RESOLVED, DISMISSED
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedbackType type = FeedbackType.GENERAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedbackStatus status = FeedbackStatus.PENDING;

    @Column(nullable = false, length = 150)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String adminReply;

    @Column
    private LocalDateTime repliedAt;

    @Column(nullable = true)   // rating is optional
    private Integer rating;

    @Column(nullable = false)
    private Boolean anonymous = false;

    // Getters & Setters

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    public FeedbackType getType() { return type; }
    public void setType(FeedbackType type) { this.type = type; }

    public FeedbackStatus getStatus() { return status; }
    public void setStatus(FeedbackStatus status) { this.status = status; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getAdminReply() { return adminReply; }
    public void setAdminReply(String adminReply) { this.adminReply = adminReply; }

    public LocalDateTime getRepliedAt() { return repliedAt; }
    public void setRepliedAt(LocalDateTime repliedAt) { this.repliedAt = repliedAt; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public Boolean getAnonymous() { return anonymous; }
    public void setAnonymous(Boolean anonymous) { this.anonymous = anonymous; }
}
