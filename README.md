# webapp

## Overview
This springboot application is implemented for a healthcheck feature.
The application tries making connection with MySQL database.
The responses and status of the connection are checked with the "/healthz" rest endpoint

## Features

- Health Check Endpoint: The "/healthz" endpoint connects to database and returns
- "200 OK' if connection is successful
- "503 Service Unavailable " if the connection is unsuccessful


## Dependencies and Prerequisites
- **Java 17** : Java application version
- **Spring Boot** : Framework for backend services
- **MySQLConnector** : For connection to local mysql database
- **Hibernate** : ORM framework to manage db operations via Spring JPA
- **Maven** : Build module for managing project dependencies

## Building the Application
``mvn clean install``

## Running the Application
To run the application enter the following in the terminal  
``mvn spring-boot:run``

## Usage
To check the application is functioning with proper health check use the following command  
``curl -vvvv http://localhost:8080/healthz``  
It should return  
- **200 OK** : If application is connected to the database
- **503 Service unavailable** : If the application fails to connect to the database
