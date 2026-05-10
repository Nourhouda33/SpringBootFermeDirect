-- Script pour vérifier et corriger la structure de la table orders

-- Vérifier la structure actuelle
DESCRIBE orders;

-- Si nécessaire, renommer les colonnes pour correspondre au mapping JPA
-- (Exécutez ces commandes SEULEMENT si les noms de colonnes ne correspondent pas)

-- ALTER TABLE orders CHANGE COLUMN sous_total sous_total DECIMAL(19,2) NOT NULL;
-- ALTER TABLE orders CHANGE COLUMN frais_livraison frais_livraison DECIMAL(19,2) NOT NULL DEFAULT 0;
-- ALTER TABLE orders CHANGE COLUMN totalttc total_ttc DECIMAL(19,2) NOT NULL;
-- ALTER TABLE orders CHANGE COLUMN numero_commande numero_commande VARCHAR(255) NOT NULL UNIQUE;
-- ALTER TABLE orders CHANGE COLUMN date_commande date_commande DATETIME;
-- ALTER TABLE orders CHANGE COLUMN statut_paiement statut_paiement VARCHAR(50) NOT NULL;
