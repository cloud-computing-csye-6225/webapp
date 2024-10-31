package com.webapp.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ResponseHandler {

    private static HttpHeaders getDefaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.setPragma("no-cache");
        headers.add("X-Content-Type-Options", "nosniff");
        return headers;
    }

    public static ResponseEntity<Map<String, String>> generateResponse(String message, HttpStatus status) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return ResponseEntity.status(status)
                .headers(getDefaultHeaders())
                .body(response);
    }

    public static ResponseEntity<Map<String, String>> generateErrorResponse(String errorMessage, HttpStatus status) {
        Map<String, String> response = new HashMap<>();
        response.put("error", errorMessage);
        return ResponseEntity.status(status)
                .headers(getDefaultHeaders())
                .body(response);
    }

    public static <T> ResponseEntity<T> generateSuccessResponse(T body, HttpStatus status) {
        return ResponseEntity.status(status)
                .headers(getDefaultHeaders())
                .body(body);
    }
}
