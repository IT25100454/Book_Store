# 📚 PageTurner — Online Bookstore Management System

A full-stack online bookstore built with **Spring Boot 3**, **Thymeleaf**, **Spring Security**, and **MySQL**. Supports customer browsing, shopping cart, order management, and a full admin dashboard with analytics, inventory management, and report generation.

---

## ✨ Features

### 🛒 Customer Features
- **Browse & Search** — Catalog of books with cover images, filterable by category (Fiction, Non-Fiction, Science, Technology, Children)
- **Book Detail Pages** — Full descriptions, pricing in LKR (Rs.), stock availability
- **Shopping Cart** — Add/remove items, quantity adjustment, real-time total calculation
- **Order Placement** — Checkout flow with order confirmation and order number generation
- **Order History** — View past orders with status tracking (Pending → Processing → Shipped → Delivered / Cancelled)
- **User Profile** — Account details, change password (with strength meter), account deletion
- **Notifications** — In-app notification bell with unread count for order updates, registration confirmations, and low-stock alerts

### 🔐 Authentication & Security
- **User Registration** with email **OTP verification** (Gmail SMTP)
- **BCrypt password hashing**
- **Role-based access control** — `ROLE_USER` and `ROLE_ADMIN`
- **Session management** with Spring Security
- **Change password** with forced re-login for security
- **Account deletion** with password confirmation

### 🛠️ Admin Dashboard
- **Analytics Overview** — Total users, total books, total orders, revenue summary
- **User Management** — View all registered users with role information
- **Book Management** — Add, edit, delete books with cover URLs, pricing, stock levels
- **Order Management** — View all orders, update order status through lifecycle
- **Inventory Report Export** — Generate and download text-based inventory reports
- **Low Stock Alerts** — Automated notifications when book stock runs low

---

## 🏗️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Java 17, Spring Boot 3.2.4 |
| **Web Framework** | Spring MVC + Thymeleaf |
| **Security** | Spring Security 6, BCrypt |
| **Database** | MySQL 8+ (with JPA / Hibernate) |
| **Email** | Spring Boot Mail (Gmail SMTP) |
| **Environment** | spring-dotenv (`.env` file support) |
| **Frontend** | Thymeleaf templates, TailwindCSS, Font Awesome, AOS animations |
| **Build** | Maven |
| **Dev Tools** | Spring Boot DevTools (LiveReload) |

---

## 📁 Project Structure

```
src/main/java/com/pageturner/
├── component/          # DataLoader (seed data), H2→MySQL migration runner
├── config/             # SecurityConfig, CustomUserDetailsService, GlobalModelAttributes
├── controller/         # AdminController, AuthController, CartController, HomeController,
│                       # NotificationController, OrderController, UserController
├── model/              # User, Book, Order, OrderItem, CartItem, Notification, PendingRegistration
├── repository/         # JPA repositories (BookRepository, UserRepository, OrderRepository, NotificationRepository)
├── service/            # Service interfaces + impl/ (Book, User, Order, Notification, OTP, Email, Report)
└── util/               # CurrencyFormatter

src/main/resources/
├── application.properties
├── static/             # CSS, JS, images
└── templates/          # Thymeleaf templates
    ├── admin/          # Admin dashboard views
    ├── auth/           # Login, Register, OTP verification
    ├── books/          # Book listing, detail pages
    ├── cart/           # Shopping cart
    ├── error/          # Custom error pages
    ├── layout/         # Base template (navbar, footer)
    ├── orders/         # Order views
    └── user/           # User profile
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **MySQL 8.0+**
- A **Gmail account** with an [App Password](https://support.google.com/accounts/answer/185833) for email OTP

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/PageTurner.git
cd PageTurner
```

### 2. Create the `.env` File

Copy the example environment file and fill in your credentials:

```bash
cp .env.example .env
```

Then edit `.env`:

```env
# Database
DB_URL=jdbc:mysql://localhost:3306/pageturner?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=your_mysql_password

# Mail (Gmail SMTP)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_gmail_app_password

# App
APP_SECRET_KEY=change-this-to-a-random-string
APP_BASE_URL=http://localhost:8080
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

The app will start at **http://localhost:8080** and automatically:
- Create the `pageturner` database if it doesn't exist
- Generate all tables via Hibernate DDL auto-update
- Seed **2 default users** and **14 sample books** on first run

### 4. Access the Application

| URL | Description |
|-----|-------------|
| `http://localhost:8080` | Home page (public) |
| `http://localhost:8080/books` | Book catalog (public) |
| `http://localhost:8080/login` | Login page |
| `http://localhost:8080/register` | Registration (with email OTP) |
| `http://localhost:8080/cart` | Shopping cart (authenticated) |
| `http://localhost:8080/profile` | User profile (authenticated) |
| `http://localhost:8080/admin` | Admin dashboard (admin only) |

---

## 🔑 Default Login Credentials

| Role | Email | Password |
|------|-------|----------|
| **Admin** | `admin@pageturner.com` | `admin123` |
| **Customer** | `user@pageturner.com` | `user123` |

> **Note:** These accounts are created automatically by the `DataLoader` component on first startup when the database is empty.

---

## ⚙️ Configuration

### Database

The app uses **MySQL** by default. The database connection is configured in `application.properties` and reads credentials from the `.env` file.

The Hibernate DDL strategy is set to `update`, so schema changes are applied automatically on restart.

### Email (OTP Verification)

Email OTP is required for new user registration. The app uses **Gmail SMTP** by default. You need to:

1. Enable 2-Step Verification on your Google account
2. Generate an [App Password](https://support.google.com/accounts/answer/185833)
3. Set `MAIL_USERNAME` and `MAIL_PASSWORD` in your `.env` file

---

## 📸 Screenshots

> _Add screenshots of your application here to showcase the UI._

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is for educational purposes.

---

<p align="center">
  Built with ❤️ using Spring Boot & Thymeleaf
</p>
