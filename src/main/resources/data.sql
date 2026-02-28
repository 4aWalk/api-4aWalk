SET FOREIGN_KEY_CHECKS = 0;

-- ==========================================
-- 1. USERS (5 Utilisateurs)
-- ==========================================
INSERT INTO users (id, nom, prenom, mail, password, adresse, age, niveau, morphologie) VALUES
                                                                                           (1, 'Martin', 'Alex', 'alex@4awalk.fr', '$2a$10$4yw3VmTFMRjKjs1utXDKIO/Zb7RpGXC/eaTT9Sw.JZqGtHtbMa5IS', '10 Rue A', 25, 'ENTRAINE', 'MOYENNE'),
                                                                                           (2, 'Stark', 'Tony', 'tony@4awalk.fr', '$2a$10$4yw3VmTFMRjKjs1utXDKIO/Zb7RpGXC/eaTT9Sw.JZqGtHtbMa5IS', '12 Ave B', 40, 'SPORTIF', 'LEGERE'),
                                                                                           (3, 'Connor', 'Sarah', 'sarah@4awalk.fr', '$2a$10$4yw3VmTFMRjKjs1utXDKIO/Zb7RpGXC/eaTT9Sw.JZqGtHtbMa5IS', '3 Chem C', 60, 'DEBUTANT', 'FORTE'),
                                                                                           (4, 'Baggins', 'Frodo', 'frodo@4awalk.fr', '$2a$10$4yw3VmTFMRjKjs1utXDKIO/Zb7RpGXC/eaTT9Sw.JZqGtHtbMa5IS', 'Comté', 32, 'ENTRAINE', 'LEGERE'),
                                                                                           (5, 'Croft', 'Lara', 'lara@4awalk.fr', '$2a$10$4yw3VmTFMRjKjs1utXDKIO/Zb7RpGXC/eaTT9Sw.JZqGtHtbMa5IS', 'Manoir', 28, 'SPORTIF', 'MOYENNE');

-- ==========================================
-- 2. FOOD PRODUCTS (50 items)
-- ==========================================
INSERT INTO food_products (id, nom, description, masse_grammes, appellation_courante, conditionnement, apport_nutritionnel_kcal, prix_euro, nb_item) VALUES
-- SOLO (nb_item = 1)
(1, 'Pâtes Bolo 1P', 'Lyo 1 part', 150.0, 'Pates', 'Sachet', 600.0, 5.0, 1),
(2, 'Poulet Curry 1P', 'Lyo 1 part', 150.0, 'Poulet', 'Sachet', 600.0, 5.0, 1),
(3, 'Riz Dinde 1P', 'Lyo 1 part', 150.0, 'Riz', 'Sachet', 600.0, 5.0, 1),
(4, 'Soupe Boeuf 1P', 'Lyo 1 part', 150.0, 'Soupe', 'Sachet', 600.0, 5.0, 1),
(5, 'Muesli Matin 1P', 'Lyo 1 part', 150.0, 'Muesli', 'Sachet', 600.0, 5.0, 1),
(6, 'Porridge 1P', 'Lyo 1 part', 150.0, 'Porridge', 'Sachet', 600.0, 5.0, 1),
(7, 'Lentilles 1P', 'Lyo 1 part', 150.0, 'Lentilles', 'Sachet', 600.0, 5.0, 1),
(8, 'Chili 1P', 'Lyo 1 part', 150.0, 'Chili', 'Sachet', 600.0, 5.0, 1),
(9, 'Hachis 1P', 'Lyo 1 part', 150.0, 'Hachis', 'Sachet', 600.0, 5.0, 1),
(10, 'Couscous 1P', 'Lyo 1 part', 150.0, 'Couscous', 'Sachet', 600.0, 5.0, 1),
(11, 'Barre Amande', 'Snack 1P', 80.0, 'Barre', 'Unité', 400.0, 3.0, 1),
(12, 'Noix Mix', 'Snack 1P', 80.0, 'Noix', 'Sachet', 400.0, 3.0, 1),
(13, 'Jerky Boeuf', 'Snack 1P', 80.0, 'Jerky', 'Sachet', 400.0, 3.0, 1),
(14, 'Chocolat Noir', 'Snack 1P', 80.0, 'Chocolat', 'Unité', 400.0, 3.0, 1),
(15, 'Compote Pomme', 'Snack 1P', 80.0, 'Compote', 'Gourde', 400.0, 3.0, 1),
(16, 'Biscuits', 'Snack 1P', 80.0, 'Biscuits', 'Sachet', 400.0, 3.0, 1),
(17, 'Saucisson', 'Snack 1P', 80.0, 'Saucisson', 'Pièce', 400.0, 3.0, 1),
(18, 'Fromage', 'Snack 1P', 80.0, 'Fromage', 'Portion', 400.0, 3.0, 1),
(19, 'Fruits Secs', 'Snack 1P', 80.0, 'Fruits Secs', 'Sachet', 400.0, 3.0, 1),
(20, 'Gel Effort', 'Snack 1P', 80.0, 'Gel', 'Tube', 400.0, 3.0, 1),

