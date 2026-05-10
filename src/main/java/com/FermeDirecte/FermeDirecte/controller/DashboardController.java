package com.FermeDirecte.FermeDirecte.controller;

import com.FermeDirecte.FermeDirecte.dto.dashboard.AdminDashboardResponse;
import com.FermeDirecte.FermeDirecte.dto.dashboard.SellerDashboardResponse;
import com.FermeDirecte.FermeDirecte.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /** GET /api/dashboard/admin — Admin overview stats */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardResponse> getAdminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    /** GET /api/dashboard/seller — Seller-specific stats */
    @GetMapping("/seller")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<SellerDashboardResponse> getSellerDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(dashboardService.getSellerDashboard(userDetails.getUsername()));
    }
}
