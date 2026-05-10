-- ========================================
-- SUPPRIMER UN COUPON DÉJÀ UTILISÉ
-- ========================================

-- PROBLÈME : "Impossible de supprimer cet élément car il est utilisé ailleurs"
-- CAUSE : Le coupon est référencé dans d'autres tables (coupon_usages, orders, etc.)

-- ========================================
-- OPTION 1 : DÉSACTIVER LE COUPON (RECOMMANDÉ)
-- ========================================
-- Au lieu de supprimer, désactivez le coupon
-- Il restera dans l'historique mais ne pourra plus être utilisé

-- Remplacer 'CODE_COUPON' par le code du coupon à désactiver
UPDATE coupons 
SET 
    actif = 0,
    bloque = 1
WHERE code = 'CODE_COUPON';

-- Vérifier
SELECT id, code, actif, bloque FROM coupons WHERE code = 'CODE_COUPON';

-- ========================================
-- OPTION 2 : SUPPRIMER COMPLÈTEMENT (ATTENTION !)
-- ========================================
-- ⚠️ ATTENTION : Cela supprimera toutes les traces du coupon
-- ⚠️ L'historique des utilisations sera perdu
-- ⚠️ À utiliser uniquement pour les coupons de test

-- Étape 1 : Trouver l'ID du coupon
SELECT id, code FROM coupons WHERE code = 'CODE_COUPON';

