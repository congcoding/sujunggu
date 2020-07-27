package com.sujunggu.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE period = 'h'")
    List<User> findUserHourly();

    @Query("SELECT u FROM User u WHERE period = 'd'")
    List<User> findUserDaily();
}