-- DUO (nb_item = 2)
(21, 'Pâtes Bolo 2P', 'Lyo 2 parts', 300.0, 'Pates', 'Sachet', 600.0, 9.0, 2),
(22, 'Poulet Curry 2P', 'Lyo 2 parts', 300.0, 'Poulet', 'Sachet', 600.0, 9.0, 2),
(23, 'Riz Dinde 2P', 'Lyo 2 parts', 300.0, 'Riz', 'Sachet', 600.0, 9.0, 2),
(24, 'Soupe Boeuf 2P', 'Lyo 2 parts', 300.0, 'Soupe', 'Sachet', 600.0, 9.0, 2),
(25, 'Muesli Matin 2P', 'Lyo 2 parts', 300.0, 'Muesli', 'Sachet', 600.0, 9.0, 2),
(26, 'Porridge 2P', 'Lyo 2 parts', 300.0, 'Porridge', 'Sachet', 600.0, 9.0, 2),
(27, 'Lentilles 2P', 'Lyo 2 parts', 300.0, 'Lentilles', 'Sachet', 600.0, 9.0, 2),
(28, 'Chili 2P', 'Lyo 2 parts', 300.0, 'Chili', 'Sachet', 600.0, 9.0, 2),
(29, 'Hachis 2P', 'Lyo 2 parts', 300.0, 'Hachis', 'Sachet', 600.0, 9.0, 2),
(30, 'Couscous 2P', 'Lyo 2 parts', 300.0, 'Couscous', 'Sachet', 600.0, 9.0, 2),
(31, 'Barre XL 2P', 'Snack 2P', 160.0, 'Barre', 'Unité', 400.0, 5.0, 2),
(32, 'Noix XL 2P', 'Snack 2P', 160.0, 'Noix', 'Sachet', 400.0, 5.0, 2),
(33, 'Jerky XL 2P', 'Snack 2P', 160.0, 'Jerky', 'Sachet', 400.0, 5.0, 2),
(34, 'Choco XL 2P', 'Snack 2P', 160.0, 'Chocolat', 'Unité', 400.0, 5.0, 2),
(35, 'Compote XL 2P', 'Snack 2P', 160.0, 'Compote', 'Gourde', 400.0, 5.0, 2),

