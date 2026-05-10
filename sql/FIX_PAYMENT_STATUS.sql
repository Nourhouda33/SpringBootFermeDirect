-- ============================================================
-- FIX : Corriger le statut de paiement des commandes existantes
-- Règle : paiement PAID uniquement si commande DELIVERED
--         paiement PENDING pour tous les autres statuts actifs
-- ============================================================

-- 1. Commandes DELIVERED → paiement PAID
UPDATE orders
SET statut_paiement = 'PAID'
WHERE statut = 'DELIVERED'
  AND statut_paiement != 'PAID';

-- 2. Commandes actives (PENDING, PAID, PROCESSING, SHIPPED) → paiement PENDING
UPDATE orders
SET statut_paiement = 'PENDING'
WHERE statut IN ('PENDING', 'PAID', 'PROCESSING', 'SHIPPED')
  AND statut_paiement = 'PAID';

-- 3. Vérification après correction
SELECT 
    statut,
    statut_paiement,
    COUNT(*) as nombre
FROM orders
GROUP BY statut, statut_paiement
ORDER BY statut;
