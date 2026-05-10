-- ============================================
-- REQUÊTES MYSQL À EXÉCUTER MANUELLEMENT UNE PAR UNE
-- ============================================
-- ⚠️ CE FICHIER EST POUR MYSQL + PHPMYADMIN
-- Copiez et exécutez chaque bloc séparément
-- ============================================

-- ============================================
-- BLOC 1 : Trouver le nom de la contrainte externe
-- ============================================
SELECT 
    TABLE_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE REFERENCED_TABLE_NAME = 'coupons'
AND TABLE_SCHEMA = 'fermedirecte';

-- NOTEZ LE NOM DE LA CONTRAINTE (ex: orders_ibfk_1)
-- Vous allez l'utiliser dans le BLOC 2


-- ============================================
-- BLOC 2 : Supprimer la contrainte externe
-- ============================================
-- REMPLACEZ 'NOM_CONTRAINTE_ICI' par le nom trouvé au BLOC 1
ALTER TABLE orders DROP FOREIGN KEY NOM_CONTRAINTE_ICI;


-- ============================================
-- BLOC 3 : Supprimer l'ancienne backup
-- ============================================
DROP TABLE IF EXISTS coupons_old_backup;


-- ============================================
-- BLOC 4 : Renommer la table coupons actuelle
-- ============================================
RENAME TABLE coupons TO coupons_old_backup;


-- ============================================
-- BLOC 5 : Créer la nouvelle table coupons (MYSQL)
-- ============================================
CREATE TABLE coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL,
    pourcentage_reduction DECIMAL(5, 2),
    montant_fixe_reduction DECIMAL(19, 2),
    livraison_gratuite BOOLEAN NOT NULL DEFAULT FALSE,
    montant_minimum DECIMAL(19, 2),
    montant_maximum_reduction DECIMAL(19, 2),
    scope VARCHAR(20) NOT NULL,
    seller_id BIGINT,
    usages_max_global INT NOT NULL,
    usages_actuels INT NOT NULL DEFAULT 0,
    usages_max_par_utilisateur INT NOT NULL DEFAULT 1,
    date_debut DATETIME NOT NULL,
    date_expiration DATETIME NOT NULL,
    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    bloque BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_coupons_seller FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================
-- BLOC 6 : Créer la table coupon_categories (MYSQL)
-- ============================================
CREATE TABLE coupon_categories (
    coupon_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (coupon_id, category_id),
    CONSTRAINT fk_coupon_categories_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================
-- BLOC 7 : Créer la table coupon_usages (MYSQL)
-- ============================================
CREATE TABLE coupon_usages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    coupon_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    montant_reduit DECIMAL(19, 2) NOT NULL,
    date_utilisation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_usage (coupon_id, user_id, order_id),
    CONSTRAINT fk_coupon_usages_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE,
    CONSTRAINT fk_coupon_usages_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_coupon_usages_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================
-- BLOC 8 : Créer les index (MYSQL)
-- ============================================
CREATE INDEX idx_coupons_code ON coupons(code);
CREATE INDEX idx_coupons_scope ON coupons(scope);
CREATE INDEX idx_coupons_seller ON coupons(seller_id);
CREATE INDEX idx_coupons_dates ON coupons(date_debut, date_expiration);
CREATE INDEX idx_coupons_actif ON coupons(actif, bloque);
CREATE INDEX idx_coupon_usages_coupon ON coupon_usages(coupon_id);
CREATE INDEX idx_coupon_usages_user ON coupon_usages(user_id);


-- ============================================
-- BLOC 9 : Recréer la contrainte externe dans orders (MYSQL)
-- ============================================
ALTER TABLE orders 
ADD CONSTRAINT fk_orders_coupon 
FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE SET NULL;


-- ============================================
-- BLOC 10 : Insérer le coupon 1 - BIENVENUE2024 (MYSQL)
-- ============================================
INSERT INTO coupons (
    code, description, 
    pourcentage_reduction, montant_fixe_reduction, livraison_gratuite,
    montant_minimum, montant_maximum_reduction,
    scope, seller_id,
    usages_max_global, usages_actuels, usages_max_par_utilisateur,
    date_debut, date_expiration
) VALUES (
    'BIENVENUE2024', 
    'Coupon de bienvenue : -20% + -5 DT + livraison offerte',
    20.00, 5.00, TRUE,
    50.00, NULL,
    'GLOBAL', NULL,
    100, 0, 1,
    NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)
);


-- ============================================
-- BLOC 11 : Insérer le coupon 2 - PROMO15 (MYSQL)
-- ============================================
INSERT INTO coupons (
    code, description, 
    pourcentage_reduction, montant_fixe_reduction, livraison_gratuite,
    montant_minimum, montant_maximum_reduction,
    scope, seller_id,
    usages_max_global, usages_actuels, usages_max_par_utilisateur,
    date_debut, date_expiration
) VALUES (
    'PROMO15', 
    'Promotion : -15% sur tout le site',
    15.00, NULL, FALSE,
    30.00, NULL,
    'GLOBAL', NULL,
    200, 0, 2,
    NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY)
);


-- ============================================
-- BLOC 12 : Insérer le coupon 3 - REDUC10 (MYSQL)
-- ============================================
INSERT INTO coupons (
    code, description, 
    pourcentage_reduction, montant_fixe_reduction, livraison_gratuite,
    montant_minimum, montant_maximum_reduction,
    scope, seller_id,
    usages_max_global, usages_actuels, usages_max_par_utilisateur,
    date_debut, date_expiration
) VALUES (
    'REDUC10', 
    'Réduction de 10 DT sur votre commande',
    NULL, 10.00, FALSE,
    40.00, NULL,
    'GLOBAL', NULL,
    150, 0, 1,
    NOW(), DATE_ADD(NOW(), INTERVAL 45 DAY)
);


-- ============================================
-- BLOC 13 : Insérer le coupon 4 - LIVRAISONGRATUITE (MYSQL)
-- ============================================
INSERT INTO coupons (
    code, description, 
    pourcentage_reduction, montant_fixe_reduction, livraison_gratuite,
    montant_minimum, montant_maximum_reduction,
    scope, seller_id,
    usages_max_global, usages_actuels, usages_max_par_utilisateur,
    date_debut, date_expiration
) VALUES (
    'LIVRAISONGRATUITE', 
    'Livraison offerte sans minimum',
    NULL, NULL, TRUE,
    NULL, NULL,
    'GLOBAL', NULL,
    500, 0, 3,
    NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY)
);


-- ============================================
-- BLOC 14 : Insérer le coupon 5 - MEGA50 (MYSQL)
-- ============================================
INSERT INTO coupons (
    code, description, 
    pourcentage_reduction, montant_fixe_reduction, livraison_gratuite,
    montant_minimum, montant_maximum_reduction,
    scope, seller_id,
    usages_max_global, usages_actuels, usages_max_par_utilisateur,
    date_debut, date_expiration
) VALUES (
    'MEGA50', 
    'Méga promo : -50% (max 30 DT de réduction)',
    50.00, NULL, FALSE,
    100.00, 30.00,
    'GLOBAL', NULL,
    50, 0, 1,
    NOW(), DATE_ADD(NOW(), INTERVAL 15 DAY)
);


-- ============================================
-- BLOC 15 : Vérifier les coupons créés
-- ============================================
SELECT 
    code,
    description,
    pourcentage_reduction,
    montant_fixe_reduction,
    livraison_gratuite,
    montant_minimum,
    usages_max_global,
    actif
FROM coupons
ORDER BY date_creation DESC;


-- ============================================
-- FIN - Vous devriez voir 5 coupons
-- ============================================
