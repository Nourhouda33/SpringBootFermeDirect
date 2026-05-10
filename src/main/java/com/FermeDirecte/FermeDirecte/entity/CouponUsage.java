// src/main/java/com/FermeDirecte/FermeDirecte/entity/CouponUsage.java
package com.FermeDirecte.FermeDirecte.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_usages", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"coupon_id", "user_id", "order_id"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CouponUsage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order commande;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal montantReduit; // Montant total économisé

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dateUtilisation;
}
