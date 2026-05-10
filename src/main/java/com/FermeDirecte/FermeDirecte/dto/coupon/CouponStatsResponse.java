// src/main/java/com/FermeDirecte/FermeDirecte/dto/coupon/CouponStatsResponse.java
package com.FermeDirecte.FermeDirecte.dto.coupon;

import lombok.*;

import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CouponStatsResponse {

    private Long couponId;
    private String code;
    private Integer nombreUtilisations;
    private BigDecimal montantTotalEconomise;
    private BigDecimal tauxUtilisation; // (usagesActuels / usagesMaxGlobal) * 100
    private Integer nombreUtilisateursUniques;
}
