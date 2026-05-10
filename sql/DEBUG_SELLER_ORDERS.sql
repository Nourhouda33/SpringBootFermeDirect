-- Script de débogage pour vérifier les commandes du vendeur

-- 1. Vérifier les utilisateurs vendeurs
SELECT u.id, u.email, u.role, sp.id as seller_profile_id, sp.nom_boutique
FROM users u
LEFT JOIN seller_profiles sp ON sp.user_id = u.id
WHERE u.role = 'SELLER';

-- 2. Vérifier les produits du vendeur (remplacez USER_ID par l'ID du vendeur)
SELECT p.id, p.nom, p.prix, sp.nom_boutique, u.email as vendeur_email
FROM products p
JOIN seller_profiles sp ON p.seller_profile_id = sp.id
JOIN users u ON sp.user_id = u.id
WHERE u.id = 'REMPLACEZ_PAR_ID_VENDEUR';

-- 3. Vérifier les commandes contenant les produits du vendeur
SELECT 
    o.id as order_id,
    o.numero_commande,
    o.date_commande,
    oi.product_id,
    p.nom as produit,
    sp.nom_boutique,
    u.email as vendeur_email
FROM orders o
JOIN order_items oi ON oi.order_id = o.id
JOIN products p ON oi.product_id = p.id
JOIN seller_profiles sp ON p.seller_profile_id = sp.id
JOIN users u ON sp.user_id = u.id
WHERE u.id = 'REMPLACEZ_PAR_ID_VENDEUR'
ORDER BY o.date_commande DESC;

-- 4. Vérifier toutes les commandes avec leurs produits
SELECT 
    o.id as order_id,
    o.numero_commande,
    o.date_commande,
    p.nom as produit,
    sp.nom_boutique,
    u.email as vendeur_email
FROM orders o
JOIN order_items oi ON oi.order_id = o.id
JOIN products p ON oi.product_id = p.id
LEFT JOIN seller_profiles sp ON p.seller_profile_id = sp.id
LEFT JOIN users u ON sp.user_id = u.id
ORDER BY o.date_commande DESC;
