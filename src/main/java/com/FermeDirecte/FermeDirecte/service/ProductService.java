package com.FermeDirecte.FermeDirecte.service;

import com.FermeDirecte.FermeDirecte.dto.product.*;
import com.FermeDirecte.FermeDirecte.entity.*;
import com.FermeDirecte.FermeDirecte.enums.Role;
import com.FermeDirecte.FermeDirecte.exception.BadRequestException;
import com.FermeDirecte.FermeDirecte.exception.ResourceNotFoundException;
import com.FermeDirecte.FermeDirecte.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SellerProfileRepository sellerProfileRepository;

    @Transactional(readOnly = true)
    public Page<ProductResponse> listerProduits(Pageable pageable) {
        return productRepository.findByActifTrue(pageable).map(this::toResponse);
    }

    // -------------------------------------------------------
    // Filtre combiné : search + categoryId + prix + vendeur + promo
    // -------------------------------------------------------
    @Transactional(readOnly = true)
    public Page<ProductResponse> filtrer(
            String keyword,
            Long categoryId,
            BigDecimal prixMin,
            BigDecimal prixMax,
            Long vendeurId,
            boolean promoOnly,
            Pageable pageable) {

        // Si aucun filtre → liste simple (plus performant)
        boolean aucunFiltre = (keyword == null || keyword.isBlank())
                && categoryId == null
                && prixMin == null
                && prixMax == null
                && vendeurId == null
                && !promoOnly;

        if (aucunFiltre) {
            return productRepository.findByActifTrue(pageable).map(this::toResponse);
        }

        return productRepository.filtrerProduits(
                keyword, categoryId, prixMin, prixMax, vendeurId, promoOnly, pageable
        ).map(this::toResponse);
    }

    // -------------------------------------------------------
    // Top-selling : top 10 produits (mis en cache 10 minutes)
    // -------------------------------------------------------
    @Cacheable("topSelling")
    @Transactional(readOnly = true)
    public List<ProductResponse> topSelling() {
        Pageable top10 = PageRequest.of(0, 10);
        return productRepository.findTopSelling(top10)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> listerTousLesProduits(Pageable pageable) {
        // Pour l'admin : retourner TOUS les produits (actifs ET inactifs)
        return productRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> listerMesProduits(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        
        SellerProfile seller = user.getSellerProfile();
        if (seller == null) {
            throw new BadRequestException("Vous n'avez pas de profil vendeur");
        }
        
        return productRepository.findBySellerProfile_IdAndActifTrue(seller.getId(), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listerMesProduitsSimple(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        
        SellerProfile seller = user.getSellerProfile();
        if (seller == null) {
            // Retourner une liste vide au lieu d'une exception
            return new ArrayList<>();
        }
        
        // Récupérer tous les produits du vendeur (actifs ET inactifs pour la gestion)
        Pageable pageable = PageRequest.of(0, 1000, Sort.by(Sort.Direction.DESC, "dateCreation"));
        Page<Product> products = productRepository.findBySellerProfile_Id(seller.getId(), pageable);
        
        return products.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable"));
        return toResponse(p);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> rechercher(String keyword, Pageable pageable) {
        return productRepository.searchByKeyword(keyword, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> filtrerParPrix(BigDecimal min, BigDecimal max, Pageable pageable) {
        return productRepository.findByPrixBetweenAndActifTrue(min, max, pageable)
                .map(this::toResponse);
    }

    @Caching(evict = {
        @CacheEvict(value = "topSelling", allEntries = true)
    })
    @Transactional
    public ProductResponse creer(ProductRequest request, String email) {
        log.info("=== CRÉATION PRODUIT ===");
        log.info("Email: {}", email);
        log.info("Nom: {}", request.getNom());
        log.info("Prix: {}", request.getPrix());
        log.info("Stock: {}", request.getStock());
        log.info("CategoryId: {}", request.getCategoryId());
        log.info("ImageUrl length: {}", request.getImageUrl() != null ? request.getImageUrl().length() : 0);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        SellerProfile seller = user.getSellerProfile();

        // Si l'utilisateur n'a pas de profil vendeur, le créer automatiquement
        if (seller == null) {
            // Vérifier que l'utilisateur a le droit de vendre
            if (user.getRole() != Role.SELLER && user.getRole() != Role.ADMIN) {
                throw new BadRequestException("Seuls les vendeurs et administrateurs peuvent créer des produits");
            }
            
            // Créer le profil vendeur
            String nomBoutique = user.getRole() == Role.ADMIN 
                ? "Administration FermeDirecte" 
                : user.getPrenom() + " " + user.getNom();
            
            seller = SellerProfile.builder()
                    .user(user)
                    .nomBoutique(nomBoutique)
                    .description(user.getRole() == Role.ADMIN 
                        ? "Boutique officielle de l'administration" 
                        : "Bienvenue dans ma boutique !")
                    .note(0.0)
                    .build();
            
            // Sauvegarder via le repository
            seller = sellerProfileRepository.save(seller);
            user.setSellerProfile(seller);
            userRepository.save(user);
        }

        Product product = Product.builder()
                .nom(request.getNom())
                .description(request.getDescription())
                .prix(request.getPrix())
                .prixPromo(request.getPrixPromo())
                .stock(request.getStock())
                .unite(request.getUnite())
                .actif(request.getActif() != null ? request.getActif() : true)
                .imageUrl(request.getImageUrl())
                .sellerProfile(seller)
                .build();

        // Ajouter la catégorie si fournie
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable"));
            product.getCategories().add(category);
        }

        productRepository.save(product);
        return toResponse(product);
    }

    @Caching(evict = {
        @CacheEvict(value = "topSelling", allEntries = true)
    })
    @Transactional
    public ProductResponse modifier(Long id, ProductRequest request, String email) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable"));

        // ⚠️ (optionnel mais recommandé) vérifier que le user est le propriétaire

        product.setNom(request.getNom());
        product.setDescription(request.getDescription());
        product.setPrix(request.getPrix());
        product.setPrixPromo(request.getPrixPromo());
        product.setStock(request.getStock());
        product.setUnite(request.getUnite());
        product.setActif(request.getActif() != null ? request.getActif() : true);
        product.setImageUrl(request.getImageUrl());

        // Mettre à jour la catégorie
        product.getCategories().clear();
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable"));
            product.getCategories().add(category);
        }

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void desactiver(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable"));

        product.setActif(false);
        productRepository.save(product);
    }

    private ProductResponse toResponse(Product p) {

        double noteMoyenne = p.getAvis().stream()
                .mapToInt(r -> r.getNote())
                .average()
                .orElse(0.0);

        int nombreAvis = p.getAvis().size();

        List<String> categories = p.getCategories().stream()
                .map(Category::getNom)
                .collect(Collectors.toList());

        List<ProductVariantResponse> variantes = p.getVariantes().stream()
                .map(v -> ProductVariantResponse.builder()
                        .id(v.getId())
                        .attribut(v.getAttribut())
                        .valeur(v.getValeur())
                        .stockSupplementaire(v.getStockSupplementaire())
                        .prixDelta(v.getPrixDelta())
                        .build())
                .collect(Collectors.toList());

        return ProductResponse.builder()
                .id(p.getId())
                .nom(p.getNom())
                .description(p.getDescription())
                .prix(p.getPrix())
                .prixPromo(p.getPrixPromo())
                .stock(p.getStock())
                .unite(p.getUnite())
                .actif(p.getActif())
                .imageUrl(p.getImageUrl())
                .nomVendeur(p.getSellerProfile().getNomBoutique())
                .categories(categories)
                .variantes(variantes)
                .noteMoyenne(noteMoyenne)
                .nombreAvis(nombreAvis)
                .build();
    }
}