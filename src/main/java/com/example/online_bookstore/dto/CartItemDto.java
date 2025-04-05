package com.example.online_bookstore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemDto {
    private Long id;

    @NotNull(message = "Book ID is required")
    private Long bookId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity should be at least 1")
    private Integer quantity;

    private String bookTitle;
    private String bookAuthor;
    private String bookImage;
    private java.math.BigDecimal price;
    private java.math.BigDecimal subtotal;
}