-- ============================================================
-- NETTOYER LES DONNÉES DE TEST - Coupons
-- ============================================================
-- Base de données : fermedirecte
-- Date : 1er mai 2026
-- Objectif : Nettoyer toutes les données de test pour recommencer
-- ============================================================

-- ============================================================
-- ÉTAPE 1 : VÉRIFIER L'ÉTAT ACTUEL
-- ============================================================

-- 1.1 Voir les commandes avec coupon
SELECT 
    o.id,
    o.numero_commande,
    o.total_ttc,
    o.date_commande,
    c.code as coupon_code,
    u.email as client_email
FROM orders o
LEFT JOIN coupons c ON o.coupon_id = c.id
JOIN users u ON o.user_id = u.id
WHERE o.coupon_id IS NOT NULL
ORDER BY o.date_commande DESC;

-- 1.2 Voir les utilisations de coupons
SELECT 
    cu.id,
    cu.date_utilisation,
    u.email as utilisateur,
    c.code as coupon
FROM coupon_usages cu
JOIN users u ON cu.user_id = u.id
JOIN coupons c ON cu.coupon_id = c.id
ORDER BY cu.date_utilisation DESC;

-- 1.3 Voir l'état des coupons de test
SELECT 
    id,
    code,
    usages_actuels,
    usages_max_global,
    usages_max_par_utilisateur,
    actif,
    bloque
FROM coupons 
WHERE code IN ('TEST1', 'TEST2')
ORDER BY code;

-- ============================================================
-- ÉTAPE 2 : NETTOYER LES DONNÉES (ATTENTION : IRRÉVERSIBLE)
-- ============================================================

-- 2.1 Supprimer les utilisations de coupons de test
DELETE FROM coupon_usages 
WHERE coupon_id IN (
    SELECT id FROM coupons WHERE code IN ('TEST1', 'TEST2')
);

-- Vérifier
SELECT COUNT(*) as nb_utilisations_restantes FROM coupon_usages;

-- 2.2 Mettre à NULL les références de coupons dans les commandes
-- (Au lieu de supprimer les commandes, on retire juste le coupon)
UPDATE orders 
SET coupon_id = NULL 
WHERE coupon_id IN (
    SELECT id FROM coupons WHERE code IN ('TEST1', 'TEST2')
);

-- Vérifier
SELECT COUNT(*) as nb_commandes_avec_coupon 
FROM orders 
WHERE coupon_id IS NOT NULL;

-- 2.3 Réinitialiser les compteurs d'utilisation
UPDATE coupons 
SET usages_actuels = 0 
WHERE code IN ('TEST1', 'TEST2');

-- Vérifier
SELECT 
    code,
    usages_actuels,
    usages_max_global
FROM coupons 
WHERE code IN ('TEST1', 'TEST2');

-- ============================================================
-- ÉTAPE 3 : VÉRIFIER QUE TOUT EST PROPRE
-- ============================================================

-- 3.1 Vérifier qu'il n'y a plus d'utilisations de TEST1 et TEST2
SELECT COUNT(*) as nb_utilisations 
FROM coupon_usages cu
JOIN coupons c ON cu.coupon_id = c.id
WHERE c.code IN ('TEST1', 'TEST2');
-- Résultat attendu : 0

-- 3.2 Vérifier que les compteurs sont à 0
SELECT 
    code,
    usages_actuels,
    CASE 
        WHEN usages_actuels = 0 THEN '✅ OK'
        ELSE '❌ PROBLÈME'
    END as statut
FROM coupons 
WHERE code IN ('TEST1', 'TEST2');
-- Résultat attendu : usages_actuels = 0 pour les deux

-- 3.3 Vérifier que les coupons sont actifs et non bloqués
SELECT 
    code,
    actif,
    bloque,
    CASE 
        WHEN actif = 1 AND bloque = 0 THEN '✅ OK'
        ELSE '❌ PROBLÈME'
    END as statut
FROM coupons 
WHERE code IN ('TEST1', 'TEST2');
-- Résultat attendu : actif = 1, bloque = 0

