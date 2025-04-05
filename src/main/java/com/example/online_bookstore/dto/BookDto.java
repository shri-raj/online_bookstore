package com.example.online_bookstore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

import com.example.online_bookstore.entity.Book;

@Data
public class BookDto {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    private String description;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price should be greater than or equal to 0")
    private BigDecimal price;

    private String isbn;

    private String coverImage;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity should be greater than or equal to 0")
    private Integer stockQuantity;

    private String category;

    // 4. DTO Improvements
    
    // Add a constructor to simplify entity to DTO conversion
    public BookDto(Book book) {
        this.id = book.getId();
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.description = book.getDescription();
        this.price = book.getPrice();
        this.isbn = book.getIsbn();
        this.coverImage = book.getCoverImage();
        this.stockQuantity = book.getStockQuantity();
        this.category = book.getCategory();
    }
    
    // Add a default no-args constructor to maintain compatibility
    public BookDto() {
    }
}



