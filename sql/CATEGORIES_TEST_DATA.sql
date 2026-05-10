-- ============================================================
-- Données de test pour les catégories
-- FermeDirecte - Gestion des catégories hiérarchiques
-- ============================================================

-- Catégories racines (niveau 1)
INSERT INTO categories (nom, description, parent_id) VALUES
('Fruits et légumes', 'Produits frais du potager et du verger', NULL),
('Produits laitiers', 'Lait, fromages, yaourts et dérivés', NULL),
('Viandes et volailles', 'Viandes fraîches et charcuterie', NULL),
('Œufs', 'Œufs frais de la ferme', NULL),
('Miel et produits de la ruche', 'Miel, pollen, propolis', NULL),
('Céréales et légumineuses', 'Farines, grains et légumes secs', NULL);

-- Sous-catégories de "Fruits et légumes" (niveau 2)
INSERT INTO categories (nom, description, parent_id) VALUES
('Fruits', 'Fruits de saison', (SELECT id FROM categories WHERE nom = 'Fruits et légumes')),
('Légumes', 'Légumes frais du potager', (SELECT id FROM categories WHERE nom = 'Fruits et légumes')),
('Herbes aromatiques', 'Basilic, persil, coriandre...', (SELECT id FROM categories WHERE nom = 'Fruits et légumes'));

-- Sous-catégories de "Fruits" (niveau 3)
INSERT INTO categories (nom, description, parent_id) VALUES
('Fruits à noyau', 'Pêches, abricots, cerises...', (SELECT id FROM categories WHERE nom = 'Fruits')),
('Fruits à pépins', 'Pommes, poires...', (SELECT id FROM categories WHERE nom = 'Fruits')),
('Fruits rouges', 'Fraises, framboises, myrtilles...', (SELECT id FROM categories WHERE nom = 'Fruits')),
('Agrumes', 'Oranges, citrons, mandarines...', (SELECT id FROM categories WHERE nom = 'Fruits'));

-- Sous-catégories de "Légumes" (niveau 3)
INSERT INTO categories (nom, description, parent_id) VALUES
('Légumes racines', 'Carottes, navets, betteraves...', (SELECT id FROM categories WHERE nom = 'Légumes')),
('Légumes feuilles', 'Salades, épinards, choux...', (SELECT id FROM categories WHERE nom = 'Légumes')),
('Légumes fruits', 'Tomates, courgettes, aubergines...', (SELECT id FROM categories WHERE nom = 'Légumes')),
('Légumineuses fraîches', 'Haricots verts, petits pois...', (SELECT id FROM categories WHERE nom = 'Légumes'));

-- Sous-catégories de "Produits laitiers" (niveau 2)
INSERT INTO categories (nom, description, parent_id) VALUES
('Fromages', 'Fromages fermiers', (SELECT id FROM categories WHERE nom = 'Produits laitiers')),
('Lait', 'Lait frais et pasteurisé', (SELECT id FROM categories WHERE nom = 'Produits laitiers')),
('Yaourts', 'Yaourts nature et aromatisés', (SELECT id FROM categories WHERE nom = 'Produits laitiers')),
('Crème et beurre', 'Crème fraîche et beurre fermier', (SELECT id FROM categories WHERE nom = 'Produits laitiers'));

-- Sous-catégories de "Fromages" (niveau 3)
INSERT INTO categories (nom, description, parent_id) VALUES
('Fromages à pâte dure', 'Comté, Gruyère...', (SELECT id FROM categories WHERE nom = 'Fromages')),
('Fromages à pâte molle', 'Camembert, Brie...', (SELECT id FROM categories WHERE nom = 'Fromages')),
('Fromages de chèvre', 'Crottins, bûches...', (SELECT id FROM categories WHERE nom = 'Fromages')),
('Fromages frais', 'Ricotta, Mascarpone...', (SELECT id FROM categories WHERE nom = 'Fromages'));

-- Sous-catégories de "Viandes et volailles" (niveau 2)
INSERT INTO categories (nom, description, parent_id) VALUES
('Bœuf', 'Viande de bœuf', (SELECT id FROM categories WHERE nom = 'Viandes et volailles')),
('Porc', 'Viande de porc', (SELECT id FROM categories WHERE nom = 'Viandes et volailles')),
('Volailles', 'Poulet, canard, dinde...', (SELECT id FROM categories WHERE nom = 'Viandes et volailles')),
('Agneau', 'Viande d\'agneau', (SELECT id FROM categories WHERE nom = 'Viandes et volailles')),
('Charcuterie', 'Saucissons, pâtés, terrines...', (SELECT id FROM categories WHERE nom = 'Viandes et volailles'));

-- Sous-catégories de "Céréales et légumineuses" (niveau 2)
INSERT INTO categories (nom, description, parent_id) VALUES
('Farines', 'Farines de blé, seigle, épeautre...', (SELECT id FROM categories WHERE nom = 'Céréales et légumineuses')),
('Grains', 'Blé, orge, avoine...', (SELECT id FROM categories WHERE nom = 'Céréales et légumineuses')),
('Légumes secs', 'Lentilles, pois chiches, haricots secs...', (SELECT id FROM categories WHERE nom = 'Céréales et légumineuses'));

-- ============================================================
-- Vérification de la structure hiérarchique
-- ============================================================

-- Afficher toutes les catégories avec leur niveau hiérarchique
-- SELECT 
--     c1.id,
--     c1.nom AS categorie,
--     c2.nom AS parent,
--     c3.nom AS grand_parent,
--     CASE 
--         WHEN c1.parent_id IS NULL THEN 'Niveau 1 (Racine)'
--         WHEN c2.parent_id IS NULL THEN 'Niveau 2'
--         ELSE 'Niveau 3'
--     END AS niveau
-- FROM categories c1
-- LEFT JOIN categories c2 ON c1.parent_id = c2.id
-- LEFT JOIN categories c3 ON c2.parent_id = c3.id
-- ORDER BY 
--     COALESCE(c3.id, c2.id, c1.id),
--     COALESCE(c2.id, c1.id),
--     c1.id;

-- Compter les catégories par niveau
-- SELECT 
--     CASE 
--         WHEN parent_id IS NULL THEN 'Niveau 1 (Racine)'
--         WHEN parent_id IN (SELECT id FROM categories WHERE parent_id IS NULL) THEN 'Niveau 2'
--         ELSE 'Niveau 3'
--     END AS niveau,
--     COUNT(*) AS nombre
-- FROM categories
-- GROUP BY niveau
-- ORDER BY niveau;
