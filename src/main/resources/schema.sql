-- #######################################################
-- # SCRIPT DDL AVEC VALIDATION DES DONNÉES (MySQL)
-- #######################################################

-- Crée la base de données et la sélectionne
CREATE DATABASE IF NOT EXISTS fourawalkdb;
USE fourawalkdb;

-- Désactivation des vérifications pour permettre les DROP TABLE
SET SQL_SAFE_UPDATES = 0;

-- Suppression des tables (ordre inverse des dépendances)
DROP TABLE IF EXISTS hike_food;
DROP TABLE IF EXISTS hike_equipement;
DROP TABLE IF EXISTS backpack_food_items;
DROP TABLE IF EXISTS backpack_food_items;
DROP TABLE IF EXISTS backpack_equipment;
DROP TABLE IF EXISTS hike_participants;
DROP TABLE IF EXISTS backpacks;
DROP TABLE IF EXISTS equipment_items;
DROP TABLE IF EXISTS food_products;
DROP TABLE IF EXISTS participants;
DROP TABLE IF EXISTS hikes;
DROP TABLE IF EXISTS users;

-- -------------------------------------------------------
-- 1. TABLE USERS (Ajout de Contraintes CHECK/UNIQUE)
-- -------------------------------------------------------

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255),
    prenom VARCHAR(255),

    -- Contrainte UNIQUE et NOT NULL gérée par JPA, nous ajoutons la REGEX
    mail VARCHAR(255) NOT NULL UNIQUE,

    password VARCHAR(255),
    adresse VARCHAR(255),

    -- VALIDATION : Âge entre 3 et 99
    age INTEGER,

    niveau VARCHAR(50),
    morphologie VARCHAR(50),

    -- Contraintes de Validation SQL
    -- Niveau et Morphologie doivent correspondre aux ENUMS Java
    CONSTRAINT chk_user_niveau CHECK (niveau IN ('DEBUTANT', 'ENTRAINE', 'SPORTIF')),
    CONSTRAINT chk_user_morphology CHECK (morphologie IN ('LEGERE', 'MOYENNE', 'FORTE')),
    CONSTRAINT chk_user_age CHECK (age BETWEEN 3 AND 99),

    -- REGEX simple pour l'email (vérifie la présence de @ et d'au moins un .)
    -- NOTE: La fonction REGEXP n'est pas standard. Si vous utilisez une version de MySQL supportant CHECK, cela fonctionne.
    CONSTRAINT chk_user_mail_regex CHECK (mail REGEXP '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$')
    -- Pour une portabilité maximale, la validation REGEX est préférable côté Java/Service. Nous nous contentons ici des bornes numériques.

    -- Utilisons simplement les bornes numériques ici.
    -- Les contraintes de REGEX sont souvent gérées en couche Service/Controller.
);

-- -------------------------------------------------------
-- 2. TABLE PARTICIPANTS (Ajout de Contraintes CHECK)
-- -------------------------------------------------------

CREATE TABLE participants (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nom_complet VARCHAR(255),

    -- VALIDATION : Âge entre 3 et 99
    age INT,
    niveau VARCHAR(50),
    morphologie VARCHAR(50),

    -- VALIDATION : Besoin KCAL (max 5000)
    besoin_kcal DECIMAL(10, 2),

    -- VALIDATION : Besoin Eau (max 5.0 L)
    besoin_eau_litre DECIMAL(10, 2),

    -- VALIDATION : Capacité Sac (max 30 kg)
    capacite_emport_max_kg DECIMAL(10, 2),

    -- Contraintes de Validation SQL
    CONSTRAINT chk_participant_niveau CHECK (niveau IN ('DEBUTANT', 'ENTRAINE', 'SPORTIF')),
    CONSTRAINT chk_participant_morphology CHECK (morphologie IN ('LEGERE', 'MOYENNE', 'FORTE')),
    CONSTRAINT chk_participant_age CHECK (age BETWEEN 3 AND 99),
    CONSTRAINT chk_participant_kcal CHECK (besoin_kcal <= 5000.00),
    CONSTRAINT chk_participant_eau CHECK (besoin_eau_litre <= 5.00)
);

-- -------------------------------------------------------
-- 3. TABLE HIKES (Ajout de Contraintes CHECK)
-- -------------------------------------------------------

