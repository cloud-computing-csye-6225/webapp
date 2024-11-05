package com.webapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class WebappApplication {

    private static final Logger logger = LogManager.getLogger(WebappApplication.class);


    // Inject the API key from the properties file
    @Value("${sendgrid.api-key}")
    private String apiKey;

    public static void main(String[] args) {
        logger.info("Application startup - info level");
        logger.debug("Application startup - debug level");
        logger.error("Application startup - error level");
        SpringApplication.run(WebappApplication.class, args);
    }

    // Bean to run the email sending logic after the application starts
    @Bean
    public CommandLineRunner sendStartupEmailRunner() {
        return args -> sendStartupEmail();
    }

    private void sendStartupEmail() {
        String requestUrl = "https://api.sendgrid.com/v3/mail/send";

        // Email content in JSON format with updated recipient
        String emailContent = "{"
                + "\"personalizations\":[{\"to\":[{\"email\":\"grandhidurga.c@northeastern.edu\",\"name\":\"Durga Grandhi\"}],\"subject\":\"Application Instance has Started\"}],"
                + "\"content\": [{\"type\": \"text/plain\", \"value\": \"The application instance has started successfully.\"}],"
                + "\"from\":{\"email\":\"no-reply@mail.cloudwebapplication.com\",\"name\":\"WebApp\"},"
                + "\"reply_to\":{\"email\":\"no-reply@mail.cloudwebapplication.com\",\"name\":\"WebApp\"}"
                + "}";

        try {
            // Set up HTTP connection
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Write JSON data to the request body
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = emailContent.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Execute the request
            int responseCode = conn.getResponseCode();
            if (responseCode == 202) {
                System.out.println("Startup email sent successfully.");
            } else {
                System.out.println("Failed to send startup email. Response code: " + responseCode);
            }
        } catch (Exception e) {
            // Log the error without interrupting the application startup
            System.err.println("Error sending startup email: " + e.getMessage());
        }
    }
}
