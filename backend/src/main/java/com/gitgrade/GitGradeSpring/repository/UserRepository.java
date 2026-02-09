package com.gitgrade.GitGradeSpring.repository;

import com.gitgrade.GitGradeSpring.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository<Entity, ID_Type>
public interface UserRepository extends JpaRepository<User, Long> {

    // Equivalent to: SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);
}