package com.pageturner.service;

import com.pageturner.model.Feedback;
import com.pageturner.model.Feedback.FeedbackStatus;
import com.pageturner.model.Feedback.FeedbackType;
import com.pageturner.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface FeedbackService {

    Feedback submit(Feedback feedback, User user);

    List<Feedback> getByUser(User user);

    Page<Feedback> getAll(Pageable pageable);

    Page<Feedback> getFiltered(FeedbackStatus status, FeedbackType type, Pageable pageable);

    Optional<Feedback> findById(Long id);

    Feedback reply(Long id, String adminReply);

    Feedback updateStatus(Long id, FeedbackStatus status);

    void delete(Long id);

    long countPending();
}
