package com.pageturner.component;

import com.pageturner.model.Book;
import com.pageturner.model.User;
import com.pageturner.repository.BookRepository;
import com.pageturner.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Order(2)
public class DataLoader implements CommandLineRunner {

    private final UserService userService;
    private final BookRepository bookRepository;

    public DataLoader(UserService userService, BookRepository bookRepository) {
        this.userService = userService;
        this.bookRepository = bookRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        loadUsers();
        loadBooks();
    }

    private void loadUsers() {
        if (userService.getTotalUsersCount() == 0) {
            User admin = new User();
            admin.setUsername("admin@pageturner.com");
            admin.setPassword("admin123");
            admin.setName("Admin User");
            admin.setEmail("admin@pageturner.com");
            admin.setAddress("123 Admin St, City, Country");
            admin.setRole("ROLE_ADMIN");
            userService.saveUser(admin);

            User customer = new User();
            customer.setUsername("user@pageturner.com");
            customer.setPassword("user123");
            customer.setName("Customer User");
            customer.setEmail("user@pageturner.com");
            customer.setAddress("456 Customer Ave, City, Country");
            customer.setRole("ROLE_USER");
            userService.saveUser(customer);
        }
    }

    private void loadBooks() {
        if (bookRepository.count() == 0) {
            List<Book> books = List.of(
                    new Book("The Great Gatsby", "F. Scott Fitzgerald", "Fiction", "9780743273565", new BigDecimal("1499.00"), 20, "A classic novel of the Jazz Age.", "https://covers.openlibrary.org/b/isbn/9780743273565-M.jpg"),
                    new Book("1984", "George Orwell", "Fiction", "9780451524935", new BigDecimal("1299.00"), 50, "A dystopian social science fiction novel.", "https://covers.openlibrary.org/b/isbn/9780451524935-M.jpg"),
                    new Book("To Kill a Mockingbird", "Harper Lee", "Fiction", "9780060935467", new BigDecimal("1899.00"), 30, "A novel about the serious issues of rape and racial inequality.", "https://covers.openlibrary.org/b/isbn/9780060935467-M.jpg"),
                    new Book("Sapiens: A Brief History of Humankind", "Yuval Noah Harari", "Non-Fiction", "9780062316097", new BigDecimal("2499.00"), 15, "Explores the history of our species.", "https://covers.openlibrary.org/b/isbn/9780062316097-M.jpg"),
                    new Book("Educated", "Tara Westover", "Non-Fiction", "9780399590504", new BigDecimal("2199.00"), 25, "A memoir about a young woman who, kept out of school, leaves her survivalist family.", "https://covers.openlibrary.org/b/isbn/9780399590504-M.jpg"),
                    new Book("Astrophysics for People in a Hurry", "Neil deGrasse Tyson", "Science", "9780393609394", new BigDecimal("1799.00"), 40, "A readable guide to the cosmos.", "https://covers.openlibrary.org/b/isbn/9780393609394-M.jpg"),
                    new Book("A Brief History of Time", "Stephen Hawking", "Science", "9780553380163", new BigDecimal("1999.00"), 22, "A landmark volume in science writing.", "https://covers.openlibrary.org/b/isbn/9780553380163-M.jpg"),
                    new Book("Clean Code", "Robert C. Martin", "Technology", "9780132350884", new BigDecimal("4500.00"), 10, "A Handbook of Agile Software Craftsmanship.", "https://covers.openlibrary.org/b/isbn/9780132350884-M.jpg"),
                    new Book("The Pragmatic Programmer", "Andrew Hunt, David Thomas", "Technology", "9780135957059", new BigDecimal("5500.00"), 4, "Your journey to mastery.", "https://covers.openlibrary.org/b/isbn/9780135957059-M.jpg"),
                    new Book("Design Patterns", "Erich Gamma etc.", "Technology", "9780201633610", new BigDecimal("6500.00"), 12, "Elements of Reusable Object-Oriented Software.", "https://covers.openlibrary.org/b/isbn/9780201633610-M.jpg"),
                    new Book("Harry Potter and the Sorcerer's Stone", "J.K. Rowling", "Children", "9780590353427", new BigDecimal("1199.00"), 60, "A boy discovers he is a wizard.", "https://covers.openlibrary.org/b/isbn/9780590353427-M.jpg"),
                    new Book("Where the Wild Things Are", "Maurice Sendak", "Children", "9780064431781", new BigDecimal("999.00"), 45, "A classic children's picture book.", "https://covers.openlibrary.org/b/isbn/9780064431781-M.jpg"),
                    new Book("The Very Hungry Caterpillar", "Eric Carle", "Children", "9780399226908", new BigDecimal("899.00"), 35, "A classic book about a famished caterpillar.", "https://covers.openlibrary.org/b/isbn/9780399226908-M.jpg"),
                    new Book("The Alchemist", "Paulo Coelho", "Fiction", "9780061122415", new BigDecimal("1499.00"), 3, "A fable about following your dream.", "https://covers.openlibrary.org/b/isbn/9780061122415-M.jpg"),
                    new Book("Atomic Habits", "James Clear", "Non-Fiction", "9780735211292", new BigDecimal("2299.00"), 100, "An easy & proven way to build good habits & break bad ones.", "https://covers.openlibrary.org/b/isbn/9780735211292-M.jpg")
            );
            bookRepository.saveAll(books);
        }
    }
}
