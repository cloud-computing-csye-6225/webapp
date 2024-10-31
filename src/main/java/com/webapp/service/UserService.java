package com.webapp.service;

import com.webapp.dto.UserDto;
import com.webapp.exception.UserNotAuthenticatedException;
import com.webapp.model.User;
import com.webapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserService{

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;





    // Create User method
    public void createUser(String email, String firstName, String lastName, String password) {
        logger.info("Checking if email exists {}",email);
        User existingUser = userRepository.findByEmail(email);
        if (existingUser!=null) {
            logger.warn("Email {} already in use",email);
            throw new IllegalArgumentException("Email is already in use");
        }
        User user = new User();
        user.setEmail(email);
        user.setUsername(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(passwordEncoder.encode(password));
        user.setAccountCreated(LocalDateTime.now());
        user.setAccountUpdated(LocalDateTime.now());
        userRepository.save(user);
    }

    // Update User method
    public void updateUser(String email, String firstName, String lastName, String password) {
        User user = userRepository.findByEmail(email);

        if(user==null) {
            throw new NoSuchElementException();
        }


        if (firstName != null || !firstName.isEmpty()) {
            user.setFirstName(firstName);
        }
        if (lastName != null || !lastName.isEmpty() ) {
            user.setLastName(lastName);
        }
        if (password!=null || !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
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


    public User getUserByAuthorization(String authorization) {
        if (authorization == null || !authorization.startsWith("Basic ")) {
            throw new UserNotAuthenticatedException("Authorization header is missing or invalid");
        }

        // Remove "Basic " prefix and decode credentials
        String base64Credentials = authorization.substring("Basic ".length()).trim();
        String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);

        // Split the credentials into username and password
        final String[] values = credentials.split(":", 2);
        if (values.length < 2) {
            throw new UserNotAuthenticatedException("Invalid Authorization format");
        }

        String email = values[0];
        String password = values[1];

        // Fetch user by email (username)
        User user = userRepository.findUserByUsername(email);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new UserNotAuthenticatedException("Invalid username or password");
        }

        // Return user after successful authentication
        return user;
    }


    public Boolean validEmail(String email) {
        String emailPattern = "[a-z0-9!#$%&\'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&\'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";
        return email.matches(emailPattern);
    }


}
