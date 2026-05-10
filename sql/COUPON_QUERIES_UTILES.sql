-- ============================================
-- REQUÊTES UTILES POUR LA GESTION DES COUPONS
-- ============================================

-- 1. Voir tous les coupons actifs
SELECT 
    c.id,
    c.code,
    c.description,
    c.pourcentage_reduction,
    c.montant_fixe_reduction,
    c.livraison_gratuite,
    c.scope,
    c.usages_actuels,
    c.usages_max_global,
    c.date_expiration,
    CASE 
        WHEN c.actif = TRUE 
         AND c.bloque = FALSE 
         AND c.date_debut <= CURRENT_TIMESTAMP 
         AND c.date_expiration > CURRENT_TIMESTAMP 
         AND c.usages_actuels < c.usages_max_global 
        THEN 'VALIDE' 
        ELSE 'INVALIDE' 
    END as statut
FROM coupons c
WHERE c.actif = TRUE
ORDER BY c.date_creation DESC;

-- 2. Statistiques globales des coupons
SELECT 
    COUNT(*) as total_coupons,
    COUNT(CASE WHEN actif = TRUE AND bloque = FALSE THEN 1 END) as coupons_actifs,
    COUNT(CASE WHEN bloque = TRUE THEN 1 END) as coupons_bloques,
    COUNT(CASE WHEN date_expiration < CURRENT_TIMESTAMP THEN 1 END) as coupons_expires,
    SUM(usages_actuels) as total_utilisations,
    COUNT(CASE WHEN scope = 'GLOBAL' THEN 1 END) as coupons_globaux,
    COUNT(CASE WHEN scope = 'SELLER' THEN 1 END) as coupons_vendeurs
FROM coupons;

-- 3. Top 10 des coupons les plus utilisés
SELECT 
    c.code,
    c.description,
    c.usages_actuels,
    c.usages_max_global,
    ROUND((c.usages_actuels::DECIMAL / c.usages_max_global) * 100, 2) as taux_utilisation,
    COUNT(DISTINCT cu.user_id) as utilisateurs_uniques,
    COALESCE(SUM(cu.montant_reduit), 0) as montant_total_economise
FROM coupons c
LEFT JOIN coupon_usages cu ON c.id = cu.coupon_id
GROUP BY c.id, c.code, c.description, c.usages_actuels, c.usages_max_global
ORDER BY c.usages_actuels DESC
LIMIT 10;

-- 4. Coupons d'un vendeur spécifique
SELECT 
    c.id,
    c.code,
    c.description,
    c.usages_actuels,
    c.usages_max_global,
    c.actif,
    c.date_expiration,
    COALESCE(SUM(cu.montant_reduit), 0) as montant_total_economise
FROM coupons c
LEFT JOIN coupon_usages cu ON c.id = cu.coupon_id
WHERE c.seller_id = <SELLER_ID>  -- Remplacer par l'ID du vendeur
GROUP BY c.id
ORDER BY c.date_creation DESC;

-- 5. Historique d'utilisation d'un coupon
SELECT 
    cu.id,
    c.code as code_coupon,
    u.prenom || ' ' || u.nom as utilisateur,
    u.email,
    o.numero_commande,
    cu.montant_reduit,
    cu.date_utilisation
FROM coupon_usages cu
JOIN coupons c ON cu.coupon_id = c.id
JOIN users u ON cu.user_id = u.id
JOIN orders o ON cu.order_id = o.id
WHERE c.id = <COUPON_ID>  -- Remplacer par l'ID du coupon
ORDER BY cu.date_utilisation DESC;

-- 6. Utilisateurs ayant utilisé un coupon spécifique
SELECT 
    u.id,
    u.prenom,
    u.nom,
    u.email,
    COUNT(cu.id) as nombre_utilisations,
    SUM(cu.montant_reduit) as total_economise,
    MAX(cu.date_utilisation) as derniere_utilisation
FROM users u
JOIN coupon_usages cu ON u.id = cu.user_id
WHERE cu.coupon_id = <COUPON_ID>  -- Remplacer par l'ID du coupon
GROUP BY u.id, u.prenom, u.nom, u.email
ORDER BY nombre_utilisations DESC;

-- 7. Coupons expirés à nettoyer (plus de 6 mois)
SELECT 
    c.id,
    c.code,
    c.description,
    c.date_expiration,
    c.usages_actuels
