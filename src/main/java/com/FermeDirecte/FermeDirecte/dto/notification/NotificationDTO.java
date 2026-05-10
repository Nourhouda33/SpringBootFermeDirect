package com.FermeDirecte.FermeDirecte.dto.notification;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String titre;
    private String message;
    private String type;        // ORDER_UPDATE | PROMO | STOCK | SYSTEM
    private String lien;
    private boolean lue;
    private LocalDateTime dateCreation;
}
