// src/main/java/com/FermeDirecte/FermeDirecte/dto/order/OrderResponse.java
package com.FermeDirecte.FermeDirecte.dto.order;

import com.FermeDirecte.FermeDirecte.enums.OrderStatus;
import com.FermeDirecte.FermeDirecte.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderResponse {

    private Long id;
    private String numeroCommande;
    private OrderStatus statut;
    private PaymentStatus statutPaiement;
    private List<OrderItemResponse> lignes;
    private BigDecimal sousTotal;
    private BigDecimal remise;
    private BigDecimal fraisLivraison;
    private BigDecimal totalTTC;
    private LocalDateTime dateCommande;

    // ── Infos client ──
    private Long clientId;
    private String prenomClient;
    private String nomClient;
    private String emailClient;
    private String telephoneClient;

    // ── Adresse de livraison ──
    private Long adresseId;
    private String adresseRue;
    private String adresseVille;
    private String adresseCodePostal;
    private String adressePays;
    private String adresseGouvernorat;
    private String adresseTelephone;
    private String adressePrenom;
    private String adresseNom;
}