FROM coupons c
WHERE c.date_expiration < CURRENT_TIMESTAMP - INTERVAL '6 months'
ORDER BY c.date_expiration;

-- 8. Vérifier si un utilisateur peut utiliser un coupon
SELECT 
    c.code,
    c.description,
    can_user_use_coupon(c.id, <USER_ID>) as peut_utiliser,  -- Remplacer USER_ID
    c.usages_max_par_utilisateur,
    COUNT(cu.id) as utilisations_actuelles
FROM coupons c
LEFT JOIN coupon_usages cu ON c.id = cu.coupon_id AND cu.user_id = <USER_ID>
WHERE c.code = '<CODE_COUPON>'  -- Remplacer par le code
GROUP BY c.id, c.code, c.description, c.usages_max_par_utilisateur;

-- 9. Rapport mensuel des coupons
SELECT 
    DATE_TRUNC('month', cu.date_utilisation) as mois,
    COUNT(DISTINCT cu.coupon_id) as coupons_utilises,
    COUNT(cu.id) as nombre_utilisations,
    COUNT(DISTINCT cu.user_id) as utilisateurs_uniques,
    SUM(cu.montant_reduit) as montant_total_economise
FROM coupon_usages cu
WHERE cu.date_utilisation >= CURRENT_TIMESTAMP - INTERVAL '12 months'
GROUP BY DATE_TRUNC('month', cu.date_utilisation)
ORDER BY mois DESC;

-- 10. Coupons hybrides (avec plusieurs types de réductions)
SELECT 
    c.code,
    c.description,
    c.pourcentage_reduction,
    c.montant_fixe_reduction,
    c.livraison_gratuite,
    c.usages_actuels,
    c.actif
FROM coupons c
WHERE (
    (c.pourcentage_reduction IS NOT NULL AND c.pourcentage_reduction > 0)::int +
    (c.montant_fixe_reduction IS NOT NULL AND c.montant_fixe_reduction > 0)::int +
    (c.livraison_gratuite = TRUE)::int
) >= 2
ORDER BY c.date_creation DESC;

-- 11. Performance des coupons par catégorie
SELECT 
    cat.nom as categorie,
    COUNT(DISTINCT c.id) as nombre_coupons,
    COUNT(cu.id) as utilisations,
    SUM(cu.montant_reduit) as montant_economise
FROM coupon_categories cc
JOIN coupons c ON cc.coupon_id = c.id
LEFT JOIN coupon_usages cu ON c.id = cu.coupon_id
LEFT JOIN categories cat ON cc.category_id = cat.id
GROUP BY cat.id, cat.nom
ORDER BY utilisations DESC;

-- 12. Coupons proches de l'expiration (7 jours)
SELECT 
    c.id,
    c.code,
    c.description,
    c.date_expiration,
    c.usages_actuels,
    c.usages_max_global,
    c.usages_max_global - c.usages_actuels as utilisations_restantes,
    EXTRACT(DAY FROM (c.date_expiration - CURRENT_TIMESTAMP)) as jours_restants
FROM coupons c
WHERE c.actif = TRUE 
  AND c.bloque = FALSE
  AND c.date_expiration BETWEEN CURRENT_TIMESTAMP AND CURRENT_TIMESTAMP + INTERVAL '7 days'
ORDER BY c.date_expiration;

-- 13. Analyse de l'efficacité des coupons
SELECT 
    c.code,
    c.scope,
    c.usages_max_global,
    c.usages_actuels,
    ROUND((c.usages_actuels::DECIMAL / c.usages_max_global) * 100, 2) as taux_utilisation,
    COALESCE(SUM(cu.montant_reduit), 0) as montant_total_economise,
    COALESCE(AVG(cu.montant_reduit), 0) as montant_moyen_par_utilisation,
    COUNT(DISTINCT cu.user_id) as utilisateurs_uniques,
    CASE 
        WHEN c.usages_actuels::DECIMAL / c.usages_max_global >= 0.8 THEN 'Très efficace'
        WHEN c.usages_actuels::DECIMAL / c.usages_max_global >= 0.5 THEN 'Efficace'
        WHEN c.usages_actuels::DECIMAL / c.usages_max_global >= 0.2 THEN 'Moyen'
        ELSE 'Faible'
    END as efficacite
FROM coupons c
LEFT JOIN coupon_usages cu ON c.id = cu.coupon_id
WHERE c.date_expiration < CURRENT_TIMESTAMP
GROUP BY c.id, c.code, c.scope, c.usages_max_global, c.usages_actuels
ORDER BY taux_utilisation DESC;

