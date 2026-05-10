package com.FermeDirecte.FermeDirecte.controller;

import com.FermeDirecte.FermeDirecte.dto.notification.NotificationCountDTO;
import com.FermeDirecte.FermeDirecte.dto.notification.NotificationDTO;
import com.FermeDirecte.FermeDirecte.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    /** GET /api/notifications — Liste des notifications de l'utilisateur connecté */
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getMesNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                notificationService.getMesNotifications(userDetails.getUsername()));
    }

    /** GET /api/notifications/count — Nombre total et non lues */
    @GetMapping("/count")
    public ResponseEntity<NotificationCountDTO> getCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                notificationService.getCount(userDetails.getUsername()));
    }

    /** PUT /api/notifications/{id}/lire — Marquer une notification comme lue */
    @PutMapping("/{id}/lire")
    public ResponseEntity<NotificationDTO> marquerLue(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                notificationService.marquerLue(id, userDetails.getUsername()));
    }

    /** PUT /api/notifications/lire-tout — Marquer toutes comme lues */
    @PutMapping("/lire-tout")
    public ResponseEntity<Void> marquerToutesLues(
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.marquerToutesLues(userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    /** DELETE /api/notifications/{id} — Supprimer une notification */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.supprimer(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
