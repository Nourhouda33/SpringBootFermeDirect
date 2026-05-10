-- ============================================================
-- Script de test pour le Tableau de Bord Vendeur
-- ============================================================
-- Ce script crée des données de test pour vérifier le bon
-- fonctionnement du tableau de bord vendeur
-- ============================================================

-- 1. Créer un utilisateur vendeur de test (si n'existe pas déjà)
INSERT INTO users (prenom, nom, email, telephone, mot_de_passe, role, actif, date_creation)
VALUES 
('Jean', 'Dupont', 'vendeur@test.com', '0612345678', '$2a$10$abcdefghijklmnopqrstuvwxyz', 'SELLER', true, NOW())
ON CONFLICT (email) DO NOTHING;

-- 2. Créer un profil vendeur
INSERT INTO seller_profiles (user_id, nom_entreprise, description, adresse, telephone, actif, date_creation)
SELECT 
    u.id,
    'Ferme Bio Dupont',
    'Producteur local de fruits et légumes bio',
    '123 Rue de la Ferme, 75001 Paris',
    '0612345678',
    true,
    NOW()
FROM users u
WHERE u.email = 'vendeur@test.com'
ON CONFLICT (user_id) DO NOTHING;

-- 3. Créer des produits avec différents niveaux de stock
INSERT INTO products (nom, description, prix, prix_promo, stock, unite, image_url, category_id, seller_profile_id, actif, date_creation)
SELECT 
    'Tomates Bio',
    'Tomates fraîches cultivées sans pesticides',
    5.50,
    NULL,
    25,
    'kg',
    'https://example.com/tomates.jpg',
    1,
    sp.id,
    true,
    NOW()
FROM seller_profiles sp
JOIN users u ON sp.user_id = u.id
WHERE u.email = 'vendeur@test.com'
ON CONFLICT DO NOTHING;

INSERT INTO products (nom, description, prix, prix_promo, stock, unite, image_url, category_id, seller_profile_id, actif, date_creation)
SELECT 
    'Carottes Bio',
    'Carottes fraîches du jardin',
    3.20,
    2.80,
    8,
    'kg',
    'https://example.com/carottes.jpg',
    1,
    sp.id,
    true,
    NOW()
FROM seller_profiles sp
JOIN users u ON sp.user_id = u.id
WHERE u.email = 'vendeur@test.com'
ON CONFLICT DO NOTHING;

INSERT INTO products (nom, description, prix, prix_promo, stock, unite, image_url, category_id, seller_profile_id, actif, date_creation)
SELECT 
    'Pommes de Terre',
    'Pommes de terre nouvelles',
    2.50,
    NULL,
    3,
    'kg',
    'https://example.com/pommes-terre.jpg',
    1,
    sp.id,
    true,
    NOW()
FROM seller_profiles sp
JOIN users u ON sp.user_id = u.id
WHERE u.email = 'vendeur@test.com'
ON CONFLICT DO NOTHING;

INSERT INTO products (nom, description, prix, prix_promo, stock, unite, image_url, category_id, seller_profile_id, actif, date_creation)
SELECT 
    'Salade Verte',
    'Salade fraîche du jour',
    1.80,
    NULL,
    50,
    'pièce',
    'https://example.com/salade.jpg',
    1,
    sp.id,
    true,
    NOW()
FROM seller_profiles sp
JOIN users u ON sp.user_id = u.id
WHERE u.email = 'vendeur@test.com'
ON CONFLICT DO NOTHING;

-- 4. Créer un client de test
INSERT INTO users (prenom, nom, email, telephone, mot_de_passe, role, actif, date_creation)
VALUES 
('Marie', 'Martin', 'client@test.com', '0698765432', '$2a$10$abcdefghijklmnopqrstuvwxyz', 'CUSTOMER', true, NOW())
ON CONFLICT (email) DO NOTHING;

-- 5. Créer des commandes avec différents statuts et dates
-- Commande 1 : EN_ATTENTE (aujourd'hui)
INSERT INTO orders (numero_commande, user_id, statut, statut_paiement, sous_total, remise, frais_livraison, total_ttc, mode_paiement, date_commande)
SELECT 
    'CMD-' || LPAD(NEXTVAL('order_seq')::TEXT, 8, '0'),
    u.id,
    'EN_ATTENTE',
    'EN_ATTENTE',
    50.00,
    0.00,
    5.00,
    55.00,
    'CARTE_BANCAIRE',
    NOW()
FROM users u
WHERE u.email = 'client@test.com';

-- Commande 2 : CONFIRME (hier)
INSERT INTO orders (numero_commande, user_id, statut, statut_paiement, sous_total, remise, frais_livraison, total_ttc, mode_paiement, date_commande)
SELECT 
    'CMD-' || LPAD(NEXTVAL('order_seq')::TEXT, 8, '0'),
    u.id,
    'CONFIRME',
    'PAYE',
    75.50,
    5.00,
    5.00,
    75.50,
    'CARTE_BANCAIRE',
    NOW() - INTERVAL '1 day'
FROM users u
WHERE u.email = 'client@test.com';

-- Commande 3 : EN_LIVRAISON (il y a 2 jours)
INSERT INTO orders (numero_commande, user_id, statut, statut_paiement, sous_total, remise, frais_livraison, total_ttc, mode_paiement, date_commande)
SELECT 
    'CMD-' || LPAD(NEXTVAL('order_seq')::TEXT, 8, '0'),
    u.id,
    'EN_LIVRAISON',
    'PAYE',
    120.00,
    10.00,
    5.00,
    115.00,
    'ESPECES',
    NOW() - INTERVAL '2 days'
FROM users u
WHERE u.email = 'client@test.com';

-- Commande 4 : LIVRE (il y a 3 jours)
INSERT INTO orders (numero_commande, user_id, statut, statut_paiement, sous_total, remise, frais_livraison, total_ttc, mode_paiement, date_commande)
SELECT 
    'CMD-' || LPAD(NEXTVAL('order_seq')::TEXT, 8, '0'),
    u.id,
    'LIVRE',
    'PAYE',
    95.00,
    0.00,
    5.00,
    100.00,
    'CARTE_BANCAIRE',
    NOW() - INTERVAL '3 days'
FROM users u
WHERE u.email = 'client@test.com';

-- Commande 5 : ANNULE (il y a 4 jours)
INSERT INTO orders (numero_commande, user_id, statut, statut_paiement, sous_total, remise, frais_livraison, total_ttc, mode_paiement, date_commande)
SELECT 
    'CMD-' || LPAD(NEXTVAL('order_seq')::TEXT, 8, '0'),
    u.id,
    'ANNULE',
    'ECHOUE',
    60.00,
    0.00,
    5.00,
    65.00,
    'CARTE_BANCAIRE',
    NOW() - INTERVAL '4 days'
FROM users u
WHERE u.email = 'client@test.com';

-- 6. Créer les lignes de commande (order_items)
-- Pour la commande EN_ATTENTE
INSERT INTO order_items (order_id, product_id, nom_produit, prix_unitaire, quantite, sous_total)
SELECT 
    o.id,
    p.id,
    p.nom,
    p.prix,
    5,
    p.prix * 5
FROM orders o
CROSS JOIN products p
JOIN seller_profiles sp ON p.seller_profile_id = sp.id
JOIN users u ON sp.user_id = u.id
WHERE o.statut = 'EN_ATTENTE' 
  AND p.nom = 'Tomates Bio'
  AND u.email = 'vendeur@test.com'
  AND NOT EXISTS (
    SELECT 1 FROM order_items oi 
    WHERE oi.order_id = o.id AND oi.product_id = p.id
  )
LIMIT 1;

-- Pour la commande CONFIRME
INSERT INTO order_items (order_id, product_id, nom_produit, prix_unitaire, quantite, sous_total)
SELECT 
    o.id,
    p.id,
    p.nom,
    p.prix,
    10,
    p.prix * 10
FROM orders o
CROSS JOIN products p
JOIN seller_profiles sp ON p.seller_profile_id = sp.id
JOIN users u ON sp.user_id = u.id
WHERE o.statut = 'CONFIRME' 
  AND p.nom = 'Carottes Bio'
  AND u.email = 'vendeur@test.com'
  AND NOT EXISTS (
    SELECT 1 FROM order_items oi 
    WHERE oi.order_id = o.id AND oi.product_id = p.id
  )
LIMIT 1;

-- Pour la commande EN_LIVRAISON
INSERT INTO order_items (order_id, product_id, nom_produit, prix_unitaire, quantite, sous_total)
SELECT 
    o.id,
    p.id,
    p.nom,
    p.prix,
    20,
    p.prix * 20
FROM orders o
CROSS JOIN products p
JOIN seller_profiles sp ON p.seller_profile_id = sp.id
JOIN users u ON sp.user_id = u.id
WHERE o.statut = 'EN_LIVRAISON' 
  AND p.nom = 'Salade Verte'
  AND u.email = 'vendeur@test.com'
  AND NOT EXISTS (
    SELECT 1 FROM order_items oi 
    WHERE oi.order_id = o.id AND oi.product_id = p.id
  )
LIMIT 1;

-- Pour la commande LIVRE
INSERT INTO order_items (order_id, product_id, nom_produit, prix_unitaire, quantite, sous_total)
SELECT 
    o.id,
    p.id,
    p.nom,
    p.prix,
    15,
    p.prix * 15
FROM orders o
CROSS JOIN products p
JOIN seller_profiles sp ON p.seller_profile_id = sp.id
JOIN users u ON sp.user_id = u.id
WHERE o.statut = 'LIVRE' 
  AND p.nom = 'Tomates Bio'
  AND u.email = 'vendeur@test.com'
  AND NOT EXISTS (
    SELECT 1 FROM order_items oi 
    WHERE oi.order_id = o.id AND oi.product_id = p.id
  )
LIMIT 1;

-- 7. Vérification des données créées
SELECT 
    '=== RÉSUMÉ DES DONNÉES DE TEST ===' as info;

SELECT 
    'Vendeur créé' as type,
    u.email,
    sp.nom_entreprise
FROM users u
JOIN seller_profiles sp ON sp.user_id = u.id
WHERE u.email = 'vendeur@test.com';

SELECT 
    'Produits créés' as type,
    COUNT(*) as nombre,
    SUM(CASE WHEN stock < 10 THEN 1 ELSE 0 END) as stock_faible
FROM products p
JOIN seller_profiles sp ON p.seller_profile_id = sp.id
JOIN users u ON sp.user_id = u.id
WHERE u.email = 'vendeur@test.com';

SELECT 
    'Commandes créées' as type,
    COUNT(*) as nombre,
    SUM(CASE WHEN statut = 'EN_ATTENTE' THEN 1 ELSE 0 END) as en_attente,
    SUM(CASE WHEN statut = 'CONFIRME' THEN 1 ELSE 0 END) as confirmees,
    SUM(CASE WHEN statut = 'EN_LIVRAISON' THEN 1 ELSE 0 END) as en_livraison,
    SUM(CASE WHEN statut = 'LIVRE' THEN 1 ELSE 0 END) as livrees,
    SUM(CASE WHEN statut = 'ANNULE' THEN 1 ELSE 0 END) as annulees
FROM orders o
WHERE EXISTS (
    SELECT 1 FROM order_items oi
    JOIN products p ON oi.product_id = p.id
    JOIN seller_profiles sp ON p.seller_profile_id = sp.id
    JOIN users u ON sp.user_id = u.id
    WHERE oi.order_id = o.id
      AND u.email = 'vendeur@test.com'
);

-- 8. Afficher les produits avec stock faible
SELECT 
    'Produits stock faible' as type,
    p.nom,
    p.stock,
    p.prix
FROM products p
JOIN seller_profiles sp ON p.seller_profile_id = sp.id
JOIN users u ON sp.user_id = u.id
WHERE u.email = 'vendeur@test.com'
  AND p.stock < 10
ORDER BY p.stock ASC;

-- 9. Calculer le revenu total
SELECT 
    'Revenu total' as type,
    SUM(oi.sous_total) as revenu_total
FROM order_items oi
JOIN orders o ON oi.order_id = o.id
JOIN products p ON oi.product_id = p.id
JOIN seller_profiles sp ON p.seller_profile_id = sp.id
JOIN users u ON sp.user_id = u.id
WHERE u.email = 'vendeur@test.com'
  AND o.statut != 'ANNULE';

-- ============================================================
-- INSTRUCTIONS D'UTILISATION
-- ============================================================
-- 1. Exécuter ce script dans votre base de données PostgreSQL
-- 2. Se connecter avec : vendeur@test.com / (mot de passe à définir)
-- 3. Accéder au tableau de bord vendeur
-- 4. Vérifier que toutes les statistiques s'affichent correctement
-- ============================================================

-- RÉSULTATS ATTENDUS :
-- - Total produits : 4
-- - Commandes en attente : 1
-- - Revenu total : ~300 DT (hors commande annulée)
-- - Produits stock faible : 2 (Carottes Bio: 8, Pommes de Terre: 3)
-- - Commandes récentes : 5 (toutes les commandes créées)
-- - Statistiques : 1 en attente, 1 confirmée, 1 en livraison, 1 livrée, 1 annulée
-- ============================================================
