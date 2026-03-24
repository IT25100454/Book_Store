package com.pageturner.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Component
@Order(1)
public class H2ToMySqlMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Value("${app.migration.h2.enabled:true}")
    private boolean migrationEnabled;

    @Value("${app.migration.h2.path:./data/pageturner}")
    private String h2Path;

    public H2ToMySqlMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!migrationEnabled) {
            return;
        }

        if (!isMySqlEmpty()) {
            return;
        }

        File h2File = new File(h2Path + ".mv.db");
        if (!h2File.exists()) {
            return;
        }

        String h2Url = "jdbc:h2:file:" + h2Path;
        Class.forName("org.h2.Driver");

        try (Connection h2Conn = DriverManager.getConnection(h2Url, "sa", "")) {
            migrateUsers(h2Conn);
            migrateBooks(h2Conn);
            migrateOrders(h2Conn);
            migrateOrderItems(h2Conn);
            migrateNotifications(h2Conn);
            resetAutoIncrement("users");
            resetAutoIncrement("books");
            resetAutoIncrement("orders");
            resetAutoIncrement("order_items");
            resetAutoIncrement("notifications");
        }
    }

    private boolean isMySqlEmpty() {
        Long users = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
        Long books = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books", Long.class);
        return (users == null || users == 0L) && (books == null || books == 0L);
    }

    private void migrateUsers(Connection h2Conn) throws Exception {
        String select = "SELECT id, active, created_at, updated_at, address, email, name, password, role, username FROM users";
        String insert = "INSERT INTO users (id, active, created_at, updated_at, address, email, name, password, role, username) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        copyRows(h2Conn, select, insert, rs -> {
            jdbcTemplate.update(insert,
                    rs.getLong("id"),
                    rs.getBoolean("active"),
                    rs.getTimestamp("created_at"),
                    rs.getTimestamp("updated_at"),
                    rs.getString("address"),
                    rs.getString("email"),
                    rs.getString("name"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getString("username"));
        });
    }

    private void migrateBooks(Connection h2Conn) throws Exception {
        String select = "SELECT id, price, stock_quantity, created_at, updated_at, author, cover_url, description, genre, isbn, title FROM books";
        String insert = "INSERT INTO books (id, price, stock_quantity, created_at, updated_at, author, cover_url, description, genre, isbn, title) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        copyRows(h2Conn, select, insert, rs -> {
            BigDecimal price = rs.getBigDecimal("price");
            jdbcTemplate.update(insert,
                    rs.getLong("id"),
                    price,
                    rs.getInt("stock_quantity"),
                    rs.getTimestamp("created_at"),
                    rs.getTimestamp("updated_at"),
                    rs.getString("author"),
                    rs.getString("cover_url"),
                    rs.getString("description"),
                    rs.getString("genre"),
                    rs.getString("isbn"),
                    rs.getString("title"));
        });
    }

    private void migrateOrders(Connection h2Conn) throws Exception {
        String select = "SELECT id, total_amount, created_at, updated_at, user_id, order_number, shipping_address, status FROM orders";
        String insert = "INSERT INTO orders (id, total_amount, created_at, updated_at, user_id, order_number, shipping_address, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        copyRows(h2Conn, select, insert, rs -> {
            jdbcTemplate.update(insert,
                    rs.getLong("id"),
                    rs.getBigDecimal("total_amount"),
                    rs.getTimestamp("created_at"),
                    rs.getTimestamp("updated_at"),
                    rs.getLong("user_id"),
                    rs.getString("order_number"),
                    rs.getString("shipping_address"),
                    rs.getString("status"));
        });
    }

    private void migrateOrderItems(Connection h2Conn) throws Exception {
        String select = "SELECT id, price, quantity, book_id, created_at, order_id, updated_at FROM order_items";
        String insert = "INSERT INTO order_items (id, price, quantity, book_id, created_at, order_id, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        copyRows(h2Conn, select, insert, rs -> {
            jdbcTemplate.update(insert,
                    rs.getLong("id"),
                    rs.getBigDecimal("price"),
                    rs.getInt("quantity"),
                    rs.getLong("book_id"),
                    rs.getTimestamp("created_at"),
                    rs.getLong("order_id"),
                    rs.getTimestamp("updated_at"));
        });
    }

    private void migrateNotifications(Connection h2Conn) throws Exception {
        String select = "SELECT id, is_read, created_at, user_id, message, link, title, type FROM notifications";
        String insert = "INSERT INTO notifications (id, is_read, created_at, user_id, message, link, title, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        copyRows(h2Conn, select, insert, rs -> {
            jdbcTemplate.update(insert,
                    rs.getLong("id"),
                    rs.getBoolean("is_read"),
                    rs.getTimestamp("created_at"),
                    rs.getObject("user_id"),
                    rs.getString("message"),
                    rs.getString("link"),
                    rs.getString("title"),
                    rs.getString("type"));
        });
    }

    private void resetAutoIncrement(String table) {
        Long maxId = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id), 0) FROM " + table, Long.class);
        long nextId = (maxId == null ? 0L : maxId) + 1L;
        jdbcTemplate.execute("ALTER TABLE " + table + " AUTO_INCREMENT = " + nextId);
    }

    private void copyRows(Connection connection, String selectSql, String insertSql, RowConsumer rowConsumer) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(selectSql);
             ResultSet rs = ps.executeQuery()) {
            Set<Long> processed = new HashSet<>();
            while (rs.next()) {
                long id = rs.getLong("id");
                if (processed.add(id)) {
                    rowConsumer.accept(rs);
                }
            }
        }
    }

    @FunctionalInterface
    private interface RowConsumer {
        void accept(ResultSet rs) throws Exception;
    }
}