-- TRIO (nb_item = 3)
(36, 'Pâtes Bolo 3P', 'Lyo 3 parts', 450.0, 'Pates', 'Sachet', 600.0, 13.0, 3),
(37, 'Poulet Curry 3P', 'Lyo 3 parts', 450.0, 'Poulet', 'Sachet', 600.0, 13.0, 3),
(38, 'Riz Dinde 3P', 'Lyo 3 parts', 450.0, 'Riz', 'Sachet', 600.0, 13.0, 3),
(39, 'Soupe Boeuf 3P', 'Lyo 3 parts', 450.0, 'Soupe', 'Sachet', 600.0, 13.0, 3),
(40, 'Muesli Matin 3P', 'Lyo 3 parts', 450.0, 'Muesli', 'Sachet', 600.0, 13.0, 3),
(41, 'Porridge 3P', 'Lyo 3 parts', 450.0, 'Porridge', 'Sachet', 600.0, 13.0, 3),
(42, 'Lentilles 3P', 'Lyo 3 parts', 450.0, 'Lentilles', 'Sachet', 600.0, 13.0, 3),
(43, 'Chili 3P', 'Lyo 3 parts', 450.0, 'Chili', 'Sachet', 600.0, 13.0, 3),
(44, 'Hachis 3P', 'Lyo 3 parts', 450.0, 'Hachis', 'Sachet', 600.0, 13.0, 3),
(45, 'Couscous 3P', 'Lyo 3 parts', 450.0, 'Couscous', 'Sachet', 600.0, 13.0, 3),
(46, 'Barre XXL 3P', 'Snack 3P', 240.0, 'Barre', 'Unité', 400.0, 7.0, 3),
(47, 'Noix XXL 3P', 'Snack 3P', 240.0, 'Noix', 'Sachet', 400.0, 7.0, 3),
(48, 'Jerky XXL 3P', 'Snack 3P', 240.0, 'Jerky', 'Sachet', 400.0, 7.0, 3),
(49, 'Choco XXL 3P', 'Snack 3P', 240.0, 'Chocolat', 'Unité', 400.0, 7.0, 3),
(50, 'Compote XXL 3P', 'Snack 3P', 240.0, 'Compote', 'Gourde', 400.0, 7.0, 3);

-- ==========================================
-- 3. EQUIPMENT ITEMS (60 items)
-- ==========================================
INSERT INTO equipment_items (id, nom, description, masse_grammes, nb_item, type, masse_a_vide) VALUES
-- REPOS (1-10)
(1, 'Tente Solo', '1 Place', 1500.0, 1, 'REPOS', 0.0),
(2, 'Tente Duo', '2 Places', 2500.0, 2, 'REPOS', 0.0),
(3, 'Tente Trio', '3 Places', 3500.0, 3, 'REPOS', 0.0),
(4, 'Matelas Solo', '1 Place', 500.0, 1, 'REPOS', 0.0),
(5, 'Matelas Duo', '2 Places', 900.0, 2, 'REPOS', 0.0),
(6, 'Matelas Trio', '3 Places', 1400.0, 3, 'REPOS', 0.0),
(7, 'Sac Solo', '1 Place', 1000.0, 1, 'REPOS', 0.0),
(8, 'Sac Duo', '2 Places', 1800.0, 2, 'REPOS', 0.0),
(9, 'Sac Trio', '3 Places', 2600.0, 3, 'REPOS', 0.0),
(10, 'Drap Solo', '1 Place', 200.0, 1, 'REPOS', 0.0),

-- SOIN (11-20)
(11, 'Trousse Solo', '1P', 200.0, 1, 'SOIN', 0.0),
(12, 'Trousse Duo', '2P', 350.0, 2, 'SOIN', 0.0),
(13, 'Trousse Trio', '3P', 500.0, 3, 'SOIN', 0.0),
(14, 'Survie Solo', '1P', 100.0, 1, 'SOIN', 0.0),
(15, 'Survie Duo', '2P', 180.0, 2, 'SOIN', 0.0),
(16, 'Survie Trio', '3P', 250.0, 3, 'SOIN', 0.0),
(17, 'Solaire Solo', '1P', 100.0, 1, 'SOIN', 0.0),
(18, 'Solaire Duo', '2P', 150.0, 2, 'SOIN', 0.0),
(19, 'Solaire Trio', '3P', 200.0, 3, 'SOIN', 0.0),
(20, 'Purif Eau', 'Toute taille', 50.0, 3, 'SOIN', 0.0),

