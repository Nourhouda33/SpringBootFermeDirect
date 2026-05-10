// src/main/java/com/FermeDirecte/FermeDirecte/service/SellerProfileService.java
package com.FermeDirecte.FermeDirecte.service;

import com.FermeDirecte.FermeDirecte.dto.seller.*;
import com.FermeDirecte.FermeDirecte.entity.SellerProfile;
import com.FermeDirecte.FermeDirecte.entity.User;
import com.FermeDirecte.FermeDirecte.exception.ResourceNotFoundException;
import com.FermeDirecte.FermeDirecte.repository.SellerProfileRepository;
import com.FermeDirecte.FermeDirecte.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerProfileService {

    private final SellerProfileRepository sellerProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public SellerProfileResponse creerOuModifier(SellerProfileRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User avec email " + email + " introuvable")
                );

        SellerProfile profile = sellerProfileRepository.findByUser_Email(email)
                .orElse(SellerProfile.builder()
                        .user(user)
                        .build()
                );

        profile.setNomBoutique(request.getNomBoutique());
        profile.setDescription(request.getDescription());
        profile.setLogo(request.getLogo());

        return toResponse(sellerProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public SellerProfileResponse getMonProfil(String email) {

        SellerProfile profile = sellerProfileRepository.findByUser_Email(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("SellerProfile pour email " + email + " introuvable")
                );

        return toResponse(profile);
    }

    @Transactional(readOnly = true)
    public SellerProfileResponse getById(Long id) {

        SellerProfile profile = sellerProfileRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("SellerProfile", id)
                );

        return toResponse(profile);
    }

    private SellerProfileResponse toResponse(SellerProfile s) {

        return SellerProfileResponse.builder()
                .id(s.getId())
                .nomBoutique(s.getNomBoutique())
                .description(s.getDescription())
                .logo(s.getLogo())
                .note(s.getNote())
                .emailVendeur(s.getUser().getEmail())
                .build();
    }
}