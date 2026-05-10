package com.FermeDirecte.FermeDirecte.controller;

import com.FermeDirecte.FermeDirecte.dto.cart.CartItemRequest;
import com.FermeDirecte.FermeDirecte.dto.cart.CartResponse;
import com.FermeDirecte.FermeDirecte.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                cartService.getPanier(userDetails.getUsername())
        );
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @Valid @RequestBody CartItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                cartService.ajouterArticle(request, userDetails.getUsername())
        );
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItemQty(
            @PathVariable Long itemId,
            @RequestBody CartItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                cartService.modifierQuantite(itemId, request.getQuantite(), userDetails.getUsername())
        );
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                cartService.retirerArticle(itemId, userDetails.getUsername())
        );
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {

        cartService.clearCart(userDetails.getUsername());

        return ResponseEntity.noContent().build();
    }
}