-- EAU (21-30)
(21, 'Outre Solo 5L', '1P', 5500.0, 1, 'EAU', 500.0),
(22, 'Gourde Solo 2L', '1P', 2500.0, 1, 'EAU', 500.0),
(23, 'Réserve Duo 5L', '2P', 3000.0, 2, 'EAU', 500.0),
(24, 'Gourde Duo 2L', '2P', 1500.0, 2, 'EAU', 500.0),
(25, 'Jerrican Trio 6L', '3P', 2500.0, 3, 'EAU', 500.0),
(26, 'Gourde Trio 3L', '3P', 1500.0, 3, 'EAU', 500.0),
(27, 'Camelbak Solo 3L', '1P', 3500.0, 1, 'EAU', 500.0),
(28, 'Camelbak Duo 4L', '2P', 2500.0, 2, 'EAU', 500.0),
(29, 'Camelbak Trio 9L', '3P', 3500.0, 3, 'EAU', 500.0),
(30, 'Filtre Solo 1L', '1P', 1500.0, 1, 'EAU', 500.0),

-- PROGRESSION (31-40)
(31, 'Bâtons Solo', '1 Paire', 400.0, 1, 'PROGRESSION', 0.0),
(32, 'Bâtons Duo', '2 Paires', 800.0, 2, 'PROGRESSION', 0.0),
(33, 'Bâtons Trio', '3 Paires', 1200.0, 3, 'PROGRESSION', 0.0),
(34, 'Lampe Solo', '1 Lampe', 150.0, 1, 'PROGRESSION', 0.0),
(35, 'Lampe Duo', '2 Lampes', 300.0, 2, 'PROGRESSION', 0.0),
(36, 'Lampe Trio', '3 Lampes', 450.0, 3, 'PROGRESSION', 0.0),
(37, 'Carte Solo', '1 Carte', 100.0, 1, 'PROGRESSION', 0.0),
(38, 'GPS Duo', '1 GPS', 200.0, 2, 'PROGRESSION', 0.0),
(39, 'Boussole Trio', '1 Boussole', 80.0, 3, 'PROGRESSION', 0.0),
(40, 'Corde Trio', 'Secours', 1500.0, 3, 'PROGRESSION', 0.0),

-- AUTRE (41-50)
(41, 'Réchaud Solo', '1P', 300.0, 1, 'AUTRE', 0.0),
(42, 'Réchaud Duo', '2P', 450.0, 2, 'AUTRE', 0.0),
(43, 'Réchaud Trio', '3P', 600.0, 3, 'AUTRE', 0.0),
(44, 'Popote Solo', '1P', 250.0, 1, 'AUTRE', 0.0),
(45, 'Popote Duo', '2P', 400.0, 2, 'AUTRE', 0.0),
(46, 'Popote Trio', '3P', 550.0, 3, 'AUTRE', 0.0),
(47, 'Couteau Solo', '1P', 120.0, 1, 'AUTRE', 0.0),
(48, 'Serviette Duo', '2P', 300.0, 2, 'AUTRE', 0.0),
(49, 'Batterie Trio', '3P', 400.0, 3, 'AUTRE', 0.0),
(50, 'Gaz Trio', '3P', 500.0, 3, 'AUTRE', 0.0),

-- VÊTEMENT (51-60)
(51, 'Veste Imperméable', 'Gore-Tex Légère', 400.0, 1, 'VETEMENT', 0.0),
(52, 'Polaire Chaude', 'Isolation Thermique', 350.0, 1, 'VETEMENT', 0.0),
(53, 'T-shirt Respirant', 'Séchage rapide', 150.0, 1, 'VETEMENT', 0.0),
(54, 'Pantalon Trek', 'Pantalon Modulable', 450.0, 1, 'VETEMENT', 0.0),
(55, 'Chaussettes Rando', 'Anti-ampoules', 80.0, 1, 'VETEMENT', 0.0),
(56, 'Gants Froid', 'Paire Hiver', 100.0, 1, 'VETEMENT', 0.0),
(57, 'Bonnet', 'Laine Mérinos', 80.0, 1, 'VETEMENT', 0.0),
(58, 'Sous-vêtement Thermo', 'Couche de base', 200.0, 1, 'VETEMENT', 0.0),
(59, 'Casquette Soleil', 'Anti-UV', 70.0, 1, 'VETEMENT', 0.0),
(60, 'Poncho Pluie', 'Couvre-sac inclus', 300.0, 1, 'VETEMENT', 0.0);

