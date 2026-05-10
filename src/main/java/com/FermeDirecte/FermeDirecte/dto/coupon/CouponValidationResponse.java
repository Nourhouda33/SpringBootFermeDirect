// src/main/java/com/FermeDirecte/FermeDirecte/dto/coupon/CouponValidationResponse.java
package com.FermeDirecte.FermeDirecte.dto.coupon;

import lombok.*;

import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CouponValidationResponse {

    private Boolean valide;
    private String message;
    
    // Détails de la réduction
    private BigDecimal reductionPourcentage;
    private BigDecimal reductionMontantFixe;
    private BigDecimal reductionLivraison;
    private BigDecimal reductionTotale;
    
    // Montants finaux
    private BigDecimal sousTotal;
    private BigDecimal fraisLivraison;
    private BigDecimal totalAvantCoupon;
    private BigDecimal totalApresCoupon;
    
    private String codeApplique;
}
