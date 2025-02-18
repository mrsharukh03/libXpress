package com.LibXpress.Repositorys;

import com.LibXpress.Entitys.BorrowedBooks;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BorrowedRepo extends JpaRepository<BorrowedBooks, Long> {
}
