-- ============================================
-- MIGRATION COUPONS HYBRIDES
-- ============================================

-- 1. Supprimer l'ancienne table coupons si elle existe
DROP TABLE IF EXISTS coupon_usages CASCADE;
DROP TABLE IF EXISTS coupon_categories CASCADE;
DROP TABLE IF EXISTS coupons CASCADE;

-- 2. Créer la nouvelle table coupons avec support hybride
CREATE TABLE coupons (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL,
    
    -- Réductions hybrides
    pourcentage_reduction DECIMAL(5, 2),  -- Ex: 20.00 pour 20%
    montant_fixe_reduction DECIMAL(19, 2), -- Ex: 5.00 DT
    livraison_gratuite BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Conditions d'application
    montant_minimum DECIMAL(19, 2),
    montant_maximum_reduction DECIMAL(19, 2),
    
    -- Portée et propriétaire
    scope VARCHAR(20) NOT NULL CHECK (scope IN ('GLOBAL', 'SELLER')),
    seller_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    
    -- Limites d'utilisation
    usages_max_global INTEGER NOT NULL,
    usages_actuels INTEGER NOT NULL DEFAULT 0,
    usages_max_par_utilisateur INTEGER NOT NULL DEFAULT 1,
    
    -- Dates
    date_debut TIMESTAMP NOT NULL,
    date_expiration TIMESTAMP NOT NULL,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Statut
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    bloque BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Contraintes
    CONSTRAINT check_dates CHECK (date_expiration > date_debut),
    CONSTRAINT check_reductions CHECK (
        pourcentage_reduction > 0 OR 
        montant_fixe_reduction > 0 OR 
        livraison_gratuite = TRUE
    ),
    CONSTRAINT check_seller_scope CHECK (
        (scope = 'GLOBAL' AND seller_id IS NULL) OR
        (scope = 'SELLER' AND seller_id IS NOT NULL)
    )
);