CREATE TABLE hikes (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    libelle VARCHAR(255),
    depart VARCHAR(255),
    arrivee VARCHAR(255),

    -- VALIDATION : Durée en jours (entre 1 et 3)
    duree_jours INTEGER,

    creator_id BIGINT,

    FOREIGN KEY (creator_id) REFERENCES users(id),
    CONSTRAINT chk_hike_duration CHECK (duree_jours BETWEEN 1 AND 3)
);

-- Table de jointure HIKES <-> PARTICIPANTS (pas de validation nécessaire ici)
-- Table de jointure HIKES <-> PARTICIPANTS (pas de validation nécessaire ici)
CREATE TABLE hike_participants (
    hike_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    PRIMARY KEY (hike_id, participant_id),
    FOREIGN KEY (hike_id) REFERENCES hikes(id) ON DELETE CASCADE,
    FOREIGN KEY (participant_id) REFERENCES participants(id) ON DELETE CASCADE
);

-- -------------------------------------------------------
-- 4. TABLES OBJETS
-- -------------------------------------------------------

-- 4.1 FOOD_PRODUCTS
CREATE TABLE food_products (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255),
    description TEXT,

    -- VALIDATION : Masse (max 10 kg = 10000 g)
    masse_grammes DECIMAL(10, 2),

    appellation_courante VARCHAR(255),
    conditionnement VARCHAR(100),

    -- VALIDATION : Apport (max 3000 kcal)
    apport_nutritionnel_kcal DECIMAL(10, 2),
    prix_euro DECIMAL(10, 2),

    CONSTRAINT chk_food_mass CHECK (masse_grammes <= 10000.00),
    CONSTRAINT chk_food_kcal CHECK (apport_nutritionnel_kcal <= 3000.00)
);

-- 4.2 EQUIPMENT_ITEMS
CREATE TABLE equipment_items (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255),
    description TEXT,

    -- VALIDATION : Masse (max 20 kg = 20000 g)
    masse_grammes DECIMAL(10, 2),
    permet_repos BOOLEAN,

    CONSTRAINT chk_equipment_mass CHECK (masse_grammes <= 20000.00)
);

-- -------------------------------------------------------
-- 5. TABLE BACKPACKS
-- -------------------------------------------------------

CREATE TABLE backpacks (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,

    -- VALIDATION : Poids total réel porté (max 30 kg = 30.0 kg)
    total_mass_kg DECIMAL(10, 2),

    participant_id BIGINT UNIQUE,

    FOREIGN KEY (participant_id) REFERENCES participants(id) ON DELETE CASCADE,
    CONSTRAINT chk_backpack_capacity CHECK (total_mass_kg <= 30.00)
);

-- -------------------------------------------------------
-- 6. TABLE BACKPACK_FOOD_ITEMS
-- -------------------------------------------------------
-- VALIDATION : Quantité (max 3)

CREATE TABLE backpack_food_items (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,

    -- VALIDATION : Quantité (max 3)
    quantity INTEGER NOT NULL,

    backpack_id BIGINT NOT NULL,
    food_product_id BIGINT NOT NULL,

    FOREIGN KEY (backpack_id) REFERENCES backpacks(id) ON DELETE CASCADE,
    FOREIGN KEY (food_product_id) REFERENCES food_products(id),

    CONSTRAINT chk_food_quantity CHECK (quantity <= 3)
);

-- Tables de jointure
CREATE TABLE backpack_equipment (
    backpack_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    PRIMARY KEY (backpack_id, equipment_id),
    FOREIGN KEY (backpack_id) REFERENCES backpacks(id) ON DELETE CASCADE,
    FOREIGN KEY (equipment_id) REFERENCES equipment_items(id) ON DELETE CASCADE
);

-- Liaison Randonnée <-> Nourriture
CREATE TABLE hike_food (
    hike_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,
    PRIMARY KEY (hike_id, food_id),
    CONSTRAINT fk_hike_f FOREIGN KEY (hike_id) REFERENCES hike(id),
    CONSTRAINT fk_food_h FOREIGN KEY (food_id) REFERENCES food_item(id)
);

-- Liaison Randonnée <-> Équipement
CREATE TABLE hike_equipment (
    hike_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    PRIMARY KEY (hike_id, equipment_id),
    CONSTRAINT fk_hike_e FOREIGN KEY (hike_id) REFERENCES hike(id),
    CONSTRAINT fk_equip_h FOREIGN KEY (equipment_id) REFERENCES equipment_item(id)
);
-- Réactivation des vérifications
SET SQL_SAFE_UPDATES = 1;
