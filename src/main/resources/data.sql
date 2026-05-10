-- ============================================================
-- FermeDirecte - Donnees de test
-- Colonnes basees sur les entites Hibernate reelles
-- Mot de passe pour tous : "password123"
-- ============================================================

-- ============================================================
-- 1. USERS
-- Colonnes : id, email, mot_de_passe, prenom, nom, telephone, role, actif, refresh_token, date_creation
-- ============================================================
INSERT IGNORE INTO users (id, email, mot_de_passe, prenom, nom, telephone, role, actif) VALUES
(1, 'admin@fermedirecte.tn',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Mohamed', 'Ben Ali',   '+216 20 123 456', 'ADMIN',    true),
(2, 'ferme.oliviers@gmail.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Ahmed',   'Trabelsi',  '+216 22 345 678', 'SELLER',   true),
(3, 'jardin.bio@gmail.com',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Fatma',   'Khelifi',   '+216 23 456 789', 'SELLER',   true),
(4, 'elevage.sahel@gmail.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Karim',   'Mansour',   '+216 24 567 890', 'SELLER',   true),
(5, 'fruits.capbon@gmail.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Leila',   'Hamdi',     '+216 25 678 901', 'SELLER',   true),
(6, 'client1@gmail.com',          '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Sami',    'Bouazizi',  '+216 26 789 012', 'CUSTOMER', true),
(7, 'client2@gmail.com',          '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Amira',   'Jebali',    '+216 27 890 123', 'CUSTOMER', true),
(8, 'client3@gmail.com',          '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Youssef', 'Gharbi',    '+216 28 901 234', 'CUSTOMER', true);


-- ============================================================
-- 2. SELLER_PROFILES
-- Colonnes : id, user_id, nom_boutique, description, logo, note
-- (pas de date_creation dans l'entite)
-- ============================================================
INSERT IGNORE INTO seller_profiles (id, user_id, nom_boutique, description, logo, note) VALUES
(1, 2, 'Ferme des Oliviers',  'Producteur huile olive extra vierge bio depuis 1985. Sfax, 500 oliviers centenaires.', 'https://via.placeholder.com/200x200?text=Oliviers', 4.8),
(2, 3, 'Jardin Bio de Nabeul','Legumes et fruits biologiques sans pesticides. Livraison fraicheur sous 24h.',          'https://via.placeholder.com/200x200?text=JardinBio', 4.6),
(3, 4, 'Elevage du Sahel',    'Viandes et produits laitiers fermiers. Animaux eleves en plein air.',                   'https://via.placeholder.com/200x200?text=Elevage',   4.9),
(4, 5, 'Fruits du Cap Bon',   'Agrumes, fraises et fruits de saison. Recolte quotidienne.',                            'https://via.placeholder.com/200x200?text=Fruits',    4.7);


-- ============================================================
-- 3. ADDRESSES
-- Colonnes : id, user_id, prenom, nom, rue, ville, code_postal, pays, gouvernorat, telephone, instructions, principal
-- ============================================================
INSERT IGNORE INTO addresses (id, user_id, prenom, nom, rue, ville, code_postal, pays, principal) VALUES
(1, 1, 'Mohamed', 'Ben Ali',  '15 Avenue Habib Bourguiba', 'Tunis',  '1000', 'Tunisie', true),
(2, 6, 'Sami',    'Bouazizi', '42 Rue de la Republique',   'Tunis',  '1001', 'Tunisie', true),
(3, 6, 'Sami',    'Bouazizi', '8 Avenue Mohamed V',        'Ariana', '2080', 'Tunisie', false),
(4, 7, 'Amira',   'Jebali',   '23 Rue Ibn Khaldoun',       'Sousse', '4000', 'Tunisie', true),
(5, 8, 'Youssef', 'Gharbi',   '67 Avenue de la Liberte',   'Sfax',   '3000', 'Tunisie', true);


-- ============================================================
-- 4. CATEGORIES
-- Colonnes : id, nom, description, parent_id
-- ============================================================
INSERT IGNORE INTO categories (id, nom, description, parent_id) VALUES
(1,  'Fruits et Legumes',  'Produits frais de saison',              NULL),
(2,  'Produits Laitiers',  'Lait, fromages, yaourts fermiers',      NULL),
(3,  'Viandes',            'Viandes fraiches fermiers',             NULL),
(4,  'Epicerie',           'Huiles, miels, conserves',              NULL),
(5,  'Boissons',           'Jus naturels, sirops artisanaux',       NULL),
(6,  'Legumes',            'Legumes frais bio',                     1),
(7,  'Fruits',             'Fruits de saison',                      1),
(8,  'Agrumes',            'Oranges, citrons, mandarines',          7),
(9,  'Fromages',           'Fromages fermiers artisanaux',          2),
(10, 'Yaourts',            'Yaourts nature et aromatises',          2),
(11, 'Viande Rouge',       'Boeuf, agneau, mouton',                 3),
(12, 'Volaille',           'Poulet, dinde fermiers',                3),
(13, 'Huiles',             'Huiles olive et vegetales',             4),
(14, 'Miels',              'Miels naturels de differentes fleurs',  4);


-- ============================================================
-- 5. PRODUCTS
-- Colonnes : id, seller_profile_id, nom, description, prix, prix_promo, stock, actif, image_url, unite
-- ============================================================
INSERT IGNORE INTO products (id, seller_profile_id, nom, description, prix, prix_promo, stock, actif, image_url, unite) VALUES
-- Ferme des Oliviers
(1,  1, 'Huile Olive Extra Vierge Bio 1L',   'Premiere pression a froid. Acidite < 0.5%. Medaille or 2023.',  35.00, 32.00, 150, true, 'https://via.placeholder.com/400x400?text=Huile+Olive',   'litre'),
(2,  1, 'Huile Olive Extra Vierge Bio 500ml','Meme qualite en format economique.',                             18.00, NULL,  200, true, 'https://via.placeholder.com/400x400?text=Huile+500ml',   'bouteille'),
(3,  1, 'Olives Noires Marinees 500g',       'Olives de table marinees avec herbes de Provence.',             12.00, NULL,   80, true, 'https://via.placeholder.com/400x400?text=Olives',        'pot'),
-- Jardin Bio
(4,  2, 'Tomates Bio',                       'Tomates coeur de boeuf sans pesticides. Gout authentique.',     4.50,  3.90,  300, true, 'https://via.placeholder.com/400x400?text=Tomates',       'kg'),
(5,  2, 'Courgettes Bio',                    'Courgettes fraiches recoltees le matin.',                       3.20,  NULL,  250, true, 'https://via.placeholder.com/400x400?text=Courgettes',    'kg'),
(6,  2, 'Salade Verte Bio',                  'Laitue croquante cultivee en plein champ.',                     2.50,  NULL,  180, true, 'https://via.placeholder.com/400x400?text=Salade',        'piece'),
(7,  2, 'Carottes Bio',                      'Carottes sucrees et croquantes.',                               3.80,  NULL,  220, true, 'https://via.placeholder.com/400x400?text=Carottes',      'kg'),
(8,  2, 'Pommes de Terre Bio',               'Variete Spunta. Ideales pour frites et puree.',                 2.80,  2.50,  500, true, 'https://via.placeholder.com/400x400?text=PdT',           'kg'),
-- Elevage du Sahel
(9,  3, 'Viande de Boeuf Entrecote',         'Viande maturee 21 jours. Elevage plein air.',                  45.00, NULL,   50, true, 'https://via.placeholder.com/400x400?text=Entrecote',     'kg'),
(10, 3, 'Poulet Fermier Entier',             'Poulet eleve en liberte, nourri aux cereales. ~1.8kg.',        22.00, 19.90,  80, true, 'https://via.placeholder.com/400x400?text=Poulet',        'piece'),
(11, 3, 'Fromage de Chevre Frais 250g',      'Fromage artisanal au lait cru. Texture cremeuse.',              8.50, NULL,   60, true, 'https://via.placeholder.com/400x400?text=Fromage',       'piece'),
(12, 3, 'Yaourt Nature Fermier 500g',        'Yaourt au lait entier sans additifs.',                          4.20, NULL,  120, true, 'https://via.placeholder.com/400x400?text=Yaourt',        'pot'),
(13, 3, 'Lait Frais Entier 1L',              'Lait cru pasteurise. A consommer sous 3 jours.',                3.50, NULL,  100, true, 'https://via.placeholder.com/400x400?text=Lait',          'litre'),
-- Fruits du Cap Bon
(14, 4, 'Oranges Maltaises',                 'Oranges juteuses et sucrees. Ideales pour jus.',                3.50,  2.90, 400, true, 'https://via.placeholder.com/400x400?text=Oranges',       'kg'),
(15, 4, 'Citrons Bio',                       'Citrons non traites. Parfaits pour cuisine.',                   4.00, NULL,  200, true, 'https://via.placeholder.com/400x400?text=Citrons',       'kg'),
(16, 4, 'Fraises de Saison',                 'Fraises parfumees sous serre. Mars-juin.',                     12.00, 10.50, 150, true, 'https://via.placeholder.com/400x400?text=Fraises',       'barquette 500g'),
(17, 4, 'Mandarines',                        'Mandarines faciles a eplucher. Sucrees sans pepins.',           3.80, NULL,  300, true, 'https://via.placeholder.com/400x400?text=Mandarines',    'kg'),
(18, 4, 'Pommes Golden',                     'Pommes croquantes et sucrees. Conservation longue.',            5.50, NULL,  250, true, 'https://via.placeholder.com/400x400?text=Pommes',        'kg');


-- ============================================================
-- 6. PRODUCT_CATEGORIES
-- Colonnes : product_id, category_id
-- ============================================================
INSERT IGNORE INTO product_categories (product_id, category_id) VALUES
(1,4),(1,13),(2,4),(2,13),(3,4),
(4,1),(4,6),(5,1),(5,6),(6,1),(6,6),(7,1),(7,6),(8,1),(8,6),
(9,3),(9,11),(10,3),(10,12),(11,2),(11,9),(12,2),(12,10),(13,2),
(14,1),(14,7),(14,8),(15,1),(15,7),(15,8),(16,1),(16,7),(17,1),(17,7),(17,8),(18,1),(18,7);


-- ============================================================
-- 7. PRODUCT_VARIANTS
-- Colonnes : id, product_id, attribut, valeur, stock_supplementaire, prix_delta
-- ============================================================
INSERT IGNORE INTO product_variants (id, product_id, attribut, valeur, stock_supplementaire, prix_delta) VALUES
(1, 1,  'Conditionnement', 'Bidon 5L',       50, 140.00),
(2, 1,  'Conditionnement', 'Coffret 2x1L',   30,  35.00),
(3, 10, 'Decoupe',         'Cuisses 2 pcs',  40,  -8.00),
(4, 10, 'Decoupe',         'Blancs 2 filets',35,  -5.00),
(5, 8,  'Conditionnement', 'Sac 5kg',       100,   2.00),
(6, 8,  'Conditionnement', 'Sac 10kg',       80,   5.00);


-- ============================================================
-- 8. COUPONS
-- Colonnes : id, code, description, pourcentage_reduction, montant_fixe_reduction,
--            livraison_gratuite, montant_minimum, montant_maximum_reduction,
--            scope, usages_max_global, usages_actuels, usages_max_par_utilisateur,
--            date_debut, date_expiration, actif, bloque
-- ============================================================
INSERT IGNORE INTO coupons (id, code, description, pourcentage_reduction, montant_fixe_reduction, livraison_gratuite, montant_minimum, scope, usages_max_global, usages_actuels, usages_max_par_utilisateur, date_debut, date_expiration, actif, bloque) VALUES
(1, 'BIENVENUE20',      'Bienvenue : -20%',                  20.00, NULL,  false, 50.00, 'GLOBAL', 100, 0, 1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY),  true, false),
(2, 'PROMO15',          'Promotion -15% sur tout le site',   15.00, NULL,  false, 30.00, 'GLOBAL', 200, 0, 2, NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY),  true, false),
(3, 'REDUC10',          'Reduction de 10 DT',                NULL,  10.00, false, 40.00, 'GLOBAL', 150, 0, 1, NOW(), DATE_ADD(NOW(), INTERVAL 45 DAY),  true, false),
(4, 'LIVRAISON',        'Livraison gratuite',                 NULL,  NULL,  true,  NULL,  'GLOBAL', 500, 0, 3, NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY),  true, false),
(5, 'MEGA50',           'Mega promo -50%',                   50.00, NULL,  false, 100.00,'GLOBAL',  50, 0, 1, NOW(), DATE_ADD(NOW(), INTERVAL 15 DAY),  true, false);


