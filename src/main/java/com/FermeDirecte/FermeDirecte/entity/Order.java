// src/main/java/com/FermeDirecte/FermeDirecte/entity/Order.java
package com.FermeDirecte.FermeDirecte.entity;

import com.FermeDirecte.FermeDirecte.enums.OrderStatus;
import com.FermeDirecte.FermeDirecte.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private Address adresseLivraison;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @Column(name = "numero_commande", nullable = false, unique = true)
    private String numeroCommande;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private OrderStatus statut;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_paiement", nullable = false)
    private PaymentStatus statutPaiement;

    @Column(name = "sous_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal sousTotal;

    @Column(name = "remise", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal remise = BigDecimal.ZERO;

    @Column(name = "frais_livraison", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal fraisLivraison = BigDecimal.ZERO;

    @Column(name = "total_ttc", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalTTC;

    @CreationTimestamp
    @Column(name = "date_commande", updatable = false)
    private LocalDateTime dateCommande;

    @JsonManagedReference("order-items")
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> lignes = new ArrayList<>();
}
