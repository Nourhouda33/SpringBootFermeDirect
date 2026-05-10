// src/main/java/com/FermeDirecte/FermeDirecte/service/ReviewService.java
package com.FermeDirecte.FermeDirecte.service;

import com.FermeDirecte.FermeDirecte.dto.review.*;
import com.FermeDirecte.FermeDirecte.entity.*;
import com.FermeDirecte.FermeDirecte.exception.BadRequestException;
import com.FermeDirecte.FermeDirecte.exception.ResourceNotFoundException;
import com.FermeDirecte.FermeDirecte.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public ReviewResponse poster(ReviewRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User avec email " + email + " introuvable"));

        Product produit = productRepository.findById(request.getProduitId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProduitId()));

        // Vérifier achat produit
        boolean aAchete = orderRepository.findByClient_Id(user.getId()).stream()
                .flatMap(o -> o.getLignes().stream())
                .anyMatch(item -> item.getProduit().getId().equals(request.getProduitId()));

        if (!aAchete) {
            throw new BadRequestException("Vous devez acheter ce produit avant de laisser un avis");
        }

        // Vérifier doublon review
        if (reviewRepository.existsByClient_IdAndProduit_Id(user.getId(), request.getProduitId())) {
            throw new BadRequestException("Vous avez déjà laissé un avis pour ce produit");
        }

        Review review = Review.builder()
                .client(user)
                .produit(produit)
                .note(request.getNote())
                .commentaire(request.getCommentaire())
                .approuve(true)   // Visible immédiatement — l'admin peut désactiver ensuite
                .build();

        return toResponse(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getParProduit(Long produitId) {

        return reviewRepository.findByProduit_IdAndApprouveTrue(produitId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewResponse approuver(Long id) {

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", id));

        review.setApprouve(true);

        return toResponse(reviewRepository.save(review));
    }

    // Récupérer TOUS les avis pour l'admin (actifs + désactivés)
    @Transactional(readOnly = true)
    public List<ReviewResponse> getAll() {
        return reviewRepository.findAllByOrderByDateCreationDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Désactiver un avis (le masquer du public)
    @Transactional
    public ReviewResponse desactiver(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", id));
        review.setApprouve(false);
        return toResponse(reviewRepository.save(review));
    }

    // Supprimer un avis
    @Transactional
    public void supprimer(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", id));
        reviewRepository.delete(review);
    }

    private ReviewResponse toResponse(Review r) {

        return ReviewResponse.builder()
                .id(r.getId())
                .nomClient(r.getClient().getPrenom() + " " + r.getClient().getNom())
                .nomProduit(r.getProduit().getNom())
                .produitId(r.getProduit().getId())
                .note(r.getNote())
                .commentaire(r.getCommentaire())
                .dateCreation(r.getDateCreation())
                .approuve(r.getApprouve())
                .build();
    }
}