// src/main/java/com/FermeDirecte/FermeDirecte/dto/coupon/CouponRequest.java
package com.FermeDirecte.FermeDirecte.dto.coupon;

import com.FermeDirecte.FermeDirecte.enums.CouponScope;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CouponRequest {

    @NotBlank(message = "Le code du coupon est obligatoire")
    @Size(min = 3, max = 50, message = "Le code doit contenir entre 3 et 50 caractères")
    private String code;

    @NotBlank(message = "La description est obligatoire")
    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    private String description;

    // Réductions hybrides (au moins une doit être définie)
    @DecimalMin(value = "0.0", message = "Le pourcentage doit être positif")
    @DecimalMax(value = "100.0", message = "Le pourcentage ne peut pas dépasser 100%")
    private BigDecimal pourcentageReduction;

    @DecimalMin(value = "0.0", message = "Le montant fixe doit être positif")
    private BigDecimal montantFixeReduction;

    @NotNull(message = "Indiquez si la livraison est gratuite")
    private Boolean livraisonGratuite;

    // Conditions
    @DecimalMin(value = "0.0", message = "Le montant minimum doit être positif")
    private BigDecimal montantMinimum;

    @DecimalMin(value = "0.0", message = "Le plafond de réduction doit être positif")
    private BigDecimal montantMaximumReduction;

    private List<Long> categoriesApplicables;

    // Portée
    @NotNull(message = "La portée du coupon est obligatoire")
    private CouponScope scope;

    // ID du vendeur (si scope = SELLER et admin crée pour un vendeur spécifique)
    private Long sellerId;

    // Limites
    @NotNull(message = "Le nombre d'usages maximum est obligatoire")
    @Min(value = 1, message = "Le nombre d'usages doit être au moins 1")
    private Integer usagesMaxGlobal;

    @NotNull(message = "Le nombre d'usages par utilisateur est obligatoire")
    @Min(value = 1, message = "Le nombre d'usages par utilisateur doit être au moins 1")
    private Integer usagesMaxParUtilisateur;

    // Dates
    @NotNull(message = "La date de début est obligatoire")
    private LocalDateTime dateDebut;

    @NotNull(message = "La date d'expiration est obligatoire")
    private LocalDateTime dateExpiration;
}
