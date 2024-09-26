package com.webapp.healthcheck.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

@RestController
@RequestMapping("/healthz")
public class HeathCheckController {
    private final Logger logger = Logger.getLogger(HeathCheckController.class.getName());
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
            logger.severe("Database connection Failed  \n Error code:\t"+ e.getErrorCode()+"\n Error message:\t"+ e.getMessage()) ;
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .header(HttpHeaders.CACHE_CONTROL,"no-cache","no-store","must-revalidate")
                    .header(HttpHeaders.PRAGMA,"no-cache")
                    .header("X_CONTENT_TYPE_OPTIONS", "nosniff")
                    .build();

        }
    }

    @RequestMapping(method = {RequestMethod.DELETE,RequestMethod.POST,RequestMethod.PATCH,RequestMethod.PUT})
    public ResponseEntity<Void> handleInvalidMethods(){
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .header(HttpHeaders.CACHE_CONTROL,"no-cache","no-store","must-revalidate")
                .header(HttpHeaders.PRAGMA,"no-cache")
                .header("X_CONTENT_TYPE_OPTIONS", "nosniff")
                .build();

    }


}
