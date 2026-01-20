-- #######################################################
-- # SCRIPT D'INSERTION (data.sql)
-- #######################################################

-- 1. Utilisateur
INSERT INTO users (id, nom, prenom, mail, password, adresse, age, niveau, morphologie) VALUES
    (1, 'Brouz', 'Admin', 'admin@4awalk.fr', '$2a$10$wTf7JzG3i8xG4oWw0oTf7eQ7xG4oWw0oTf7e', '12 Rue de la SAE', 30, 'SPORTIF', 'LEGERE');

-- 2. Produits et Équipements
INSERT INTO food_products (id, nom, description, masse_grammes, appellation_courante, conditionnement, apport_nutritionnel_kcal, prix_euro) VALUES
    (1, 'Ration Normale', 'Ration énergétique', 150.00, 'Ration', 'Sachet', 600.00, 5.00);

INSERT INTO equipment_items (id, nom, description, masse_grammes, permet_repos) VALUES
    (1, 'Sac de Couchage', 'Sac standard', 1500.00, TRUE);

-- 3. Participant et Randonnée
INSERT INTO participants (id, nom_complet, age, niveau, morphologie, besoin_kcal, besoin_eau_litre, capacite_emport_max_kg) VALUES
    (1, 'Jean Marcheur', 25, 'ENTRAINE', 'MOYENNE', 2500, 3, 15.0);

INSERT INTO hikes (id, libelle, depart, arrivee, duree_jours, creator_id) VALUES
    (1, 'Tour de l Aubrac', 'Rodez', 'Laguiole', 2, 1);

-- 4. Sac à dos
INSERT INTO backpacks (id, participant_id, total_mass_kg) VALUES
    (1, 1, 5.0);

-- 5. Contenu du sac (Utilisation de la table unique backpack_food_items)
INSERT INTO backpack_food_items (id, quantity, backpack_id, food_product_id) VALUES
    (1, 3, 1, 1);

INSERT INTO backpack_equipment (backpack_id, equipment_id) VALUES
    (1, 1);

-- 6. Contenu de la randonnée
INSERT INTO hike_food (hike_id, food_id) VALUES (1, 1);
INSERT INTO hike_equipment (hike_id, equipment_id) VALUES (1, 1);