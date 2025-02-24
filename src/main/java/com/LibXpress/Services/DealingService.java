package com.LibXpress.Services;

import com.LibXpress.APIResponse.TransactionReceipt;
import com.LibXpress.DTOs.BookDTO.BorrowBookRequestDTO;
import com.LibXpress.DTOs.TransactionDTO.TransactionDTO;
import com.LibXpress.Entitys.Books;
import com.LibXpress.Entitys.BorrowedBooks;
import com.LibXpress.Entitys.Transaction;
import com.LibXpress.Entitys.User;
import com.LibXpress.Repositorys.BookRepo;
import com.LibXpress.Repositorys.BorrowedRepo;
import com.LibXpress.Repositorys.TransactionRepo;
import com.LibXpress.Repositorys.UserRepo;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DealingService {

    private final PaymentService paymentService;
    private final UserRepo userRepo;
    private final BookService bookService;
    private final TransactionRepo transactionRepo;
    private final BookRepo bookRepo;
    private final BorrowedRepo borrowedRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    public DealingService(PaymentService paymentService, UserRepo userRepo, BookService bookService,
                          TransactionRepo transactionRepo, BookRepo bookRepo, BorrowedRepo borrowedRepo) {
        this.paymentService = paymentService;
        this.userRepo = userRepo;
        this.bookService = bookService;
        this.transactionRepo = transactionRepo;
        this.bookRepo = bookRepo;
        this.borrowedRepo = borrowedRepo;
    }

    @Transactional
    public ResponseEntity<?> purchaseBook(Long bookId, String userId, int quantity, String method) {
        Books book = bookService.getBookById(bookId);
        User user = userRepo.findById(userId).orElse(null);

        if (book == null || user == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (!user.isActiveStatus()) {
            return new ResponseEntity<>("Please Verify email || Phone", HttpStatus.UPGRADE_REQUIRED);
        }

        if (book.getAvailableQuantity() < quantity) {
            return new ResponseEntity<>("Not enough stock available", HttpStatus.BAD_REQUEST);
        }

        Double amount = book.getPriceOffline() * quantity;
        boolean paymentSuccess = processPayment(method, user.getPhone(), amount);

        if (paymentSuccess) {
            book.setAvailableQuantity(book.getAvailableQuantity() - quantity);
            bookRepo.save(book);
        }

        Transaction transaction = createTransaction(user, book, amount, method, paymentSuccess);
        transactionRepo.save(transaction);

        TransactionReceipt receipt = createTransactionReceipt(transaction, book, quantity, method, amount, user);
        if (!paymentSuccess) return new ResponseEntity<>(receipt, HttpStatus.INTERNAL_SERVER_ERROR);

        return new ResponseEntity<>(receipt, HttpStatus.OK);
    }

    public ResponseEntity<?> findUserTransaction(String userId) {
        List<Transaction> transactionList = transactionRepo.findByUserEmail(userId);

        if (transactionList.isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
        }

        List<TransactionDTO> dtos = new ArrayList<>();
        for (Transaction transaction : transactionList) {
            TransactionDTO dto = modelMapper.map(transaction, TransactionDTO.class);
            dto.setUserId(transaction.getUser().getEmail());
            dtos.add(dto);
        }
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> borrowBook(BorrowBookRequestDTO borrowRequestDTO,String userId) {
        Books book = bookService.getBookById(borrowRequestDTO.getBookId());
        User user = userRepo.findById(userId).orElse(null);

        if (book == null || user == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (!user.isActiveStatus()) {
            return new ResponseEntity<>("Please Verify email || Phone", HttpStatus.UPGRADE_REQUIRED);
        }

        if (book.getAvailableQuantity() < borrowRequestDTO.getQuantity()) {
            return new ResponseEntity<>("Not enough stock available", HttpStatus.BAD_REQUEST);
        }

        Double amount = (book.getPriceOffline() * borrowRequestDTO.getQuantity()) / 3;
        boolean paymentSuccess = paymentService.payWithWallet(user.getPhone(), amount);

        if (paymentSuccess) {
            book.setAvailableQuantity(book.getAvailableQuantity() - borrowRequestDTO.getQuantity());
            bookRepo.save(book);
        }

        Transaction transaction = createTransaction(user, book, amount, "Wallet", paymentSuccess);
        transactionRepo.save(transaction);

        LocalDate returnDate = LocalDate.now().plusDays(borrowRequestDTO.getDurationInDays());

        BorrowedBooks borrowedBooks = new BorrowedBooks();
        borrowedBooks.setBook(book);
        borrowedBooks.setUser(user);
        borrowedBooks.setBorrowDate(LocalDateTime.now());
        borrowedBooks.setQuentity(borrowRequestDTO.getQuantity());
        borrowedBooks.setReturnDate(returnDate);
        borrowedRepo.save(borrowedBooks);

        TransactionReceipt receipt = createTransactionReceipt(transaction, book, borrowRequestDTO.getQuantity(), "Wallet", amount, user);
        receipt.setReturnDate(returnDate);
        if (!paymentSuccess) return new ResponseEntity<>(receipt, HttpStatus.INTERNAL_SERVER_ERROR);

        return new ResponseEntity<>(receipt, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> returnBook(Long borrowedBookId, String userId) {
        BorrowedBooks borrowedBook = borrowedRepo.findById(borrowedBookId).orElse(null);
        if (borrowedBook == null) {
            return new ResponseEntity<>("Borrowed record not found", HttpStatus.NOT_FOUND);
        }

        if (!borrowedBook.getUser().getEmail().equals(userId)) {
            return new ResponseEntity<>("This book was not borrowed by the given user", HttpStatus.BAD_REQUEST);
        }

        LocalDate returnDate = LocalDate.now();
        LocalDate dueDate = borrowedBook.getReturnDate();

        double lateFee = 0.0;
        if (returnDate.isAfter(dueDate)) {
            long daysLate = java.time.temporal.ChronoUnit.DAYS.between(dueDate, returnDate);
            lateFee = daysLate * 5.0; // ₹5 per day late fee
        }

        Books book = borrowedBook.getBook();
        book.setAvailableQuantity(book.getAvailableQuantity() + borrowedBook.getQuentity());
        bookRepo.save(book);
        User user = borrowedBook.getUser();
        double amount = lateFee;
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setBook(book);
        transaction.setAmount(amount);
        transaction.setPayMethod("None");
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setTransactionStatus("Returned");

        transactionRepo.save(transaction);
        borrowedBook.setReturnDate(returnDate);
        borrowedRepo.deleteById(borrowedBook.getId());

        TransactionReceipt receipt = createTransactionReceipt(transaction, book, 1, "None", amount, user);
        receipt.setReturnDate(returnDate);
        receipt.setTransactionStatus("Returned");

        return new ResponseEntity<>(receipt, HttpStatus.OK);
    }

    // Helper methods

    private boolean processPayment(String method, String userPhone, Double amount) {
        switch (method.toLowerCase()) {
            case "upigetway":
                return paymentService.payWithUpi(amount);
            case "wallet":
                return paymentService.payWithWallet(userPhone, amount);
            case "cash":
                return true; // Cash payment is always successful
            default:
                return false;
        }
    }

    private Transaction createTransaction(User user, Books book, Double amount, String method, boolean paymentSuccess) {
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setBook(book);
        transaction.setAmount(amount);
        transaction.setPayMethod(method);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setTransactionStatus(paymentSuccess ? "completed" : "Failed");
        return transaction;
    }

    private TransactionReceipt createTransactionReceipt(Transaction transaction, Books book, int quantity, String method, Double amount, User user) {
        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setTransactionId(transaction.getId());
        receipt.setTotalAmmout(amount);
        receipt.setBookPrice(book.getPriceOffline());
        receipt.setQuentity(quantity);
        receipt.setPayMethod(method);
        receipt.setUserId(user.getEmail());
        receipt.setUserName(user.getName());
        receipt.setBookName(book.getTitle());
        receipt.setTransactionDate(LocalDateTime.now());
        receipt.setTransactionStatus(transaction.getTransactionStatus());
        return receipt;
    }
}
