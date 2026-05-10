-- ============================================
-- DONNÉES DE TEST - COUPONS HYBRIDES
-- ============================================
-- Ce fichier contient des données de test variées
-- pour tester tous les scénarios possibles
-- ============================================

-- Nettoyer les données de test existantes (optionnel)
-- DELETE FROM coupon_usages;
-- DELETE FROM coupon_categories;
-- DELETE FROM coupons WHERE code LIKE 'TEST%';

-- ============================================
-- 1. COUPONS GLOBAUX (ADMIN)
-- ============================================

-- Coupon hybride complet : % + fixe + livraison
INSERT INTO coupons (
    code, description, 
    pourcentage_reduction, montant_fixe_reduction, livraison_gratuite,
    montant_minimum, montant_maximum_reduction,
    scope, seller_id,
    usages_max_global, usages_max_par_utilisateur,
    date_debut, date_expiration
) VALUES 
(
    'BIENVENUE2024', 
    'Coupon de bienvenue : -20% + -5 DT + livraison offerte',
    20.00, 5.00, TRUE,
    50.00, NULL,
    'GLOBAL', NULL,
    100, 1,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30 days'
),

-- Coupon pourcentage seul
(
    'PROMO15', 
    'Promotion : -15% sur tout le site',
    15.00, NULL, FALSE,
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
    'Livraison offerte sans minimum d''achat',
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
),

-- Coupon pourcentage + livraison
(
    'PROMO20PLUS', 
    'Promo spéciale : -20% + livraison offerte',
    20.00, NULL, TRUE,
    60.00, NULL,
    'GLOBAL', NULL,
    80, 1,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '20 days'
),

-- Coupon montant fixe + livraison
(
    'FIXE15PLUS', 
    'Réduction de 15 DT + livraison offerte',
    NULL, 15.00, TRUE,
    70.00, NULL,
    'GLOBAL', NULL,
    60, 1,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '25 days'
),

-- Coupon avec montant minimum élevé
(
    'VIP30', 
    'Coupon VIP : -30% pour les grosses commandes',
    30.00, NULL, FALSE,
    200.00, NULL,
    'GLOBAL', NULL,
    30, 1,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '40 days'
),

-- Coupon expiré (pour tester)
(
    'EXPIRE2023', 
    'Coupon expiré pour test',
    10.00, NULL, FALSE,
    20.00, NULL,
    'GLOBAL', NULL,
    100, 1,
    CURRENT_TIMESTAMP - INTERVAL '60 days',
    CURRENT_TIMESTAMP - INTERVAL '30 days'
),

-- Coupon inactif (pour tester)
(
    'INACTIF', 
    'Coupon désactivé pour test',
    15.00, NULL, FALSE,
    30.00, NULL,
    'GLOBAL', NULL,
    100, 1,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30 days'
);

-- Désactiver le coupon inactif
UPDATE coupons SET actif = FALSE WHERE code = 'INACTIF';

-- ============================================
-- 2. COUPONS VENDEUR (SELLER)
-- ============================================
-- Note: Remplacer <SELLER_ID> par un ID réel de vendeur

-- Exemple de coupon vendeur (décommenter et adapter)
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
),
(
    'SHOP2024', 
    'Offre spéciale boutique : -15% + livraison offerte',
    15.00, NULL, TRUE,
    40.00, NULL,
    'SELLER', <SELLER_ID>,
    50, 1,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30 days'
);
*/

-- ============================================
-- 3. COUPONS PAR CATÉGORIE
-- ============================================
-- Note: Adapter les category_id selon votre base de données

-- Coupon pour catégorie 1 (ex: Fruits)
INSERT INTO coupons (
    code, description, 
    pourcentage_reduction, montant_fixe_reduction, livraison_gratuite,
    montant_minimum, montant_maximum_reduction,
    scope, seller_id,
    usages_max_global, usages_max_par_utilisateur,
    date_debut, date_expiration
) VALUES 
(
    'FRUITS20', 
    '20% de réduction sur les fruits',
    20.00, NULL, FALSE,
    25.00, NULL,
    'GLOBAL', NULL,
    150, 3,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30 days'
);

