// src/main/java/com/FermeDirecte/FermeDirecte/dto/dashboard/SellerDashboardResponse.java
package com.FermeDirecte.FermeDirecte.dto.dashboard;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SellerDashboardResponse {

    private Long totalProduits;
    private Long commandesEnAttente;
    private BigDecimal revenuTotal;
    private Long produitsStockFaible;
    private List<Map<String, Object>> stockFaible;
    private List<Map<String, Object>> commandesRecentes;
    private List<Map<String, Object>> revenusParJour;
    private Map<String, Object> statistiquesCommandes;
    private List<Map<String, Object>> topProduits;  // Top 5 produits du vendeur
}
