SET FOREIGN_KEY_CHECKS = 0;

-- 1. Nettoyage
DROP TABLE IF EXISTS hike_equipment;
DROP TABLE IF EXISTS hike_food_products;
DROP TABLE IF EXISTS hike_participants;
DROP TABLE IF EXISTS backpack_food_items;
DROP TABLE IF EXISTS backpack_equipment;
DROP TABLE IF EXISTS backpacks;
DROP TABLE IF EXISTS points_of_interest;
DROP TABLE IF EXISTS hikes;
DROP TABLE IF EXISTS equipment_items;
DROP TABLE IF EXISTS food_products;
DROP TABLE IF EXISTS participants;
DROP TABLE IF EXISTS users;

-- 2. Tables de base (Sans dépendances)
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
                              age INT,
                              niveau VARCHAR(50),
                              morphologie VARCHAR(50),
                              creator BOOLEAN,
                              besoin_kcal INTEGER NOT NULL,
                              besoin_eau_litre INTEGER NOT NULL,
                              capacite_emport_max_kg DOUBLE NOT NULL
);

-- Table POI (Créée ici pour que Hike puisse la référencer)
CREATE TABLE points_of_interest (
                                    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                    nom VARCHAR(255) NOT NULL,
                                    description VARCHAR(500),
                                    latitude DOUBLE NOT NULL,
                                    longitude DOUBLE NOT NULL,
                                    hike_id BIGINT
);

-- 3. Table des Randonnées
CREATE TABLE hikes (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       libelle VARCHAR(255) NOT NULL,
                       duree_jours INT NOT NULL,
                       creator_id BIGINT,
                       depart_id BIGINT,
                       arrivee_id BIGINT,
                       CONSTRAINT fk_hike_creator FOREIGN KEY (creator_id) REFERENCES users(id),
                       CONSTRAINT fk_hike_depart FOREIGN KEY (depart_id) REFERENCES points_of_interest(id),
                       CONSTRAINT fk_hike_arrivee FOREIGN KEY (arrivee_id) REFERENCES points_of_interest(id)
);

-- 4. Catalogue et Équipement
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

-- 5. Sac à dos et Contenu
CREATE TABLE backpacks (
                           id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                           total_mass_kg DOUBLE NOT NULL,
                           participant_id BIGINT UNIQUE,
                           FOREIGN KEY (participant_id) REFERENCES participants(id) ON DELETE CASCADE
);

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

-- 6. Tables de jointures Randonnées (Noms alignés sur Hibernate)
CREATE TABLE hike_participants (
                                   hike_id BIGINT NOT NULL,
                                   participant_id BIGINT NOT NULL,
                                   PRIMARY KEY (hike_id, participant_id),
                                   FOREIGN KEY (hike_id) REFERENCES hikes(id) ON DELETE CASCADE,
                                   FOREIGN KEY (participant_id) REFERENCES participants(id) ON DELETE CASCADE
);

CREATE TABLE hike_food_products (
                                    hike_id BIGINT NOT NULL,
                                    food_product_id BIGINT NOT NULL,
                                    PRIMARY KEY (hike_id, food_product_id),
                                    FOREIGN KEY (hike_id) REFERENCES hikes(id) ON DELETE CASCADE,
                                    FOREIGN KEY (food_product_id) REFERENCES food_products(id) ON DELETE CASCADE
);

CREATE TABLE hike_equipment (
                                hike_id BIGINT NOT NULL,
                                equipment_id BIGINT NOT NULL,
                                PRIMARY KEY (hike_id, equipment_id),
                                FOREIGN KEY (hike_id) REFERENCES hikes(id) ON DELETE CASCADE,
                                FOREIGN KEY (equipment_id) REFERENCES equipment_items(id) ON DELETE CASCADE
);

SET FOREIGN_KEY_CHECKS = 1;