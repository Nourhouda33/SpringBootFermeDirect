// src/main/java/com/FermeDirecte/FermeDirecte/dto/coupon/SellerResponse.java
package com.FermeDirecte.FermeDirecte.dto.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String nomComplet; // nom + prenom
}
