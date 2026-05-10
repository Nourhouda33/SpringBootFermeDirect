# Résolution de l'erreur lors du passage de commande

## Problème identifié

L'erreur "Une erreur inattendue s'est produite" lors de la finalisation de commande était causée par :

1. **Adresse non persistée** : Lorsqu'une nouvelle adresse était créée, elle n'était pas sauvegardée en base de données avant d'être référencée par la commande.
2. **Colonnes manquantes** : Les nouvelles colonnes de la table `addresses` (prenom, nom, gouvernorat, telephone, instructions) pourraient ne pas exister dans votre base de données.

## Solutions appliquées

### 1. Correction du service OrderService

**Fichier modifié** : `OrderService.java`

**Changements** :
- Ajout de l'injection du `AddressRepository`
- Sauvegarde explicite de l'adresse avant de l'utiliser dans la commande avec `addressRepository.save(adresse)`

### 2. Script SQL pour mettre à jour la base de données

**Fichier** : `UPDATE_ADDRESS_TABLE.sql`

Ce script ajoute les colonnes manquantes à la table `addresses`.

## Étapes pour résoudre le problème

### Étape 1 : Redémarrer le backend

1. Arrêtez le serveur backend s'il est en cours d'exécution
2. Recompilez le projet :
   ```bash
   cd backend
   mvn clean install
   ```
3. Redémarrez le serveur :
   ```bash
   mvn spring-boot:run
   ```

### Étape 2 : Vérifier la base de données (optionnel mais recommandé)

Si le problème persiste, exécutez manuellement le script SQL :

1. Ouvrez phpMyAdmin (http://localhost/phpmyadmin)
2. Sélectionnez la base de données `fermedirecte`
3. Allez dans l'onglet "SQL"
4. Copiez et exécutez le contenu du fichier `UPDATE_ADDRESS_TABLE.sql`

### Étape 3 : Tester la commande

1. Connectez-vous à l'application frontend
2. Ajoutez des produits au panier
3. Allez à la page de checkout
4. Remplissez l'adresse de livraison
5. Finalisez la commande

## Vérification des logs

Si l'erreur persiste, vérifiez les logs du backend pour identifier le problème exact :

```bash
# Dans le terminal où le backend est lancé, cherchez les erreurs
# Les logs devraient afficher le message d'erreur complet
```

Les erreurs courantes peuvent inclure :
- `Column 'prenom' not found` → Exécutez le script SQL
- `object references an unsaved transient instance` → Vérifiez que le code modifié est bien déployé
- `Connection refused` → Vérifiez que MySQL est démarré (XAMPP)

## Améliorations futures possibles

1. **Validation côté frontend** : Ajouter plus de validation pour les champs d'adresse
2. **Gestion des adresses temporaires** : Permettre de ne pas sauvegarder l'adresse si l'utilisateur ne le souhaite pas
3. **Messages d'erreur plus explicites** : Améliorer les messages d'erreur pour faciliter le débogage

## Contact

Si le problème persiste après ces étapes, vérifiez :
- Que XAMPP est bien démarré (Apache et MySQL)
- Que la base de données `fermedirecte` existe
- Que les ports 8080 (backend) et 4200 (frontend) ne sont pas utilisés par d'autres applications
