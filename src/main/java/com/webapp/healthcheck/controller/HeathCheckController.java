package com.webapp.healthcheck.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RestController
@RequestMapping("/healthz")
public class HeathCheckController {

    @Autowired
    private DataSource dataSource;

    @GetMapping
    ResponseEntity<Void> healthCheck(@RequestBody(required = false) String body){
        if(body!=null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header(HttpHeaders.CACHE_CONTROL,"no-cache","no-store","must-revalidate")
                    .header(HttpHeaders.PRAGMA,"no-cache")
                    .header("X_CONTENT_TYPE_OPTIONS", "nosniff")
                    .build();
        }
        try(Connection connection = dataSource.getConnection()) {
            if(connection.isValid(2) ){
                return ResponseEntity.status(HttpStatus.OK)
                        .header(HttpHeaders.CACHE_CONTROL,"no-cache","no-store","must-revalidate")
                        .header(HttpHeaders.PRAGMA,"no-cache")
                        .header("X_CONTENT_TYPE_OPTIONS", "nosniff")
                        .build();
            }
            else{
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .header(HttpHeaders.CACHE_CONTROL,"no-cache","no-store","must-revalidate")
                        .header(HttpHeaders.PRAGMA,"no-cache")
                        .header("X_CONTENT_TYPE_OPTIONS", "nosniff")
                        .build();
            }

        }
        catch (SQLException e){
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .header(HttpHeaders.CACHE_CONTROL,"no-cache","no-store","must-revalidate")
                    .header(HttpHeaders.PRAGMA,"no-cache")
                    .header("X_CONTENT_TYPE_OPTIONS", "nosniff")
                    .build();

        }
    }


}
