-- Vérifier l'état des produits dans la base de données
SELECT 
    p.id,
    p.nom,
    p.prix,
    p.stock,
    p.actif,
    sp.nom_boutique as vendeur,
    u.email as email_vendeur
FROM product p
LEFT JOIN seller_profile sp ON p.seller_profile_id = sp.id
LEFT JOIN user u ON sp.user_id = u.id
ORDER BY p.date_creation DESC;

-- Si les produits ne sont pas actifs, les activer :
-- UPDATE product SET actif = true WHERE actif = false;
