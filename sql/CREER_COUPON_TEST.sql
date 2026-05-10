-- ========================================
-- CRÉER UN COUPON DE TEST POUR LE CHECKOUT
-- ========================================

-- 1. Vérifier si le coupon existe déjà
SELECT 
    id,
    code,
    description,
    pourcentage_reduction,
    montant_fixe_reduction,
    livraison_gratuite,
    actif,
    bloque,
    date_debut,
    date_expiration,
    usages_actuels,
    usages_max_global
FROM coupons 
WHERE code = 'BIENVENUE10';

-- Si le coupon n'existe pas, le créer :

-- 2. Supprimer l'ancien coupon s'il existe (optionnel)
-- DELETE FROM coupon_usages WHERE coupon_id IN (SELECT id FROM coupons WHERE code = 'BIENVENUE10');
-- DELETE FROM coupon_categories WHERE coupon_id IN (SELECT id FROM coupons WHERE code = 'BIENVENUE10');
-- DELETE FROM coupons WHERE code = 'BIENVENUE10';

-- 3. Créer le coupon BIENVENUE10 (10% de réduction)
INSERT INTO coupons (
    code,
    description,
    pourcentage_reduction,
    montant_fixe_reduction,
    livraison_gratuite,
    montant_minimum,
    montant_maximum_reduction,
    scope,
    vendeur_id,
    usages_max_global,
    usages_actuels,
    usages_max_par_utilisateur,
    date_debut,
    date_expiration,
    date_creation,
    actif,
    bloque
) VALUES (
    'BIENVENUE10',                          -- code
    'Réduction de 10% pour tous',          -- description
    10.0,                                   -- pourcentage_reduction (10%)
    NULL,                                   -- montant_fixe_reduction
    0,                                      -- livraison_gratuite (false)
    0.0,                                    -- montant_minimum (pas de minimum)
    NULL,                                   -- montant_maximum_reduction (pas de maximum)
    'GLOBAL',                               -- scope (pour tous les vendeurs)
    NULL,                                   -- vendeur_id (NULL = global)
    1000,                                   -- usages_max_global
    0,                                      -- usages_actuels
    10,                                     -- usages_max_par_utilisateur
    '2026-01-01 00:00:00',                 -- date_debut
    '2026-12-31 23:59:59',                 -- date_expiration
    NOW(),                                  -- date_creation
    1,                                      -- actif (true)
    0                                       -- bloque (false)
);

-- 4. Créer d'autres coupons de test

-- Coupon avec montant fixe
INSERT INTO coupons (
    code, description, pourcentage_reduction, montant_fixe_reduction,
    livraison_gratuite, montant_minimum, scope, vendeur_id,
    usages_max_global, usages_actuels, usages_max_par_utilisateur,
    date_debut, date_expiration, date_creation, actif, bloque
) VALUES (
    'FIXE5DT', 'Réduction de 5 DT', NULL, 5.0,
    0, 20.0, 'GLOBAL', NULL,
    500, 0, 5,
    '2026-01-01', '2026-12-31', NOW(), 1, 0
);

-- Coupon avec livraison gratuite
INSERT INTO coupons (
    code, description, pourcentage_reduction, montant_fixe_reduction,
    livraison_gratuite, montant_minimum, scope, vendeur_id,
    usages_max_global, usages_actuels, usages_max_par_utilisateur,
    date_debut, date_expiration, date_creation, actif, bloque
) VALUES (
    'LIVRAISONGRATUITE', 'Livraison offerte', NULL, NULL,
    1, 30.0, 'GLOBAL', NULL,
    200, 0, 3,
    '2026-01-01', '2026-12-31', NOW(), 1, 0
);

-- Coupon hybride (10% + 5 DT + livraison gratuite)
INSERT INTO coupons (
    code, description, pourcentage_reduction, montant_fixe_reduction,
    livraison_gratuite, montant_minimum, scope, vendeur_id,
    usages_max_global, usages_actuels, usages_max_par_utilisateur,
    date_debut, date_expiration, date_creation, actif, bloque
) VALUES (
    'SUPER20', 'Super réduction : 10% + 5 DT + livraison gratuite', 10.0, 5.0,
    1, 50.0, 'GLOBAL', NULL,
    100, 0, 2,
    '2026-01-01', '2026-12-31', NOW(), 1, 0
);

-- Coupon avec réduction de 20%
INSERT INTO coupons (
    code, description, pourcentage_reduction, montant_fixe_reduction,
    livraison_gratuite, montant_minimum, scope, vendeur_id,
    usages_max_global, usages_actuels, usages_max_par_utilisateur,
    date_debut, date_expiration, date_creation, actif, bloque
) VALUES (
    'PROMO20', 'Réduction de 20%', 20.0, NULL,
    0, 15.0, 'GLOBAL', NULL,
    300, 0, 5,
    '2026-01-01', '2026-12-31', NOW(), 1, 0
);

-- 5. Vérifier que les coupons ont été créés
SELECT 
    id,
    code,
    description,
    pourcentage_reduction,
    montant_fixe_reduction,
    livraison_gratuite,
    montant_minimum,
    actif,
    bloque,
    DATE_FORMAT(date_debut, '%Y-%m-%d') as debut,
    DATE_FORMAT(date_expiration, '%Y-%m-%d') as expiration,
    usages_actuels,
    usages_max_global
FROM coupons 
WHERE code IN ('BIENVENUE10', 'FIXE5DT', 'LIVRAISONGRATUITE', 'SUPER20', 'PROMO20')
ORDER BY code;

-- 6. Statistiques
SELECT 
    COUNT(*) as total_coupons,
    SUM(CASE WHEN actif = 1 THEN 1 ELSE 0 END) as coupons_actifs,
    SUM(CASE WHEN bloque = 1 THEN 1 ELSE 0 END) as coupons_bloques,
    SUM(CASE WHEN date_expiration < NOW() THEN 1 ELSE 0 END) as coupons_expires
FROM coupons;

-- ========================================
-- RÉSULTAT ATTENDU
-- ========================================
-- Vous devriez voir 5 coupons créés :
-- 1. BIENVENUE10    : 10% de réduction
-- 2. FIXE5DT        : 5 DT de réduction (minimum 20 DT)
-- 3. LIVRAISONGRATUITE : Livraison gratuite (minimum 30 DT)
-- 4. SUPER20        : 10% + 5 DT + livraison gratuite (minimum 50 DT)
-- 5. PROMO20        : 20% de réduction (minimum 15 DT)
--
-- Tous doivent avoir :
-- - actif = 1
-- - bloque = 0
-- - date_debut <= aujourd'hui
-- - date_expiration >= aujourd'hui
-- ========================================
