package com.example.online_bookstore.controller;

import com.example.online_bookstore.dto.CartDto;
import com.example.online_bookstore.dto.CartItemDto;
import com.example.online_bookstore.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartDto> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @PostMapping("/add")
    public ResponseEntity<CartDto> addItem(@RequestBody CartItemDto cartItemDto) {
        return ResponseEntity.ok(cartService.addItemToCart(cartItemDto));
    }
    
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDto> updateItem(@PathVariable Long itemId, @RequestBody CartItemDto cartItemDto) {
        return ResponseEntity.ok(cartService.updateCartItem(itemId, cartItemDto));
    }
    
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDto> removeItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeCartItem(itemId));
    }
    
    @DeleteMapping("/clear")
    public ResponseEntity<CartDto> clearCart() {
        return ResponseEntity.ok(cartService.clearCart());
    }
}