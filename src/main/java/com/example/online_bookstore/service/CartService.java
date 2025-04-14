package com.example.online_bookstore.service;

import com.example.online_bookstore.dto.CartDto;
import com.example.online_bookstore.dto.CartItemDto;
import com.example.online_bookstore.entity.Book;
import com.example.online_bookstore.entity.Cart;
import com.example.online_bookstore.entity.CartItem;
import com.example.online_bookstore.entity.User;
import com.example.online_bookstore.exception.BusinessLogicException;
import com.example.online_bookstore.exception.ResourceNotFoundException;
import com.example.online_bookstore.exception.UnauthorizedException;
import com.example.online_bookstore.repo.BookRepository;
import com.example.online_bookstore.repo.CartItemRepository;
import com.example.online_bookstore.repo.CartRepository;
import com.example.online_bookstore.repo.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       UserRepository userRepository,
                       BookRepository bookRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public CartDto getCart() {
        User user = getCurrentUser();
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + user.getId()));

        return convertToDto(cart);
    }

    @Transactional
    public CartDto addItemToCart(CartItemDto cartItemDto) {
        if (cartItemDto.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        
        User user = getCurrentUser();
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + user.getId()));

        Book book = bookRepository.findById(cartItemDto.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + cartItemDto.getBookId()));
        
        if (book.getStockQuantity() < cartItemDto.getQuantity()) {
            throw new BusinessLogicException("Not enough stock for book: " + book.getTitle());
        }
        
        if (isBookInCart(cart, book.getId())) {
            CartItem item = cartItemRepository.findByCartIdAndBookId(cart.getId(), book.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
            item.setQuantity(item.getQuantity() + cartItemDto.getQuantity());
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setBook(book);
            newItem.setQuantity(cartItemDto.getQuantity());
            newItem.setPrice(book.getPrice());
            cart.addItem(newItem);
        }

        cart.updateTotalPrice();
        Cart updatedCart = cartRepository.save(cart);
        return convertToDto(updatedCart);
    }

    @Transactional
    public CartDto updateCartItem(Long itemId, CartItemDto cartItemDto) {
        if (cartItemDto.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        
        User user = getCurrentUser();
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + user.getId()));

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new UnauthorizedException("Cart item does not belong to the user's cart");
        }

        item.setQuantity(cartItemDto.getQuantity());
        cartItemRepository.save(item);

        cart.updateTotalPrice();
        Cart updatedCart = cartRepository.save(cart);
        return convertToDto(updatedCart);
    }

    @Transactional
    public CartDto removeCartItem(Long itemId) {
        User user = getCurrentUser();
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + user.getId()));

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new UnauthorizedException("Cart item does not belong to the user's cart");
        }

        cart.removeItem(item);
        cartItemRepository.delete(item);

        Cart updatedCart = cartRepository.save(cart);
        return convertToDto(updatedCart);
    }

    @Transactional
    public CartDto clearCart() {
        User user = getCurrentUser();
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + user.getId()));

        // Remove all items
        List<CartItem> items = cart.getItems();
        cartItemRepository.deleteAll(items);
        items.clear();

        cart.setTotalPrice(BigDecimal.ZERO);
        Cart updatedCart = cartRepository.save(cart);
        return convertToDto(updatedCart);
    }

    // Helper methods for DTO conversion
    private CartDto convertToDto(Cart cart) {
        CartDto cartDto = new CartDto();
        cartDto.setId(cart.getId());
        cartDto.setTotalPrice(cart.getTotalPrice());

        List<CartItemDto> itemDtos = cart.getItems().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        cartDto.setItems(itemDtos);
        cartDto.setItemCount(itemDtos.size());

        return cartDto;
    }

    private CartItemDto convertToDto(CartItem item) {
        CartItemDto dto = new CartItemDto();
        dto.setId(item.getId());
        dto.setBookId(item.getBook().getId());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setSubtotal(item.getSubtotal());
        dto.setBookTitle(item.getBook().getTitle());
        dto.setBookAuthor(item.getBook().getAuthor());
        dto.setBookImage(item.getBook().getCoverImage());
        return dto;
    }

    // Add this helper method
    private boolean isBookInCart(Cart cart, Long bookId) {
        return cart.getItems().stream()
                .anyMatch(item -> item.getBook().getId().equals(bookId));
    }
}