-- ============================================================
-- Script SQL : Créer un utilisateur Administrateur
-- FermeDirecte - Création manuelle d'un compte admin
-- ============================================================

-- ⚠️ IMPORTANT : Ce script est fourni pour référence uniquement.
-- L'admin est maintenant créé AUTOMATIQUEMENT au démarrage du backend.
-- Utilisez ce script seulement si vous avez besoin de créer un admin manuellement.

-- ============================================================
-- Vérifier si l'admin existe déjà
-- ============================================================
SELECT 
    u.id,
    u.email,
    u.prenom,
    u.nom,
    u.role,
    u.actif,
    CASE 
        WHEN sp.id IS NULL THEN '❌ MANQUANT'
        ELSE '✅ OK'
    END AS statut_profil_vendeur
FROM users u
LEFT JOIN seller_profiles sp ON u.id = sp.user_id
WHERE u.email = 'admin@fermedirecte.com';

-- ============================================================
-- Créer l'utilisateur admin (si nécessaire)
-- ============================================================

-- Note : Le mot de passe est "Admin123!" encodé en BCrypt
-- Pour générer un nouveau hash BCrypt, utilisez plutôt l'API /api/auth/register

-- Supprimer l'admin existant si vous voulez le recréer (ATTENTION : DESTRUCTIF)
-- DELETE FROM seller_profiles WHERE user_id IN (SELECT id FROM users WHERE email = 'admin@fermedirecte.com');
-- DELETE FROM users WHERE email = 'admin@fermedirecte.com';

-- Créer l'utilisateur admin
INSERT INTO users (email, mot_de_passe, prenom, nom, telephone, role, actif, date_inscription)
VALUES (
    'admin@fermedirecte.com',
    -- Mot de passe : Admin123! (encodé en BCrypt avec strength 10)
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Admin',
    'FermeDirecte',
    '0123456789',
    'ADMIN',
    true,
    NOW()
);

-- Récupérer l'ID de l'utilisateur créé
SET @admin_user_id = LAST_INSERT_ID();

-- Créer le profil vendeur pour l'admin
INSERT INTO seller_profiles (user_id, nom_boutique, description, note)
VALUES (
    @admin_user_id,
    'Administration FermeDirecte',
    'Boutique officielle de l''administration',
    0.0
);

-- ============================================================
-- Vérification finale
-- ============================================================
SELECT 
    u.id,
    u.email,
    u.prenom,
    u.nom,
    u.role,
    u.actif,
    sp.id AS seller_profile_id,
    sp.nom_boutique
FROM users u
LEFT JOIN seller_profiles sp ON u.id = sp.user_id
WHERE u.email = 'admin@fermedirecte.com';

-- ============================================================
-- CREDENTIALS ADMIN
-- ============================================================
-- Email : admin@fermedirecte.com
-- Mot de passe : Admin123!
-- ============================================================

-- ============================================================
-- NOTES IMPORTANTES
-- ============================================================
-- 1. L'admin est créé AUTOMATIQUEMENT au démarrage du backend
--    grâce à la classe DataInitializer.java
-- 
-- 2. Utilisez ce script SQL seulement si :
--    - Vous avez supprimé l'admin par erreur
--    - Le DataInitializer ne fonctionne pas
--    - Vous voulez créer un admin avec un mot de passe différent
--
-- 3. Pour générer un nouveau hash BCrypt :
--    - Utilisez l'API : POST /api/auth/register avec role: "ADMIN"
--    - Ou utilisez un générateur en ligne : https://bcrypt-generator.com/
--    - Ou utilisez un outil Java/Spring
--
-- 4. En production, changez IMMÉDIATEMENT le mot de passe par défaut !
-- ============================================================
