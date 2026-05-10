package com.FermeDirecte.FermeDirecte.service;

import com.FermeDirecte.FermeDirecte.dto.notification.NotificationCountDTO;
import com.FermeDirecte.FermeDirecte.dto.notification.NotificationDTO;
import com.FermeDirecte.FermeDirecte.entity.Notification;
import com.FermeDirecte.FermeDirecte.entity.User;
import com.FermeDirecte.FermeDirecte.exception.ResourceNotFoundException;
import com.FermeDirecte.FermeDirecte.repository.NotificationRepository;
import com.FermeDirecte.FermeDirecte.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // ── Créer une notification ────────────────────────────────────
    @Transactional
    public void create(Long userId, String titre, String message, String type, String lien) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        Notification notification = Notification.builder()
                .user(user)
                .titre(titre)
                .message(message)
                .type(type)
                .lien(lien)
                .lue(false)
                .build();

        notificationRepository.save(notification);
        log.debug("Notification créée pour user {} : {}", userId, titre);
    }

    // ── Liste des notifications de l'utilisateur ─────────────────
    @Transactional(readOnly = true)
    public List<NotificationDTO> getMesNotifications(String email) {
        User user = getUser(email);
        return notificationRepository
                .findByUser_IdOrderByDateCreationDesc(user.getId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Compteur non lues ─────────────────────────────────────────
    @Transactional(readOnly = true)
    public NotificationCountDTO getCount(String email) {
        User user = getUser(email);
        long total   = notificationRepository.countByUser_Id(user.getId());
        long nonLues = notificationRepository.countByUser_IdAndLueFalse(user.getId());
        return NotificationCountDTO.builder().total(total).nonLues(nonLues).build();
    }

    // ── Marquer une notification comme lue ───────────────────────
    @Transactional
    public NotificationDTO marquerLue(Long id, String email) {
        User user = getUser(email);
        Notification notif = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable"));

        if (!notif.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Notification introuvable");
        }

        notif.setLue(true);
        return toDTO(notificationRepository.save(notif));
    }

    // ── Marquer toutes comme lues ─────────────────────────────────
    @Transactional
    public void marquerToutesLues(String email) {
        User user = getUser(email);
        notificationRepository.marquerToutesLues(user.getId());
    }

    // ── Supprimer une notification ────────────────────────────────
    @Transactional
    public void supprimer(Long id, String email) {
        User user = getUser(email);
        Notification notif = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable"));

        if (!notif.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Notification introuvable");
        }

        notificationRepository.delete(notif);
    }

    // ── Helpers ───────────────────────────────────────────────────
    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    private NotificationDTO toDTO(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .titre(n.getTitre())
                .message(n.getMessage())
                .type(n.getType())
                .lien(n.getLien())
                .lue(n.isLue())
                .dateCreation(n.getDateCreation())
                .build();
    }
}
