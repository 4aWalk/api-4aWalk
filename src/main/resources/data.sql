-- #######################################################
-- # SCRIPT D'INSERTION AVEC VÉRIFICATION (MySQL)
-- #######################################################

USE fourawalkdb;

-- IMPORTANT : Désactivation pour permettre les DELETE
SET SQL_SAFE_UPDATES = 0;

-- Nettoyage
DELETE FROM backpack_food_items;
DELETE FROM backpack_equipment;
DELETE FROM hike_participants;
DELETE FROM backpacks;
DELETE FROM participants;
DELETE FROM equipment_items;
DELETE FROM food_products;
DELETE FROM hikes;
DELETE FROM users;

-- Réinitialisation des compteurs
ALTER TABLE users AUTO_INCREMENT = 1;
-- ... autres AUTO_INCREMENT ...

-- --- 1. Insertion Utilisateur Valide (ID 1) ---
SET @password_hash = '$2a$10$wTf7JzG3i8xG4oWw0oTf7eQ7xG4oWw0oTf7e';

INSERT INTO users (id, nom, prenom, mail, password, adresse, age, niveau, morphologie) VALUES
    (1, 'Valid', 'User', 'valid.user@test.com', @password_hash, '12 Rue de la Validité', 30, 'SPORTIF', 'LEGERE');
-- NOTE: Un âge > 99 ou niveau 'EXPERT' échouerait ici

-- --- 2. Insertion d'Objets Valides ---

-- FoodProduct (ID 1): Masse (150g < 10kg), Kcal (600 < 3000)
INSERT INTO food_products (id, nom, description, masse_grammes, appellation_courante, conditionnement, apport_nutritionnel_kcal, prix_euro) VALUES
    (1, 'Ration Normale', 'Ration énergétique', 150.00, 'Ration', 'Sachet', 600.00, 5.00);

-- EquipmentItem (ID 1): Masse (1500g < 20kg)
INSERT INTO equipment_items (id, nom, description, masse_grammes, permet_repos) VALUES
    (1, 'Sac de Couchage', 'Sac standard', 1500.00, TRUE);

-- --- 3. Insertion d'un Participant Valide (ID 1) ---
-- Besoins valides (Kcal 2500 < 5000, Eau 3.0 < 5.0)
INSERT INTO participants (id, nom_complet, age, niveau, morphologie, besoin_kcal, besoin_eau_litre, capacite_emport_max_kg) VALUES
    (1, 'Participant Validé', 25, 'ENTRAINE', 'MOYENNE', 2500.0, 3.0, 15.0);


-- --- 4. Création d'une Randonnée Valide (ID 1) ---
-- Durée 2 jours (entre 1 et 3)
INSERT INTO hikes (id, libelle, depart, arrivee, duree_jours, creator_id) VALUES
    (1, 'Rando Validée', 'Départ A', 'Arrivée B', 2, 1);

-- --- 5. Sac à Dos et Contenu Valide ---
INSERT INTO backpacks (id, participant_id, total_mass_kg) VALUES
    (1, 1, 5.0); -- Poids 5 kg (< 30 kg)

-- Ajout de Nourriture : Quantité 3 (<= 3)
INSERT INTO backpack_food_items (id, backpack_id, food_product_id, quantity) VALUES
    (1, 1, 1, 3);

INSERT INTO backpack_equipment (backpack_id, equipment_id) VALUES
    (1, 1);

INSERT INTO hike_food (hike_id, food_id) VALUES (1, 1);
INSERT INTO hike_equipment (hike_id, equipment_id) VALUES (1, 1);

-- Réactivation des vérifications
SET SQL_SAFE_UPDATES = 1;
