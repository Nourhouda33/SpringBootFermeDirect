// src/main/java/com/FermeDirecte/FermeDirecte/dto/order/OrderRequest.java
package com.FermeDirecte.FermeDirecte.dto.order;

import com.FermeDirecte.FermeDirecte.dto.address.AddressRequest;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderRequest {

    // Soit on envoie l'ID d'une adresse existante
    private Long adresseId;
    
    // Soit on envoie une nouvelle adresse
    private AddressRequest adresse;

    private String codeCoupon;
    
    private String modePaiement;
    
    private String notes;
}
