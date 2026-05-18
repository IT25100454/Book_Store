package com.pageturner.controller;

import com.pageturner.model.Feedback;
import com.pageturner.model.Feedback.FeedbackStatus;
import com.pageturner.model.Feedback.FeedbackType;
import com.pageturner.service.FeedbackService;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/feedback")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFeedbackController {

    private final FeedbackService feedbackService;

    public AdminFeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    // =========================
    // FEEDBACK LIST
    // =========================

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(required = false) FeedbackStatus status,
                       @RequestParam(required = false) FeedbackType type,
                       Model model) {

        Pageable pageable = PageRequest.of(page, size);

        model.addAttribute(
                "feedbackPage",
                feedbackService.getFiltered(status, type, pageable)
        );

        model.addAttribute("statuses", FeedbackStatus.values());

        model.addAttribute("types", FeedbackType.values());

        model.addAttribute("selectedStatus", status);

        model.addAttribute("selectedType", type);

        model.addAttribute(
                "pendingCount",
                feedbackService.countPending()
        );

        return "admin/feedback-list";
    }


    // =========================
    // FEEDBACK DETAIL
    // =========================

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         Model model) {

        Feedback feedback = feedbackService.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Feedback not found: " + id
                        )
                );

        model.addAttribute("feedback", feedback);

        model.addAttribute(
                "statuses",
                FeedbackStatus.values()
        );

        return "admin/feedback-detail";
    }


    // =========================
    // ADMIN REPLY
    // =========================

    @PostMapping("/{id}/reply")
    public String reply(@PathVariable Long id,
                        @RequestParam String adminReply,
                        RedirectAttributes redirectAttributes) {

        feedbackService.reply(id, adminReply);

        redirectAttributes.addFlashAttribute(
                "success",
                "Reply sent successfully."
        );

        return "redirect:/admin/feedback/" + id;
    }


    // =========================
    // UPDATE STATUS
    // =========================

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam FeedbackStatus status,
                               RedirectAttributes redirectAttributes) {

        feedbackService.updateStatus(id, status);

        redirectAttributes.addFlashAttribute(
                "success",
                "Status updated successfully."
        );

        return "redirect:/admin/feedback/" + id;
    }


    // =========================
    // PUBLISH / UNPUBLISH
    // =========================

    @PostMapping("/{id}/publish")
    public String togglePublish(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {

        Feedback feedback = feedbackService.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Feedback not found: " + id
                        )
                );

        // Toggle value
        feedback.setPublished(!feedback.getPublished());

        // Save
        feedbackService.save(feedback);

        redirectAttributes.addFlashAttribute(
                "success",
                feedback.getPublished()
                        ? "Feedback published to users."
                        : "Feedback hidden from users."
        );

        return "redirect:/admin/feedback/" + id;
    }


    // =========================
    // DELETE FEEDBACK
    // =========================

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         RedirectAttributes redirectAttributes) {

        feedbackService.delete(id);

        redirectAttributes.addFlashAttribute(
                "success",
                "Feedback deleted successfully."
        );

        return "redirect:/admin/feedback";
    }

}
