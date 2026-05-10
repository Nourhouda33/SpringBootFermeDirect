// src/main/java/com/FermeDirecte/FermeDirecte/repository/CouponRepository.java
package com.FermeDirecte.FermeDirecte.repository;

import com.FermeDirecte.FermeDirecte.entity.Coupon;
import com.FermeDirecte.FermeDirecte.enums.CouponScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    // Coupons globaux (admin)
    List<Coupon> findByScope(CouponScope scope);

    // Coupons d'un vendeur spécifique
    List<Coupon> findByScopeAndVendeurId(CouponScope scope, Long vendeurId);

    // Coupons actifs et valides
    @Query("SELECT c FROM Coupon c WHERE c.actif = true AND c.bloque = false " +
           "AND c.dateDebut <= :now AND c.dateExpiration > :now " +
           "AND c.usagesActuels < c.usagesMaxGlobal")
    List<Coupon> findAllValides(@Param("now") LocalDateTime now);

    // Coupons valides pour un vendeur
    @Query("SELECT c FROM Coupon c WHERE c.actif = true AND c.bloque = false " +
           "AND c.dateDebut <= :now AND c.dateExpiration > :now " +
           "AND c.usagesActuels < c.usagesMaxGlobal " +
           "AND c.vendeur.id = :vendeurId")
    List<Coupon> findValidesForSeller(@Param("vendeurId") Long vendeurId, @Param("now") LocalDateTime now);

    // Coupons expirés
    @Query("SELECT c FROM Coupon c WHERE c.dateExpiration < :now")
    List<Coupon> findExpires(@Param("now") LocalDateTime now);
}