-- ============================================================
-- 9. CARTS
-- Colonnes : id, user_id, date_modification
-- ============================================================
INSERT IGNORE INTO carts (id, user_id) VALUES
(1, 6),
(2, 7),
(3, 8);


-- ============================================================
-- 10. CART_ITEMS
-- Colonnes : id, cart_id, product_id, product_variant_id, quantite
-- ============================================================
INSERT IGNORE INTO cart_items (id, cart_id, product_id, product_variant_id, quantite) VALUES
(1, 1, 1,    NULL, 2),
(2, 1, 4,    NULL, 3),
(3, 1, 14,   NULL, 5),
(4, 2, 10,   NULL, 1),
(5, 2, 8,    5,    1),
(6, 2, 6,    NULL, 2),
(7, 3, 16,   NULL, 2),
(8, 3, 11,   NULL, 1);


-- ============================================================
-- 11. ORDERS
-- Colonnes : id, user_id, address_id, coupon_id, numero_commande,
--            statut, statut_paiement, sous_total, remise, frais_livraison, total_ttc
-- ============================================================
INSERT IGNORE INTO orders (id, user_id, address_id, coupon_id, numero_commande, statut, statut_paiement, sous_total, remise, frais_livraison, total_ttc) VALUES
(1, 6, 2, 1,    'CMD-2024-001', 'DELIVERED',  'PAID',    85.50, 17.10, 7.00, 75.40),
(2, 7, 4, NULL, 'CMD-2024-002', 'SHIPPED',    'PAID',    52.30,  0.00, 7.00, 59.30),
(3, 8, 5, 3,    'CMD-2024-003', 'PROCESSING', 'PAID',    95.00, 10.00, 7.00, 92.00),
(4, 6, 2, NULL, 'CMD-2024-004', 'PENDING',    'PENDING', 45.00,  0.00, 7.00, 52.00);


