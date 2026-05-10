// src/main/java/com/FermeDirecte/FermeDirecte/repository/CouponUsageRepository.java
package com.FermeDirecte.FermeDirecte.repository;

import com.FermeDirecte.FermeDirecte.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {

    // Nombre d'utilisations d'un coupon par un utilisateur
    int countByCouponIdAndUtilisateurId(Long couponId, Long utilisateurId);

    // Toutes les utilisations d'un coupon
    List<CouponUsage> findByCouponId(Long couponId);

    // Utilisations par utilisateur
    List<CouponUsage> findByUtilisateurId(Long utilisateurId);

    // Montant total économisé pour un coupon
    @Query("SELECT COALESCE(SUM(cu.montantReduit), 0) FROM CouponUsage cu WHERE cu.coupon.id = :couponId")
    BigDecimal getTotalSavingsForCoupon(@Param("couponId") Long couponId);

    // Nombre d'utilisateurs uniques pour un coupon
    @Query("SELECT COUNT(DISTINCT cu.utilisateur.id) FROM CouponUsage cu WHERE cu.coupon.id = :couponId")
    Long countUniqueUsersForCoupon(@Param("couponId") Long couponId);
}
