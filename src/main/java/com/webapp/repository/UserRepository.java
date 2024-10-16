package com.webapp.repository;

import com.webapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u from User u WHERE u.email = :email")
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email = :email " +
            "AND (:firstName IS NULL OR u.firstName = :firstName) " +
            "AND (:lastName IS NULL OR u.lastName = :lastName)")
    Optional<User> findByEmailandOtherFeilds(String email, String firstName, String lastName);

}
