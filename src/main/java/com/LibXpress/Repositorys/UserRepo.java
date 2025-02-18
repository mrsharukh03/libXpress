package com.LibXpress.Repositorys;

import com.LibXpress.Entitys.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<User, String> {
    User findUserByPhone(String phone);
}
