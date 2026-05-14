package com.pageturner.service.impl;

import com.pageturner.model.Feedback;
import com.pageturner.model.Feedback.FeedbackStatus;
import com.pageturner.model.Feedback.FeedbackType;
import com.pageturner.model.User;
import com.pageturner.repository.FeedbackRepository;
import com.pageturner.service.FeedbackService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;

    public FeedbackServiceImpl(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    @Override
    public Feedback submit(Feedback feedback, User user) {
        feedback.setUser(user);
        feedback.setStatus(FeedbackStatus.PENDING);
        return feedbackRepository.save(feedback);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Feedback> getByUser(User user) {
        return feedbackRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Feedback> getAll(Pageable pageable) {
        return feedbackRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Feedback> getFiltered(FeedbackStatus status, FeedbackType type, Pageable pageable) {
        return feedbackRepository.findFiltered(status, type, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Feedback> findById(Long id) {
        return feedbackRepository.findById(id);
    }

    @Override
    public Feedback reply(Long id, String adminReply) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found: " + id));
        feedback.setAdminReply(adminReply);
        feedback.setRepliedAt(LocalDateTime.now());
        feedback.setStatus(FeedbackStatus.REVIEWED);
        return feedbackRepository.save(feedback);
    }

    @Override
    public Feedback updateStatus(Long id, FeedbackStatus status) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found: " + id));
        feedback.setStatus(status);
        return feedbackRepository.save(feedback);
    }

    @Override
    public void delete(Long id) {
        feedbackRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPending() {
        return feedbackRepository.countByStatus(FeedbackStatus.PENDING);
    }
}
