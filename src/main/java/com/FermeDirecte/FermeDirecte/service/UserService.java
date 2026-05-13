// src/main/java/com/FermeDirecte/FermeDirecte/service/UserService.java
package com.FermeDirecte.FermeDirecte.service;

import com.FermeDirecte.FermeDirecte.dto.address.AddressResponse;
import com.FermeDirecte.FermeDirecte.dto.user.UserProfileRequest;
import com.FermeDirecte.FermeDirecte.dto.user.UserProfileResponse;
import com.FermeDirecte.FermeDirecte.entity.User;
import com.FermeDirecte.FermeDirecte.exception.ResourceNotFoundException;
import com.FermeDirecte.FermeDirecte.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public java.util.List<java.util.Map<String, Object>> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    java.util.Map<String, Object> userMap = new java.util.HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("email", user.getEmail());
                    userMap.put("prenom", user.getPrenom());
                    userMap.put("nom", user.getNom());
                    userMap.put("telephone", user.getTelephone());
                    userMap.put("role", user.getRole());
                    userMap.put("actif", user.getActif());
                    userMap.put("dateInscription", user.getDateCreation());
                    return userMap;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .prenom(user.getPrenom())
                .nom(user.getNom())
                .telephone(user.getTelephone())
                .role(user.getRole())
                .dateCreation(user.getDateCreation())
                .adresses(user.getAdresses().stream()
                        .map(address -> AddressResponse.builder()
                                .id(address.getId())
                                .prenom(address.getPrenom())
                                .nom(address.getNom())
                                .rue(address.getRue())
                                .ville(address.getVille())
                                .codePostal(address.getCodePostal())
                                .pays(address.getPays())
                                .gouvernorat(address.getGouvernorat())
                                .telephone(address.getTelephone())
                                .instructions(address.getInstructions())
                                .principal(address.getPrincipal())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }


    @Transactional
    public UserProfileResponse updateUserById(Long id, UserProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Cet email est déjà utilisé");
            }
            user.setEmail(request.getEmail());
        }
        user.setPrenom(request.getPrenom());
        user.setNom(request.getNom());
        user.setTelephone(request.getTelephone());

        User saved = userRepository.save(user);
        return UserProfileResponse.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .prenom(saved.getPrenom())
                .nom(saved.getNom())
                .telephone(saved.getTelephone())
                .role(saved.getRole())
                .dateCreation(saved.getDateCreation())
                .adresses(java.util.List.of())
                .build();
    }

    @Transactional
    public UserProfileResponse updateProfile(String currentEmail, UserProfileRequest request) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (!currentEmail.equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Cet email est déjà utilisé");
            }
            user.setEmail(request.getEmail());
        }

        user.setPrenom(request.getPrenom());
        user.setNom(request.getNom());
        user.setTelephone(request.getTelephone());

        User savedUser = userRepository.save(user);

        return UserProfileResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .prenom(savedUser.getPrenom())
                .nom(savedUser.getNom())
                .telephone(savedUser.getTelephone())
                .role(savedUser.getRole())
                .dateCreation(savedUser.getDateCreation())
                .adresses(savedUser.getAdresses().stream()
                        .map(address -> AddressResponse.builder()
                                .id(address.getId())
                                .prenom(address.getPrenom())
                                .nom(address.getNom())
                                .rue(address.getRue())
                                .ville(address.getVille())
                                .codePostal(address.getCodePostal())
                                .pays(address.getPays())
                                .gouvernorat(address.getGouvernorat())
                                .telephone(address.getTelephone())
                                .instructions(address.getInstructions())
                                .principal(address.getPrincipal())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
