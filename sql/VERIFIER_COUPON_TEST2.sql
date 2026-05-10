-- Vérifier le coupon TEST2
SELECT 
    id,
    code,
    description,
    pourcentage_reduction,
    montant_fixe_reduction,
    livraison_gratuite,
    montant_minimum,
    montant_maximum_reduction,
    scope,
    vendeur_id,
    actif,
    bloque,
    usages_actuels,
    usages_max_global,
    usages_max_par_utilisateur,
    DATE_FORMAT(date_debut, '%Y-%m-%d') as debut,
    DATE_FORMAT(date_expiration, '%Y-%m-%d') as expiration
FROM coupons 
WHERE code = 'TEST2';

-- Nettoyer les utilisations de TEST2
DELETE FROM coupon_usages 
WHERE coupon_id IN (SELECT id FROM coupons WHERE code = 'TEST2');

-- Réinitialiser le compteur
UPDATE coupons 
SET usages_actuels = 0 
WHERE code = 'TEST2';

-- Vérifier
SELECT code, usages_actuels FROM coupons WHERE code = 'TEST2';