-- Associer à la catégorie 1
INSERT INTO coupon_categories (coupon_id, category_id)
SELECT id, 1 FROM coupons WHERE code = 'FRUITS20';

-- Coupon pour catégories multiples (1 et 2)
INSERT INTO coupons (
    code, description, 
    pourcentage_reduction, montant_fixe_reduction, livraison_gratuite,
    montant_minimum, montant_maximum_reduction,
    scope, seller_id,
    usages_max_global, usages_max_par_utilisateur,
    date_debut, date_expiration
) VALUES 
(
    'BIO15', 
    '15% sur les produits bio',
    15.00, NULL, FALSE,
    30.00, NULL,
    'GLOBAL', NULL,
    200, 2,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '45 days'
);

-- Associer aux catégories 1 et 2
INSERT INTO coupon_categories (coupon_id, category_id)
SELECT id, 1 FROM coupons WHERE code = 'BIO15'
UNION ALL
SELECT id, 2 FROM coupons WHERE code = 'BIO15';

-- ============================================
-- 4. COUPONS POUR TESTS SPÉCIFIQUES
-- ============================================

-- Coupon avec limite d'utilisation très basse (pour tester épuisement)
INSERT INTO coupons (
    code, description, 
    pourcentage_reduction, montant_fixe_reduction, livraison_gratuite,
    montant_minimum, montant_maximum_reduction,
    scope, seller_id,
    usages_max_global, usages_max_par_utilisateur,
    date_debut, date_expiration
) VALUES 
(
    'LIMITE3', 
    'Coupon limité à 3 utilisations',
    10.00, NULL, FALSE,
    20.00, NULL,
    'GLOBAL', NULL,
    3, 1,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30 days'
),

-- Coupon avec montant minimum très élevé (pour tester validation)
(
    'MINIMUM500', 
    'Coupon pour grosses commandes uniquement',
    25.00, NULL, FALSE,
    500.00, NULL,
    'GLOBAL', NULL,
    20, 1,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30 days'
),

-- Coupon qui commence dans le futur (pour tester validation)
(
    'FUTUR2025', 
    'Coupon qui commence dans le futur',
    20.00, NULL, FALSE,
    30.00, NULL,
    'GLOBAL', NULL,
    100, 1,
    CURRENT_TIMESTAMP + INTERVAL '7 days',
    CURRENT_TIMESTAMP + INTERVAL '37 days'
),

-- Coupon avec plafond très bas (pour tester le plafond)
(
    'PLAFOND5', 
    'Coupon avec plafond de 5 DT',
    50.00, NULL, FALSE,
    20.00, 5.00,
    'GLOBAL', NULL,
    100, 1,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30 days'
);

-- ============================================
-- 5. STATISTIQUES ET VÉRIFICATIONS
-- ============================================

-- Voir tous les coupons créés
SELECT 
    code,
    description,
    pourcentage_reduction,
    montant_fixe_reduction,
    livraison_gratuite,
    montant_minimum,
    scope,
    usages_max_global,
    actif,
    CASE 
        WHEN actif = TRUE 
         AND bloque = FALSE 
         AND date_debut <= CURRENT_TIMESTAMP 
         AND date_expiration > CURRENT_TIMESTAMP 
         AND usages_actuels < usages_max_global 
        THEN 'VALIDE' 
        ELSE 'INVALIDE' 
    END as statut
FROM coupons
ORDER BY date_creation DESC;

