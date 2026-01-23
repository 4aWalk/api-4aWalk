-- 1. Utilisateur
INSERT INTO users (id, nom, prenom, mail, password, adresse, age, niveau, morphologie) VALUES
    (1, 'Brouz', 'Admin', 'admin@4awalk.fr', '$2a$10$f6pXv.9lYn1W.xU8F1KjOec9K3VqYtV4W6h8U/Xq7u6G2O5iC9.m.', '12 Rue de la SAE', 30, 'SPORTIF', 'LEGERE');

-- 2. Produits et Équipements
INSERT INTO food_products (id, nom, description, masse_grammes, appellation_courante, conditionnement, apport_nutritionnel_kcal, prix_euro) VALUES
    (1, 'Ration Normale', 'Ration énergétique', 150.00, 'Ration', 'Sachet', 600.00, 5.00);

INSERT INTO equipment_items (id, nom, description, masse_grammes, permet_repos) VALUES
    (1, 'Sac de Couchage', 'Sac standard', 1500.00, TRUE);

-- 3. Points d'Intérêt (Correction : ajout de hike_id à NULL car Hibernate l'a créé)
INSERT INTO points_of_interest (id, nom, description, latitude, longitude, hike_id) VALUES
                                                                                         (1, 'Rodez Centre', 'Départ de la randonnée', 44.3526, 2.5734, 1),
                                                                                         (2, 'Laguiole Village', 'Arrivée prévue', 44.6844, 2.8472, 1);

-- 4. Participants
INSERT INTO participants (id, nom_complet, age, niveau, morphologie, besoin_kcal, besoin_eau_litre, capacite_emport_max_kg) VALUES
    (1, 'Jean Marcheur', 25, 'ENTRAINE', 'MOYENNE', 2500, 3, 15.0);

-- 5. Randonnée
INSERT INTO hikes (id, libelle, depart_id, arrivee_id, duree_jours, creator_id) VALUES
    (1, 'Tour de l Aubrac', 1, 2, 2, 1);

-- 6. Tables de jointure
INSERT INTO hike_participants (hike_id, participant_id) VALUES (1, 1);
INSERT INTO hike_food_products (hike_id, food_product_id) VALUES (1, 1);
INSERT INTO hike_equipment (hike_id, equipment_id) VALUES (1, 1);