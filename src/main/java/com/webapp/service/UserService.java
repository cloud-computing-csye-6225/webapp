package com.webapp.service;

import com.amazonaws.services.dynamodbv2.xspec.S;
import com.webapp.dto.UserDto;
import com.webapp.exception.UserNotAuthenticatedException;
import com.webapp.model.User;
import com.webapp.repository.UserRepository;
import com.webapp.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;


import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserService{

    private static final Logger logger = LogManager.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AmazonSNSClient snsClient;

    @Value("${app.base-url}")
    private AppConfig appConfig;
    
    @Value("${SNS_TOPIC_ARN}")
    private String SNS_TOPIC_ARN;





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
        user.setUserVerified(false);
        userRepository.save(user);

        String verificationLink = generateVerificationLink(user);
        publishToSns(email,user.getFirstName(), verificationLink);

        logger.info("User created successfully and SNS message published for verification");
    }

    private String generateVerificationLink(User user) {
        String baseUrl = appConfig.getBaseUrl();
        return baseUrl +"/v1/user/verifyuser/"+user.getId();
    }

    private void publishToSns(String email, String firstName, String verificationLink) {
        try {
            String message = String.format("{\"email\":\"%s\",\"firstName\":\"%s\",\"verificationLink\":\"%s\"}",
                    email, firstName, verificationLink);

            PublishRequest publishRequest = new PublishRequest(SNS_TOPIC_ARN, message);
            PublishResult publishResult = snsClient.publish(publishRequest);

            logger.info("SNS message published successfully. Message ID: {}", publishResult.getMessageId());
        } catch (Exception e) {
            logger.error("Failed to publish message to SNS: {}", e.getMessage(), e);
            throw new RuntimeException("Error publishing to SNS", e);
        }
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
        // Check if email is verified
        if(user.isUserVerified() ==false){
            throw new UserNotAuthenticatedException("Email not verified");
        }

        // Return user after successful authentication
        return user;
    }


    public Boolean validEmail(String email) {
        String emailPattern = "[a-z0-9!#$%&\'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&\'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";
        return email.matches(emailPattern);
    }

    public void verifyUserById(Long id) {
        // Find the user by ID
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        // Check if already verified
        if (user.isUserVerified()) {
            throw new IllegalStateException("User is already verified");
        }

        // Update verification status
        user.setUserVerified(true);
        userRepository.save(user);
    }




}
