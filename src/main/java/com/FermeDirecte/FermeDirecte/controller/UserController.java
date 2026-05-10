// src/main/java/com/FermeDirecte/FermeDirecte/controller/UserController.java
package com.FermeDirecte.FermeDirecte.controller;

import com.FermeDirecte.FermeDirecte.dto.user.UserProfileRequest;
import com.FermeDirecte.FermeDirecte.dto.user.UserProfileResponse;
import com.FermeDirecte.FermeDirecte.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * GET /api/users
     * Récupère tous les utilisateurs (Admin uniquement)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * PUT /api/users/{id}
     * Met à jour un utilisateur par son ID (Admin uniquement)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> updateUserById(
            @PathVariable Long id,
            @Valid @RequestBody UserProfileRequest request) {
        return ResponseEntity.ok(userService.updateUserById(id, request));
    }

    /**
     * GET /api/users/profile
     * Récupère le profil de l'utilisateur connecté
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.getProfile(email));
    }

    /**
     * PUT /api/users/profile
     * Met à jour le profil de l'utilisateur connecté
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Valid @RequestBody UserProfileRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.updateProfile(email, request));
    }
}
