package com.LibXpress.Repositorys;

import com.LibXpress.DTOs.UserDTO.UserRegistrationDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserRegistationRepo extends JpaRepository<UserRegistrationDTO,String> {

    List<UserRegistrationDTO> findByRegistrationDateBefore(LocalDate sevenDaysAgo);
}
