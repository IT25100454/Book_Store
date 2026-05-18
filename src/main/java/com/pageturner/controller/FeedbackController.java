package com.pageturner.controller;

import com.pageturner.model.Feedback;
import com.pageturner.model.Feedback.FeedbackType;
import com.pageturner.model.User;
import com.pageturner.service.FeedbackService;
import com.pageturner.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UserService userService;

    public FeedbackController(FeedbackService feedbackService, UserService userService) {
        this.feedbackService = feedbackService;
        this.userService = userService;
    }

    @GetMapping("/submit")
    public String submitForm(Model model) {
        model.addAttribute("feedback", new Feedback());
        model.addAttribute("types", FeedbackType.values());
        return "feedback/submit";
    }

    @PostMapping("/submit")
    public String submit(@ModelAttribute Feedback feedback,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            if (user == null) throw new IllegalStateException("User not found");
            feedbackService.submit(feedback, user);
            redirectAttributes.addFlashAttribute("success", "Thank you — your feedback has been received.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Something went wrong. Please try again.");
        }
        return "redirect:/feedback/my-feedback";
    }

    @GetMapping("/my-feedback")
    public String myFeedback(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        if (user == null) return "redirect:/login";
        model.addAttribute("feedbacks", feedbackService.getByUser(user));
        return "feedback/my-feedback";
    }
}
