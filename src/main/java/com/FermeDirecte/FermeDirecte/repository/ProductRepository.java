// src/main/java/com/FermeDirecte/FermeDirecte/repository/ProductRepository.java
package com.FermeDirecte.FermeDirecte.repository;

import com.FermeDirecte.FermeDirecte.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByActifTrue(Pageable pageable);

    // -------------------------------------------------------
    // Recherche full-text : nom + description + catégorie
    // -------------------------------------------------------
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN p.categories c " +
           "WHERE p.actif = true AND " +
           "(LOWER(p.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.nom) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // -------------------------------------------------------
    // Filtre combiné : search + categoryId + prixMin + prixMax
    // Tous les paramètres sont optionnels (null = ignoré)
    // -------------------------------------------------------
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN p.categories c " +
           "WHERE p.actif = true " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "     LOWER(p.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(c.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:categoryId IS NULL OR c.id = :categoryId) " +
           "AND (:prixMin IS NULL OR p.prix >= :prixMin) " +
           "AND (:prixMax IS NULL OR p.prix <= :prixMax) " +
           "AND (:vendeurId IS NULL OR p.sellerProfile.id = :vendeurId) " +
           "AND (:promoOnly = false OR p.prixPromo IS NOT NULL)")
    Page<Product> filtrerProduits(
            @Param("keyword")    String keyword,
            @Param("categoryId") Long categoryId,
            @Param("prixMin")    BigDecimal prixMin,
            @Param("prixMax")    BigDecimal prixMax,
            @Param("vendeurId")  Long vendeurId,
            @Param("promoOnly")  boolean promoOnly,
            Pageable pageable);

    Page<Product> findByPrixBetweenAndActifTrue(BigDecimal min, BigDecimal max, Pageable pageable);

    Page<Product> findBySellerProfile_IdAndActifTrue(Long sellerId, Pageable pageable);

    // Récupérer tous les produits d'un vendeur (actifs et inactifs)
    Page<Product> findBySellerProfile_Id(Long sellerId, Pageable pageable);

    // -------------------------------------------------------
    // Top-selling : produits les plus commandés
    // JOIN optimisé — une seule requête SQL avec GROUP BY
    // -------------------------------------------------------
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN OrderItem oi ON oi.produit = p " +
           "WHERE p.actif = true " +
           "GROUP BY p " +
           "ORDER BY COALESCE(SUM(oi.quantite), 0) DESC")
    List<Product> findTopSelling(Pageable pageable);
}
