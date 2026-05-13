package com.pageturner.service.impl;

import com.pageturner.model.Book;
import com.pageturner.repository.BookRepository;
import com.pageturner.service.ReportService;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    private final BookRepository bookRepository;

    public ReportServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public boolean exportInventoryReport() {
        List<Book> books = bookRepository.findAll();
        File directory = new File("data");
        if (!directory.exists()) {
            directory.mkdir();
        }

        File file = new File("data/inventory_report.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("--- PageTurner Inventory Report ---");
            writer.newLine();
            writer.write("Generated at: " + LocalDateTime.now());
            writer.newLine();
            writer.write("---------------------------------");
            writer.newLine();
            writer.newLine();

            writer.write(String.format("%-15s | %-30s | %-20s | %-10s | %-10s", 
                    "ISBN", "Title", "Author", "Price", "Stock"));
            writer.newLine();
            writer.write("--------------------------------------------------------------------------------------------------");
            writer.newLine();

            for (Book book : books) {
                writer.write(String.format("%-15s | %-30s | %-20s | $%-9.2f | %-10d",
                        book.getIsbn(),
                        truncate(book.getTitle(), 30),
                        truncate(book.getAuthor(), 20),
                        book.getPrice(),
                        book.getStockQuantity()));
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String truncate(String text, int length) {
        if (text == null) return "";
        if (text.length() <= length) return text;
        return text.substring(0, length - 3) + "...";
    }
}
