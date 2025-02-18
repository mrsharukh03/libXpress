package com.LibXpress.Services;

import com.LibXpress.DTOs.TransactionDTO.TransactionDTO;
import com.LibXpress.Entitys.Transaction;
import com.LibXpress.Repositorys.TransactionRepo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransationService {

    private final TransactionRepo transactionRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
     TransationService(TransactionRepo transactionRepo) {
        this.transactionRepo = transactionRepo;
    }

    public ResponseEntity<?> getAllTransistions() {
        List<Transaction> allTransistions = transactionRepo.findAll();

        if(allTransistions.isEmpty()){
            allTransistions = new ArrayList<>();
            return new ResponseEntity<>(allTransistions, HttpStatus.NOT_FOUND);
        }

        List<TransactionDTO> transactionDTOs = new ArrayList<>();
        for (Transaction transaction : allTransistions) {
            TransactionDTO transactionDTO = modelMapper.map(transaction, TransactionDTO.class);
            transactionDTO.setBookId(transaction.getBook().getId());
            transactionDTO.setUserId(transaction.getUser().getEmail());

            transactionDTOs.add(transactionDTO);
        }
        return new ResponseEntity<>(transactionDTOs,HttpStatus.OK);
    }
}
