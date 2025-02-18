package com.LibXpress.Repositorys;

import com.LibXpress.Entitys.Books;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookRepo extends JpaRepository<Books, Long> {

    // Custom query for searching across title, author, and category (without language)
    Page<Books> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrCategoryContainingIgnoreCase(
            String keyword, String keyword1, String keyword2, Pageable pageable);
}
