-- Script pour ajouter les nouvelles colonnes à la table addresses

ALTER TABLE addresses ADD COLUMN IF NOT EXISTS prenom VARCHAR(255);
ALTER TABLE addresses ADD COLUMN IF NOT EXISTS nom VARCHAR(255);
ALTER TABLE addresses ADD COLUMN IF NOT EXISTS gouvernorat VARCHAR(255);
ALTER TABLE addresses ADD COLUMN IF NOT EXISTS telephone VARCHAR(255);
ALTER TABLE addresses ADD COLUMN IF NOT EXISTS instructions TEXT;
