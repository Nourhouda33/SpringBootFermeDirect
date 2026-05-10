package com.FermeDirecte.FermeDirecte.controller;

import com.FermeDirecte.FermeDirecte.dto.order.OrderRequest;
import com.FermeDirecte.FermeDirecte.dto.order.OrderResponse;
import com.FermeDirecte.FermeDirecte.enums.OrderStatus;
import com.FermeDirecte.FermeDirecte.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class OrderController {

    private final OrderService orderService;

    // ✅ PASSER COMMANDE
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.passerCommande(request, userDetails.getUsername()));
    }

    // ✅ MES COMMANDES — route principale du sujet
    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> getMyOrdersAlias(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                orderService.mesCommandes(userDetails.getUsername())
        );
    }

    // ✅ MES COMMANDES — route alternative (compatibilité frontend)
    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                orderService.mesCommandes(userDetails.getUsername())
        );
    }

    // ✅ COMMANDES DU VENDEUR
    @GetMapping("/seller-orders")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<OrderResponse>> getSellerOrders(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                orderService.commandesVendeur(userDetails.getUsername())
        );
    }

    // ✅ TOUTES LES COMMANDES (ADMIN)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.toutesCommandes());
    }

    // ✅ GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                orderService.getById(id, userDetails.getUsername())
        );
    }

    // ✅ UPDATE STATUS (ADMIN et SELLER)
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        String statutStr = body.get("statut");
        OrderStatus statut = OrderStatus.valueOf(statutStr);

        return ResponseEntity.ok(
                orderService.mettreAJourStatut(id, statut, userDetails.getUsername())
        );
    }

    // ✅ MODIFIER ADRESSE DE LIVRAISON (ADMIN)
    @PutMapping("/{id}/address")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateAddress(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {
        return ResponseEntity.ok(orderService.mettreAJourAdresse(id, body));
    }

    // ✅ ANNULER — PUT (conforme sujet ShopFlow)
    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrderPut(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                orderService.annuler(id, userDetails.getUsername())
        );
    }

    // ✅ ANNULER — POST (compatibilité frontend existant)
    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                orderService.annuler(id, userDetails.getUsername())
        );
    }
}