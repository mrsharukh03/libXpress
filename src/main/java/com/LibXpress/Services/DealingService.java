package com.LibXpress.Services;

import com.LibXpress.APIResponse.TransactionReceipt;
import com.LibXpress.Entitys.Books;
import com.LibXpress.Entitys.Transaction;
import com.LibXpress.Entitys.User;
import com.LibXpress.Repositorys.BookRepo;
import com.LibXpress.Repositorys.TransactionRepo;
import com.LibXpress.Repositorys.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DealingService {

    final private PaymentService paymentService;
    final private UserRepo userRepo;
    final private BookService bookService;
    final private TransactionRepo transactionRepo;
    final private BookRepo bookRepo;

    @Autowired
    public DealingService(PaymentService paymentService, UserRepo userRepo, BookService bookService, TransactionRepo transactionRepo, BookRepo bookRepo) {
        this.paymentService = paymentService;
        this.userRepo = userRepo;
        this.bookService = bookService;
        this.transactionRepo = transactionRepo;
        this.bookRepo = bookRepo;
    }
    @Transactional
    public ResponseEntity<?> buyBook(Long bookId, String userId, int quantity,String method) {
        Books book = bookService.getBookById(bookId);
        User user = userRepo.findById(userId).orElse(null);

        if(book == null) return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        if(user == null) return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        if(!user.isActiveStatus()) return new ResponseEntity<>("Please Verify email || Phone ",HttpStatus.UPGRADE_REQUIRED);
        if(book.getAvailableQuantity() < quantity) return new ResponseEntity<>("Not enough stock available",HttpStatus.BAD_REQUEST);
        Double ammount = book.getPriceOffline() * quantity;
        boolean paymentSuccess = false;
        if(method.equalsIgnoreCase("upigetway")){
            paymentSuccess = paymentService.payWithUpi(ammount);
        } else if (method.equalsIgnoreCase("wallet")) {
            paymentSuccess = paymentService.payWithWallet(user.getPhone(),ammount);
        }else if(method.equalsIgnoreCase("cash")){
            paymentSuccess = true;
        }else{
            paymentSuccess = false;
        }

        if(paymentSuccess){
            book.setAvailableQuantity(book.getAvailableQuantity() - quantity);
        }
        bookRepo.save(book);
        Transaction transaction = new Transaction();

        transaction.setUser(user);
        transaction.setBook(book);
        transaction.setAmount(ammount);
        transaction.setPayMethod(method);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setTransactionStatus(paymentSuccess ? "completed" : "Failed");

        transactionRepo.save(transaction);

        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setTransactionId(transaction.getId());
        receipt.setTotalAmmout(ammount);
        receipt.setBookPrice(book.getPriceOffline());
        receipt.setQuentity(quantity);
        receipt.setPayMethod(method);
        receipt.setUserId(user.getEmail());
        receipt.setUserName(user.getName());
        receipt.setBookName(book.getTitle());
        receipt.setTransactionDate(LocalDateTime.now());
        receipt.setTransactionStatus(transaction.getTransactionStatus());
        if(!paymentSuccess) return new ResponseEntity<>(receipt,HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(receipt,HttpStatus.OK);
    }
}