-- 3. Table pour les catégories applicables
CREATE TABLE coupon_categories (
    coupon_id BIGINT NOT NULL REFERENCES coupons(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (coupon_id, category_id)
);

-- 4. Table pour tracker les utilisations
CREATE TABLE coupon_usages (
    id BIGSERIAL PRIMARY KEY,
    coupon_id BIGINT NOT NULL REFERENCES coupons(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    montant_reduit DECIMAL(19, 2) NOT NULL,
    date_utilisation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE (coupon_id, user_id, order_id)
);

-- 5. Index pour améliorer les performances
CREATE INDEX idx_coupons_code ON coupons(UPPER(code));
CREATE INDEX idx_coupons_scope ON coupons(scope);
CREATE INDEX idx_coupons_seller ON coupons(seller_id);
CREATE INDEX idx_coupons_dates ON coupons(date_debut, date_expiration);
CREATE INDEX idx_coupons_actif ON coupons(actif, bloque);
CREATE INDEX idx_coupon_usages_coupon ON coupon_usages(coupon_id);
CREATE INDEX idx_coupon_usages_user ON coupon_usages(user_id);
CREATE INDEX idx_coupon_categories_coupon ON coupon_categories(coupon_id);

-- 6. Données de test : Coupons globaux (Admin)
INSERT INTO coupons (
    code, description, 
    pourcentage_reduction, montant_fixe_reduction, livraison_gratuite,
    montant_minimum, montant_maximum_reduction,
    scope, seller_id,
    usages_max_global, usages_max_par_utilisateur,
    date_debut, date_expiration
) VALUES 
-- Coupon hybride complet
(
    'BIENVENUE2024', 
    'Coupon de bienvenue : -20% + -5 DT + livraison offerte',
    20.00, 5.00, TRUE,
    50.00, NULL,
    'GLOBAL', NULL,
    100, 1,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30 days'
),

-- Coupon pourcentage + livraison
(
    'PROMO15', 
    'Promotion : -15% + livraison gratuite',
    15.00, NULL, TRUE,
    30.00, NULL,
    'GLOBAL', NULL,
    200, 2,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '60 days'
),

-- Coupon montant fixe seul
(
    'REDUC10', 
    'Réduction de 10 DT sur votre commande',
    NULL, 10.00, FALSE,
    40.00, NULL,
    'GLOBAL', NULL,
    150, 1,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '45 days'
),

-- Coupon livraison gratuite seule
(
    'LIVRAISONGRATUITE', 
    'Livraison offerte sans minimum',
    NULL, NULL, TRUE,
    NULL, NULL,
    'GLOBAL', NULL,
    500, 3,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '90 days'
),

-- Coupon avec plafond de réduction
(
    'MEGA50', 
    'Méga promo : -50% (max 30 DT de réduction)',
    50.00, NULL, FALSE,
    100.00, 30.00,
    'GLOBAL', NULL,
    50, 1,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '15 days'
);

-- 7. Exemple de coupon vendeur (à adapter avec un vrai seller_id)
-- Décommenter et remplacer <SELLER_ID> par un ID réel
/*
INSERT INTO coupons (
    code, description, 
    pourcentage_reduction, montant_fixe_reduction, livraison_gratuite,
    montant_minimum, montant_maximum_reduction,
    scope, seller_id,
    usages_max_global, usages_max_par_utilisateur,
    date_debut, date_expiration
) VALUES 
(
    'MABOUTIQUE10', 
    'Coupon boutique : -10% sur tous mes produits',
    10.00, NULL, FALSE,
    20.00, NULL,
    'SELLER', <SELLER_ID>,
    100, 2,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30 days'
);
*/

-- 8. Vue pour les statistiques des coupons
CREATE OR REPLACE VIEW v_coupon_stats AS
SELECT 
    c.id,
    c.code,
    c.description,
    c.scope,
    c.usages_actuels,
    c.usages_max_global,
    ROUND((c.usages_actuels::DECIMAL / NULLIF(c.usages_max_global, 0)) * 100, 2) as taux_utilisation,
    COUNT(DISTINCT cu.user_id) as utilisateurs_uniques,
    COALESCE(SUM(cu.montant_reduit), 0) as montant_total_economise,
    c.actif,
    c.bloque,
    CASE 
        WHEN c.actif = TRUE 
         AND c.bloque = FALSE 
         AND c.date_debut <= CURRENT_TIMESTAMP 
         AND c.date_expiration > CURRENT_TIMESTAMP 
         AND c.usages_actuels < c.usages_max_global 
        THEN TRUE 
        ELSE FALSE 
    END as est_valide
FROM coupons c
LEFT JOIN coupon_usages cu ON c.id = cu.coupon_id
GROUP BY c.id;

-- 9. Fonction pour vérifier si un utilisateur peut utiliser un coupon
CREATE OR REPLACE FUNCTION can_user_use_coupon(
    p_coupon_id BIGINT,
    p_user_id BIGINT
) RETURNS BOOLEAN AS $$
DECLARE
    v_coupon RECORD;
    v_usage_count INTEGER;
BEGIN
    -- Récupérer le coupon
    SELECT * INTO v_coupon FROM coupons WHERE id = p_coupon_id;
    
    IF NOT FOUND THEN
        RETURN FALSE;
    END IF;
    
    -- Vérifier si le coupon est valide
    IF v_coupon.actif = FALSE OR v_coupon.bloque = TRUE THEN
        RETURN FALSE;
    END IF;
    
    IF v_coupon.date_debut > CURRENT_TIMESTAMP OR v_coupon.date_expiration <= CURRENT_TIMESTAMP THEN
        RETURN FALSE;
    END IF;
    
    IF v_coupon.usages_actuels >= v_coupon.usages_max_global THEN
        RETURN FALSE;
    END IF;
    
    -- Vérifier les utilisations de l'utilisateur
    SELECT COUNT(*) INTO v_usage_count
    FROM coupon_usages
    WHERE coupon_id = p_coupon_id AND user_id = p_user_id;
    
    IF v_usage_count >= v_coupon.usages_max_par_utilisateur THEN
        RETURN FALSE;
    END IF;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- 10. Trigger pour mettre à jour automatiquement usages_actuels
CREATE OR REPLACE FUNCTION update_coupon_usage_count()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE coupons 
    SET usages_actuels = usages_actuels + 1
    WHERE id = NEW.coupon_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_coupon_usage
AFTER INSERT ON coupon_usages
FOR EACH ROW
EXECUTE FUNCTION update_coupon_usage_count();

-- Fin de la migration
SELECT 'Migration des coupons hybrides terminée avec succès!' as message;
