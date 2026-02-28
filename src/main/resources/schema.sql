SET FOREIGN_KEY_CHECKS = 0;

-- 1. Nettoyage
DROP TABLE IF EXISTS brought_equipment;
DROP TABLE IF EXISTS group_equipment_items;
DROP TABLE IF EXISTS group_equipments;
DROP TABLE IF EXISTS hike_food_products;
DROP TABLE IF EXISTS hike_participants;
DROP TABLE IF EXISTS backpack_equipment;
DROP TABLE IF EXISTS backpacks;
DROP TABLE IF EXISTS points_of_interest;
DROP TABLE IF EXISTS hikes;
DROP TABLE IF EXISTS equipment_items;
DROP TABLE IF EXISTS food_products;
DROP TABLE IF EXISTS participants;
DROP TABLE IF EXISTS users;

-- 2. Création des tables
CREATE TABLE users (
                       id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                       nom VARCHAR(255) NOT NULL,
                       prenom VARCHAR(255) NOT NULL,
                       mail VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255),
                       adresse VARCHAR(255),
                       age INTEGER,
                       niveau VARCHAR(50),
                       morphologie VARCHAR(50)
);

CREATE TABLE participants (
                              id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                              nom VARCHAR(255) NOT NULL,
                              prenom VARCHAR(255) NOT NULL,
                              age INT,
                              niveau VARCHAR(50),
                              morphologie VARCHAR(50),
                              creator BOOLEAN,
                              creator_id BIGINT,
                              besoin_kcal INTEGER NOT NULL,
                              besoin_eau_litre INTEGER NOT NULL,
                              capacite_emport_max_kg DOUBLE NOT NULL
);

CREATE TABLE points_of_interest (
                                    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                    nom VARCHAR(255) NOT NULL,
                                    description VARCHAR(500),
                                    latitude DOUBLE NOT NULL,
                                    longitude DOUBLE NOT NULL,
                                    hike_id BIGINT,
                                    sequence INT
);

CREATE TABLE hikes (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       libelle VARCHAR(255) NOT NULL,
                       duree_jours INT NOT NULL DEFAULT 1 CHECK (duree_jours >= 1 AND duree_jours <= 3),
                       creator_id BIGINT,
                       depart_id BIGINT,
                       arrivee_id BIGINT,
                       CONSTRAINT fk_hike_creator FOREIGN KEY (creator_id) REFERENCES users(id),
                       CONSTRAINT fk_hike_depart FOREIGN KEY (depart_id) REFERENCES points_of_interest(id),
                       CONSTRAINT fk_hike_arrivee FOREIGN KEY (arrivee_id) REFERENCES points_of_interest(id)
);

CREATE TABLE food_products (
                               id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                               nom VARCHAR(255),
                               description VARCHAR(255),
                               masse_grammes DOUBLE NOT NULL,
                               appellation_courante VARCHAR(255),
                               conditionnement VARCHAR(255),
                               apport_nutritionnel_kcal DOUBLE NOT NULL,
                               prix_euro DOUBLE NOT NULL,
                               nb_item INT DEFAULT 1 CHECK (nb_item >= 1 AND nb_item <= 3)
);

CREATE TABLE equipment_items (
                                 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                 nom VARCHAR(255),
                                 description VARCHAR(255),
                                 masse_grammes DOUBLE NOT NULL,
                                 nb_item INT DEFAULT 1 CHECK (nb_item >= 1 AND nb_item <= 3),
                                 type varchar(50),
                                 masse_a_vide DOUBLE NOT NULL DEFAULT 0.0
);

CREATE TABLE backpacks (
                           id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                           total_mass_kg DOUBLE NOT NULL,
                           participant_id BIGINT UNIQUE,
                           FOREIGN KEY (participant_id) REFERENCES participants(id) ON DELETE CASCADE
);

CREATE TABLE backpack_equipment (
                                    backpack_id BIGINT NOT NULL,
                                    equipment_id BIGINT NOT NULL,
                                    PRIMARY KEY (backpack_id, equipment_id),
                                    FOREIGN KEY (backpack_id) REFERENCES backpacks(id) ON DELETE CASCADE,
                                    FOREIGN KEY (equipment_id) REFERENCES equipment_items(id) ON DELETE CASCADE
);

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

CREATE TABLE group_equipments (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  type VARCHAR(50) NOT NULL,
                                  hike_id BIGINT NOT NULL,
                                  CONSTRAINT fk_group_hike FOREIGN KEY (hike_id) REFERENCES hikes(id) ON DELETE CASCADE
);

CREATE TABLE group_equipment_items (
                                       group_id BIGINT NOT NULL,
                                       equipment_id BIGINT NOT NULL,
                                       item_order INT NOT NULL,
                                       PRIMARY KEY (group_id, item_order),
                                       CONSTRAINT fk_gei_group FOREIGN KEY (group_id) REFERENCES group_equipments(id) ON DELETE CASCADE,
                                       CONSTRAINT fk_gei_item FOREIGN KEY (equipment_id) REFERENCES equipment_items(id)
);

CREATE TABLE belong_equipment (
                                  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                  hike_id BIGINT NOT NULL,
                                  participant_id BIGINT NOT NULL,
                                  equipment_id BIGINT NOT NULL,
                                  CONSTRAINT fk_be_hike FOREIGN KEY (hike_id) REFERENCES hikes(id) ON DELETE CASCADE,
                                  CONSTRAINT fk_be_participant FOREIGN KEY (participant_id) REFERENCES participants(id) ON DELETE CASCADE,
                                  CONSTRAINT fk_be_equipment FOREIGN KEY (equipment_id) REFERENCES equipment_items(id) ON DELETE CASCADE
);

SET FOREIGN_KEY_CHECKS = 1;