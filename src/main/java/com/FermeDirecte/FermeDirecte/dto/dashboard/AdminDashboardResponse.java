// src/main/java/com/FermeDirecte/FermeDirecte/dto/dashboard/AdminDashboardResponse.java
package com.FermeDirecte.FermeDirecte.dto.dashboard;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AdminDashboardResponse {

    private Long totalUtilisateurs;
    private Long totalCommandes;
    private Long totalProduits;
    private BigDecimal chiffreAffairesGlobal;
    
    // Statistiques de croissance (optionnel)
    private Double croissanceUtilisateurs;  // % de croissance ce mois
    private Double croissanceCommandes;
    private Double croissanceProduits;
    private Double croissanceCA;
    
    private List<Map<String, Object>> topProduits;
    private List<Map<String, Object>> commandesRecentes;
    private List<Map<String, Object>> produitsRecents;  // Nouveaux produits
    
    // Données pour les graphiques
    private List<Map<String, Object>> revenusParMois;  // Revenus mensuels (12 mois)
    private Map<String, Long> commandesParStatut;      // Commandes par statut
    private Map<String, Long> utilisateursParRole;     // Utilisateurs par rôle
}
