// src/main/java/com/FermeDirecte/FermeDirecte/service/CouponService.java
package com.FermeDirecte.FermeDirecte.service;

import com.FermeDirecte.FermeDirecte.dto.coupon.*;
import com.FermeDirecte.FermeDirecte.entity.*;
import com.FermeDirecte.FermeDirecte.enums.CouponScope;
import com.FermeDirecte.FermeDirecte.enums.Role;
import com.FermeDirecte.FermeDirecte.repository.CouponRepository;
import com.FermeDirecte.FermeDirecte.repository.CouponUsageRepository;
import com.FermeDirecte.FermeDirecte.repository.UserRepository;
import com.FermeDirecte.FermeDirecte.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;

    // ==================== CRÉATION ====================

    @Transactional
    public CouponResponse createCoupon(CouponRequest request, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Validation des permissions
        if (request.getScope() == CouponScope.GLOBAL && user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Seuls les admins peuvent créer des coupons globaux");
        }

        if (request.getScope() == CouponScope.SELLER && user.getRole() != Role.SELLER && user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Seuls les vendeurs et admins peuvent créer des coupons boutique");
        }

        // Vérifier que le code n'existe pas déjà
        if (couponRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new RuntimeException("Ce code coupon existe déjà");
        }

        // Validation : au moins une réduction doit être définie
        if ((request.getPourcentageReduction() == null || request.getPourcentageReduction().compareTo(BigDecimal.ZERO) == 0)
            && (request.getMontantFixeReduction() == null || request.getMontantFixeReduction().compareTo(BigDecimal.ZERO) == 0)
            && !request.getLivraisonGratuite()) {
            throw new RuntimeException("Au moins une réduction doit être définie");
        }

        // Validation des dates
        if (request.getDateExpiration().isBefore(request.getDateDebut())) {
            throw new RuntimeException("La date d'expiration doit être après la date de début");
        }

        // Déterminer le vendeur
        User vendeur = null;
        if (request.getScope() == CouponScope.SELLER) {
            if (request.getSellerId() != null && user.getRole() == Role.ADMIN) {
                // Admin crée un coupon pour un vendeur spécifique
                vendeur = userRepository.findById(request.getSellerId())
                    .orElseThrow(() -> new RuntimeException("Vendeur non trouvé"));
                if (vendeur.getRole() != Role.SELLER) {
                    throw new RuntimeException("L'utilisateur spécifié n'est pas un vendeur");
                }
            } else {
                // Vendeur crée son propre coupon
                vendeur = user;
            }
        }

        Coupon coupon = Coupon.builder()
            .code(request.getCode().toUpperCase())
            .description(request.getDescription())
            .pourcentageReduction(request.getPourcentageReduction())
            .montantFixeReduction(request.getMontantFixeReduction())
            .livraisonGratuite(request.getLivraisonGratuite())
            .montantMinimum(request.getMontantMinimum())
            .montantMaximumReduction(request.getMontantMaximumReduction())
            .categoriesApplicables(request.getCategoriesApplicables() != null ? request.getCategoriesApplicables() : List.of())
            .scope(request.getScope())
            .vendeur(vendeur)
            .usagesMaxGlobal(request.getUsagesMaxGlobal())
            .usagesMaxParUtilisateur(request.getUsagesMaxParUtilisateur())
            .dateDebut(request.getDateDebut())
            .dateExpiration(request.getDateExpiration())
            .actif(true)
            .bloque(false)
            .build();

        coupon = couponRepository.save(coupon);
        return mapToResponse(coupon);
    }

    // ==================== LECTURE ====================

    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<CouponResponse> getCouponsForSeller(String email) {
        User seller = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return couponRepository.findByScopeAndVendeurId(CouponScope.SELLER, seller.getId()).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<SellerResponse> getAllSellers() {
        return userRepository.findByRole(Role.SELLER).stream()
            .map(seller -> SellerResponse.builder()
                .id(seller.getId())
                .nom(seller.getNom())
                .prenom(seller.getPrenom())
                .email(seller.getEmail())
                .nomComplet(seller.getPrenom() + " " + seller.getNom())
                .build())
            .collect(Collectors.toList());
    }

    public List<CouponResponse> getGlobalCoupons() {
        return couponRepository.findByScope(CouponScope.GLOBAL).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public CouponResponse getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Coupon non trouvé"));
        return mapToResponse(coupon);
    }

    // ==================== VALIDATION & CALCUL ====================

    @Transactional(readOnly = true)
    public CouponValidationResponse validateCoupon(String code, String email, BigDecimal sousTotal,
                                                     BigDecimal fraisLivraison, List<Long> categoryIds) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
            .orElseThrow(() -> new RuntimeException("Code coupon invalide"));

        // Vérifications de base
        if (!coupon.isValide()) {
            return CouponValidationResponse.builder()
                .valide(false)
                .message(getInvalidReason(coupon))
                .build();
        }

        // Vérifier le montant minimum
        if (coupon.getMontantMinimum() != null && sousTotal.compareTo(coupon.getMontantMinimum()) < 0) {
            return CouponValidationResponse.builder()
                .valide(false)
                .message("Montant minimum de " + coupon.getMontantMinimum() + " DT requis")
                .build();
        }

        // Vérifier les catégories — si le coupon a des restrictions,
        // on récupère les catégories réelles du panier depuis la base de données
        if (!coupon.getCategoriesApplicables().isEmpty()) {
            // Récupérer les catégories réelles des produits du panier de l'utilisateur
            List<Long> cartCategoryIds = cartRepository.findByUser_Id(user.getId())
                .map(cart -> cart.getLignes().stream()
                    .flatMap(item -> item.getProduit().getCategories().stream())
                    .map(category -> category.getId())
                    .distinct()
                    .collect(java.util.stream.Collectors.toList()))
                .orElse(java.util.List.of());

            // Fusionner avec les categoryIds envoyés par le frontend (au cas où)
            List<Long> allCategoryIds = new java.util.ArrayList<>(cartCategoryIds);
            if (categoryIds != null) {
                categoryIds.stream()
                    .filter(id -> !allCategoryIds.contains(id))
                    .forEach(allCategoryIds::add);
            }

            boolean hasApplicableCategory = allCategoryIds.stream()
                .anyMatch(coupon::isApplicableToCategory);

            if (!hasApplicableCategory) {
                return CouponValidationResponse.builder()
                    .valide(false)
                    .message("Ce coupon ne s'applique pas aux produits de votre panier")
                    .build();
            }
        }

        // Vérifier les utilisations par utilisateur
        int usageCount = couponUsageRepository.countByCouponIdAndUtilisateurId(coupon.getId(), user.getId());
        if (usageCount >= coupon.getUsagesMaxParUtilisateur()) {
            return CouponValidationResponse.builder()
                .valide(false)
                .message("Vous avez déjà utilisé ce coupon le nombre maximum de fois")
                .build();
        }

        // Calculer les réductions (ordre : % -> fixe -> livraison)
        BigDecimal montantApresReduction = sousTotal;
        BigDecimal reductionPourcentage = BigDecimal.ZERO;
        BigDecimal reductionMontantFixe = BigDecimal.ZERO;
        BigDecimal reductionLivraison = BigDecimal.ZERO;

        // 1. Réduction en pourcentage
        if (coupon.getPourcentageReduction() != null && coupon.getPourcentageReduction().compareTo(BigDecimal.ZERO) > 0) {
            reductionPourcentage = sousTotal
                .multiply(coupon.getPourcentageReduction())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            montantApresReduction = montantApresReduction.subtract(reductionPourcentage);
        }

        // 2. Réduction montant fixe
        if (coupon.getMontantFixeReduction() != null && coupon.getMontantFixeReduction().compareTo(BigDecimal.ZERO) > 0) {
            reductionMontantFixe = coupon.getMontantFixeReduction();
            montantApresReduction = montantApresReduction.subtract(reductionMontantFixe);
        }

        // 3. Livraison gratuite
        BigDecimal fraisLivraisonFinaux = fraisLivraison;
        if (coupon.getLivraisonGratuite()) {
            reductionLivraison = fraisLivraison;
            fraisLivraisonFinaux = BigDecimal.ZERO;
        }

        // S'assurer que le montant ne descend pas en dessous de 0
        if (montantApresReduction.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal exces = montantApresReduction.abs();
            montantApresReduction = BigDecimal.ZERO;
            // Ajuster les réductions pour ne pas dépasser le sous-total
            if (reductionMontantFixe.compareTo(BigDecimal.ZERO) > 0) {
                reductionMontantFixe = reductionMontantFixe.subtract(exces);
            }
        }

        // Appliquer le plafond de réduction si défini
        BigDecimal reductionTotale = reductionPourcentage.add(reductionMontantFixe).add(reductionLivraison);
        if (coupon.getMontantMaximumReduction() != null && reductionTotale.compareTo(coupon.getMontantMaximumReduction()) > 0) {
            BigDecimal reduction = reductionTotale.subtract(coupon.getMontantMaximumReduction());
            reductionTotale = coupon.getMontantMaximumReduction();
            montantApresReduction = montantApresReduction.add(reduction);
        }

        BigDecimal totalAvantCoupon = sousTotal.add(fraisLivraison);
        BigDecimal totalApresCoupon = montantApresReduction.add(fraisLivraisonFinaux);

        return CouponValidationResponse.builder()
            .valide(true)
            .message("Coupon appliqué avec succès")
            .reductionPourcentage(reductionPourcentage)
            .reductionMontantFixe(reductionMontantFixe)
            .reductionLivraison(reductionLivraison)
            .reductionTotale(reductionTotale)
            .sousTotal(sousTotal)
            .fraisLivraison(fraisLivraisonFinaux)
            .totalAvantCoupon(totalAvantCoupon)
            .totalApresCoupon(totalApresCoupon)
            .codeApplique(coupon.getCode())
            .build();
    }

    // ==================== UTILISATION ====================

    @Transactional
    public void applyCoupon(String code, String email, Order order) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
            .orElseThrow(() -> new RuntimeException("Code coupon invalide"));

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Incrémenter le compteur d'utilisations
        coupon.setUsagesActuels(coupon.getUsagesActuels() + 1);
        couponRepository.save(coupon);

        // Enregistrer l'utilisation
        BigDecimal montantReduit = order.getSousTotal()
            .add(order.getFraisLivraison())
            .subtract(order.getTotalTTC());

        CouponUsage usage = CouponUsage.builder()
            .coupon(coupon)
            .utilisateur(user)
            .commande(order)
            .montantReduit(montantReduit)
            .build();

        couponUsageRepository.save(usage);
    }

    // ==================== MISE À JOUR ====================

    @Transactional
    public CouponResponse updateCoupon(Long id, CouponRequest request, String email) {
        Coupon coupon = couponRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Coupon non trouvé"));

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Vérifier les permissions
        if (coupon.getScope() == CouponScope.SELLER && !coupon.getVendeur().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Vous n'avez pas la permission de modifier ce coupon");
        }

        if (user.getRole() != Role.ADMIN && user.getRole() != Role.SELLER) {
            throw new RuntimeException("Permission refusée");
        }

        // Mise à jour
        coupon.setDescription(request.getDescription());
        coupon.setPourcentageReduction(request.getPourcentageReduction());
        coupon.setMontantFixeReduction(request.getMontantFixeReduction());
        coupon.setLivraisonGratuite(request.getLivraisonGratuite());
        coupon.setMontantMinimum(request.getMontantMinimum());
        coupon.setMontantMaximumReduction(request.getMontantMaximumReduction());
        coupon.setCategoriesApplicables(request.getCategoriesApplicables() != null ? request.getCategoriesApplicables() : List.of());
        coupon.setUsagesMaxGlobal(request.getUsagesMaxGlobal());
        coupon.setUsagesMaxParUtilisateur(request.getUsagesMaxParUtilisateur());
        coupon.setDateDebut(request.getDateDebut());
        coupon.setDateExpiration(request.getDateExpiration());

        coupon = couponRepository.save(coupon);
        return mapToResponse(coupon);
    }

    @Transactional
    public void toggleCouponStatus(Long id, String email) {
        Coupon coupon = couponRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Coupon non trouvé"));

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Vérifier les permissions
        if (coupon.getScope() == CouponScope.SELLER && !coupon.getVendeur().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Vous n'avez pas la permission de modifier ce coupon");
        }

        coupon.setActif(!coupon.getActif());
        couponRepository.save(coupon);
    }

    @Transactional
    public void blockCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Coupon non trouvé"));
        coupon.setBloque(true);
        couponRepository.save(coupon);
    }

    @Transactional
    public void unblockCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Coupon non trouvé"));
        coupon.setBloque(false);
        couponRepository.save(coupon);
    }

    // ==================== SUPPRESSION ====================

    @Transactional
    public void deleteCoupon(Long id, String email) {
        Coupon coupon = couponRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Coupon non trouvé"));

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Vérifier les permissions
        if (coupon.getScope() == CouponScope.SELLER && !coupon.getVendeur().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Vous n'avez pas la permission de supprimer ce coupon");
        }

        // Ne pas supprimer si déjà utilisé
        if (coupon.getUsagesActuels() > 0) {
            throw new RuntimeException("Impossible de supprimer un coupon déjà utilisé. Vous pouvez le désactiver.");
        }

        couponRepository.delete(coupon);
    }

    // ==================== STATISTIQUES ====================

    public CouponStatsResponse getCouponStats(Long id) {
        Coupon coupon = couponRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Coupon non trouvé"));

        BigDecimal montantTotal = couponUsageRepository.getTotalSavingsForCoupon(id);
        Long uniqueUsers = couponUsageRepository.countUniqueUsersForCoupon(id);
        
        BigDecimal tauxUtilisation = BigDecimal.ZERO;
        if (coupon.getUsagesMaxGlobal() > 0) {
            tauxUtilisation = BigDecimal.valueOf(coupon.getUsagesActuels())
                .divide(BigDecimal.valueOf(coupon.getUsagesMaxGlobal()), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }

        return CouponStatsResponse.builder()
            .couponId(coupon.getId())
            .code(coupon.getCode())
            .nombreUtilisations(coupon.getUsagesActuels())
            .montantTotalEconomise(montantTotal)
            .tauxUtilisation(tauxUtilisation)
            .nombreUtilisateursUniques(uniqueUsers.intValue())
            .build();
    }

    // ==================== HELPERS ====================

    private CouponResponse mapToResponse(Coupon coupon) {
        BigDecimal montantTotal = couponUsageRepository.getTotalSavingsForCoupon(coupon.getId());
        
        return CouponResponse.builder()
            .id(coupon.getId())
            .code(coupon.getCode())
            .description(coupon.getDescription())
            .pourcentageReduction(coupon.getPourcentageReduction())
            .montantFixeReduction(coupon.getMontantFixeReduction())
            .livraisonGratuite(coupon.getLivraisonGratuite())
            .montantMinimum(coupon.getMontantMinimum())
            .montantMaximumReduction(coupon.getMontantMaximumReduction())
            .categoriesApplicables(coupon.getCategoriesApplicables())
            .scope(coupon.getScope())
            .vendeurId(coupon.getVendeur() != null ? coupon.getVendeur().getId() : null)
            .vendeurNom(coupon.getVendeur() != null ? coupon.getVendeur().getPrenom() + " " + coupon.getVendeur().getNom() : null)
            .usagesMaxGlobal(coupon.getUsagesMaxGlobal())
            .usagesActuels(coupon.getUsagesActuels())
            .usagesMaxParUtilisateur(coupon.getUsagesMaxParUtilisateur())
            .dateDebut(coupon.getDateDebut())
            .dateExpiration(coupon.getDateExpiration())
            .dateCreation(coupon.getDateCreation())
            .actif(coupon.getActif())
            .bloque(coupon.getBloque())
            .valide(coupon.isValide())
            .montantTotalEconomise(montantTotal)
            .build();
    }

    private String getInvalidReason(Coupon coupon) {
        if (!coupon.getActif()) return "Ce coupon est désactivé";
        if (coupon.getBloque()) return "Ce coupon a été bloqué";
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getDateDebut())) return "Ce coupon n'est pas encore actif";
        if (now.isAfter(coupon.getDateExpiration())) return "Ce coupon a expiré";
        if (coupon.getUsagesActuels() >= coupon.getUsagesMaxGlobal()) return "Ce coupon a atteint sa limite d'utilisation";
        
        return "Ce coupon n'est pas valide";
    }
}