-- 14. Créer un coupon vendeur pour un vendeur existant
-- Remplacer <SELLER_ID> par l'ID réel du vendeur
INSERT INTO coupons (
    code, description, 
    pourcentage_reduction, montant_fixe_reduction, livraison_gratuite,
    montant_minimum, montant_maximum_reduction,
    scope, seller_id,
    usages_max_global, usages_max_par_utilisateur,
    date_debut, date_expiration
) VALUES 
(
    'MABOUTIQUE20', 
    'Coupon boutique : -20% + livraison offerte',
    20.00, NULL, TRUE,
    30.00, NULL,
    'SELLER', <SELLER_ID>,
    50, 2,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30 days'
);

-- 15. Désactiver tous les coupons expirés
UPDATE coupons 
SET actif = FALSE 
WHERE date_expiration < CURRENT_TIMESTAMP 
  AND actif = TRUE;

-- 16. Nettoyer les coupons jamais utilisés et expirés depuis plus de 3 mois
DELETE FROM coupons 
WHERE usages_actuels = 0 
  AND date_expiration < CURRENT_TIMESTAMP - INTERVAL '3 months';

-- 17. Vue pour le dashboard admin
CREATE OR REPLACE VIEW v_admin_coupon_dashboard AS
SELECT 
    (SELECT COUNT(*) FROM coupons WHERE actif = TRUE AND bloque = FALSE) as coupons_actifs,
    (SELECT COUNT(*) FROM coupons WHERE date_expiration < CURRENT_TIMESTAMP) as coupons_expires,
    (SELECT SUM(usages_actuels) FROM coupons) as total_utilisations,
    (SELECT COUNT(DISTINCT user_id) FROM coupon_usages) as utilisateurs_uniques,
    (SELECT COALESCE(SUM(montant_reduit), 0) FROM coupon_usages) as montant_total_economise,
    (SELECT COUNT(*) FROM coupons WHERE scope = 'GLOBAL') as coupons_globaux,
    (SELECT COUNT(*) FROM coupons WHERE scope = 'SELLER') as coupons_vendeurs;

-- 18. Vue pour le dashboard vendeur
CREATE OR REPLACE VIEW v_seller_coupon_dashboard AS
SELECT 
    c.seller_id,
    COUNT(c.id) as nombre_coupons,
    COUNT(CASE WHEN c.actif = TRUE AND c.bloque = FALSE THEN 1 END) as coupons_actifs,
    SUM(c.usages_actuels) as total_utilisations,
    COALESCE(SUM(cu.montant_reduit), 0) as montant_total_economise,
    COUNT(DISTINCT cu.user_id) as clients_uniques
FROM coupons c
LEFT JOIN coupon_usages cu ON c.id = cu.coupon_id
WHERE c.scope = 'SELLER'
GROUP BY c.seller_id;

-- 19. Rechercher des coupons par mot-clé
SELECT 
    c.id,
    c.code,
    c.description,
    c.scope,
    c.actif,
    c.date_expiration
FROM coupons c
WHERE 
    LOWER(c.code) LIKE LOWER('%<KEYWORD>%')  -- Remplacer KEYWORD
    OR LOWER(c.description) LIKE LOWER('%<KEYWORD>%')
ORDER BY c.date_creation DESC;

-- 20. Dupliquer un coupon existant
INSERT INTO coupons (
    code, description, 
    pourcentage_reduction, montant_fixe_reduction, livraison_gratuite,
    montant_minimum, montant_maximum_reduction,
    scope, seller_id,
    usages_max_global, usages_max_par_utilisateur,
    date_debut, date_expiration,
    actif, bloque
)
SELECT 
    '<NEW_CODE>',  -- Nouveau code
    description,
    pourcentage_reduction, montant_fixe_reduction, livraison_gratuite,
    montant_minimum, montant_maximum_reduction,
    scope, seller_id,
    usages_max_global, usages_max_par_utilisateur,
    CURRENT_TIMESTAMP,  -- Nouvelle date de début
    CURRENT_TIMESTAMP + INTERVAL '30 days',  -- Nouvelle date d'expiration
    TRUE, FALSE
FROM coupons
WHERE id = <COUPON_ID>;  -- ID du coupon à dupliquer

-- Fin des requêtes utiles
