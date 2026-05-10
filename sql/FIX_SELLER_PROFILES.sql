-- ============================================================
-- Script de correction : Ajouter des profils vendeurs manquants
-- FermeDirecte - Fix pour les utilisateurs ADMIN et SELLER sans profil
-- ============================================================

-- 1. Vérifier les utilisateurs sans profil vendeur
SELECT 
    u.id,
    u.email,
    u.prenom,
    u.nom,
    u.role,
    CASE 
        WHEN sp.id IS NULL THEN 'MANQUANT'
        ELSE 'OK'
    END AS statut_profil
FROM users u
LEFT JOIN seller_profiles sp ON u.id = sp.user_id
WHERE u.role IN ('SELLER', 'ADMIN')
ORDER BY u.role, u.id;

-- 2. Créer des profils vendeurs pour les ADMIN sans profil
INSERT INTO seller_profiles (user_id, nom_boutique, description, note)
SELECT 
    u.id,
    'Administration FermeDirecte',
    'Boutique officielle de l''administration',
    0.0
FROM users u
LEFT JOIN seller_profiles sp ON u.id = sp.user_id
WHERE u.role = 'ADMIN' 
  AND sp.id IS NULL;

-- 3. Créer des profils vendeurs pour les SELLER sans profil
INSERT INTO seller_profiles (user_id, nom_boutique, description, note)
SELECT 
    u.id,
    CONCAT(u.prenom, ' ', u.nom),
    'Bienvenue dans ma boutique !',
    0.0
FROM users u
LEFT JOIN seller_profiles sp ON u.id = sp.user_id
WHERE u.role = 'SELLER' 
  AND sp.id IS NULL;

-- 4. Vérification finale
SELECT 
    u.role,
    COUNT(*) AS total_utilisateurs,
    COUNT(sp.id) AS avec_profil,
    COUNT(*) - COUNT(sp.id) AS sans_profil
FROM users u
LEFT JOIN seller_profiles sp ON u.id = sp.user_id
WHERE u.role IN ('SELLER', 'ADMIN')
GROUP BY u.role;

-- 5. Afficher tous les profils vendeurs créés
SELECT 
    sp.id,
    sp.nom_boutique,
    u.email,
    u.role,
    sp.description
FROM seller_profiles sp
JOIN users u ON sp.user_id = u.id
ORDER BY u.role, sp.id;

-- ============================================================
-- Notes d'utilisation :
-- ============================================================
-- 1. Exécuter ce script si vous avez des utilisateurs ADMIN ou SELLER
--    créés avant la mise à jour du code
-- 2. Le script est idempotent : il ne créera pas de doublons
-- 3. Après exécution, tous les ADMIN et SELLER auront un profil vendeur
-- ============================================================