-- ============================================================
-- ÉTAPE 4 : RÉACTIVER LES COUPONS (SI NÉCESSAIRE)
-- ============================================================

-- 4.1 Réactiver les coupons de test
UPDATE coupons 
SET 
    actif = 1,
    bloque = 0
WHERE code IN ('TEST1', 'TEST2');

-- 4.2 Vérifier les dates de validité
SELECT 
    code,
    DATE_FORMAT(date_debut, '%Y-%m-%d %H:%i') as debut,
    DATE_FORMAT(date_expiration, '%Y-%m-%d %H:%i') as expiration,
    CASE 
        WHEN NOW() BETWEEN date_debut AND date_expiration THEN '✅ VALIDE'
        WHEN NOW() < date_debut THEN '⏳ PAS ENCORE ACTIF'
        WHEN NOW() > date_expiration THEN '❌ EXPIRÉ'
    END as statut_date
FROM coupons 
WHERE code IN ('TEST1', 'TEST2');

-- 4.3 Prolonger la date d'expiration si nécessaire (1 an)
UPDATE coupons 
SET date_expiration = DATE_ADD(NOW(), INTERVAL 1 YEAR)
WHERE code IN ('TEST1', 'TEST2')
AND date_expiration < NOW();

-- ============================================================
-- ÉTAPE 5 : RAPPORT FINAL
-- ============================================================

-- 5.1 Résumé complet des coupons de test
SELECT 
    code,
    description,
    pourcentage_reduction,
    montant_fixe_reduction,
    livraison_gratuite,
    montant_minimum,
    usages_actuels,
    usages_max_global,
    usages_max_par_utilisateur,
    actif,
    bloque,
    DATE_FORMAT(date_debut, '%Y-%m-%d') as debut,
    DATE_FORMAT(date_expiration, '%Y-%m-%d') as expiration
FROM coupons 
WHERE code IN ('TEST1', 'TEST2')
ORDER BY code;

-- 5.2 Statistiques globales
SELECT 
    'Coupons de test' as type,
    COUNT(*) as nombre,
    SUM(usages_actuels) as total_utilisations,
    SUM(CASE WHEN actif = 1 THEN 1 ELSE 0 END) as actifs,
    SUM(CASE WHEN bloque = 1 THEN 1 ELSE 0 END) as bloques
FROM coupons 
WHERE code IN ('TEST1', 'TEST2');

-- ============================================================
-- OPTION : SUPPRIMER COMPLÈTEMENT LES COUPONS DE TEST
-- ============================================================
-- ⚠️ ATTENTION : Cette section supprime définitivement les coupons
-- ⚠️ Décommentez uniquement si vous voulez tout recommencer

/*
-- Supprimer les utilisations
DELETE FROM coupon_usages 
WHERE coupon_id IN (
    SELECT id FROM coupons WHERE code IN ('TEST1', 'TEST2')
);

-- Mettre à NULL dans les commandes
UPDATE orders 
SET coupon_id = NULL 
WHERE coupon_id IN (
    SELECT id FROM coupons WHERE code IN ('TEST1', 'TEST2')
);

-- Supprimer les associations catégories
DELETE FROM coupon_categories 
WHERE coupon_id IN (
    SELECT id FROM coupons WHERE code IN ('TEST1', 'TEST2')
);

-- Supprimer les coupons
DELETE FROM coupons 
WHERE code IN ('TEST1', 'TEST2');

-- Vérifier
SELECT COUNT(*) as nb_coupons_test 
FROM coupons 
WHERE code IN ('TEST1', 'TEST2');
-- Résultat attendu : 0
*/

-- ============================================================
-- NOTES
-- ============================================================
-- 1. Exécutez les étapes dans l'ordre
-- 2. Vérifiez chaque résultat avant de passer à l'étape suivante
-- 3. Les commandes UPDATE sont sûres (elles ne suppriment pas de données)
-- 4. La section OPTION est commentée pour éviter les suppressions accidentelles
-- 5. Après le nettoyage, redémarrez le backend
-- ============================================================
