package com.LibXpress.Repositorys;

import com.LibXpress.Entitys.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepo extends JpaRepository<Transaction,Long> {
}
