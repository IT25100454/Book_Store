package com.pageturner.component;

import com.pageturner.model.Book;
import com.pageturner.model.Feedback;
import com.pageturner.model.Feedback.FeedbackStatus;
import com.pageturner.model.Feedback.FeedbackType;
import com.pageturner.model.User;
import com.pageturner.repository.BookRepository;
import com.pageturner.repository.FeedbackRepository;
import com.pageturner.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds sample feedback data for development.
 * NOTE: If you already have a DataInitializer / CommandLineRunner bean,
 * copy the seedFeedback() method body into it instead of using this class,
 * to avoid a duplicate CommandLineRunner conflict.
 */
@Configuration
public class FeedbackDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(FeedbackDataInitializer.class);

    @Bean
    @Order(10) // run after your main data seeder
    public CommandLineRunner seedFeedback(FeedbackRepository feedbackRepo,
                                          UserRepository userRepo,
                                          BookRepository bookRepo) {
        return args -> {
            if (feedbackRepo.count() > 0) {
                log.info("Feedback data already seeded — skipping.");
                return;
            }

            List<User> users = userRepo.findAll();
            List<Book> books = bookRepo.findAll();

            if (users.isEmpty()) {
                log.warn("No users found — skipping feedback seed.");
                return;
            }

            User u1 = users.get(0);
            User u2 = users.size() > 1 ? users.get(1) : u1;
            Book b1 = books.isEmpty() ? null : books.get(0);
            Book b2 = books.size() > 1 ? books.get(1) : b1;

            // 1 — General compliment, pending, no reply
            save(feedbackRepo, u1, null, FeedbackType.GENERAL, FeedbackStatus.PENDING,
                    "Love the reading experience", 5,
                    "PageTurner has become my favourite online bookshop. The editorial presentation is beautiful and the curated picks always surprise me. Keep it up!",
                    false, null, null);

            // 2 — Book review, resolved with admin reply
            save(feedbackRepo, u2, b1, FeedbackType.BOOK_REVIEW, FeedbackStatus.RESOLVED,
                    "Brilliant read — highly recommend", 5,
                    "One of the best books I have read this year. The prose is sharp and the pacing never falters. Happy to see it featured on the staff picks shelf.",
                    false,
                    "Thank you so much for sharing this. It's one of our team's favourites too — really glad you enjoyed it!",
                    LocalDateTime.now().minusDays(2));

            // 3 — Complaint, reviewed
            save(feedbackRepo, u1, null, FeedbackType.COMPLAINT, FeedbackStatus.REVIEWED,
                    "Checkout flow needs improvement", 2,
                    "The cart sometimes loses items when I navigate away. I had to add the same book three times before it stuck. Please look into this.",
                    false,
                    "Thank you for flagging this — our team has identified the issue and a fix is being deployed this week. We apologise for the inconvenience.",
                    LocalDateTime.now().minusDays(1));

            // 4 — Suggestion, pending
            save(feedbackRepo, u2, null, FeedbackType.SUGGESTION, FeedbackStatus.PENDING,
                    "Add a reading list / wishlist feature", 4,
                    "It would be fantastic to save books to a private list without adding them to the cart. A simple 'save for later' button on each book card would go a long way.",
                    false, null, null);

            // 5 — Book review, pending, anonymous
            save(feedbackRepo, u1, b2, FeedbackType.BOOK_REVIEW, FeedbackStatus.PENDING,
                    "Underwhelming for the hype", 2,
                    "The writing style didn't resonate with me at all. The premise is great but the execution is slow. Not what I expected from the staff pick label.",
                    true, null, null);

            // 6 — Compliment, dismissed (spam-ish)
            save(feedbackRepo, u2, null, FeedbackType.COMPLIMENT, FeedbackStatus.DISMISSED,
                    "Best website ever", null,
                    "Absolutely perfect in every way. 10 out of 10.",
                    false, null, null);

            // 7 — General, pending
            save(feedbackRepo, u1, null, FeedbackType.GENERAL, FeedbackStatus.PENDING,
                    "Mobile experience on older Android", 3,
                    "The scrollytelling hero section is stunning on desktop but stutters quite a bit on my Android phone. Might be worth a performance pass for lower-end devices.",
                    false, null, null);

            // 8 — Book review with 5 stars, resolved
            if (books.size() > 2) {
                save(feedbackRepo, u2, books.get(2), FeedbackType.BOOK_REVIEW, FeedbackStatus.RESOLVED,
                        "Changed how I think about science", 5,
                        "Finished this in two sittings. The way the author breaks down complex ideas without dumbing them down is rare. Would love more titles like this in the Science section.",
                        false,
                        "We're thrilled it resonated so deeply! We'll be adding more popular-science titles next month — keep an eye on the new arrivals section.",
                        LocalDateTime.now().minusHours(5));
            }

            log.info("Seeded {} sample feedback entries.", feedbackRepo.count());
        };
    }

    private void save(FeedbackRepository repo, User user, Book book,
                      FeedbackType type, FeedbackStatus status,
                      String subject, Integer rating, String message,
                      boolean anonymous, String adminReply, LocalDateTime repliedAt) {
        Feedback fb = new Feedback();
        fb.setUser(user);
        fb.setBook(book);
        fb.setType(type);
        fb.setStatus(status);
        fb.setSubject(subject);
        fb.setRating(rating);
        fb.setMessage(message);
        fb.setAnonymous(anonymous);
        fb.setAdminReply(adminReply);
        fb.setRepliedAt(repliedAt);
        repo.save(fb);
    }
}
