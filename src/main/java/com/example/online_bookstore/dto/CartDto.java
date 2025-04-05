package com.example.online_bookstore.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartDto {
    private Long id;
    private List<CartItemDto> items;
    private BigDecimal totalPrice;
    private int itemCount;
}