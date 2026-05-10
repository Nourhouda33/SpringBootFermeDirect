// src/main/java/com/FermeDirecte/FermeDirecte/service/DashboardService.java
package com.FermeDirecte.FermeDirecte.service;

import com.FermeDirecte.FermeDirecte.dto.dashboard.*;
import com.FermeDirecte.FermeDirecte.entity.Order;
import com.FermeDirecte.FermeDirecte.enums.OrderStatus;
import com.FermeDirecte.FermeDirecte.exception.ResourceNotFoundException;
import com.FermeDirecte.FermeDirecte.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final SellerProfileRepository sellerProfileRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getAdminDashboard() {

        List<Order> commandes = orderRepository.findAll();
        
        // Date du début du mois actuel et du mois précédent
        java.time.LocalDate debutMoisActuel = java.time.LocalDate.now().withDayOfMonth(1);
        java.time.LocalDate debutMoisPrecedent = debutMoisActuel.minusMonths(1);

        // Chiffre d'affaires global (hors commandes annulées)
        BigDecimal ca = commandes.stream()
                .filter(o -> o.getStatut() != OrderStatus.CANCELLED)
                .map(Order::getTotalTTC)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcul des croissances
        long commandesMoisActuel = commandes.stream()
                .filter(o -> o.getDateCommande() != null && 
                        !o.getDateCommande().toLocalDate().isBefore(debutMoisActuel))
                .count();
        
        long commandesMoisPrecedent = commandes.stream()
                .filter(o -> o.getDateCommande() != null &&
                        !o.getDateCommande().toLocalDate().isBefore(debutMoisPrecedent) &&
                        o.getDateCommande().toLocalDate().isBefore(debutMoisActuel))
                .count();
        
        double croissanceCommandes = calculerCroissance(commandesMoisPrecedent, commandesMoisActuel);

        // Commandes récentes avec informations client
        List<Map<String, Object>> commandesRecentes = commandes.stream()
                .sorted(Comparator.comparing(
                        Order::getDateCommande,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .limit(5)
                .map(o -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", o.getId());
                    m.put("numeroCommande", o.getNumeroCommande());
                    m.put("nomClient", o.getClient().getPrenom() + " " + o.getClient().getNom());
                    m.put("statut", o.getStatut());
                    m.put("totalTTC", o.getTotalTTC());
                    m.put("dateCommande", o.getDateCommande());
                    return m;
                })
                .collect(Collectors.toList());

        // Top produits (les plus vendus)
        Map<Long, Map<String, Object>> produitsVendus = new HashMap<>();
        
        commandes.stream()
                .filter(o -> o.getStatut() != OrderStatus.CANCELLED)
                .flatMap(o -> o.getLignes().stream())
                .forEach(item -> {
                    Long productId = item.getProduit().getId();
                    produitsVendus.putIfAbsent(productId, new HashMap<>());
                    Map<String, Object> produitData = produitsVendus.get(productId);
                    
                    produitData.put("nomProduit", item.getProduit().getNom());
                    produitData.put("vendeur", item.getProduit().getSellerProfile().getNomBoutique());
                    
                    int quantite = (int) produitData.getOrDefault("quantiteVendue", 0);
                    produitData.put("quantiteVendue", quantite + item.getQuantite());
                    
                    BigDecimal ca_produit = (BigDecimal) produitData.getOrDefault("chiffreAffaires", BigDecimal.ZERO);
                    produitData.put("chiffreAffaires", 
                        ca_produit.add(item.getPrixUnitaire().multiply(BigDecimal.valueOf(item.getQuantite()))));
                });

        List<Map<String, Object>> topProduits = produitsVendus.values().stream()
                .sorted((p1, p2) -> {
                    int q1 = (int) p1.get("quantiteVendue");
                    int q2 = (int) p2.get("quantiteVendue");
                    return Integer.compare(q2, q1);
                })
                .limit(5)
                .collect(Collectors.toList());

        // Produits récents (ajoutés ce mois)
        List<Map<String, Object>> produitsRecents = productRepository.findAll().stream()
                .filter(p -> p.getDateCreation() != null &&
                        !p.getDateCreation().toLocalDate().isBefore(debutMoisActuel))
                .sorted(Comparator.comparing(
                        p -> p.getDateCreation(),
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .limit(5)
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", p.getId());
                    m.put("nom", p.getNom());
                    m.put("prix", p.getPrix());
                    m.put("vendeur", p.getSellerProfile().getNomBoutique());
                    m.put("dateCreation", p.getDateCreation());
                    m.put("statut", p.getActif() ? "ACTIF" : "INACTIF");
                    return m;
                })
                .collect(Collectors.toList());

        return AdminDashboardResponse.builder()
                .totalUtilisateurs(userRepository.count())
                .totalCommandes((long) commandes.size())
                .totalProduits(productRepository.count())
                .chiffreAffairesGlobal(ca)
                .croissanceCommandes(croissanceCommandes)
                .croissanceProduits(calculerCroissanceProduits(debutMoisActuel, debutMoisPrecedent))
                .croissanceCA(calculerCroissanceCA(commandes, debutMoisActuel, debutMoisPrecedent))
                .topProduits(topProduits)
                .commandesRecentes(commandesRecentes)
                .produitsRecents(produitsRecents)
                .revenusParMois(calculerRevenusParMois(commandes))
                .commandesParStatut(calculerCommandesParStatut(commandes))
                .utilisateursParRole(calculerUtilisateursParRole())
                .build();
    }

    private List<Map<String, Object>> calculerRevenusParMois(List<Order> commandes) {
        List<Map<String, Object>> revenusParMois = new ArrayList<>();
        java.time.LocalDate maintenant = java.time.LocalDate.now();
        
        for (int i = 11; i >= 0; i--) {
            java.time.YearMonth mois = java.time.YearMonth.from(maintenant.minusMonths(i));
            java.time.LocalDate debutMois = mois.atDay(1);
            java.time.LocalDate finMois = mois.atEndOfMonth();
            
            BigDecimal revenuMois = commandes.stream()
                    .filter(o -> o.getStatut() != OrderStatus.CANCELLED)
                    .filter(o -> o.getDateCommande() != null)
                    .filter(o -> {
                        java.time.LocalDate dateCmd = o.getDateCommande().toLocalDate();
                        return !dateCmd.isBefore(debutMois) && !dateCmd.isAfter(finMois);
                    })
                    .map(Order::getTotalTTC)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Map<String, Object> moisData = new HashMap<>();
            moisData.put("mois", mois.getMonth().toString());
            moisData.put("annee", mois.getYear());
            moisData.put("revenu", revenuMois);
            revenusParMois.add(moisData);
        }
        
        return revenusParMois;
    }

    private Map<String, Long> calculerCommandesParStatut(List<Order> commandes) {
        Map<String, Long> commandesParStatut = new HashMap<>();
        commandesParStatut.put("EN_ATTENTE", commandes.stream()
                .filter(o -> o.getStatut() == OrderStatus.PENDING).count());
        commandesParStatut.put("EN_COURS", commandes.stream()
                .filter(o -> o.getStatut() == OrderStatus.PAID || o.getStatut() == OrderStatus.PROCESSING).count());
        commandesParStatut.put("LIVRE", commandes.stream()
                .filter(o -> o.getStatut() == OrderStatus.DELIVERED).count());
        commandesParStatut.put("ANNULE", commandes.stream()
                .filter(o -> o.getStatut() == OrderStatus.CANCELLED).count());
        return commandesParStatut;
    }

    private Map<String, Long> calculerUtilisateursParRole() {
        Map<String, Long> utilisateursParRole = new HashMap<>();
        List<com.FermeDirecte.FermeDirecte.entity.User> users = userRepository.findAll();
        
        utilisateursParRole.put("CUSTOMER", users.stream()
                .filter(u -> u.getRole() == com.FermeDirecte.FermeDirecte.enums.Role.CUSTOMER)
                .count());
        utilisateursParRole.put("SELLER", users.stream()
                .filter(u -> u.getRole() == com.FermeDirecte.FermeDirecte.enums.Role.SELLER)
                .count());
        utilisateursParRole.put("ADMIN", users.stream()
                .filter(u -> u.getRole() == com.FermeDirecte.FermeDirecte.enums.Role.ADMIN)
                .count());
        
        return utilisateursParRole;
    }

    private double calculerCroissance(long valeurPrecedente, long valeurActuelle) {
        if (valeurPrecedente == 0) return valeurActuelle > 0 ? 100.0 : 0.0;
        return ((double) (valeurActuelle - valeurPrecedente) / valeurPrecedente) * 100.0;
    }

    private double calculerCroissanceProduits(java.time.LocalDate debutMoisActuel, java.time.LocalDate debutMoisPrecedent) {
        long produitsMoisActuel = productRepository.findAll().stream()
                .filter(p -> p.getDateCreation() != null &&
                        !p.getDateCreation().toLocalDate().isBefore(debutMoisActuel))
                .count();
        
        long produitsMoisPrecedent = productRepository.findAll().stream()
                .filter(p -> p.getDateCreation() != null &&
                        !p.getDateCreation().toLocalDate().isBefore(debutMoisPrecedent) &&
                        p.getDateCreation().toLocalDate().isBefore(debutMoisActuel))
                .count();
        
        return calculerCroissance(produitsMoisPrecedent, produitsMoisActuel);
    }

    private double calculerCroissanceCA(List<Order> commandes, java.time.LocalDate debutMoisActuel, java.time.LocalDate debutMoisPrecedent) {
        BigDecimal caMoisActuel = commandes.stream()
                .filter(o -> o.getStatut() != OrderStatus.CANCELLED)
                .filter(o -> o.getDateCommande() != null &&
                        !o.getDateCommande().toLocalDate().isBefore(debutMoisActuel))
                .map(Order::getTotalTTC)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal caMoisPrecedent = commandes.stream()
                .filter(o -> o.getStatut() != OrderStatus.CANCELLED)
                .filter(o -> o.getDateCommande() != null &&
                        !o.getDateCommande().toLocalDate().isBefore(debutMoisPrecedent) &&
                        o.getDateCommande().toLocalDate().isBefore(debutMoisActuel))
                .map(Order::getTotalTTC)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (caMoisPrecedent.compareTo(BigDecimal.ZERO) == 0) {
            return caMoisActuel.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        
        return caMoisActuel.subtract(caMoisPrecedent)
                .divide(caMoisPrecedent, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    @Transactional(readOnly = true)
    public SellerDashboardResponse getSellerDashboard(String email) {

        var profile = sellerProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("SellerProfile", 0L));

        long totalProduits = profile.getProduits().size();

        // Compter les commandes en attente
        long commandesEnAttente = orderRepository.findAll().stream()
                .filter(o -> o.getStatut() == OrderStatus.PENDING)
                .flatMap(o -> o.getLignes().stream())
                .filter(item -> item.getProduit()
                        .getSellerProfile()
                        .getId()
                        .equals(profile.getId()))
                .map(item -> item.getCommande().getId())
                .distinct()
                .count();

        // Calculer le revenu total
        BigDecimal revenu = orderRepository.findAll().stream()
                .filter(o -> o.getStatut() != OrderStatus.CANCELLED)
                .flatMap(o -> o.getLignes().stream())
                .filter(item -> item.getProduit()
                        .getSellerProfile()
                        .getId()
                        .equals(profile.getId()))
                .map(item -> item.getPrixUnitaire()
                        .multiply(BigDecimal.valueOf(item.getQuantite())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Produits avec stock faible (< 10)
        List<Map<String, Object>> stockFaible = profile.getProduits().stream()
                .filter(p -> p.getStock() < 10)
                .sorted(Comparator.comparing(p -> p.getStock()))
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", p.getId());
                    m.put("nom", p.getNom());
                    m.put("stock", p.getStock());
                    m.put("prix", p.getPrix());
                    return m;
                })
                .collect(Collectors.toList());

        long produitsStockFaible = stockFaible.size();

        // Récupérer les commandes récentes contenant les produits du vendeur
        List<Map<String, Object>> commandesRecentes = orderRepository.findAll().stream()
                .filter(order -> order.getLignes().stream()
                        .anyMatch(item -> item.getProduit()
                                .getSellerProfile()
                                .getId()
                                .equals(profile.getId())))
                .sorted(Comparator.comparing(
                        Order::getDateCommande,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .limit(5)
                .map(o -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", o.getId());
                    m.put("numeroCommande", o.getNumeroCommande());
                    m.put("statut", o.getStatut());
                    m.put("totalTTC", o.getTotalTTC());
                    m.put("dateCommande", o.getDateCommande());
                    return m;
                })
                .collect(Collectors.toList());

        // Statistiques des commandes par statut
        Map<String, Object> statistiquesCommandes = new HashMap<>();
        List<Order> toutesCommandes = orderRepository.findAll().stream()
                .filter(order -> order.getLignes().stream()
                        .anyMatch(item -> item.getProduit()
                                .getSellerProfile()
                                .getId()
                                .equals(profile.getId())))
                .collect(Collectors.toList());

        statistiquesCommandes.put("total", (long) toutesCommandes.size());
        statistiquesCommandes.put("enAttente", toutesCommandes.stream()
                .filter(o -> o.getStatut() == OrderStatus.PENDING).count());
        statistiquesCommandes.put("confirmees", toutesCommandes.stream()
                .filter(o -> o.getStatut() == OrderStatus.PAID).count());
        statistiquesCommandes.put("enLivraison", toutesCommandes.stream()
                .filter(o -> o.getStatut() == OrderStatus.SHIPPED).count());
        statistiquesCommandes.put("livrees", toutesCommandes.stream()
                .filter(o -> o.getStatut() == OrderStatus.DELIVERED).count());
        statistiquesCommandes.put("annulees", toutesCommandes.stream()
                .filter(o -> o.getStatut() == OrderStatus.CANCELLED).count());

        // Revenus par jour (7 derniers jours)
        List<Map<String, Object>> revenusParJour = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate date = java.time.LocalDate.now().minusDays(i);
            BigDecimal revenuJour = orderRepository.findAll().stream()
                    .filter(o -> o.getStatut() != OrderStatus.CANCELLED)
                    .filter(o -> o.getDateCommande() != null && 
                            o.getDateCommande().toLocalDate().equals(date))
                    .flatMap(o -> o.getLignes().stream())
                    .filter(item -> item.getProduit()
                            .getSellerProfile()
                            .getId()
                            .equals(profile.getId()))
                    .map(item -> item.getPrixUnitaire()
                            .multiply(BigDecimal.valueOf(item.getQuantite())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> jour = new HashMap<>();
            jour.put("date", date.toString());
            jour.put("revenu", revenuJour);
            revenusParJour.add(jour);
        }

        // Top 5 produits du vendeur (les plus vendus)
        Map<Long, Map<String, Object>> produitsVendus = new HashMap<>();
        
        orderRepository.findAll().stream()
                .filter(o -> o.getStatut() != OrderStatus.CANCELLED)
                .flatMap(o -> o.getLignes().stream())
                .filter(item -> item.getProduit()
                        .getSellerProfile()
                        .getId()
                        .equals(profile.getId()))
                .forEach(item -> {
                    Long productId = item.getProduit().getId();
                    produitsVendus.putIfAbsent(productId, new HashMap<>());
                    Map<String, Object> produitData = produitsVendus.get(productId);
                    
                    produitData.put("nomProduit", item.getProduit().getNom());
                    
                    int quantite = (int) produitData.getOrDefault("quantiteVendue", 0);
                    produitData.put("quantiteVendue", quantite + item.getQuantite());
                    
                    BigDecimal ca_produit = (BigDecimal) produitData.getOrDefault("chiffreAffaires", BigDecimal.ZERO);
                    produitData.put("chiffreAffaires", 
                        ca_produit.add(item.getPrixUnitaire().multiply(BigDecimal.valueOf(item.getQuantite()))));
                });

        List<Map<String, Object>> topProduits = produitsVendus.values().stream()
                .sorted((p1, p2) -> {
                    int q1 = (int) p1.get("quantiteVendue");
                    int q2 = (int) p2.get("quantiteVendue");
                    return Integer.compare(q2, q1);
                })
                .limit(5)
                .collect(Collectors.toList());

        return SellerDashboardResponse.builder()
                .totalProduits(totalProduits)
                .commandesEnAttente(commandesEnAttente)
                .revenuTotal(revenu)
                .produitsStockFaible(produitsStockFaible)
                .stockFaible(stockFaible != null ? stockFaible : new ArrayList<>())
                .commandesRecentes(commandesRecentes != null ? commandesRecentes : new ArrayList<>())
                .revenusParJour(revenusParJour != null ? revenusParJour : new ArrayList<>())
                .statistiquesCommandes(statistiquesCommandes != null ? statistiquesCommandes : new HashMap<>())
                .topProduits(topProduits != null ? topProduits : new ArrayList<>())
                .build();
    }
}