-- ==========================================
-- 4. POI (20 Points)
-- ==========================================
INSERT INTO points_of_interest (id, nom, description, latitude, longitude, hike_id, sequence) VALUES
                                                                                                  (1, 'D A', 'P', 44.1, 2.1, NULL, 0), (2, 'A A', 'R', 44.2, 2.2, NULL, 1),
                                                                                                  (3, 'D B', 'G', 45.1, 3.1, NULL, 0), (4, 'A B', 'L', 45.2, 3.2, NULL, 1),
                                                                                                  (5, 'D C', 'V', 46.1, 4.1, NULL, 0), (6, 'A C', 'S', 46.2, 4.2, NULL, 1),
                                                                                                  (7, 'D D', 'F', 47.1, 5.1, NULL, 0), (8, 'A D', 'V', 47.2, 5.2, NULL, 1),
                                                                                                  (9, 'D E', 'C', 48.1, 6.1, NULL, 0), (10, 'A E', 'C', 48.2, 6.2, NULL, 1),
                                                                                                  (11, 'D F', 'P', 43.1, 1.1, NULL, 0), (12, 'A F', 'P', 43.2, 1.2, NULL, 1),
                                                                                                  (13, 'D G', 'P', 44.5, 2.5, NULL, 0), (14, 'A G', 'G', 44.6, 2.6, NULL, 1),
                                                                                                  (15, 'D H', 'C', 45.5, 3.5, NULL, 0), (16, 'A H', 'C', 45.6, 3.6, NULL, 1),
                                                                                                  (17, 'D I', 'M', 46.5, 4.5, NULL, 0), (18, 'A I', 'C', 46.6, 4.6, NULL, 1),
                                                                                                  (19, 'D J', 'S', 47.5, 5.5, NULL, 0), (20, 'A J', 'B', 47.6, 5.6, NULL, 1);

-- ==========================================
-- 5. HIKES (10 Randonnées)
-- ==========================================
INSERT INTO hikes (id, libelle, depart_id, arrivee_id, duree_jours, creator_id) VALUES
                                                                                    (1, 'Rando Solo 1 J', 1, 2, 1, 1),
                                                                                    (2, 'Week-end Duo', 3, 4, 2, 1),
                                                                                    (3, 'Balade Trio', 5, 6, 2, 2),
                                                                                    (4, 'Trek Duo 3 J', 7, 8, 3, 2),
                                                                                    (5, 'Boucle Solo', 9, 10, 1, 3),
                                                                                    (6, 'Littoral Trio', 11, 12, 2, 3),
                                                                                    (7, 'Marche Solo', 13, 14, 3, 4),
                                                                                    (8, 'Grotte Duo', 15, 16, 2, 4),
                                                                                    (9, 'Château Solo', 17, 18, 1, 5),
                                                                                    (10, 'Survie Trio', 19, 20, 3, 5);

