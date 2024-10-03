package com.webapp.controller;

import com.webapp.dto.UserDto;
import com.webapp.service.UserService;
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
