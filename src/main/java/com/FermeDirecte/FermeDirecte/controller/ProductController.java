// src/main/java/com/FermeDirecte/FermeDirecte/controller/ProductController.java
package com.FermeDirecte.FermeDirecte.controller;

import com.FermeDirecte.FermeDirecte.dto.product.ProductRequest;
import com.FermeDirecte.FermeDirecte.dto.product.ProductResponse;
import com.FermeDirecte.FermeDirecte.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * GET /api/products
     * Liste paginée avec filtres combinés :
     *   ?search=tomate  &categoryId=2  &prixMin=1  &prixMax=50
     *   &vendeurId=3    &promo=true
     *   &sortBy=prix    &sortDir=asc
     *   &page=0         &size=12
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "12")  int size,
            @RequestParam(required = false)     String search,
            @RequestParam(required = false)     Long categoryId,
            @RequestParam(required = false)     BigDecimal prixMin,
            @RequestParam(required = false)     BigDecimal prixMax,
            @RequestParam(required = false)     Long vendeurId,
            @RequestParam(defaultValue = "false") boolean promo,
            @RequestParam(defaultValue = "dateCreation") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        // Tri : prix | dateCreation (nouveautés) | noteMoyenne (popularité)
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(
                productService.filtrer(search, categoryId, prixMin, prixMax, vendeurId, promo, pageable)
        );
    }

    /**
     * GET /api/products/search?q=tomate
     * Recherche full-text (nom + description + catégorie)
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> search(
            @RequestParam("q")                  String q,
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "12")  int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("dateCreation").descending());
        return ResponseEntity.ok(productService.rechercher(q, pageable));
    }

    /**
     * GET /api/products/top-selling
     * Top 10 meilleures ventes
     */
    @GetMapping("/top-selling")
    public ResponseEntity<List<ProductResponse>> topSelling() {
        return ResponseEntity.ok(productService.topSelling());
    }

    /** GET /api/products/all — tous les produits (ADMIN) */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "1000") int size,
            @RequestParam(defaultValue = "dateCreation") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(productService.listerTousLesProduits(pageable));
    }

    /** GET /api/products/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    /** GET /api/products/seller/my-products */
    @GetMapping("/seller/my-products")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<List<ProductResponse>> getMyProducts(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                productService.listerMesProduitsSimple(userDetails.getUsername())
        );
    }

    /** GET /api/products/seller/my-products-paginated */
    @GetMapping("/seller/my-products-paginated")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<Page<ProductResponse>> getMyProductsPaginated(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        return ResponseEntity.ok(
                productService.listerMesProduits(userDetails.getUsername(), pageable)
        );
    }

    /** POST /api/products */
    @PostMapping
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.creer(request, userDetails.getUsername()));
    }

    /** PUT /api/products/{id} */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                productService.modifier(id, request, userDetails.getUsername())
        );
    }

    /** DELETE /api/products/{id} — soft delete */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        productService.desactiver(id);
        return ResponseEntity.noContent().build();
    }

    /** PATCH /api/products/{id}/toggle */
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<ProductResponse> toggleProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        productService.desactiver(id);
        return ResponseEntity.ok(productService.getById(id));
    }
}