-- ==========================================
-- 6. PARTICIPANTS (20 Participants)
-- ==========================================
INSERT INTO participants (id, prenom, nom, age, niveau, morphologie, creator, creator_id, besoin_kcal, besoin_eau_litre, capacite_emport_max_kg) VALUES
                                                                                                                                                     (1, 'Alex', 'M', 25, 'ENTRAINE', 'MOYENNE', TRUE, 1, 3000, 5, 0.0),
                                                                                                                                                     (2, 'Alex', 'M', 25, 'ENTRAINE', 'MOYENNE', TRUE, 1, 3000, 5, 0.0),
                                                                                                                                                     (3, 'Jean', 'D', 30, 'DEBUTANT', 'FORTE', FALSE, 1, 2000, 3, 12.0),
                                                                                                                                                     (4, 'Tony', 'S', 40, 'SPORTIF', 'LEGERE', TRUE, 2, 3000, 5, 0.0),
                                                                                                                                                     (5, 'Paul', 'D', 35, 'ENTRAINE', 'MOYENNE', FALSE, 2, 2000, 3, 15.0),
                                                                                                                                                     (6, 'Luc', 'B', 28, 'SPORTIF', 'LEGERE', FALSE, 2, 2200, 3, 16.0),
                                                                                                                                                     (7, 'Tony', 'S', 40, 'SPORTIF', 'LEGERE', TRUE, 2, 3000, 5, 0.0),
                                                                                                                                                     (8, 'Marc', 'N', 45, 'ENTRAINE', 'FORTE', FALSE, 2, 2400, 4, 14.0),
                                                                                                                                                     (9, 'Sarah', 'C', 60, 'DEBUTANT', 'FORTE', TRUE, 3, 3000, 5, 0.0),
                                                                                                                                                     (10, 'Sarah', 'C', 60, 'DEBUTANT', 'FORTE', TRUE, 3, 3000, 5, 0.0),
                                                                                                                                                     (11, 'Emma', 'G', 55, 'DEBUTANT', 'MOYENNE', FALSE, 3, 1800, 2, 10.0),
                                                                                                                                                     (12, 'Lea', 'R', 50, 'ENTRAINE', 'LEGERE', FALSE, 3, 2000, 3, 12.0),
                                                                                                                                                     (13, 'Frodo', 'B', 32, 'ENTRAINE', 'LEGERE', TRUE, 4, 3000, 5, 0.0),
                                                                                                                                                     (14, 'Frodo', 'B', 32, 'ENTRAINE', 'LEGERE', TRUE, 4, 3000, 5, 0.0),
                                                                                                                                                     (15, 'Sam', 'G', 33, 'SPORTIF', 'FORTE', FALSE, 4, 2500, 4, 20.0),
                                                                                                                                                     (16, 'Lara', 'C', 28, 'SPORTIF', 'MOYENNE', TRUE, 5, 3000, 5, 0.0),
                                                                                                                                                     (17, 'Lara', 'C', 28, 'SPORTIF', 'MOYENNE', TRUE, 5, 3000, 5, 0.0),
                                                                                                                                                     (18, 'Jon', 'S', 30, 'SPORTIF', 'MOYENNE', FALSE, 5, 2600, 4, 18.0),
                                                                                                                                                     (19, 'Arya', 'S', 20, 'ENTRAINE', 'LEGERE', FALSE, 5, 2000, 3, 14.0);

-- ==========================================
-- 7. LIENS HIKES <-> PARTICIPANTS
-- ==========================================
INSERT INTO hike_participants (hike_id, participant_id) VALUES
                                                            (1, 1), (2, 2), (2, 3), (3, 4), (3, 5), (3, 6), (4, 7), (4, 8), (5, 9),
                                                            (6, 10), (6, 11), (6, 12), (7, 13), (8, 14), (8, 15), (9, 16), (10, 17), (10, 18), (10, 19);

