package com.webapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI defineOpenApi() {

        Server server = new Server();
        server.setUrl("http://localhost:8080");
        server.setDescription("Development Environment");


        Contact myContact = new Contact();
        myContact.setName("Chakradhar Grandhi");
        myContact.setEmail("grandhidurga.c@northeastern.edu");

        Info information = new Info()
                .title("User Management System API")
                .version("1.0")
                .description("This API exposes endpoints to manage users.")
                .contact(myContact);


        return new OpenAPI().info(information).servers(List.of(server));
    }
}
