package com.webapp.controller;

import com.webapp.dto.UserDto;
import com.webapp.exception.UserNotAuthenticatedException;
import com.webapp.model.User;
import com.webapp.service.UserService;
import com.webapp.utils.ResponseHandler;
import com.timgroup.statsd.StatsDClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/v1/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private StatsDClient statsDClient;

    private static final Logger logger = LogManager.getLogger(UserController.class);

    @Operation(summary = "Create a new user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Improper request or email already in use"),
            @ApiResponse(responseCode = "503", description = "Service unavailable")
    })
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> requestBody) {
        long startTime = System.currentTimeMillis();
        statsDClient.incrementCounter("api.user.create.count");

        try {
            if (!isValidRequest(requestBody)) {
                return ResponseHandler.generateErrorResponse("Improper Request", HttpStatus.BAD_REQUEST);
            }

            String email = (String) requestBody.get("email");
            String firstName = (String) requestBody.get("firstname");
            String lastName = (String) requestBody.get("lastname");
            String password = (String) requestBody.get("password");

            if (email == null || firstName == null || lastName == null || password == null) {
                return ResponseHandler.generateErrorResponse("Missing parameters", HttpStatus.BAD_REQUEST);
            }

            userService.createUser(email, firstName, lastName, password);

            long duration = System.currentTimeMillis() - startTime;
            statsDClient.recordExecutionTime("api.user.create.duration", duration);
            return ResponseHandler.generateResponse("User created successfully", HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request or email already in use: {}", e.getMessage());
            return ResponseHandler.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage());
            return ResponseHandler.generateErrorResponse("Service unavailable", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @Operation(summary = "Update an existing user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User details updated successfully"),
            @ApiResponse(responseCode = "400", description = "Improper request"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "503", description = "Service unavailable")
    })
    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody Map<String, Object> requestBody) {
        long startTime = System.currentTimeMillis();
        statsDClient.incrementCounter("api.user.update.count");

        try {
            if (!isValidRequest(requestBody)) {
                return ResponseHandler.generateErrorResponse("Improper Request", HttpStatus.BAD_REQUEST);
            }

            String email = (String) requestBody.get("email");
            String firstName = (String) requestBody.get("firstname");
            String lastName = (String) requestBody.get("lastname");
            String password = (String) requestBody.get("password");

            userService.updateUser(email, firstName, lastName, password);

            long duration = System.currentTimeMillis() - startTime;
            statsDClient.recordExecutionTime("api.user.update.duration", duration);
            return ResponseHandler.generateResponse("User details updated successfully", HttpStatus.OK);

        } catch (NoSuchElementException e) {
            logger.warn("User not found for update: {}", e.getMessage());
            return ResponseHandler.generateErrorResponse("User not found", HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid data provided: {}", e.getMessage());
            return ResponseHandler.generateErrorResponse("Invalid user data", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage());
            return ResponseHandler.generateErrorResponse("Service unavailable", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @Operation(summary = "Retrieve an existing user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User details retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Improper request"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "503", description = "Service unavailable")
    })
    @GetMapping
    public ResponseEntity<?> getUser(@RequestBody Map<String, Object> requestBody,
                                     @RequestHeader(value = "Authorization") String authorization) {
        long startTime = System.currentTimeMillis();
        statsDClient.incrementCounter("api.user.get.count");

        try {
            if (!isValidRequest(requestBody)) {
                return ResponseHandler.generateErrorResponse("Improper Request", HttpStatus.BAD_REQUEST);
            }

            User foundUser = userService.getUserByAuthorization(authorization);

            long duration = System.currentTimeMillis() - startTime;
            statsDClient.recordExecutionTime("api.user.get.duration", duration);
            return ResponseHandler.generateSuccessResponse(foundUser, HttpStatus.OK);

        } catch (UserNotAuthenticatedException e) {
            logger.warn("Unauthorized access attempt: {}", e.getMessage());
            return ResponseHandler.generateErrorResponse("Unauthorized access", HttpStatus.UNAUTHORIZED);
        } catch (NoSuchElementException e) {
            logger.warn("User not found: {}", e.getMessage());
            return ResponseHandler.generateErrorResponse("User not found", HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request data: {}", e.getMessage());
            return ResponseHandler.generateErrorResponse("Invalid request data", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error retrieving user: {}", e.getMessage());
            return ResponseHandler.generateErrorResponse("Service unavailable", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    // Handle invalid methods
    @RequestMapping(method = {RequestMethod.PATCH, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.DELETE})
    public ResponseEntity<?> handleInvalidMethods() {
        return ResponseHandler.generateErrorResponse("Method not allowed", HttpStatus.METHOD_NOT_ALLOWED);
    }

    // Helper method for request validation
    private boolean isValidRequest(Map<String, Object> requestBody) {
        for (String key : requestBody.keySet()) {
            if (!key.equals("email") && !key.equals("firstname") && !key.equals("lastname") && !key.equals("password")) {
                return false;
            }
        }
        return true;
    }
}
