-- ============================================================
-- Script SQL Simplifié pour Tester le Tableau de Bord Vendeur
-- ============================================================
-- Ce script ajoute des données de test simples
-- ============================================================

-- Vérifier l'email du vendeur connecté
-- Remplacez 'votre_email@example.com' par l'email réel du vendeur

-- 1. Trouver l'ID du vendeur
SELECT u.id as user_id, u.email, sp.id as seller_profile_id, sp.nom_entreprise
FROM users u
LEFT JOIN seller_profiles sp ON sp.user_id = u.id
WHERE u.role = 'SELLER'
ORDER BY u.id DESC
LIMIT 5;

-- 2. Vérifier les produits existants du vendeur
-- Remplacez <SELLER_PROFILE_ID> par l'ID du profil vendeur
SELECT p.id, p.nom, p.prix, p.stock, p.seller_profile_id
FROM products p
WHERE p.seller_profile_id = <SELLER_PROFILE_ID>;

-- 3. Vérifier les commandes existantes
SELECT o.id, o.numero_commande, o.statut, o.total_ttc, o.date_commande
FROM orders o
ORDER BY o.date_commande DESC
LIMIT 10;

-- 4. Vérifier les lignes de commande (order_items) liées aux produits du vendeur
SELECT oi.id, oi.order_id, oi.product_id, oi.nom_produit, oi.quantite, oi.sous_total, o.statut
FROM order_items oi
JOIN orders o ON oi.order_id = o.id
JOIN products p ON oi.product_id = p.id
WHERE p.seller_profile_id = <SELLER_PROFILE_ID>
ORDER BY o.date_commande DESC;

-- ============================================================
-- SI VOUS N'AVEZ PAS DE DONNÉES, CRÉEZ-EN :
-- ============================================================

-- A. Créer des produits avec stock faible (si vous n'en avez pas)
-- Remplacez <SELLER_PROFILE_ID> et <CATEGORY_ID> par les bonnes valeurs

/*
INSERT INTO products (nom, description, prix, stock, unite, seller_profile_id, category_id, actif, date_creation)
VALUES 
('Produit Test 1', 'Description test 1', 10.50, 5, 'kg', <SELLER_PROFILE_ID>, <CATEGORY_ID>, true, NOW()),
('Produit Test 2', 'Description test 2', 15.00, 8, 'kg', <SELLER_PROFILE_ID>, <CATEGORY_ID>, true, NOW()),
('Produit Test 3', 'Description test 3', 20.00, 25, 'kg', <SELLER_PROFILE_ID>, <CATEGORY_ID>, true, NOW());
*/

-- B. Créer une commande de test (si vous n'en avez pas)
-- Remplacez <USER_ID> par l'ID d'un utilisateur client

/*
INSERT INTO orders (numero_commande, user_id, statut, statut_paiement, sous_total, remise, frais_livraison, total_ttc, mode_paiement, date_commande)
VALUES 
('CMD-TEST-001', <USER_ID>, 'EN_ATTENTE', 'EN_ATTENTE', 50.00, 0.00, 5.00, 55.00, 'CARTE_BANCAIRE', NOW());
*/

-- C. Créer des lignes de commande (order_items)
-- Remplacez <ORDER_ID> et <PRODUCT_ID> par les bonnes valeurs

/*
INSERT INTO order_items (order_id, product_id, nom_produit, prix_unitaire, quantite, sous_total)
VALUES 
(<ORDER_ID>, <PRODUCT_ID>, 'Produit Test 1', 10.50, 5, 52.50);
*/

-- ============================================================
-- VÉRIFICATION FINALE
-- ============================================================

-- Vérifier le nombre total de produits du vendeur
SELECT COUNT(*) as total_produits
FROM products p
WHERE p.seller_profile_id = <SELLER_PROFILE_ID> AND p.actif = true;

-- Vérifier le nombre de produits en stock faible (< 10)
SELECT COUNT(*) as produits_stock_faible
FROM products p
WHERE p.seller_profile_id = <SELLER_PROFILE_ID> AND p.actif = true AND p.stock < 10;

-- Vérifier le nombre de commandes en attente
SELECT COUNT(DISTINCT o.id) as commandes_en_attente
FROM orders o
JOIN order_items oi ON oi.order_id = o.id
JOIN products p ON oi.product_id = p.id
WHERE p.seller_profile_id = <SELLER_PROFILE_ID> AND o.statut = 'EN_ATTENTE';

-- Vérifier le revenu total (hors commandes annulées)
SELECT SUM(oi.sous_total) as revenu_total
FROM order_items oi
JOIN orders o ON oi.order_id = o.id
JOIN products p ON oi.product_id = p.id
WHERE p.seller_profile_id = <SELLER_PROFILE_ID> AND o.statut != 'ANNULE';

-- ============================================================
-- INSTRUCTIONS
-- ============================================================
-- 1. Exécutez d'abord les requêtes SELECT pour trouver vos IDs
-- 2. Remplacez <SELLER_PROFILE_ID>, <USER_ID>, <CATEGORY_ID>, etc. par les vraies valeurs
-- 3. Décommentez et exécutez les INSERT si nécessaire
-- 4. Vérifiez avec les requêtes de vérification finale
-- 5. Rechargez le tableau de bord dans l'application
-- ============================================================
