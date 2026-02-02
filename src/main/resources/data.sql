SET FOREIGN_KEY_CHECKS = 0; -- INDISPENSABLE pour les dépendances circulaires

INSERT INTO users (id, nom, prenom, mail, password, adresse, age, niveau, morphologie) VALUES
                                                                                           (1, 'Brouz', 'Admin', 'admin@4awalk.fr', '$2a$10$wTf7JzG3i8xG4oWw0oTf7eQ7xG4oWw0oTf7e', '12 Rue de la SAE', 30, 'SPORTIF', 'LEGERE'),
                                                                                           (2, 'Tony', 'Admin', 'test@4awalk.fr', 'Motpassefort!99', '12 Rue de la SAE', 30, 'SPORTIF', 'LEGERE');

INSERT INTO food_products (id, nom, description, masse_grammes, appellation_courante, conditionnement, apport_nutritionnel_kcal, prix_euro, nb_item) VALUES
                                                                                                                                                         (1, 'Ration Normale', 'Ration énergétique', 150.00, 'Ration', 'Sachet', 600.00, 5.00, 3),
                                                                                                                                                         (2, 'Lentille', 'lentille lyophilisé', 100.00, 'lentille lyophilisé', 'Sachet', 300.00, 2.00, 1);

INSERT INTO equipment_items (id, nom, description, masse_grammes, nb_item, type, masse_a_vide) VALUES
                                                                                                   (1, 'Sac de Couchage', 'Sac standard', 1500.00, 3, 'REPOS', 0.0),
                                                                                                   (2, 'Trousse de secours pour 2', 'trousse soin pour 2 personnes', 1000.00, 2, 'SOIN', 0.0),
                                                                                                   (3, 'Trousse de secours pour 3', 'trousse soin pour 3 personnes', 1200.00, 3, 'SOIN', 0.0);

-- Attention: Sequence ajoutée à 0 par défaut
INSERT INTO points_of_interest (id, nom, description, latitude, longitude, hike_id, sequence) VALUES
                                                                                                  (1, 'Rodez Centre', 'Départ de la randonnée', 44.3526, 2.5734, NULL, 0),
                                                                                                  (2, 'Laguiole Village', 'Arrivée prévue', 44.6844, 2.8472, NULL, 1),
                                                                                                  (3, 'Un point d interêt', 'un nouveau poi', 45.6844, 3.8472, 2, 0);

INSERT INTO participants (id, age, niveau, morphologie, creator, besoin_kcal, besoin_eau_litre, capacite_emport_max_kg) VALUES
                                                                                                                            (1, 25, 'ENTRAINE', 'MOYENNE', TRUE, 2500, 3, 15.0),
                                                                                                                            (2, 25, 'ENTRAINE', 'MOYENNE', False, 2600, 4, 18.0);

INSERT INTO hikes (id, libelle, depart_id, arrivee_id, duree_jours, creator_id) VALUES
                                                                                    (1, 'Tour de l Aubrac', 1, 2, 2, 1),
                                                                                    (2, 'La randonné de Tony', 1, 2, 2, 2);

INSERT INTO hike_participants (hike_id, participant_id) VALUES
                                                            (1, 1), (1, 2);

INSERT INTO hike_food_products (hike_id, food_product_id) VALUES
                                                              (1, 1), (2,2);

INSERT INTO group_equipments (id, type_nom, hike_id) VALUES
                                                         (1, 'SOIN', 1), (2,'SOIN',2);

INSERT INTO group_equipment_items (group_id, equipment_id, item_order) VALUES
                                                                           (1, 2, 0), (1, 3, 1), (2, 2, 0), (2, 3, 1);

SET FOREIGN_KEY_CHECKS = 1;