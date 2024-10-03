package com.webapp.service;

import com.webapp.dto.UserDto;
import com.webapp.model.User;
import com.webapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;



    // Create User method
    public void createUser(UserDto userDto) {
        logger.info("Checking if email exists {}",userDto.getEmail());
        Optional<User> existingUser = userRepository.findByEmail(userDto.getEmail());
        if (existingUser.isPresent()) {
            logger.warn("Email {} already in use",userDto.getEmail());
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPassword(userDto.getPassword());
        user.setAccountCreated(LocalDateTime.now());
        user.setAccountUpdated(LocalDateTime.now());

        userRepository.save(user);
    }

    // Update User method
    public void updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (userDto.getEmail() != null) {
            throw new IllegalArgumentException("Email cannot be updated");
        }
        if (userDto.getAccountCreated() != null || userDto.getAccountUpdated() != null) {
            throw new IllegalArgumentException("Account created/updated timestamps cannot be modified");
        }
        if (userDto.getFirstName() != null) {
            user.setFirstName(userDto.getFirstName());
        }
        if (userDto.getLastName() != null) {
            user.setLastName(userDto.getLastName());
        }
        if (userDto.getPassword() != null) {
            user.setPassword(userDto.getPassword());
        }

        user.setAccountUpdated(LocalDateTime.now());
        userRepository.save(user);
    }

    // Get user method
    public UserDto getUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return null;
        }

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
