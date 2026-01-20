-- #######################################################
-- # SCRIPT DDL NETTOYÉ (MySQL)
-- #######################################################

SET FOREIGN_KEY_CHECKS = 0;

-- Suppression propre pour repartir à zéro (ordre des dépendances respecté)
DROP TABLE IF EXISTS points_of_interest;
DROP TABLE IF EXISTS hike_equipment;
DROP TABLE IF EXISTS hike_food;
DROP TABLE IF EXISTS backpack_food_items;
DROP TABLE IF EXISTS backpack_equipment;
DROP TABLE IF EXISTS hike_participants;
DROP TABLE IF EXISTS backpacks;
DROP TABLE IF EXISTS equipment_items;
DROP TABLE IF EXISTS food_products;
DROP TABLE IF EXISTS participants;
DROP TABLE IF EXISTS hikes;
DROP TABLE IF EXISTS users;

-- Tables de base
CREATE TABLE users (
                       id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                       nom VARCHAR(255),
                       prenom VARCHAR(255),
                       mail VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255),
                       adresse VARCHAR(255),
                       age INTEGER,
                       niveau VARCHAR(50),
                       morphologie VARCHAR(50)
);

CREATE TABLE participants (
                              id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                              nom_complet VARCHAR(255),
                              age INT,
                              niveau VARCHAR(50),
                              morphologie VARCHAR(50),
                              besoin_kcal INTEGER NOT NULL,
                              besoin_eau_litre INTEGER NOT NULL,
                              capacite_emport_max_kg DOUBLE NOT NULL
);

CREATE TABLE hikes (
                       id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                       libelle VARCHAR(255),
                       depart VARCHAR(255),
                       arrivee VARCHAR(255),
                       duree_jours INTEGER,
                       creator_id BIGINT,
                       FOREIGN KEY (creator_id) REFERENCES users(id)
);

CREATE TABLE food_products (
                               id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                               nom VARCHAR(255),
                               description VARCHAR(255),
                               masse_grammes DOUBLE NOT NULL,
                               appellation_courante VARCHAR(255),
                               conditionnement VARCHAR(255),
                               apport_nutritionnel_kcal DOUBLE NOT NULL,
                               prix_euro DOUBLE NOT NULL
);

CREATE TABLE equipment_items (
                                 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                 nom VARCHAR(255),
                                 description VARCHAR(255),
                                 masse_grammes DOUBLE NOT NULL,
                                 permet_repos BOOLEAN
);

CREATE TABLE backpacks (
                           id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                           total_mass_kg DOUBLE NOT NULL,
                           participant_id BIGINT UNIQUE,
                           FOREIGN KEY (participant_id) REFERENCES participants(id) ON DELETE CASCADE
);

-- Tables de détails et liaisons
CREATE TABLE backpack_food_items (
                                     id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                     quantity INTEGER NOT NULL,
                                     backpack_id BIGINT NOT NULL,
                                     food_product_id BIGINT NOT NULL,
                                     FOREIGN KEY (backpack_id) REFERENCES backpacks(id) ON DELETE CASCADE,
                                     FOREIGN KEY (food_product_id) REFERENCES food_products(id)
);

CREATE TABLE backpack_equipment (
                                    backpack_id BIGINT NOT NULL,
                                    equipment_id BIGINT NOT NULL,
                                    PRIMARY KEY (backpack_id, equipment_id),
                                    FOREIGN KEY (backpack_id) REFERENCES backpacks(id) ON DELETE CASCADE,
                                    FOREIGN KEY (equipment_id) REFERENCES equipment_items(id) ON DELETE CASCADE
);

-- Liaisons Randonnées
CREATE TABLE hike_participants (
                                   hike_id BIGINT NOT NULL, participant_id BIGINT NOT NULL,
                                   PRIMARY KEY (hike_id, participant_id),
                                   FOREIGN KEY (hike_id) REFERENCES hikes(id) ON DELETE CASCADE,
                                   FOREIGN KEY (participant_id) REFERENCES participants(id) ON DELETE CASCADE
);

CREATE TABLE hike_food (
                           hike_id BIGINT NOT NULL, food_id BIGINT NOT NULL,
                           PRIMARY KEY (hike_id, food_id),
                           FOREIGN KEY (hike_id) REFERENCES hikes(id) ON DELETE CASCADE,
                           FOREIGN KEY (food_id) REFERENCES food_products(id) ON DELETE CASCADE
);

CREATE TABLE hike_equipment (
                                hike_id BIGINT NOT NULL, equipment_id BIGINT NOT NULL,
                                PRIMARY KEY (hike_id, equipment_id),
                                FOREIGN KEY (hike_id) REFERENCES hikes(id) ON DELETE CASCADE,
                                FOREIGN KEY (equipment_id) REFERENCES equipment_items(id) ON DELETE CASCADE
);

SET FOREIGN_KEY_CHECKS = 1;