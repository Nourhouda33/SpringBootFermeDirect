// src/main/java/com/FermeDirecte/FermeDirecte/controller/CouponController.java
package com.FermeDirecte.FermeDirecte.controller;

import com.FermeDirecte.FermeDirecte.dto.coupon.*;
import com.FermeDirecte.FermeDirecte.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CouponController {

    private final CouponService couponService;

    // ==================== ADMIN ====================

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CouponResponse> createGlobalCoupon(
            @Valid @RequestBody CouponRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CouponResponse response = couponService.createCoupon(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CouponResponse>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @GetMapping("/admin/global")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CouponResponse>> getGlobalCoupons() {
        return ResponseEntity.ok(couponService.getGlobalCoupons());
    }

    @GetMapping("/admin/sellers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SellerResponse>> getAllSellers() {
        return ResponseEntity.ok(couponService.getAllSellers());
    }

    @PutMapping("/admin/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> blockCoupon(@PathVariable Long id) {
        couponService.blockCoupon(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/{id}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unblockCoupon(@PathVariable Long id) {
        couponService.unblockCoupon(id);
        return ResponseEntity.ok().build();
    }

    // ==================== VENDEUR ====================

    @PostMapping("/seller")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<CouponResponse> createSellerCoupon(
            @Valid @RequestBody CouponRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CouponResponse response = couponService.createCoupon(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/seller/my-coupons")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<CouponResponse>> getMyCoupons(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(couponService.getCouponsForSeller(userDetails.getUsername()));
    }

    @PutMapping("/seller/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<CouponResponse> updateMyCoupon(
            @PathVariable Long id,
            @Valid @RequestBody CouponRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CouponResponse response = couponService.updateCoupon(id, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/seller/{id}/toggle")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Void> toggleMyCouponStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        couponService.toggleCouponStatus(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/seller/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Void> deleteMyCoupon(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        couponService.deleteCoupon(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/seller/{id}/stats")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<CouponStatsResponse> getMyCouponStats(@PathVariable Long id) {
        return ResponseEntity.ok(couponService.getCouponStats(id));
    }

    // ==================== COMMUN (ADMIN + VENDEUR) ====================

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<CouponResponse> getCouponById(@PathVariable Long id) {
        return ResponseEntity.ok(couponService.getCouponById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<CouponResponse> updateCoupon(
            @PathVariable Long id,
            @Valid @RequestBody CouponRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CouponResponse response = couponService.updateCoupon(id, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<Void> deleteCoupon(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        couponService.deleteCoupon(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<CouponStatsResponse> getCouponStats(@PathVariable Long id) {
        return ResponseEntity.ok(couponService.getCouponStats(id));
    }

    // ==================== CLIENT ====================

    // ✅ VALIDER COUPON — POST avec params (existant, utilisé par le frontend)
    @PostMapping("/validate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CouponValidationResponse> validateCoupon(
            @RequestParam String code,
            @RequestParam BigDecimal sousTotal,
            @RequestParam BigDecimal fraisLivraison,
            @RequestParam(required = false) List<Long> categoryIds,
            @AuthenticationPrincipal UserDetails userDetails) {

        CouponValidationResponse response = couponService.validateCoupon(
            code,
            userDetails.getUsername(),
            sousTotal,
            fraisLivraison,
            categoryIds != null ? categoryIds : List.of()
        );
        return ResponseEntity.ok(response);
    }

    // ✅ VALIDER COUPON — GET /{code} (conforme sujet ShopFlow)
    @GetMapping("/validate/{code}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CouponValidationResponse> validateCouponByPath(
            @PathVariable String code,
            @RequestParam(defaultValue = "0") BigDecimal sousTotal,
            @RequestParam(defaultValue = "0") BigDecimal fraisLivraison,
            @RequestParam(required = false) List<Long> categoryIds,
            @AuthenticationPrincipal UserDetails userDetails) {

        CouponValidationResponse response = couponService.validateCoupon(
            code,
            userDetails.getUsername(),
            sousTotal,
            fraisLivraison,
            categoryIds != null ? categoryIds : List.of()
        );
        return ResponseEntity.ok(response);
    }
}
