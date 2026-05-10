-- ============================================================
-- AJOUTER LA COLONNE REMISE DANS LA TABLE ORDERS
-- ============================================================
-- Base de données : fermedirecte
-- Date : 1er mai 2026
-- Objectif : Stocker le montant de la réduction appliquée par le coupon
-- ============================================================

-- Vérifier la structure actuelle
DESCRIBE orders;

-- Ajouter la colonne remise (si elle n'existe pas déjà)
ALTER TABLE orders 
ADD COLUMN remise DECIMAL(19,2) NOT NULL DEFAULT 0.00 
AFTER sous_total;

-- Vérifier que la colonne a été ajoutée
DESCRIBE orders;

-- Vérifier les données existantes
SELECT 
    id,
    numero_commande,
    sous_total,
    remise,
    frais_livraison,
    total_ttc,
    coupon_id
FROM orders
ORDER BY date_commande DESC
LIMIT 5;

-- ============================================================
-- RECALCULER LA REMISE POUR LES COMMANDES EXISTANTES
-- ============================================================

-- Pour les commandes avec coupon, recalculer la remise
-- Formule : remise = sous_total + frais_livraison - total_ttc

UPDATE orders
SET remise = sous_total + frais_livraison - total_ttc
WHERE coupon_id IS NOT NULL
AND remise = 0;

-- Vérifier le résultat
SELECT 
    o.numero_commande,
    o.sous_total,
    o.remise,
    o.frais_livraison,
    o.total_ttc,
    c.code as coupon_code,
    -- Vérification : sous_total - remise + frais_livraison = total_ttc
    (o.sous_total - o.remise + o.frais_livraison) as total_recalcule,
    CASE 
        WHEN ABS((o.sous_total - o.remise + o.frais_livraison) - o.total_ttc) < 0.01 
        THEN '✅ OK' 
        ELSE '❌ ERREUR' 
    END as verification
FROM orders o
LEFT JOIN coupons c ON o.coupon_id = c.id
WHERE o.coupon_id IS NOT NULL
ORDER BY o.date_commande DESC;

-- ============================================================
-- NOTES
-- ============================================================
-- 1. La colonne remise stocke le montant total de la réduction
-- 2. Le calcul final est : total_ttc = sous_total - remise + frais_livraison
-- 3. Si le coupon offre la livraison gratuite, frais_livraison = 0
-- 4. La remise peut inclure : réduction %, réduction fixe, et/ou livraison gratuite
-- ============================================================
