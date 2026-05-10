// src/main/java/com/FermeDirecte/FermeDirecte/dto/product/ProductRequest.java
package com.FermeDirecte.FermeDirecte.dto.product;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Le nom du produit est obligatoire")
    private String nom;

    private String description;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    private BigDecimal prix;

    @DecimalMin(value = "0.01", message = "Le prix promo doit être supérieur à 0")
    private BigDecimal prixPromo;

    @NotNull(message = "Le stock est obligatoire")
    @Min(value = 0, message = "Le stock ne peut pas être négatif")
    private Integer stock;

    @Builder.Default
    private Boolean actif = true;

    private String imageUrl;

    private String unite;

    // Catégorie unique (optionnelle)
    private Long categoryId;
}