-- ============================================================
-- 12. ORDER_ITEMS
-- Colonnes : id, order_id, product_id, product_variant_id, quantite, prix_unitaire
-- ============================================================
INSERT IGNORE INTO order_items (id, order_id, product_id, product_variant_id, quantite, prix_unitaire) VALUES
(1, 1, 1,  NULL, 2, 32.00),
(2, 1, 4,  NULL, 5,  3.90),
(3, 2, 10, NULL, 1, 19.90),
(4, 2, 8,  NULL,10,  2.50),
(5, 2, 6,  NULL, 3,  2.50),
(6, 3, 14, NULL,10,  2.90),
(7, 3, 16, NULL, 3, 10.50),
(8, 3, 1,  NULL, 1, 32.00),
(9, 4, 9,  NULL, 1, 45.00);


-- ============================================================
-- 13. REVIEWS
-- Colonnes : id, user_id, product_id, note, commentaire, approuve
-- ============================================================
INSERT IGNORE INTO reviews (id, user_id, product_id, note, commentaire, approuve) VALUES
(1, 6, 1,  5, 'Excellente huile olive ! Gout fruite et authentique. Je recommande.', true),
(2, 7, 10, 5, 'Poulet fermier de qualite exceptionnelle. Chair tendre et savoureuse.', true),
(3, 8, 14, 4, 'Oranges bien juteuses. Bon rapport qualite/prix.', true),
(4, 6, 4,  5, 'Tomates bio delicieuses ! Vrai gout de tomate comme autrefois.', true),
(5, 7, 8,  4, 'Bonnes pommes de terre. Parfaites pour les frites maison.', true),
(6, 8, 16, 5, 'Fraises parfumees et sucrees. Mes enfants adorent !', true);

-- ============================================================
-- FIN
-- ============================================================
