// src/main/java/com/FermeDirecte/FermeDirecte/repository/UserRepository.java
package com.FermeDirecte.FermeDirecte.repository;

import com.FermeDirecte.FermeDirecte.entity.User;
import com.FermeDirecte.FermeDirecte.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByRefreshToken(String refreshToken);
    Optional<User> findByResetToken(String resetToken);
    List<User> findByRole(Role role);
}
