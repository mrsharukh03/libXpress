package com.LibXpress.Services;

import com.LibXpress.DTOs.BookDTO.BookDTO;
import com.LibXpress.DTOs.BookDTO.BookUpdateDTO;
import com.LibXpress.DTOs.BookDTO.FeedbackDTO;
import com.LibXpress.Entitys.Books;
import com.LibXpress.Entitys.Feedback;
import com.LibXpress.Entitys.User;
import com.LibXpress.Repositorys.FeedbackRepo;
import com.LibXpress.Repositorys.BookRepo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookService.class);

    private final BookRepo bookRepository;
    private final FeedbackRepo feedbackRepo;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Autowired
    public BookService(BookRepo bookRepository, FeedbackRepo feedbackRepo, UserService userService, ModelMapper modelMapper) {
        this.bookRepository = bookRepository;
        this.feedbackRepo = feedbackRepo;
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    // Add or Update Book
    public boolean save(BookDTO bookDTO) {
        try {
            Books book = modelMapper.map(bookDTO, Books.class);
            bookRepository.save(book);
            return true;
        } catch (Exception e) {
            logger.error("Error saving book: ", e);
            return false;
        }
    }

    // Save all books in bulk
    public boolean saveAll(List<BookDTO> bookDTOs) {
        try {
            List<Books> books = bookDTOs.stream()
                    .map(bookDTO -> modelMapper.map(bookDTO, Books.class))
                    .collect(Collectors.toList());
            bookRepository.saveAll(books);
            return true;
        } catch (Exception e) {
            logger.error("Error saving books: ", e);
            return false;
        }
    }

    // Delete a book by ID
    public boolean deleteById(Long bookId) {
        try {
            if (bookRepository.existsById(bookId)) {
                bookRepository.deleteById(bookId);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error deleting book: ", e);
            return false;
        }
    }

    // Get all books with pagination and sorting by views in descending order
    public Page<BookDTO> getBooks(int page, int size) {
        try {
            if (page < 0 || size <= 0) {
                logger.warn("Invalid pagination parameters: page={}, size={}", page, size);
                return Page.empty();
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("views")));
            return bookRepository.findAll(pageable).map(book -> modelMapper.map(book, BookDTO.class));
        } catch (Exception e) {
            logger.error("Error fetching paged books: ", e);
            return Page.empty();
        }
    }

    // Get a book by ID
    public Books getBookById(Long bookId) {
        try {
            return bookRepository.findById(bookId).orElse(null);
        } catch (Exception e) {
            logger.error("Error fetching book by ID: ", e);
            return null;
        }
    }

    // Get a BookDTO by ID
    public BookDTO getBookDTOById(Long bookId) {
        Books book = getBookById(bookId);
        return (book != null) ? modelMapper.map(book, BookDTO.class) : null;
    }

    // Update a book
    public boolean updateBook(BookUpdateDTO bookDTO) {
        Books existingBook = getBookById(bookDTO.getBookId());

        if (existingBook == null) {
            logger.error("Book not found with ID: {}", bookDTO.getBookId());
            return false;
        }

        if (!Validator.isValidQuantity(bookDTO.getTotalQuantity(), existingBook.getAvailableQuantity())) {
            logger.error("Invalid total quantity for Book ID: {}", bookDTO.getBookId());
            return false;
        }

        if (!Validator.isValidPrice(bookDTO.getPricePdf()) || !Validator.isValidPrice(bookDTO.getPriceOffline())) {
            logger.error("Invalid price for Book ID: {}", bookDTO.getBookId());
            return false;
        }

        updateBookDetails(existingBook, bookDTO);
        bookRepository.save(existingBook);
        return true;
    }

    private void updateBookDetails(Books existingBook, BookUpdateDTO bookDTO) {
        if (bookDTO.getTitle() != null) existingBook.setTitle(bookDTO.getTitle());
        if (bookDTO.getAuthor() != null) existingBook.setAuthor(bookDTO.getAuthor());
        if (bookDTO.getPosterURL() != null) existingBook.setPosterURL(bookDTO.getPosterURL());
        if (bookDTO.getLanguage() != null) existingBook.setLanguage(bookDTO.getLanguage());
        if (bookDTO.getCategory() != null) existingBook.setCategory(bookDTO.getCategory());
        if (bookDTO.getAvailableQuantity() != null) existingBook.setAvailableQuantity(bookDTO.getAvailableQuantity());
        if (bookDTO.getTotalQuantity() != null) existingBook.setTotalQuantity(existingBook.getTotalQuantity() + bookDTO.getTotalQuantity());
        if (bookDTO.getIsAvailableForBorrow() != null) existingBook.setAvailableForBorrow(bookDTO.getIsAvailableForBorrow());
        if (bookDTO.getBorrowDuration() != null) existingBook.setBorrowDuration(bookDTO.getBorrowDuration());
        if (bookDTO.getPricePdf() != null) existingBook.setPricePdf(bookDTO.getPricePdf());
        if (bookDTO.getPriceOffline() != null) existingBook.setPriceOffline(bookDTO.getPriceOffline());
    }

    // Search for books by keyword with pagination
    public List<BookDTO> searchBooks(String keyword, int page, int size) {
        if (keyword == null || keyword.isEmpty()) return new ArrayList<>();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("views")));
        Page<Books> result = bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrCategoryContainingIgnoreCase(keyword, keyword, keyword, pageable);

        return result.isEmpty() ? new ArrayList<>() : result.getContent().stream()
                .map(book -> modelMapper.map(book, BookDTO.class))
                .collect(Collectors.toList());
    }

    // Add a view count to a book
    public void addViews(Long bookId) {
        Books book = getBookById(bookId);
        if (book != null) {
            book.setViews(book.getViews() == null ? 1 : book.getViews() + 1);
            bookRepository.save(book);
        }
    }

    // Add feedback to a book
    public boolean addFeedback(FeedbackDTO feedbackDTO) {
        Books book = getBookById(feedbackDTO.getBookID());
        User user = userService.findById(feedbackDTO.getUserEmail());

        if (book != null && user != null && user.isActiveStatus()) {
            Feedback feedback = new Feedback();
            feedback.setRating(feedbackDTO.getRating());
            feedback.setComments(feedbackDTO.getComments());
            feedback.setBook(book);
            feedback.setUser(user);

            feedbackRepo.save(feedback);
            return true;
        }
        return false;
    }

    // Get all feedbacks for a book
    public List<FeedbackDTO> getAllFeedbacks(Long bookId) {
        Books book = getBookById(bookId);
        if (book == null || book.getFeedbacks() == null) return new ArrayList<>();

        return book.getFeedbacks().stream()
                .map(feedback -> modelMapper.map(feedback, FeedbackDTO.class))
                .collect(Collectors.toList());
    }

    // Static Validator class for validation logic
    private static class Validator {

        public static boolean isValidQuantity(int totalQuantity, int availableQuantity) {
            return totalQuantity >= availableQuantity;
        }

        public static boolean isValidPrice(Double price) {
            return price != null && price >= 0;
        }
    }
}
