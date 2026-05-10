package com.FermeDirecte.FermeDirecte.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private String type;   // ORDER_UPDATE, PROMO, STOCK, SYSTEM

    @Column
    private String lien;   // ex: "/orders/123"

    @Column(nullable = false)
    @Builder.Default
    private boolean lue = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dateCreation;
}