-- Étape 2 : Supprimer les utilisations (remplacer X par l'ID du coupon)
DELETE FROM coupon_usages WHERE coupon_id = X;

-- Étape 3 : Supprimer les catégories associées
DELETE FROM coupon_categories WHERE coupon_id = X;

-- Étape 4 : Mettre à NULL les références dans les commandes
UPDATE orders SET coupon_id = NULL WHERE coupon_id = X;

-- Étape 5 : Supprimer le coupon
DELETE FROM coupons WHERE id = X;

-- ========================================
-- OPTION 3 : SUPPRIMER TOUS LES COUPONS DE TEST
-- ========================================
-- ⚠️ ATTENTION : Cela supprime TOUS les coupons de test
-- ⚠️ À utiliser uniquement en développement

-- Supprimer les utilisations de tous les coupons de test
DELETE FROM coupon_usages 
WHERE coupon_id IN (
    SELECT id FROM coupons 
    WHERE code LIKE 'TEST%' OR code LIKE 'DEMO%'
);

-- Supprimer les catégories de tous les coupons de test
DELETE FROM coupon_categories 
WHERE coupon_id IN (
    SELECT id FROM coupons 
    WHERE code LIKE 'TEST%' OR code LIKE 'DEMO%'
);

-- Mettre à NULL les références dans les commandes
UPDATE orders 
SET coupon_id = NULL 
WHERE coupon_id IN (
    SELECT id FROM coupons 
    WHERE code LIKE 'TEST%' OR code LIKE 'DEMO%'
);

-- Supprimer les coupons de test
DELETE FROM coupons 
WHERE code LIKE 'TEST%' OR code LIKE 'DEMO%';

-- ========================================
-- VÉRIFIER LES DÉPENDANCES D'UN COUPON
-- ========================================
-- Avant de supprimer, vérifiez où le coupon est utilisé

-- Remplacer X par l'ID du coupon
SET @coupon_id = X;

-- Nombre d'utilisations
SELECT COUNT(*) as nb_utilisations 
FROM coupon_usages 
WHERE coupon_id = @coupon_id;

-- Nombre de commandes
SELECT COUNT(*) as nb_commandes 
FROM orders 
WHERE coupon_id = @coupon_id;

-- Nombre de catégories
SELECT COUNT(*) as nb_categories 
FROM coupon_categories 
WHERE coupon_id = @coupon_id;

-- Détails complets
SELECT 
    'coupon_usages' as table_name,
    COUNT(*) as count
FROM coupon_usages 
WHERE coupon_id = @coupon_id

UNION ALL

SELECT 
    'orders' as table_name,
    COUNT(*) as count
FROM orders 
WHERE coupon_id = @coupon_id

UNION ALL

SELECT 
    'coupon_categories' as table_name,
    COUNT(*) as count
FROM coupon_categories 
WHERE coupon_id = @coupon_id;

-- ========================================
-- SCRIPT COMPLET POUR SUPPRIMER UN COUPON SPÉCIFIQUE
-- ========================================
-- Remplacer 'TEST1' par le code du coupon à supprimer

-- 1. Trouver l'ID
SET @code_to_delete = 'TEST1';
SET @coupon_id = (SELECT id FROM coupons WHERE code = @code_to_delete);

-- 2. Afficher les dépendances
SELECT 
    @coupon_id as coupon_id,
    @code_to_delete as code,
    (SELECT COUNT(*) FROM coupon_usages WHERE coupon_id = @coupon_id) as usages,
    (SELECT COUNT(*) FROM orders WHERE coupon_id = @coupon_id) as commandes,
    (SELECT COUNT(*) FROM coupon_categories WHERE coupon_id = @coupon_id) as categories;

-- 3. Supprimer les dépendances
DELETE FROM coupon_usages WHERE coupon_id = @coupon_id;
DELETE FROM coupon_categories WHERE coupon_id = @coupon_id;
UPDATE orders SET coupon_id = NULL WHERE coupon_id = @coupon_id;

-- 4. Supprimer le coupon
DELETE FROM coupons WHERE id = @coupon_id;

-- 5. Vérifier
SELECT * FROM coupons WHERE code = @code_to_delete;
-- Devrait retourner 0 ligne

-- ========================================
-- NETTOYER TOUS LES COUPONS DE TEST
-- ========================================
-- Script complet pour repartir de zéro

-- Désactiver temporairement les contraintes de clés étrangères
SET FOREIGN_KEY_CHECKS = 0;

-- Supprimer tous les coupons de test
DELETE FROM coupon_usages 
WHERE coupon_id IN (SELECT id FROM coupons WHERE code LIKE 'TEST%');

DELETE FROM coupon_categories 
WHERE coupon_id IN (SELECT id FROM coupons WHERE code LIKE 'TEST%');

DELETE FROM coupons WHERE code LIKE 'TEST%';

-- Réactiver les contraintes
SET FOREIGN_KEY_CHECKS = 1;

-- Vérifier
SELECT code FROM coupons WHERE code LIKE 'TEST%';
-- Devrait retourner 0 ligne

-- ========================================
-- RECOMMANDATIONS
-- ========================================

-- ✅ RECOMMANDÉ : Désactiver au lieu de supprimer
-- UPDATE coupons SET actif = 0, bloque = 1 WHERE code = 'CODE';

-- ⚠️ ATTENTION : Supprimer uniquement en développement
-- DELETE FROM coupons WHERE code = 'CODE';

-- 🚫 NE JAMAIS : Supprimer en production sans backup
-- Toujours faire un backup avant de supprimer des données

-- ========================================
-- BACKUP AVANT SUPPRESSION
-- ========================================

-- Sauvegarder le coupon avant de le supprimer
CREATE TABLE IF NOT EXISTS coupons_backup LIKE coupons;
INSERT INTO coupons_backup SELECT * FROM coupons WHERE code = 'CODE_COUPON';

-- Sauvegarder les utilisations
CREATE TABLE IF NOT EXISTS coupon_usages_backup LIKE coupon_usages;
INSERT INTO coupon_usages_backup 
SELECT * FROM coupon_usages 
WHERE coupon_id = (SELECT id FROM coupons WHERE code = 'CODE_COUPON');

-- Restaurer si nécessaire
-- INSERT INTO coupons SELECT * FROM coupons_backup WHERE code = 'CODE_COUPON';
-- INSERT INTO coupon_usages SELECT * FROM coupon_usages_backup WHERE coupon_id = X;
