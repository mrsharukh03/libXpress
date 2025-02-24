package com.LibXpress.Repositorys;

import com.LibXpress.Entitys.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepo extends JpaRepository<Transaction,Long> {
    List<Transaction> findByUserEmail(String userId);
}
