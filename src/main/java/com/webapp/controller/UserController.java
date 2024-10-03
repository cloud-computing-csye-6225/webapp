package com.webapp.controller;

import com.webapp.dto.UserDto;
import com.webapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    UserService userService;

    // Create User
    @Operation(summary = "Creating new User",description = "The api takes Json parameters like FirstName, LastName, Email, password as input to create a new user")
    @ApiResponses( {
            @ApiResponse(responseCode = "201",description = "User created Successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request. Invalid user data")

    }
    )
    @PostMapping
    public ResponseEntity<Void> createUser(@RequestBody UserDto userDto){
        try{
            userService.createUser(userDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header(HttpHeaders.CACHE_CONTROL,"no-cache","no-store","must-revalidate")
                    .header(HttpHeaders.PRAGMA,"no-cache")
                    .header("X_CONTENT_TYPE_OPTIONS", "nosniff")
                    .build();
        }
        catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.OK)
                    .header(HttpHeaders.CACHE_CONTROL,"no-cache","no-store","must-revalidate")
                    .header(HttpHeaders.PRAGMA,"no-cache")
                    .header("X_CONTENT_TYPE_OPTIONS", "nosniff")
                    .build();

        }
    }

    //Update user
    @Operation(summary = "Update Existing user by Id",description = "The api takes Json parameters like FirstName, LastName, Email, with id passed as input to update the details of an existing user")
    @ApiResponses( {
            @ApiResponse(responseCode = "200",description = "User details updated Successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request. Invalid user data")

    }
    )
    @PutMapping("/byId/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable Long id,@RequestBody UserDto userDto){
       try{
           userService.updateUser(id,userDto);
           return ResponseEntity.status(HttpStatus.OK)
                   .header(HttpHeaders.CACHE_CONTROL,"no-cache","no-store","must-revalidate")
                   .header(HttpHeaders.PRAGMA,"no-cache")
                   .header("X_CONTENT_TYPE_OPTIONS", "nosniff")
                   .build();
       }
       catch (IllegalArgumentException e){
           return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                   .header(HttpHeaders.CACHE_CONTROL,"no-cache","no-store","must-revalidate")
                   .header(HttpHeaders.PRAGMA,"no-cache")
                   .header("X_CONTENT_TYPE_OPTIONS", "nosniff")
                   .build();

       }

    }
    //  Get User
    @Operation(summary = "Get Existing user by Id",description = "The api takes Json parameters like id passed as input to fetch the details of an existing user")
    @ApiResponses( {
            @ApiResponse(responseCode = "200",description = "User details updated Successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request. Invalid user data")

    }
    )
    @GetMapping("/byId/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        UserDto userDto = userService.getUser(id);
        if (userDto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CACHE_CONTROL,"no-cache","no-store","must-revalidate")
                    .header(HttpHeaders.PRAGMA,"no-cache")
                    .header("X_CONTENT_TYPE_OPTIONS", "nosniff")
                    .build();
        }
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.CACHE_CONTROL,"no-cache","no-store","must-revalidate")
                .header(HttpHeaders.PRAGMA,"no-cache")
                .header("X_CONTENT_TYPE_OPTIONS", "nosniff")
                .body(userDto);
    }

    // delete User
    @Operation(summary = "Delete user by Id",description = "The api takes Json parameters like id passed as input to delete the details of an existing user after finding a match")
    @ApiResponses( {
            @ApiResponse(responseCode = "200",description = "User details deleted Successfully"),
            @ApiResponse(responseCode = "404", description = "User data not found")

    }
    )
    @DeleteMapping("/byId/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.status(HttpStatus.OK)
                    .header(HttpHeaders.CACHE_CONTROL,"no-cache","no-store","must-revalidate")
                    .header(HttpHeaders.PRAGMA,"no-cache")
                    .header("X_CONTENT_TYPE_OPTIONS", "nosniff")
                    .build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CACHE_CONTROL,"no-cache","no-store","must-revalidate")
                    .header(HttpHeaders.PRAGMA,"no-cache")
                    .header("X_CONTENT_TYPE_OPTIONS", "nosniff")
                    .build();
        }
    }
}
