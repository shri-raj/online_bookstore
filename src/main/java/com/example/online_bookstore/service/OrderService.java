package com.example.online_bookstore.service;

import com.example.online_bookstore.dto.CheckoutRequest;
import com.example.online_bookstore.dto.OrderDto;
import com.example.online_bookstore.dto.OrderItemDto;
import com.example.online_bookstore.entity.*;
import com.example.online_bookstore.exception.ResourceNotFoundException;
import com.example.online_bookstore.exception.UnauthorizedException;
import com.example.online_bookstore.repo.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final BookService bookService;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CartRepository cartRepository,
                        UserRepository userRepository,
                        BookRepository bookRepository,
                        BookService bookService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.bookService = bookService;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public List<OrderDto> getUserOrders() {
        User user = getCurrentUser();
        List<Order> orders = orderRepository.findByUserId(user.getId());

        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public OrderDto getOrderById(Long orderId) {
        User user = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Ensure order belongs to current user unless admin
        if (!order.getUser().getId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
            throw new UnauthorizedException("You are not authorized to view this order");
        }

        return convertToDto(order);
    }

    @Transactional
    public OrderDto createOrder(CheckoutRequest checkoutRequest) {
        User user = getCurrentUser();
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + user.getId()));

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot create order with empty cart");
        }

        // Create new order
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setShippingAddress(checkoutRequest.getShippingAddress());
        order.setPaymentMethod(checkoutRequest.getPaymentMethod());

        // Calculate total amount and create order items
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            Book book = cartItem.getBook();

            // Check stock availability
            if (book.getStockQuantity() < cartItem.getQuantity()) {
                throw new IllegalStateException("Not enough stock for book: " + book.getTitle());
            }

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setBook(book);
            orderItem.setBookTitle(book.getTitle());
            orderItem.setBookAuthor(book.getAuthor());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(book.getPrice());

            // Update book stock
            bookService.updateBookStock(book.getId(), cartItem.getQuantity());

            // Add to total
            totalAmount = totalAmount.add(orderItem.getSubtotal());
            orderItems.add(orderItem);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        // Save order items
        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
            orderItemRepository.save(item);
            savedOrder.addOrderItem(item);
        }

        // Clear cart
        List<CartItem> cartItems = new ArrayList<>(cart.getItems());
        for (CartItem item : cartItems) {
            cart.removeItem(item);
        }
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);

        return convertToDto(savedOrder);
    }

    @Transactional
    public OrderDto updateOrderStatus(Long orderId, String status) {
        User user = getCurrentUser();

        if (!"ADMIN".equals(user.getRole())) {
            throw new UnauthorizedException("You are not authorized to view this order");
        }

        // But in the same class, you're using RuntimeException:
        if (!"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("You are not authorized to update order status");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        return convertToDto(updatedOrder);
    }

    // Helper methods for DTO conversion
    private OrderDto convertToDto(Order order) {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(order.getId());
        orderDto.setUserId(order.getUser().getId());
        orderDto.setUserName(order.getUser().getName());
        orderDto.setOrderDate(order.getOrderDate());
        orderDto.setTotalAmount(order.getTotalAmount());
        orderDto.setStatus(order.getStatus());
        orderDto.setShippingAddress(order.getShippingAddress());
        orderDto.setPaymentMethod(order.getPaymentMethod());

        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        orderDto.setItems(itemDtos);

        return orderDto;
    }

    private OrderItemDto convertToDto(OrderItem item) {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(item.getId());
        dto.setBookId(item.getBook().getId());
        dto.setBookTitle(item.getBookTitle());
        dto.setBookAuthor(item.getBookAuthor());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setSubtotal(item.getSubtotal());
        return dto;
    }

    /**
     * Retrieves all orders in the system (for admin use)
     * @return List of all orders
     */
    public List<OrderDto> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}