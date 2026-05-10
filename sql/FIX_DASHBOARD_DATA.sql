-- ============================================================
-- Script de Correction Rapide pour le Tableau de Bord
-- ============================================================
-- Ce script met à jour vos données existantes pour que
-- le tableau de bord affiche correctement toutes les sections
-- ============================================================

-- ÉTAPE 1 : Trouver votre seller_profile_id
-- Copiez le résultat de cette requête
SELECT 
    u.id as user_id,
    u.email,
    sp.id as seller_profile_id,
    sp.nom_entreprise
FROM users u
JOIN seller_profiles sp ON sp.user_id = u.id
WHERE u.role = 'SELLER'
ORDER BY u.id DESC;

-- NOTEZ VOTRE seller_profile_id : _______

-- ============================================================
-- ÉTAPE 2 : Mettre à jour le stock de vos produits
-- ============================================================
-- Cela va mettre 2 de vos produits en stock faible (< 10)
-- Remplacez <SELLER_PROFILE_ID> par votre ID

-- Mettre le premier produit à stock = 3 (critique)
UPDATE products
SET stock = 3
WHERE id = (
    SELECT id FROM products 
    WHERE seller_profile_id = <SELLER_PROFILE_ID> 
    AND actif = true
    ORDER BY id 
    LIMIT 1
);

-- Mettre le deuxième produit à stock = 7 (faible)
UPDATE products
SET stock = 7
WHERE id = (
    SELECT id FROM products 
    WHERE seller_profile_id = <SELLER_PROFILE_ID> 
    AND actif = true
    ORDER BY id 
    LIMIT 1 OFFSET 1
);

-- Vérifier le résultat
SELECT id, nom, stock 
FROM products 
WHERE seller_profile_id = <SELLER_PROFILE_ID> 
AND actif = true
ORDER BY stock ASC;

-- ============================================================
-- ÉTAPE 3 : Mettre à jour les dates des commandes
-- ============================================================
-- Cela va mettre vos commandes dans les 7 derniers jours

-- Mettre la commande la plus récente à aujourd'hui
UPDATE orders
SET date_commande = NOW()
WHERE id IN (
    SELECT DISTINCT o.id
    FROM orders o
    JOIN order_items oi ON oi.order_id = o.id
    JOIN products p ON oi.product_id = p.id
    WHERE p.seller_profile_id = <SELLER_PROFILE_ID>
    ORDER BY o.date_commande DESC
    LIMIT 1
);

-- Mettre la 2ème commande à hier
UPDATE orders
SET date_commande = NOW() - INTERVAL '1 day'
WHERE id IN (
    SELECT DISTINCT o.id
    FROM orders o
    JOIN order_items oi ON oi.order_id = o.id
    JOIN products p ON oi.product_id = p.id
    WHERE p.seller_profile_id = <SELLER_PROFILE_ID>
    ORDER BY o.date_commande DESC
    LIMIT 1 OFFSET 1
);

-- Mettre la 3ème commande à il y a 2 jours
UPDATE orders
SET date_commande = NOW() - INTERVAL '2 days'
WHERE id IN (
    SELECT DISTINCT o.id
    FROM orders o
    JOIN order_items oi ON oi.order_id = o.id
    JOIN products p ON oi.product_id = p.id
    WHERE p.seller_profile_id = <SELLER_PROFILE_ID>
    ORDER BY o.date_commande DESC
    LIMIT 1 OFFSET 2
);

-- Vérifier le résultat
SELECT o.id, o.numero_commande, o.statut, o.date_commande, o.total_ttc
FROM orders o
JOIN order_items oi ON oi.order_id = o.id
JOIN products p ON oi.product_id = p.id
WHERE p.seller_profile_id = <SELLER_PROFILE_ID>
ORDER BY o.date_commande DESC;

-- ============================================================
-- ÉTAPE 4 : VÉRIFICATION FINALE
-- ============================================================

-- Vérifier que tout est OK
SELECT 
    'Total Produits' as statistique,
    COUNT(*) as valeur
FROM products 
WHERE seller_profile_id = <SELLER_PROFILE_ID> AND actif = true

UNION ALL

SELECT 
    'Produits Stock Faible',
    COUNT(*)
FROM products 
WHERE seller_profile_id = <SELLER_PROFILE_ID> AND actif = true AND stock < 10

UNION ALL

SELECT 
    'Commandes Récentes (7j)',
    COUNT(DISTINCT o.id)
FROM orders o
JOIN order_items oi ON oi.order_id = o.id
JOIN products p ON oi.product_id = p.id
WHERE p.seller_profile_id = <SELLER_PROFILE_ID>
AND o.date_commande >= NOW() - INTERVAL '7 days'

UNION ALL

SELECT 
    'Commandes En Attente',
    COUNT(DISTINCT o.id)
FROM orders o
JOIN order_items oi ON oi.order_id = o.id
JOIN products p ON oi.product_id = p.id
WHERE p.seller_profile_id = <SELLER_PROFILE_ID>
AND o.statut = 'EN_ATTENTE'

UNION ALL

SELECT 
    'Revenu Total (DT)',
    COALESCE(SUM(oi.sous_total), 0)
FROM order_items oi
JOIN orders o ON oi.order_id = o.id
JOIN products p ON oi.product_id = p.id
WHERE p.seller_profile_id = <SELLER_PROFILE_ID>
AND o.statut != 'ANNULE';

-- ============================================================
-- RÉSULTAT ATTENDU :
-- ============================================================
-- Total Produits          : 2 ou plus
-- Produits Stock Faible   : 2
-- Commandes Récentes (7j) : 1 ou plus
-- Commandes En Attente    : 1
-- Revenu Total (DT)       : > 0
-- ============================================================

-- ============================================================
-- APRÈS AVOIR EXÉCUTÉ CE SCRIPT :
-- ============================================================
-- 1. Rechargez le tableau de bord dans votre navigateur
-- 2. Vous devriez maintenant voir :
--    ✅ Le graphique des revenus avec des barres
--    ✅ Les commandes récentes dans le tableau
--    ✅ Les produits en stock faible
--    ✅ Les statistiques détaillées
-- ============================================================
