package com.LibXpress.Services;

import com.LibXpress.DTOs.UserDTO.UserRegistrationDTO;
import com.LibXpress.Repositorys.UserRegistationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BackgroundWork {

    @Autowired
    private UserRegistationRepo userRepository;

    // This method will run every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    @Async
    public void deleteOldUsers() {
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(3);

        List<UserRegistrationDTO> usersToDelete = userRepository.findByRegistrationDateBefore(sevenDaysAgo);

        // Deleting old users
        for (UserRegistrationDTO user : usersToDelete) {
            userRepository.delete(user);
        }

        // Optional: Print the number of deleted users
        System.out.println("Deleted " + usersToDelete.size() + " users who were registered more than 7 days ago.");
    }
}
