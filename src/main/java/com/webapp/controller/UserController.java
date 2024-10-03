package com.webapp.controller;

import com.webapp.dto.UserDto;
import com.webapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        catch (IllegalArgumentException e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        }
    }
    
}
