// src/main/java/com/FermeDirecte/FermeDirecte/entity/Coupon.java
package com.FermeDirecte.FermeDirecte.entity;

import com.FermeDirecte.FermeDirecte.enums.CouponScope;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "coupons")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Coupon {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 500)
    private String description;

    // Réductions hybrides
    @Column(precision = 5, scale = 2)
    private BigDecimal pourcentageReduction; // Ex: 20.00 pour 20%

    @Column(precision = 19, scale = 2)
    private BigDecimal montantFixeReduction; // Ex: 5.00 DT

    @Column(nullable = false)
    @Builder.Default
    private Boolean livraisonGratuite = false;

    // Conditions d'application
    @Column(precision = 19, scale = 2)
    private BigDecimal montantMinimum; // Montant minimum de commande

    @Column(precision = 19, scale = 2)
    private BigDecimal montantMaximumReduction; // Plafond de réduction

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "coupon_categories", joinColumns = @JoinColumn(name = "coupon_id"))
    @Column(name = "category_id")
    @Builder.Default
    private List<Long> categoriesApplicables = new ArrayList<>(); // Si vide = toutes catégories

    // Portée et propriétaire
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponScope scope; // GLOBAL (admin) ou SELLER (vendeur)

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seller_id")
    private User vendeur; // Null si scope = GLOBAL

    // Limites d'utilisation
    @Column(nullable = false)
    private Integer usagesMaxGlobal; // Nombre total d'utilisations autorisées

    @Column(nullable = false)
    @Builder.Default
    private Integer usagesActuels = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer usagesMaxParUtilisateur = 1; // Limite par utilisateur

    // Dates
    @Column(nullable = false)
    private LocalDateTime dateDebut;

    @Column(nullable = false)
    private LocalDateTime dateExpiration;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dateCreation;

    // Statut
    @Column(nullable = false)
    @Builder.Default
    private Boolean actif = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean bloque = false; // Admin peut bloquer un coupon

    // Relations
    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CouponUsage> utilisations = new ArrayList<>();

    // Méthodes utilitaires
    public boolean isValide() {
        LocalDateTime now = LocalDateTime.now();
        return actif && !bloque 
            && now.isAfter(dateDebut) 
            && now.isBefore(dateExpiration)
            && usagesActuels < usagesMaxGlobal;
    }

    public boolean isApplicableToCategory(Long categoryId) {
        return categoriesApplicables.isEmpty() || categoriesApplicables.contains(categoryId);
    }
}
