package com.FermeDirecte.FermeDirecte.controller;

import com.FermeDirecte.FermeDirecte.dto.review.ReviewRequest;
import com.FermeDirecte.FermeDirecte.dto.review.ReviewResponse;
import com.FermeDirecte.FermeDirecte.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // POST /api/reviews — Poster un avis (CUSTOMER, achat vérifié)
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReviewResponse> posterAvis(
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.poster(request, userDetails.getUsername()));
    }

    // GET /api/reviews/product/{productId} — Avis actifs d'un produit (public)
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponse>> getAvisParProduit(
            @PathVariable Long productId) {

        return ResponseEntity.ok(reviewService.getParProduit(productId));
    }

    // GET /api/reviews — Tous les avis pour l'admin
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReviewResponse>> getAllAvis() {
        return ResponseEntity.ok(reviewService.getAll());
    }

    // PUT /api/reviews/{id}/activate — Réactiver un avis (ADMIN)
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewResponse> activerAvis(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.approuver(id));
    }

    // PUT /api/reviews/{id}/deactivate — Désactiver un avis (ADMIN)
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewResponse> desactiverAvis(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.desactiver(id));
    }

    // DELETE /api/reviews/{id} — Supprimer un avis (ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supprimerAvis(@PathVariable Long id) {
        reviewService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
