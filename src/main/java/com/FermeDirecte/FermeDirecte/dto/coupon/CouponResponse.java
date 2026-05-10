// src/main/java/com/FermeDirecte/FermeDirecte/dto/coupon/CouponResponse.java
package com.FermeDirecte.FermeDirecte.dto.coupon;

import com.FermeDirecte.FermeDirecte.enums.CouponScope;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CouponResponse {

    private Long id;
    private String code;
    private String description;

    // Réductions
    private BigDecimal pourcentageReduction;
    private BigDecimal montantFixeReduction;
    private Boolean livraisonGratuite;

    // Conditions
    private BigDecimal montantMinimum;
    private BigDecimal montantMaximumReduction;
    private List<Long> categoriesApplicables;

    // Portée
    private CouponScope scope;
    private Long vendeurId;
    private String vendeurNom;

    // Limites
    private Integer usagesMaxGlobal;
    private Integer usagesActuels;
    private Integer usagesMaxParUtilisateur;

    // Dates
    private LocalDateTime dateDebut;
    private LocalDateTime dateExpiration;
    private LocalDateTime dateCreation;

    // Statut
    private Boolean actif;
    private Boolean bloque;
    private Boolean valide; // Calculé : actif && !bloque && dans les dates && usages disponibles

    // Statistiques
    private BigDecimal montantTotalEconomise;
}
