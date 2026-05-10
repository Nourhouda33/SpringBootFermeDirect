-- ============================================================
-- FermeDirecte — Schéma SQL propre et normalisé
-- Base : MySQL 8.x
-- Généré après analyse complète du backend Spring Boot
-- ============================================================
-- Ordre de création respectant les dépendances FK
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
SET NAMES utf8mb4;

-- ============================================================
-- 0. Création de la base
-- ============================================================
CREATE DATABASE IF NOT EXISTS fermedirecte
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE fermedirecte;

-- ============================================================
-- 1. USERS
-- Table centrale : Admin, Seller, Customer
-- ============================================================
CREATE TABLE users (
                       id             BIGINT          NOT NULL AUTO_INCREMENT,
                       email          VARCHAR(255)    NOT NULL,
                       mot_de_passe   VARCHAR(255)    NOT NULL,           -- BCrypt hash
                       prenom         VARCHAR(100)    NOT NULL,
                       nom            VARCHAR(100)    NOT NULL,
                       telephone      VARCHAR(20),
                       role           ENUM('ADMIN','SELLER','CUSTOMER') NOT NULL,
                       actif          BOOLEAN         NOT NULL DEFAULT TRUE,
                       refresh_token  VARCHAR(512),                       -- stocké haché (SHA-256)
                       date_creation  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       PRIMARY KEY (id),
                       UNIQUE KEY uq_users_email (email),
                       KEY idx_users_role (role),
                       KEY idx_users_actif (actif)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 2. SELLER_PROFILES
-- Un vendeur = un User de rôle SELLER + un SellerProfile
-- ============================================================
CREATE TABLE seller_profiles (
                                 id            BIGINT          NOT NULL AUTO_INCREMENT,
                                 user_id       BIGINT          NOT NULL,
                                 nom_boutique  VARCHAR(255)    NOT NULL,
                                 description   TEXT,
                                 logo          VARCHAR(512),
                                 note          DECIMAL(3,2)    NOT NULL DEFAULT 0.00,   -- ex: 4.75
                                 date_creation DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 PRIMARY KEY (id),
                                 UNIQUE KEY uq_seller_user (user_id),
                                 KEY idx_seller_note (note),
                                 CONSTRAINT fk_seller_user
                                     FOREIGN KEY (user_id) REFERENCES users(id)
                                         ON DELETE CASCADE ON UPDATE CASCADE,
                                 CONSTRAINT chk_seller_note CHECK (note >= 0 AND note <= 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 3. ADDRESSES
-- Adresses de livraison d'un utilisateur
-- ============================================================
CREATE TABLE addresses (
                           id           BIGINT       NOT NULL AUTO_INCREMENT,
                           user_id      BIGINT       NOT NULL,
                           rue          VARCHAR(255) NOT NULL,
                           ville        VARCHAR(100) NOT NULL,
                           code_postal  VARCHAR(20)  NOT NULL,
                           pays         VARCHAR(100) NOT NULL DEFAULT 'Tunisie',
                           principal    BOOLEAN      NOT NULL DEFAULT FALSE,

                           PRIMARY KEY (id),
                           KEY idx_address_user (user_id),
                           CONSTRAINT fk_address_user
                               FOREIGN KEY (user_id) REFERENCES users(id)
                                   ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 4. CATEGORIES
-- Auto-référencée : catégorie parent → sous-catégories
-- ============================================================
CREATE TABLE categories (
                            id          BIGINT       NOT NULL AUTO_INCREMENT,
                            nom         VARCHAR(150) NOT NULL,
                            description TEXT,
                            parent_id   BIGINT,                     -- NULL = catégorie racine

                            PRIMARY KEY (id),
                            UNIQUE KEY uq_category_nom (nom),
                            KEY idx_category_parent (parent_id),
                            CONSTRAINT fk_category_parent
                                FOREIGN KEY (parent_id) REFERENCES categories(id)
                                    ON DELETE SET NULL ON UPDATE CASCADE   -- ⚠️ corrigé : pas de cascade destructive
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 5. PRODUCTS
-- Produits agricoles vendus par un SellerProfile
-- ============================================================
CREATE TABLE products (
                          id               BIGINT          NOT NULL AUTO_INCREMENT,
                          seller_profile_id BIGINT         NOT NULL,
                          nom              VARCHAR(255)    NOT NULL,
                          description      TEXT,
                          prix             DECIMAL(19,2)   NOT NULL,
                          prix_promo       DECIMAL(19,2),
                          stock            INT             NOT NULL DEFAULT 0,
                          actif            BOOLEAN         NOT NULL DEFAULT TRUE,
                          image_url        VARCHAR(512),
                          unite            VARCHAR(50),               -- ex: kg, litre, pièce
                          date_creation    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          PRIMARY KEY (id),
                          KEY idx_product_seller (seller_profile_id),
                          KEY idx_product_actif (actif),
                          KEY idx_product_prix (prix),
                          CONSTRAINT fk_product_seller
                              FOREIGN KEY (seller_profile_id) REFERENCES seller_profiles(id)
                                  ON DELETE CASCADE ON UPDATE CASCADE,
                          CONSTRAINT chk_product_prix  CHECK (prix > 0),
                          CONSTRAINT chk_product_prix_promo CHECK (prix_promo IS NULL OR (prix_promo > 0 AND prix_promo < prix)),
                          CONSTRAINT chk_product_stock CHECK (stock >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 6. PRODUCT_CATEGORIES (table de jonction N-N)
-- Un produit peut appartenir à plusieurs catégories
-- ============================================================
CREATE TABLE product_categories (
                                    product_id  BIGINT NOT NULL,
                                    category_id BIGINT NOT NULL,

                                    PRIMARY KEY (product_id, category_id),
                                    CONSTRAINT fk_pc_product
                                        FOREIGN KEY (product_id) REFERENCES products(id)
                                            ON DELETE CASCADE ON UPDATE CASCADE,
                                    CONSTRAINT fk_pc_category
                                        FOREIGN KEY (category_id) REFERENCES categories(id)
                                            ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 7. PRODUCT_VARIANTS
-- Variantes d'un produit (ex: poids=500g, conditionnement=caisse)
-- Contrainte d'unicité : pas deux fois le même attribut+valeur
-- ============================================================
CREATE TABLE product_variants (
                                  id                  BIGINT        NOT NULL AUTO_INCREMENT,
                                  product_id          BIGINT        NOT NULL,
                                  attribut            VARCHAR(100)  NOT NULL,  -- ex: "poids"
                                  valeur              VARCHAR(100)  NOT NULL,  -- ex: "500g"
                                  stock_supplementaire INT          NOT NULL DEFAULT 0,
                                  prix_delta          DECIMAL(19,2) NOT NULL DEFAULT 0.00,  -- différentiel de prix

                                  PRIMARY KEY (id),
                                  UNIQUE KEY uq_variant_product_attr_val (product_id, attribut, valeur),  -- ✅ corrigé
                                  KEY idx_variant_product (product_id),
                                  CONSTRAINT fk_variant_product
                                      FOREIGN KEY (product_id) REFERENCES products(id)
                                          ON DELETE CASCADE ON UPDATE CASCADE,
                                  CONSTRAINT chk_variant_stock CHECK (stock_supplementaire >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 8. COUPONS
-- Codes de réduction (pourcentage ou montant fixe)
-- ============================================================
CREATE TABLE coupons (
                         id              BIGINT        NOT NULL AUTO_INCREMENT,
                         code            VARCHAR(50)   NOT NULL,
                         type            ENUM('PERCENT','FIXED') NOT NULL,
                         valeur          DECIMAL(19,2) NOT NULL,
                         date_expiration DATETIME      NOT NULL,
                         usages_max      INT           NOT NULL,
                         usages_actuels  INT           NOT NULL DEFAULT 0,
                         actif           BOOLEAN       NOT NULL DEFAULT TRUE,

                         PRIMARY KEY (id),
                         UNIQUE KEY uq_coupon_code (code),
                         KEY idx_coupon_actif_expiration (actif, date_expiration),
                         CONSTRAINT chk_coupon_valeur      CHECK (valeur > 0),
                         CONSTRAINT chk_coupon_usages_max  CHECK (usages_max > 0),
                         CONSTRAINT chk_coupon_usages_curr CHECK (usages_actuels >= 0),
                         CONSTRAINT chk_coupon_percent     CHECK (type <> 'PERCENT' OR valeur <= 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 9. CARTS
-- Un panier actif par utilisateur (relation 1-1)
-- ============================================================
CREATE TABLE carts (
                       id                BIGINT   NOT NULL AUTO_INCREMENT,
                       user_id           BIGINT   NOT NULL,
                       date_modification DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                           ON UPDATE CURRENT_TIMESTAMP,

                       PRIMARY KEY (id),
                       UNIQUE KEY uq_cart_user (user_id),
                       CONSTRAINT fk_cart_user
                           FOREIGN KEY (user_id) REFERENCES users(id)
                               ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 10. CART_ITEMS
-- Lignes d'un panier
-- CORRECTION : product_variant_id est optionnel MAIS doit cohérer avec product_id
-- ============================================================
CREATE TABLE cart_items (
                            id                 BIGINT NOT NULL AUTO_INCREMENT,
                            cart_id            BIGINT NOT NULL,
                            product_id         BIGINT NOT NULL,
                            product_variant_id BIGINT,           -- NULL si pas de variante choisie
                            quantite           INT    NOT NULL,

                            PRIMARY KEY (id),
    -- Un même produit (+ variante) ne peut apparaître qu'une fois par panier
                            UNIQUE KEY uq_cart_item (cart_id, product_id, product_variant_id),
                            KEY idx_cart_item_cart    (cart_id),
                            KEY idx_cart_item_product (product_id),
                            CONSTRAINT fk_cart_item_cart
                                FOREIGN KEY (cart_id) REFERENCES carts(id)
                                    ON DELETE CASCADE ON UPDATE CASCADE,
                            CONSTRAINT fk_cart_item_product
                                FOREIGN KEY (product_id) REFERENCES products(id)
                                    ON DELETE CASCADE ON UPDATE CASCADE,
                            CONSTRAINT fk_cart_item_variant
                                FOREIGN KEY (product_variant_id) REFERENCES product_variants(id)
                                    ON DELETE SET NULL ON UPDATE CASCADE,
                            CONSTRAINT chk_cart_item_quantite CHECK (quantite > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 11. ORDERS
-- Commandes passées
-- ============================================================
CREATE TABLE orders (
                        id                BIGINT        NOT NULL AUTO_INCREMENT,
                        user_id           BIGINT        NOT NULL,
                        address_id        BIGINT        NOT NULL,
                        coupon_id         BIGINT,
                        numero_commande   VARCHAR(50)   NOT NULL,
                        statut            ENUM('PENDING','PAID','PROCESSING','SHIPPED','DELIVERED','CANCELLED') NOT NULL DEFAULT 'PENDING',
                        statut_paiement   ENUM('PENDING','PAID','REFUNDED') NOT NULL DEFAULT 'PENDING',
                        sous_total        DECIMAL(19,2) NOT NULL,
                        frais_livraison   DECIMAL(19,2) NOT NULL DEFAULT 0.00,
                        total_ttc         DECIMAL(19,2) NOT NULL,
                        date_commande     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        PRIMARY KEY (id),
                        UNIQUE KEY uq_order_numero (numero_commande),
                        KEY idx_order_user    (user_id),
                        KEY idx_order_statut  (statut),
                        KEY idx_order_date    (date_commande),
                        CONSTRAINT fk_order_user
                            FOREIGN KEY (user_id) REFERENCES users(id)
                                ON DELETE RESTRICT ON UPDATE CASCADE,
                        CONSTRAINT fk_order_address
                            FOREIGN KEY (address_id) REFERENCES addresses(id)
                                ON DELETE RESTRICT ON UPDATE CASCADE,
                        CONSTRAINT fk_order_coupon
                            FOREIGN KEY (coupon_id) REFERENCES coupons(id)
                                ON DELETE SET NULL ON UPDATE CASCADE,
                        CONSTRAINT chk_order_sous_total    CHECK (sous_total >= 0),
                        CONSTRAINT chk_order_frais         CHECK (frais_livraison >= 0),
                        CONSTRAINT chk_order_total         CHECK (total_ttc >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 12. ORDER_ITEMS
-- Lignes d'une commande (snapshot prix au moment de la commande)
-- ============================================================
CREATE TABLE order_items (
                             id                 BIGINT        NOT NULL AUTO_INCREMENT,
                             order_id           BIGINT        NOT NULL,
                             product_id         BIGINT        NOT NULL,
                             product_variant_id BIGINT,
                             quantite           INT           NOT NULL,
                             prix_unitaire      DECIMAL(19,2) NOT NULL,   -- prix figé au moment de la commande

                             PRIMARY KEY (id),
                             KEY idx_order_item_order   (order_id),
                             KEY idx_order_item_product (product_id),
                             CONSTRAINT fk_order_item_order
                                 FOREIGN KEY (order_id) REFERENCES orders(id)
                                     ON DELETE CASCADE ON UPDATE CASCADE,
                             CONSTRAINT fk_order_item_product
                                 FOREIGN KEY (product_id) REFERENCES products(id)
                                     ON DELETE RESTRICT ON UPDATE CASCADE,
                             CONSTRAINT fk_order_item_variant
                                 FOREIGN KEY (product_variant_id) REFERENCES product_variants(id)
                                     ON DELETE SET NULL ON UPDATE CASCADE,
                             CONSTRAINT chk_order_item_quantite    CHECK (quantite > 0),
                             CONSTRAINT chk_order_item_prix        CHECK (prix_unitaire > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 13. REVIEWS
-- Avis clients sur les produits
-- ============================================================
CREATE TABLE reviews (
                         id            BIGINT   NOT NULL AUTO_INCREMENT,
                         user_id       BIGINT   NOT NULL,
                         product_id    BIGINT   NOT NULL,
                         note          TINYINT  NOT NULL,          -- 1 à 5
                         commentaire   TEXT,
                         approuve      BOOLEAN  NOT NULL DEFAULT FALSE,
                         date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         PRIMARY KEY (id),
    -- Un utilisateur ne peut noter un produit qu'une seule fois
                         UNIQUE KEY uq_review_user_product (user_id, product_id),
                         KEY idx_review_product  (product_id),
                         KEY idx_review_approuve (approuve),
                         CONSTRAINT fk_review_user
                             FOREIGN KEY (user_id) REFERENCES users(id)
                                 ON DELETE CASCADE ON UPDATE CASCADE,
                         CONSTRAINT fk_review_product
                             FOREIGN KEY (product_id) REFERENCES products(id)
                                 ON DELETE CASCADE ON UPDATE CASCADE,
                         CONSTRAINT chk_review_note CHECK (note BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- FIN DU SCHEMA
-- ============================================================
-- Ordre de création respecté :
-- 1. users
-- 2. seller_profiles     (dépend de users)
-- 3. addresses           (dépend de users)
-- 4. categories          (auto-référencée)
-- 5. products            (dépend de seller_profiles)
-- 6. product_categories  (dépend de products + categories)
-- 7. product_variants    (dépend de products)
-- 8. coupons             (indépendant)
-- 9. carts               (dépend de users)
-- 10. cart_items         (dépend de carts + products + product_variants)
-- 11. orders             (dépend de users + addresses + coupons)
-- 12. order_items        (dépend de orders + products + product_variants)
-- 13. reviews            (dépend de users + products)
-- ============================================================