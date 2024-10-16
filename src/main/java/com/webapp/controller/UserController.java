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
import java.util.Map;
import java.util.NoSuchElementException;

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
    public ResponseEntity<Void> createUser(@RequestBody Map<String, Object> requestBody){
        try{
            if(!isValidRequest(requestBody)){
                throw new IllegalArgumentException("Improper Request");
            }

            String email = (String) requestBody.get("email");
            String firstName = (String) requestBody.get("firstname");
            String lastName = (String) requestBody.get("lastname");
            String password = (String) requestBody.get("password");
            if(email == null || firstName == null || lastName ==null || password ==null){
                throw new IllegalArgumentException("Missing parameters");
            }

            userService.createUser(email,firstName,lastName,password);
            return ResponseEntity.status(HttpStatus.CREATED)
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
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
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
    @PutMapping()
    public ResponseEntity<Void> updateUser(@RequestBody Map<String,Object> requestBody){
       try{
           if (!isValidRequest(requestBody)) {
               throw new IllegalArgumentException();
           }
           String email = (String) requestBody.get("email");
           String firstName = (String) requestBody.get("firstname");
           String lastName = (String) requestBody.get("lastname");
           String password = (String) requestBody.get("password");


           userService.updateUser(email,firstName,lastName,password);
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
       catch (NoSuchElementException e){
           return ResponseEntity.status(HttpStatus.NOT_FOUND)
                   .header(HttpHeaders.CACHE_CONTROL,"no-cache","no-store","must-revalidate")
                   .header(HttpHeaders.PRAGMA,"no-cache")
                   .header("X_CONTENT_TYPE_OPTIONS", "nosniff")
                   .build();

       }
       catch (Exception e){
           return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
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
    @GetMapping()
    public ResponseEntity<UserDto> getUser(@RequestBody Map<String,Object> requestBody) {
        try{
        if (!isValidRequest(requestBody)) {
            throw new IllegalArgumentException();
        }

        String email = (String) requestBody.get("email");
        String firstName = (String) requestBody.get("firstname");
        String lastName = (String) requestBody.get("lastname");

        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException();

        }
        UserDto foundUser = userService.getUser(email, firstName, lastName);
        if (foundUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache", "no-store", "must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .header("X_CONTENT_TYPE_OPTIONS", "nosniff")
                    .build();
        }

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache", "no-store", "must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header("X_CONTENT_TYPE_OPTIONS", "nosniff")
                .body(foundUser);
    }
        catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache", "no-store", "must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .header("X_CONTENT_TYPE_OPTIONS", "nosniff")
                    .build();

        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache", "no-store", "must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .header("X_CONTENT_TYPE_OPTIONS", "nosniff")
                    .build();
        }

    }

    // delete User
    @Operation(summary = "Delete user by Id",description = "The api takes Json parameters like id passed as input to delete the details of an existing user after finding a match")
    @ApiResponses( {
            @ApiResponse(responseCode = "200",description = "User details deleted Successfully"),
            @ApiResponse(responseCode = "404", description = "User data not found")

    }
    )
    @DeleteMapping("/{id}")
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

    @RequestMapping(method = {RequestMethod.PATCH,RequestMethod.HEAD,RequestMethod.OPTIONS})
    public ResponseEntity<Void> handleInvalidMethods(){
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .header(HttpHeaders.CACHE_CONTROL,"no-cache","no-store","must-revalidate")
                .header(HttpHeaders.PRAGMA,"no-cache")
                .header("X_CONTENT_TYPE_OPTIONS", "nosniff")
                .build();

    }

    private boolean isValidRequest(Map<String,Object> requestBody){
        for(String key: requestBody.keySet()){
            if(!key.equals("email") && !key.equals("firstname") && !key.equals("lastname") && !key.equals("password")){
                return false;
            }
        }
        return true;
    }
}