-- Compter les coupons par type
SELECT 
    CASE 
        WHEN pourcentage_reduction IS NOT NULL AND montant_fixe_reduction IS NOT NULL AND livraison_gratuite = TRUE THEN 'Hybride complet'
        WHEN pourcentage_reduction IS NOT NULL AND montant_fixe_reduction IS NOT NULL THEN 'Pourcentage + Fixe'
        WHEN pourcentage_reduction IS NOT NULL AND livraison_gratuite = TRUE THEN 'Pourcentage + Livraison'
        WHEN montant_fixe_reduction IS NOT NULL AND livraison_gratuite = TRUE THEN 'Fixe + Livraison'
        WHEN pourcentage_reduction IS NOT NULL THEN 'Pourcentage seul'
        WHEN montant_fixe_reduction IS NOT NULL THEN 'Fixe seul'
        WHEN livraison_gratuite = TRUE THEN 'Livraison seule'
        ELSE 'Autre'
    END as type_coupon,
    COUNT(*) as nombre
FROM coupons
GROUP BY type_coupon
ORDER BY nombre DESC;

-- Voir les coupons avec catégories
SELECT 
    c.code,
    c.description,
    STRING_AGG(cc.category_id::TEXT, ', ') as categories
FROM coupons c
LEFT JOIN coupon_categories cc ON c.id = cc.coupon_id
GROUP BY c.id, c.code, c.description
HAVING COUNT(cc.category_id) > 0;

-- ============================================
-- 6. REQUÊTES DE TEST
-- ============================================

-- Tester la fonction can_user_use_coupon
-- Remplacer <USER_ID> par un ID réel
/*
SELECT 
    code,
    can_user_use_coupon(id, <USER_ID>) as peut_utiliser
FROM coupons
WHERE actif = TRUE;
*/

-- Voir les coupons valides actuellement
SELECT 
    code,
    description,
    pourcentage_reduction,
    montant_fixe_reduction,
    livraison_gratuite,
    montant_minimum,
    usages_actuels,
    usages_max_global
FROM coupons
WHERE actif = TRUE 
  AND bloque = FALSE 
  AND date_debut <= CURRENT_TIMESTAMP 
  AND date_expiration > CURRENT_TIMESTAMP 
  AND usages_actuels < usages_max_global
ORDER BY date_creation DESC;

-- ============================================
-- 7. SCÉNARIOS DE TEST RECOMMANDÉS
-- ============================================

/*
SCÉNARIO 1: Coupon hybride complet
- Code: BIENVENUE2024
- Panier: 100 DT
- Attendu: -20% (20 DT) + -5 DT + Livraison offerte (10 DT) = 35 DT d'économie

SCÉNARIO 2: Coupon avec plafond
- Code: MEGA50
- Panier: 100 DT
- Attendu: -50% = 50 DT, mais plafonné à 30 DT

SCÉNARIO 3: Montant minimum non atteint
- Code: BIENVENUE2024
- Panier: 30 DT
- Attendu: Erreur "Montant minimum de 50 DT requis"

SCÉNARIO 4: Coupon expiré
- Code: EXPIRE2023
- Attendu: Erreur "Ce coupon a expiré"

SCÉNARIO 5: Coupon inactif
- Code: INACTIF
- Attendu: Erreur "Ce coupon est désactivé"

SCÉNARIO 6: Limite d'utilisation atteinte
- Code: LIMITE3
- Après 3 utilisations
- Attendu: Erreur "Ce coupon a atteint sa limite d'utilisation"

SCÉNARIO 7: Coupon par catégorie
- Code: FRUITS20
- Panier avec produits de catégorie 1
- Attendu: -20% appliqué

SCÉNARIO 8: Coupon par catégorie (non applicable)
- Code: FRUITS20
- Panier sans produits de catégorie 1
- Attendu: Erreur "Ce coupon ne s'applique pas aux produits de votre panier"
*/

-- ============================================
-- FIN DES DONNÉES DE TEST
-- ============================================

SELECT 'Données de test créées avec succès!' as message,
       COUNT(*) as nombre_coupons
FROM coupons;
