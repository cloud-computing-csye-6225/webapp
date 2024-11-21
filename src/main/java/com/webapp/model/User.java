package com.webapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Random;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User {
    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    private long id;

    @Column(unique = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    private String password;
    private String firstName;
    private String lastName;
    @NotNull
    private String username;
    private LocalDateTime accountCreated;
    private LocalDateTime accountUpdated;
    private boolean userVerified;

    @PrePersist
    public void generateUniqueId() {
        if (this.id == 0) {
            this.id = new Random().nextLong();
            if (this.id < 0) {
                this.id = -this.id;
            }
        }
    }


}