-- ==========================================
-- 8. LIENS HIKES <-> FOOD PRODUCTS
-- ==========================================
INSERT INTO hike_food_products (hike_id, food_product_id) VALUES
                                                              (1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
                                                              (2, 21), (2, 22), (2, 23), (2, 24), (2, 11),
                                                              (3, 36), (3, 37), (3, 38), (3, 39),
                                                              (4, 25), (4, 26), (4, 27), (4, 28), (4, 31),
                                                              (5, 6), (5, 7), (5, 8), (5, 9), (5, 10),
                                                              (6, 40), (6, 41), (6, 42), (6, 46), (6, 12),
                                                              (7, 1), (7, 2), (7, 3), (7, 4), (7, 5),
                                                              (8, 21), (8, 22), (8, 23), (8, 24), (8, 32),
                                                              (9, 6), (9, 7), (9, 8), (9, 9), (9, 10),
                                                              (10, 43), (10, 44), (10, 45), (10, 46);

-- ==========================================
-- 9. GROUP EQUIPMENTS & ITEMS
-- ==========================================
INSERT INTO group_equipments (id, type, hike_id) VALUES
                                                     (1, 'SOIN', 1), (2, 'EAU', 1), (3, 'PROGRESSION', 1), (36, 'VETEMENT', 1), -- Ajout vêtement Hike 1
                                                     (4, 'SOIN', 2), (5, 'EAU', 2), (6, 'PROGRESSION', 2), (7, 'REPOS', 2), (37, 'VETEMENT', 2), -- Ajout vêtement Hike 2
                                                     (8, 'SOIN', 3), (9, 'EAU', 3), (10, 'PROGRESSION', 3), (38, 'VETEMENT', 3), -- Ajout vêtement Hike 3
                                                     (11, 'SOIN', 4), (12, 'EAU', 4), (13, 'PROGRESSION', 4), (14, 'REPOS', 4), (39, 'VETEMENT', 4), -- Ajout vêtement Hike 4
                                                     (15, 'SOIN', 5), (16, 'EAU', 5), (17, 'PROGRESSION', 5),
                                                     (18, 'SOIN', 6), (19, 'EAU', 6), (20, 'PROGRESSION', 6), (21, 'REPOS', 6),
                                                     (22, 'SOIN', 7), (23, 'EAU', 7), (24, 'PROGRESSION', 7),
                                                     (25, 'SOIN', 8), (26, 'EAU', 8), (27, 'PROGRESSION', 8), (28, 'REPOS', 8),
                                                     (29, 'SOIN', 9), (30, 'EAU', 9), (31, 'PROGRESSION', 9),
                                                     (32, 'SOIN', 10), (33, 'EAU', 10), (34, 'PROGRESSION', 10), (35, 'REPOS', 10);

INSERT INTO group_equipment_items (group_id, equipment_id, item_order) VALUES
                                                                           (1, 11, 0), (2, 21, 0), (3, 31, 0),
                                                                           (36, 51, 0), -- Hike 1 : Veste
                                                                           (4, 12, 0), (5, 23, 0), (5, 24, 1), (5, 30, 2), (6, 32, 0), (7, 2, 0),
                                                                           (37, 51, 0), (37, 52, 1), -- Hike 2 : Veste + Polaire
                                                                           (8, 13, 0), (9, 25, 0), (9, 26, 1), (9, 24, 2), (10, 33, 0),
                                                                           (38, 53, 0), (38, 54, 1), (38, 55, 2), -- Hike 3 : T-shirt + Pantalon + Chaussettes
                                                                           (11, 12, 0), (12, 23, 0), (12, 28, 1), (13, 32, 0), (14, 2, 0),
                                                                           (39, 51, 0), (39, 54, 1), -- Hike 4 : Veste + Pantalon
                                                                           (15, 11, 0), (16, 21, 0), (17, 31, 0),
                                                                           (18, 13, 0), (19, 25, 0), (19, 26, 1), (19, 22, 2), (20, 33, 0), (21, 3, 0),
                                                                           (22, 11, 0), (23, 21, 0), (24, 31, 0),
                                                                           (25, 12, 0), (26, 23, 0), (26, 28, 1), (27, 32, 0), (28, 2, 0),
                                                                           (29, 11, 0), (30, 21, 0), (31, 31, 0),
                                                                           (32, 13, 0), (33, 25, 0), (33, 25, 1), (34, 33, 0), (35, 3, 0);

-- ==========================================
-- 10. BELONG EQUIPMENT
-- ==========================================
INSERT INTO belong_equipment (hike_id, participant_id, equipment_id) VALUES
-- Randonnée 1 (Solo 1J, Participant: 1)
(1, 1, 51),

-- Randonnée 2 (Duo 2J, Participants: 2, 3)
(2, 2, 2),
(2, 2, 51),
(2, 3, 52),

-- Randonnée 4 (Duo 3J, Participants: 7, 8)
(4, 7, 2),
(4, 7, 51),
(4, 8, 54);

SET FOREIGN_KEY_CHECKS = 1;