-- ========================================
-- VÉRIFIER LA STRUCTURE DE LA TABLE ORDERS
-- ========================================

-- 1. Voir la structure complète de la table orders
DESCRIBE orders;

-- 2. Voir la création de la table
SHOW CREATE TABLE orders;

-- 3. Vérifier si la colonne coupon_id existe
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_KEY
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'fermedirecte'
AND TABLE_NAME = 'orders'
AND COLUMN_NAME = 'coupon_id';

-- 4. Vérifier les contraintes de clés étrangères
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'fermedirecte'
AND TABLE_NAME = 'orders'
AND REFERENCED_TABLE_NAME IS NOT NULL;

-- ========================================
-- SI LA COLONNE coupon_id N'EXISTE PAS
-- ========================================

-- Ajouter la colonne coupon_id
ALTER TABLE orders 
ADD COLUMN coupon_id BIGINT NULL;

-- Ajouter la contrainte de clé étrangère
ALTER TABLE orders
ADD CONSTRAINT fk_orders_coupon
FOREIGN KEY (coupon_id) REFERENCES coupons(id)
ON DELETE SET NULL;

-- Vérifier que la colonne a été ajoutée
DESCRIBE orders;

-- ========================================
-- TESTER AVEC UNE COMMANDE
-- ========================================

-- Voir les dernières commandes
SELECT 
    id,
    numero_commande,
    coupon_id,
    total_ttc,
    date_commande
FROM orders
ORDER BY date_commande DESC
LIMIT 5;

-- Voir les coupons disponibles
SELECT 
    id,
    code,
    actif,
    usages_actuels,
    usages_max_global
FROM coupons
WHERE actif = 1
ORDER BY date_creation DESC;
