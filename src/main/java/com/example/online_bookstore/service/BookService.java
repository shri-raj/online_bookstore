package com.example.online_bookstore.service;

import com.example.online_bookstore.dto.BookDto;
import com.example.online_bookstore.entity.Book;
import com.example.online_bookstore.exception.BusinessLogicException;
import com.example.online_bookstore.exception.ResourceNotFoundException;
import com.example.online_bookstore.repo.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<BookDto> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public BookDto getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        return convertToDto(book);
    }

    public List<BookDto> getBooksByCategory(String category) {
        return bookRepository.findByCategory(category).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<BookDto> searchBooks(String query) {
        List<Book> titleResults = bookRepository.findByTitleContainingIgnoreCase(query);
        List<Book> authorResults = bookRepository.findByAuthorContainingIgnoreCase(query);
    
        Set<Book> combinedResults = new HashSet<>(titleResults);
        combinedResults.addAll(authorResults);
        
        return combinedResults.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookDto createBook(BookDto bookDto) {
        Book book = convertToEntity(bookDto);
        Book savedBook = bookRepository.save(book);
        return convertToDto(savedBook);
    }

    @Transactional
    public BookDto updateBook(Long id, BookDto bookDto) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        existingBook.setTitle(bookDto.getTitle());
        existingBook.setAuthor(bookDto.getAuthor());
        existingBook.setDescription(bookDto.getDescription());
        existingBook.setPrice(bookDto.getPrice());
        existingBook.setIsbn(bookDto.getIsbn());
        existingBook.setCoverImage(bookDto.getCoverImage());
        existingBook.setStockQuantity(bookDto.getStockQuantity());
        existingBook.setCategory(bookDto.getCategory());

        Book updatedBook = bookRepository.save(existingBook);
        return convertToDto(updatedBook);
    }

    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
    }

    @Transactional
    public void updateBookStock(Long bookId, int quantity) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        int newQuantity = book.getStockQuantity() - quantity;
        if (newQuantity < 0) {
            throw new BusinessLogicException("Insufficient stock for book: " + book.getTitle());
        }

        book.setStockQuantity(newQuantity);
        bookRepository.save(book);
    }

    private BookDto convertToDto(Book book) {
        BookDto bookDto = new BookDto();
        bookDto.setId(book.getId());
        bookDto.setTitle(book.getTitle());
        bookDto.setAuthor(book.getAuthor());
        bookDto.setDescription(book.getDescription());
        bookDto.setPrice(book.getPrice());
        bookDto.setIsbn(book.getIsbn());
        bookDto.setCoverImage(book.getCoverImage());
        bookDto.setStockQuantity(book.getStockQuantity());
        bookDto.setCategory(book.getCategory());
        return bookDto;
    }

    private Book convertToEntity(BookDto bookDto) {
        Book book = new Book();
        book.setId(bookDto.getId());
        book.setTitle(bookDto.getTitle());
        book.setAuthor(bookDto.getAuthor());
        book.setDescription(bookDto.getDescription());
        book.setPrice(bookDto.getPrice());
        book.setIsbn(bookDto.getIsbn());
        book.setCoverImage(bookDto.getCoverImage());
        book.setStockQuantity(bookDto.getStockQuantity());
        book.setCategory(bookDto.getCategory());
        return book;
    }
}