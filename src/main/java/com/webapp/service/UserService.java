package com.webapp.service;

import com.webapp.dto.UserDto;
import com.webapp.model.User;
import com.webapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;



    // Create User method
    public void createUser(String email, String firstName, String lastName, String password) {
        logger.info("Checking if email exists {}",email);
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            logger.warn("Email {} already in use",email);
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(password);
        user.setAccountCreated(LocalDateTime.now());
        user.setAccountUpdated(LocalDateTime.now());
        userRepository.save(user);
    }

    // Update User method
    public void updateUser(String email, String firstName, String lastName, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException());


        if (firstName != null || !firstName.isEmpty()) {
            user.setFirstName(firstName);
        }
        if (lastName != null || !lastName.isEmpty() ) {
            user.setLastName(lastName);
        }
        if (password!=null || !password.isEmpty()) {
            user.setPassword(password);
        }
        user.setAccountUpdated(LocalDateTime.now());
        userRepository.save(user);
    }

    // Get user method
    public UserDto getUser(String email, String firstName, String lastName) {
        Optional<User> user = userRepository.findByEmailandOtherFeilds(email, firstName, lastName);
        if (user.isPresent()) {
            return convertToDto(user.get());
        }
        return null;
    }

        private UserDto convertToDto(User user){
            UserDto userDto = new UserDto();
            userDto.setEmail(user.getEmail());
            userDto.setFirstName(user.getFirstName());
            userDto.setLastName(user.getLastName());
            userDto.setAccountCreated(user.getAccountCreated());
            userDto.setAccountUpdated(user.getAccountUpdated());
            return userDto;
        }



    // Delete User method
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        userRepository.delete(user);
    }
}
