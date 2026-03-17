SET FOREIGN_KEY_CHECKS = 0;

-- ==========================================
-- 1. USERS
-- ==========================================
INSERT INTO users (id, nom, prenom, mail, password, adresse, age, niveau, morphologie) VALUES
                                                                                           (1, 'Martin', 'Alex', 'alex@4awalk.fr', '$2a$10$4yw3VmTFMRjKjs1utXDKIO/Zb7RpGXC/eaTT9Sw.JZqGtHtbMa5IS', '10 Rue A', 25, 'ENTRAINE', 'MOYENNE'),
                                                                                           (2, 'Stark', 'Tony', 'tony@4awalk.fr', '$2a$10$4yw3VmTFMRjKjs1utXDKIO/Zb7RpGXC/eaTT9Sw.JZqGtHtbMa5IS', '12 Ave B', 40, 'SPORTIF', 'LEGERE'),
                                                                                           (3, 'Connor', 'Sarah', 'sarah@4awalk.fr', '$2a$10$4yw3VmTFMRjKjs1utXDKIO/Zb7RpGXC/eaTT9Sw.JZqGtHtbMa5IS', '3 Chem C', 60, 'DEBUTANT', 'FORTE'),
                                                                                           (4, 'Baggins', 'Frodo', 'frodo@4awalk.fr', '$2a$10$4yw3VmTFMRjKjs1utXDKIO/Zb7RpGXC/eaTT9Sw.JZqGtHtbMa5IS', 'Comté', 32, 'ENTRAINE', 'LEGERE'),
                                                                                           (5, 'Croft', 'Lara', 'lara@4awalk.fr', '$2a$10$4yw3VmTFMRjKjs1utXDKIO/Zb7RpGXC/eaTT9Sw.JZqGtHtbMa5IS', 'Manoir', 28, 'SPORTIF', 'MOYENNE');

-- ==========================================
-- 2. FOOD PRODUCTS
-- ==========================================
INSERT INTO food_products (nom, description, masse_grammes, appellation_courante, conditionnement, apport_nutritionnel_kcal, prix_euro, nb_item) VALUES

-- =====================
-- PLATS LYOPHILISÉS
-- =====================
(1, 'Riz au lait vanille', 'Dessert lyophilisé', 120.0, 'Riz au lait', 'Sachet', 480.0, 4.50, 1),
(2, 'Purée de pommes de terre', 'Flocons instantanés', 100.0, 'Purée', 'Sachet', 370.0, 2.80, 1),
(3, 'Purée de pommes de terre', 'Flocons instantanés - format duo', 190.0, 'Purée', 'Sachet', 700.0, 5.20, 2),
(4, 'Bolognaise aux lentilles', 'Repas lyophilisé végétarien', 140.0, 'Bolognaise', 'Sachet', 520.0, 5.50, 1),
(5, 'Tajine poulet pois chiches', 'Lyophilisé', 155.0, 'Tajine', 'Sachet', 580.0, 6.20, 1),
(6, 'Tajine poulet pois chiches', 'Lyophilisé - 2 parts', 300.0, 'Tajine', 'Sachet', 1140.0, 11.50, 2),
(7, 'Dahl de lentilles corail', 'Plat végétalien lyophilisé', 130.0, 'Dahl', 'Sachet', 490.0, 5.80, 1),
(8, 'Dahl de lentilles corail', 'Plat végétalien lyophilisé - duo', 250.0, 'Dahl', 'Sachet', 940.0, 10.80, 2),
(9, 'Bouillabaisse de la mer', 'Soupe lyophilisée', 90.0, 'Soupe', 'Sachet', 280.0, 5.90, 1),
(10, 'Ratatouille à la provençale', 'Légumes lyophilisés', 110.0, 'Ratatouille', 'Sachet', 210.0, 4.80, 1),
(11, 'Cassoulet traditionnel', 'Lyophilisé', 160.0, 'Cassoulet', 'Sachet', 620.0, 6.50, 1),
(12, 'Cassoulet traditionnel', 'Lyophilisé - duo', 310.0, 'Cassoulet', 'Sachet', 1200.0, 12.00, 2),
(13, 'Cassoulet traditionnel', 'Lyophilisé - trio', 460.0, 'Cassoulet', 'Sachet', 1780.0, 17.50, 3),
(14, 'Saumon aux légumes', 'Lyophilisé', 145.0, 'Saumon', 'Sachet', 530.0, 7.20, 1),
(15, 'Saumon aux légumes', 'Lyophilisé - 2 parts', 280.0, 'Saumon', 'Sachet', 1020.0, 13.50, 2),
(16, 'Bœuf bourguignon', 'Lyophilisé', 150.0, 'Bœuf', 'Sachet', 570.0, 6.80, 1),
(17, 'Bœuf bourguignon', 'Lyophilisé - 3 parts', 440.0, 'Bœuf', 'Sachet', 1680.0, 19.50, 3),
(18, 'Poulet rôti purée', 'Lyophilisé', 155.0, 'Poulet', 'Sachet', 590.0, 6.00, 1),
(19, 'Soupe minestrone', 'Lyophilisé', 85.0, 'Soupe', 'Sachet', 260.0, 3.90, 1),
(20, 'Soupe minestrone', 'Lyophilisé - duo', 165.0, 'Soupe', 'Sachet', 500.0, 7.20, 2),
(21, 'Riz thaï crevettes', 'Lyophilisé', 150.0, 'Riz', 'Sachet', 550.0, 6.50, 1),
(22, 'Riz thaï crevettes', 'Lyophilisé - 2 parts', 290.0, 'Riz', 'Sachet', 1060.0, 12.20, 2),
(23, 'Quinoa légumes rôtis', 'Lyophilisé végétarien', 120.0, 'Quinoa', 'Sachet', 430.0, 5.70, 1),
(24, 'Quinoa légumes rôtis', 'Lyophilisé - duo', 230.0, 'Quinoa', 'Sachet', 820.0, 10.50, 2),
(25, 'Pâtes à la carbonara', 'Lyophilisé', 145.0, 'Pates', 'Sachet', 610.0, 5.90, 1),
(26, 'Pâtes à la carbonara', 'Lyophilisé - 3 parts', 420.0, 'Pates', 'Sachet', 1770.0, 16.90, 3),
(27, 'Gratin dauphinois', 'Lyophilisé', 130.0, 'Gratin', 'Sachet', 500.0, 5.40, 1),
(28, 'Curry végétarien riz basmati', 'Lyophilisé', 155.0, 'Curry', 'Sachet', 560.0, 5.80, 1),
(29, 'Curry végétarien riz basmati', 'Lyophilisé - 3 parts', 450.0, 'Curry', 'Sachet', 1630.0, 16.50, 3),
(30, 'Soupe poireaux pommes de terre', 'Lyophilisé', 80.0, 'Soupe', 'Sachet', 220.0, 3.60, 1),
(31, 'Soupe poireaux pommes de terre', 'Lyophilisé - 3 parts', 230.0, 'Soupe', 'Sachet', 640.0, 9.90, 3),
(32, 'Paella aux fruits de mer', 'Lyophilisé', 160.0, 'Paella', 'Sachet', 580.0, 7.50, 1),
(33, 'Paella aux fruits de mer', 'Lyophilisé - 2 parts', 310.0, 'Paella', 'Sachet', 1120.0, 14.00, 2),
(34, 'Galettes de sarrasin garnies', 'Lyophilisé breton', 125.0, 'Galette', 'Sachet', 420.0, 5.20, 1),
(35, 'Haricots rouges au chorizo', 'Lyophilisé', 150.0, 'Haricots', 'Sachet', 570.0, 5.50, 1),
(36, 'Haricots rouges au chorizo', 'Lyophilisé - duo', 290.0, 'Haricots', 'Sachet', 1100.0, 10.50, 2),
(37, 'Pot-au-feu légumes bœuf', 'Lyophilisé', 145.0, 'Pot-au-feu', 'Sachet', 490.0, 6.20, 1),
(38, 'Pot-au-feu légumes bœuf', 'Lyophilisé - 3 parts', 430.0, 'Pot-au-feu', 'Sachet', 1450.0, 17.90, 3),

-- =====================
-- PETIT-DÉJEUNER
-- =====================
(39, 'Granola myrtilles', 'Céréales croustillantes', 100.0, 'Granola', 'Sachet', 420.0, 3.20, 1),
(40, 'Granola myrtilles', 'Céréales croustillantes - duo', 190.0, 'Granola', 'Sachet', 800.0, 5.90, 2),
(41, 'Granola myrtilles', 'Céréales croustillantes - trio', 280.0, 'Granola', 'Sachet', 1180.0, 8.50, 3),
(42, 'Flocons d''avoine instantanés', 'Cuisson rapide', 80.0, 'Flocons', 'Sachet', 300.0, 1.90, 1),
(43, 'Flocons d''avoine instantanés', 'Cuisson rapide - duo', 155.0, 'Flocons', 'Sachet', 580.0, 3.50, 2),
(44, 'Muesli suisse aux fruits', 'Sans cuisson', 100.0, 'Muesli', 'Sachet', 390.0, 3.00, 1),
(45, 'Gruau instantané miel', 'Porridge sucré', 90.0, 'Porridge', 'Sachet', 340.0, 2.50, 1),
(46, 'Semoule sucrée vanille', 'Petit-déjeuner chaud', 80.0, 'Semoule', 'Sachet', 310.0, 2.20, 1),
(47, 'Crêpes déshydratées mix', 'Préparation complète', 150.0, 'Crêpe', 'Sachet', 580.0, 3.80, 2),
(48, 'Pain de mie grillé individuel', 'Tartines emballées', 60.0, 'Pain', 'Sachet', 240.0, 1.50, 1),
(49, 'Bouillie de millet cacao', 'Lyophilisé sans gluten', 90.0, 'Bouillie', 'Sachet', 350.0, 3.40, 1),

-- =====================
-- SNACKS SUCRÉS
-- =====================
(50, 'Barre céréales miel amandes', 'Energy bar', 55.0, 'Barre', 'Unité', 220.0, 2.20, 1),
(51, 'Barre céréales miel amandes', 'Energy bar - pack duo', 110.0, 'Barre', 'Sachet', 440.0, 4.00, 2),
(52, 'Barre céréales miel amandes', 'Energy bar - pack trio', 165.0, 'Barre', 'Sachet', 660.0, 5.50, 3),
(53, 'Barre protéinée chocolat', 'High protein 20g', 68.0, 'Barre', 'Unité', 270.0, 2.80, 1),
(54, 'Barre protéinée chocolat', 'Pack 2 barres', 136.0, 'Barre', 'Sachet', 540.0, 5.20, 2),
(55, 'Barre protéinée chocolat', 'Pack 3 barres', 204.0, 'Barre', 'Sachet', 810.0, 7.50, 3),
(56, 'Barre fruits rouges quinoa', 'Vegan', 60.0, 'Barre', 'Unité', 230.0, 2.50, 1),
(57, 'Barre noix cajou abricot', 'Naturelle', 50.0, 'Barre', 'Unité', 210.0, 2.00, 1),
(58, 'Barre noix cajou abricot', 'Pack 3', 150.0, 'Barre', 'Sachet', 630.0, 5.50, 3),
(59, 'Barre coco chocolat noir', 'Type Bounty outdoor', 55.0, 'Barre', 'Unité', 250.0, 2.30, 1),
(60, 'Tablette chocolat au lait', '80g tablette', 80.0, 'Chocolat', 'Unité', 440.0, 1.80, 1),
(61, 'Tablette chocolat noir 72%', 'Antioxydant', 80.0, 'Chocolat', 'Unité', 460.0, 2.00, 1),
(62, 'Tablette chocolat noir 72%', 'Pack 2', 160.0, 'Chocolat', 'Sachet', 920.0, 3.80, 2),
(63, 'Carrés chocolat blanc', '70g', 70.0, 'Chocolat', 'Unité', 390.0, 1.90, 1),
(64, 'Pépites de chocolat', 'Snack vrac', 50.0, 'Chocolat', 'Sachet', 275.0, 1.60, 1),
(65, 'Guimauves mini', 'Légères', 60.0, 'Bonbon', 'Sachet', 200.0, 1.50, 1),
(66, 'Caramels mous au beurre salé', 'Bretagne', 70.0, 'Bonbon', 'Sachet', 290.0, 2.00, 1),
(67, 'Pâte de fruits abricot', 'Confiserie artisanale', 50.0, 'Confiserie', 'Sachet', 155.0, 1.80, 1),
(68, 'Crackers sésame miel', '4 crackers', 60.0, 'Biscuits', 'Sachet', 260.0, 1.70, 1),
(69, 'Biscuits sablés beurre', 'Petits beurre', 60.0, 'Biscuits', 'Sachet', 290.0, 1.60, 1),
(70, 'Cookies pépites chocolat', '3 cookies', 75.0, 'Biscuits', 'Sachet', 330.0, 2.00, 1),
(71, 'Gaufrettes fourrées noisette', '4 gaufrettes', 65.0, 'Biscuits', 'Sachet', 310.0, 1.50, 1),
(72, 'Pain d''épices tranches', '4 tranches emballées', 80.0, 'Pain', 'Sachet', 280.0, 2.10, 1),
(73, 'Flapjack avoine sirop érable', 'Outdoor UK style', 70.0, 'Barre', 'Unité', 300.0, 2.50, 1),
(74, 'Flapjack avoine sirop érable', 'Pack 2', 140.0, 'Barre', 'Sachet', 600.0, 4.80, 2),
(75, 'Halva sésame pistache', 'Confiserie orientale', 80.0, 'Confiserie', 'Unité', 380.0, 2.50, 1),

-- =====================
-- SNACKS SALÉS
-- =====================
(76, 'Saucisson sec à l''ail', 'Charcuterie sèche', 100.0, 'Saucisson', 'Pièce', 450.0, 3.50, 1),
(77, 'Saucisson sec à l''ail', 'Pack 2 saucissons', 200.0, 'Saucisson', 'Sachet', 900.0, 6.50, 2),
(78, 'Rosette de Lyon', 'Charcuterie fine en tranches', 80.0, 'Saucisson', 'Sachet', 360.0, 3.20, 1),
(79, 'Chorizo doux', 'Tranche emballée sous vide', 80.0, 'Charcuterie', 'Sachet', 340.0, 3.00, 1),
(80, 'Jambon cru Bayonne', 'Tranches sous vide 2p', 80.0, 'Jambon', 'Sachet', 280.0, 3.80, 1),
(81, 'Fromage Comté 12 mois', 'Mini portion 30g', 60.0, 'Fromage', 'Portion', 240.0, 2.50, 1),
(82, 'Fromage Comté 12 mois', 'Double portion', 120.0, 'Fromage', 'Portion', 480.0, 4.80, 2),
(83, 'Babybel original', 'Portion cire rouge', 50.0, 'Fromage', 'Unité', 170.0, 1.20, 1),
(84, 'Babybel original', 'Pack 2', 100.0, 'Fromage', 'Sachet', 340.0, 2.20, 2),
(85, 'Babybel original', 'Pack 3', 150.0, 'Fromage', 'Sachet', 510.0, 3.20, 3),
(86, 'Fromage de brebis Ossau-Iraty', 'Portion sous vide', 70.0, 'Fromage', 'Portion', 270.0, 3.00, 1),
(87, 'Crackers riz sel de mer', 'Galettes sans gluten', 55.0, 'Biscuits', 'Sachet', 210.0, 1.40, 1),
(88, 'Crackers riz sel de mer', 'Pack 3', 165.0, 'Biscuits', 'Sachet', 630.0, 3.80, 3),
(89, 'Gressins à l''huile d''olive', '10 gressins', 80.0, 'Pain', 'Sachet', 340.0, 1.80, 1),
(90, 'Chips de légumes', 'Betterave, panais, carotte', 50.0, 'Chips', 'Sachet', 230.0, 2.20, 1),
(91, 'Chips de lentilles piment', 'High protein snack', 55.0, 'Chips', 'Sachet', 220.0, 2.50, 1),
(92, 'Pistaches grillées salées', 'Noix à coque', 70.0, 'Noix', 'Sachet', 410.0, 2.80, 1),
(93, 'Pistaches grillées salées', 'Format duo', 140.0, 'Noix', 'Sachet', 820.0, 5.20, 2),
(94, 'Amandes grillées', 'Sans sel', 60.0, 'Noix', 'Sachet', 360.0, 2.50, 1),
(95, 'Amandes grillées', 'Pack 3', 180.0, 'Noix', 'Sachet', 1080.0, 6.80, 3),
(96, 'Noix de cajou nature', 'Non salées', 60.0, 'Noix', 'Sachet', 350.0, 2.90, 1),
(97, 'Noix de cajou nature', 'Pack 2', 120.0, 'Noix', 'Sachet', 700.0, 5.50, 2),
(98, 'Mélange trail mix', 'Noix, raisins, cranberries', 70.0, 'Noix', 'Sachet', 320.0, 3.00, 1),
(99, 'Mélange trail mix', 'Duo', 140.0, 'Noix', 'Sachet', 640.0, 5.60, 2),
(100, 'Mélange trail mix', 'Trio', 210.0, 'Noix', 'Sachet', 960.0, 8.00, 3),
(101, 'Noix de Grenoble décortiquées', 'IGP', 50.0, 'Noix', 'Sachet', 330.0, 2.60, 1),
(102, 'Cacahuètes grillées salées', 'Classique', 60.0, 'Noix', 'Sachet', 330.0, 1.20, 1),
(103, 'Noix macadamia', 'Premium', 60.0, 'Noix', 'Sachet', 430.0, 3.80, 1),
(104, 'Olives noires dénoyautées', 'Sachets hermétiques', 70.0, 'Olives', 'Sachet', 140.0, 1.80, 1),
(105, 'Olives vertes pimentées', 'Sachet hermétique', 70.0, 'Olives', 'Sachet', 120.0, 1.80, 1),
(106, 'Tomates séchées huile d''olive', 'Sous vide', 80.0, 'Légume séché', 'Sachet', 210.0, 2.50, 1),
(107, 'Champignons séchés shiitake', 'Déshydratés', 50.0, 'Légume séché', 'Sachet', 130.0, 3.50, 1),
(108, 'Thon en boîte à l''huile', '1 boîte 80g', 80.0, 'Thon', 'Boîte', 200.0, 2.00, 1),
(109, 'Thon en boîte à l''huile', '2 boîtes', 160.0, 'Thon', 'Sachet', 400.0, 3.80, 2),
(110, 'Thon en boîte à l''huile', '3 boîtes', 240.0, 'Thon', 'Sachet', 600.0, 5.50, 3),
(111, 'Sardines à l''huile d''olive', '1 boîte 100g', 100.0, 'Poisson', 'Boîte', 220.0, 2.20, 1),
(112, 'Maquereau sauce moutarde', '1 boîte 125g', 125.0, 'Poisson', 'Boîte', 290.0, 2.50, 1),
(113, 'Foie gras de canard', 'Portion individuelle 30g', 50.0, 'Foie gras', 'Portion', 155.0, 4.50, 1),
(114, 'Rillettes de porc', 'Pot individuel 50g', 50.0, 'Rillettes', 'Pot', 200.0, 1.80, 1),
(115, 'Tartare de saumon fumé', 'Sachet sous vide 60g', 60.0, 'Poisson', 'Sachet', 110.0, 4.20, 1),

-- =====================
-- FRUITS SECS & DÉSHYDRATÉS
-- =====================
(116, 'Abricots secs', 'Moelleux non soufrés', 80.0, 'Fruits Secs', 'Sachet', 195.0, 2.20, 1),
(117, 'Abricots secs', 'Pack duo', 155.0, 'Fruits Secs', 'Sachet', 380.0, 4.00, 2),
(118, 'Raisins secs sultanas', 'Doux', 60.0, 'Fruits Secs', 'Sachet', 180.0, 1.50, 1),
(119, 'Raisins secs sultanas', 'Pack trio', 180.0, 'Fruits Secs', 'Sachet', 540.0, 4.00, 3),
(120, 'Dattes medjool', 'Fraîches séchées 4p', 80.0, 'Fruits Secs', 'Sachet', 220.0, 2.80, 1),
(121, 'Dattes medjool', 'Pack duo', 160.0, 'Fruits Secs', 'Sachet', 440.0, 5.20, 2),
(122, 'Cranberries séchées', 'Sucrées légèrement', 60.0, 'Fruits Secs', 'Sachet', 190.0, 2.00, 1),
(123, 'Figues séchées', '4 figues', 80.0, 'Fruits Secs', 'Sachet', 200.0, 2.00, 1),
(124, 'Pruneaux dénoyautés', '6 pruneaux', 90.0, 'Fruits Secs', 'Sachet', 200.0, 1.80, 1),
(125, 'Mangue séchée', 'Tropicale', 60.0, 'Fruits Secs', 'Sachet', 175.0, 2.50, 1),
(126, 'Banane séchée chips', 'Croquante', 55.0, 'Fruits Secs', 'Sachet', 245.0, 1.90, 1),
(127, 'Banane séchée chips', 'Pack 3', 165.0, 'Fruits Secs', 'Sachet', 735.0, 5.20, 3),
(128, 'Noix de coco déshydratée', 'Copeaux grillés', 50.0, 'Fruits Secs', 'Sachet', 265.0, 1.80, 1),
(129, 'Compote pomme cannelle', 'Gourde 90g', 90.0, 'Compote', 'Gourde', 70.0, 1.20, 1),
(130, 'Compote pomme poire', 'Gourde 90g', 90.0, 'Compote', 'Gourde', 65.0, 1.20, 1),
(131, 'Compote abricot mangue', 'Gourde 90g', 90.0, 'Compote', 'Gourde', 75.0, 1.30, 1),
(132, 'Compote pomme cannelle', 'Pack 2 gourdes', 180.0, 'Compote', 'Sachet', 140.0, 2.30, 2),
(133, 'Compote pomme cannelle', 'Pack 3 gourdes', 270.0, 'Compote', 'Sachet', 210.0, 3.30, 3),

-- =====================
-- ENERGIE & EFFORT
-- =====================
(134, 'Gel énergétique goût citron', 'Running/trail', 62.0, 'Gel', 'Tube', 155.0, 1.80, 1),
(135, 'Gel énergétique goût framboise', 'Avec caféine', 62.0, 'Gel', 'Tube', 155.0, 1.80, 1),
(136, 'Gel énergétique goût framboise', 'Pack 2 gels', 124.0, 'Gel', 'Sachet', 310.0, 3.40, 2),
(137, 'Gel énergétique goût framboise', 'Pack 3 gels', 186.0, 'Gel', 'Sachet', 465.0, 4.90, 3),
(138, 'Gel énergétique banane', 'Isotonique', 70.0, 'Gel', 'Tube', 175.0, 2.00, 1),
(139, 'Chews énergétiques cola', 'Gommes à mâcher', 60.0, 'Gel', 'Sachet', 200.0, 2.50, 1),
(140, 'Chews énergétiques cola', 'Pack 2', 120.0, 'Gel', 'Sachet', 400.0, 4.50, 2),
(141, 'Bonbons glucose', 'Dextrose pur', 50.0, 'Gel', 'Sachet', 195.0, 1.50, 1),
(142, 'Boisson isotonique poudre citron', 'Sachet 1 dose', 50.0, 'Boisson poudre', 'Sachet', 185.0, 2.00, 1),
(143, 'Boisson isotonique poudre orange', 'Sachet 1 dose', 50.0, 'Boisson poudre', 'Sachet', 185.0, 2.00, 1),
(144, 'Boisson isotonique poudre orange', 'Pack 2 doses', 100.0, 'Boisson poudre', 'Sachet', 370.0, 3.80, 2),
(145, 'Boisson isotonique poudre orange', 'Pack 3 doses', 150.0, 'Boisson poudre', 'Sachet', 555.0, 5.50, 3),
(146, 'Boisson de récupération vanille', 'Protéines + glucides', 70.0, 'Boisson poudre', 'Sachet', 270.0, 3.50, 1),
(147, 'Électrolytes poudre neutre', 'Hydratation sels', 10.0, 'Boisson poudre', 'Sachet', 50.0, 1.20, 1),

-- =====================
-- BOISSONS CHAUDES
-- =====================
(148, 'Café soluble Nescafé', 'Sticks 2g individuels', 50.0, 'Café', 'Sachet', 50.0, 2.00, 1),
(149, 'Café lyophilisé premium', 'Arabica', 50.0, 'Café', 'Sachet', 50.0, 3.50, 1),
(150, 'Thé vert matcha', 'Sachet x4', 50.0, 'Thé', 'Sachet', 50.0, 2.50, 1),
(151, 'Infusion camomille miel', 'x4 sachets', 50.0, 'Infusion', 'Sachet', 50.0, 1.80, 1),
(152, 'Chocolat chaud instantané', 'Sachet 25g', 75.0, 'Chocolat chaud', 'Sachet', 290.0, 1.50, 1),
(153, 'Chocolat chaud instantané', 'Pack 3 sachets', 225.0, 'Chocolat chaud', 'Sachet', 870.0, 4.20, 3),
(154, 'Bouillon poule cube', '3 cubes', 50.0, 'Bouillon', 'Sachet', 60.0, 1.00, 1),
(155, 'Soupe miso instantanée', '2 sachets', 50.0, 'Soupe', 'Sachet', 70.0, 2.20, 2),
(156, 'Lait en poudre entier', '3 doses individuelles', 75.0, 'Lait', 'Sachet', 360.0, 2.00, 1),

-- =====================
-- CONSERVES LONGUE DURÉE
-- =====================
(157, 'Haricots blancs sauce tomate', 'Boîte 220g', 220.0, 'Haricots', 'Boîte', 250.0, 1.50, 1),
(158, 'Lentilles vertes du Puy', 'Boîte 200g', 200.0, 'Lentilles', 'Boîte', 200.0, 1.80, 1),
(159, 'Pois chiches naturel', 'Boîte 200g', 200.0, 'Légumineuse', 'Boîte', 220.0, 1.40, 1),
(160, 'Maïs doux en conserve', 'Boîte 200g', 200.0, 'Légume conserve', 'Boîte', 160.0, 1.20, 1),
(161, 'Riz blanc cuit sous vide', '200g - prêt à consommer', 200.0, 'Riz', 'Sachet', 260.0, 2.20, 1),
(162, 'Riz blanc cuit sous vide', 'Pack 2', 400.0, 'Riz', 'Sachet', 520.0, 4.00, 2),
(163, 'Pâtes cuites sous vide', 'Penne 200g', 200.0, 'Pates', 'Sachet', 280.0, 2.20, 1),
(164, 'Purée de tomate concentrée', 'Tube 70g', 70.0, 'Condiment', 'Tube', 55.0, 1.30, 1),
(165, 'Beurre de cacahuète', 'Pot individuel 32g', 50.0, 'Beurre noix', 'Pot', 195.0, 1.80, 1),
(166, 'Beurre de cacahuète', 'Pack 2', 100.0, 'Beurre noix', 'Sachet', 390.0, 3.40, 2),
(167, 'Beurre d''amande', 'Pot individuel 32g', 50.0, 'Beurre noix', 'Pot', 185.0, 2.50, 1),
(168, 'Miel en dose individuelle', 'Miel de fleurs 30g', 50.0, 'Miel', 'Sachet', 150.0, 1.20, 1),
(169, 'Miel en dose individuelle', 'Pack 3', 150.0, 'Miel', 'Sachet', 450.0, 3.20, 3),
(170, 'Nutella mini pot', '30g', 50.0, 'Pâte à tartiner', 'Pot', 165.0, 1.00, 1),
(171, 'Huile d''olive vierge extra', 'Gourde souple 50ml', 50.0, 'Huile', 'Gourde', 440.0, 1.50, 1),
(172, 'Sauce soja', 'Sticks 10ml x3', 50.0, 'Condiment', 'Sachet', 60.0, 1.50, 1),
(173, 'Sel & poivre assaisonnement', 'Mélange trek 15g', 50.0, 'Condiment', 'Sachet', 50.0, 0.80, 1),
(174, 'Parmesan râpé', 'Portion individuelle 20g', 50.0, 'Fromage', 'Sachet', 90.0, 1.20, 1);

-- ==========================================
-- 3. EQUIPMENT ITEMS
-- ==========================================

INSERT INTO equipment_items (nom, description, masse_grammes, nb_item, type, masse_a_vide) VALUES

-- =====================
-- REPOS
-- =====================
(1, 'Tente ultra-légère 1P', 'Shelter type Duplex', 900.0, 1, 'REPOS', 0.0),
(2, 'Tente bikepacking 1P', 'Tarp + sol intégré', 1200.0, 1, 'REPOS', 0.0),
(3, 'Tente 4 saisons 2P', 'Résistante tempête', 3200.0, 2, 'REPOS', 0.0),
(4, 'Bivy bag Gore-Tex', 'Couverture de survie avancée', 450.0, 1, 'REPOS', 0.0),
(5, 'Hamac ultralite', 'Nylon ripstop avec sangles', 550.0, 1, 'REPOS', 0.0),
(6, 'Hamac double', 'Avec moustiquaire intégrée', 950.0, 2, 'REPOS', 0.0),
(7, 'Matelas mousse fermée', 'Rouleau fin 180x50cm', 400.0, 1, 'REPOS', 0.0),
(8, 'Matelas gonflable 2P', 'Double couche thermique', 1100.0, 2, 'REPOS', 0.0),
(9, 'Matelas auto-gonflant', 'Épaisseur 3.8cm', 700.0, 1, 'REPOS', 0.0),
(10, 'Oreiller gonflable', 'Compressible 45g', 50.0, 1, 'REPOS', 0.0),
(11, 'Sac de couchage -5°C confort', 'Duvet synthétique', 1200.0, 1, 'REPOS', 0.0),
(12, 'Sac de couchage -10°C duvet', 'Plumes d''oie 800cuin', 900.0, 1, 'REPOS', 0.0),
(13, 'Sac de couchage +10°C été', 'Léger saison chaude', 600.0, 1, 'REPOS', 0.0),
(14, 'Housse de sac de couchage', 'Soie 100g', 100.0, 1, 'REPOS', 0.0),
(15, 'Drap de sac soie', 'Format duo', 180.0, 2, 'REPOS', 0.0),
(16, 'Tarp bâche légère', '2x3m, ancres inclus', 700.0, 2, 'REPOS', 0.0),
(17, 'Tarp bâche légère', '3x4m format trio', 1100.0, 3, 'REPOS', 0.0),
(18, 'Piquet de tente titane', 'Lot de 4', 80.0, 1, 'REPOS', 0.0),
(19, 'Piquet de tente titane', 'Lot de 8', 160.0, 2, 'REPOS', 0.0),
(20, 'Couverture de survie dorée', 'Réutilisable 200x160cm', 200.0, 1, 'REPOS', 0.0),
(21, 'Couverture de survie dorée', 'Pack 2', 400.0, 2, 'REPOS', 0.0),
(22, 'Couverture de survie dorée', 'Pack 3', 600.0, 3, 'REPOS', 0.0),
(23, 'Masque de sommeil', 'Contour oculaire', 50.0, 1, 'REPOS', 0.0),
(24, 'Bouchons d''oreilles mousse', 'Lot 6 paires', 50.0, 3, 'REPOS', 0.0),

-- =====================
-- SOIN
-- =====================
(25, 'Trousse de soins complète', 'Pansements, compresses, désinfectant, bandage', 350.0, 1, 'SOIN', 0.0),
(26, 'Trousse de soins duo', 'Kit 2 personnes', 600.0, 2, 'SOIN', 0.0),
(27, 'Trousse de soins trio', 'Kit 3 personnes', 850.0, 3, 'SOIN', 0.0),
(28, 'Bandage élastique 10cm', 'Strapping', 100.0, 1, 'SOIN', 0.0),
(29, 'Désinfectant spray 30ml', 'Biseptine', 100.0, 1, 'SOIN', 0.0),
(30, 'Ampoules pansements hydro', 'Anti-ampoules x8', 80.0, 1, 'SOIN', 0.0),
(31, 'Ampoules pansements hydro', 'Pack 2 boîtes', 160.0, 2, 'SOIN', 0.0),
(32, 'SAM splint gouttière', 'Attelle d''urgence', 90.0, 1, 'SOIN', 0.0),
(33, 'Couverture de survie argent', 'Légère 200x160cm', 100.0, 1, 'SOIN', 0.0),
(34, 'Couverture de survie argent', 'Pack 2', 200.0, 2, 'SOIN', 0.0),
(35, 'Couverture de survie argent', 'Pack 3', 300.0, 3, 'SOIN', 0.0),
(36, 'Comprimés purification eau Micropur', 'Boîte 30cp', 60.0, 1, 'SOIN', 0.0),
(37, 'Comprimés purification eau Micropur', 'Duo 2x30cp', 120.0, 2, 'SOIN', 0.0),
(38, 'Comprimés purification eau Micropur', 'Trio 3x30cp', 180.0, 3, 'SOIN', 0.0),
(39, 'Crème solaire SPF50+ tube', '50ml', 100.0, 1, 'SOIN', 0.0),
(40, 'Crème solaire SPF50+ tube', 'Format duo 2x50ml', 200.0, 2, 'SOIN', 0.0),
(41, 'Crème solaire SPF50+ tube', 'Format trio 3x50ml', 300.0, 3, 'SOIN', 0.0),
(42, 'Répulsif insectes', 'DEET 50% spray 30ml', 80.0, 1, 'SOIN', 0.0),
(43, 'Répulsif insectes', 'Pack 2', 160.0, 2, 'SOIN', 0.0),
(44, 'Baume à lèvres SPF20', 'Avec filtre solaire', 50.0, 1, 'SOIN', 0.0),
(45, 'Vaseline tube', '30ml multifonction', 60.0, 1, 'SOIN', 0.0),
(46, 'Anti-nauséeux comprimés', 'Mercalm x6', 50.0, 1, 'SOIN', 0.0),
(47, 'Paracétamol 1000mg', 'Boîte 8 comprimés', 50.0, 1, 'SOIN', 0.0),
(48, 'Paracétamol 1000mg', 'Pack 2 boîtes', 100.0, 2, 'SOIN', 0.0),
(49, 'Ibuprofène 400mg', 'Boîte 8 comprimés', 50.0, 1, 'SOIN', 0.0),
(50, 'Sérum physiologique', 'Unidoses x5', 100.0, 1, 'SOIN', 0.0),
(51, 'Thermomètre digital', 'Compact 10g', 50.0, 1, 'SOIN', 0.0),
(52, 'Ciseaux pliants inox', 'Mini 10cm', 50.0, 1, 'SOIN', 0.0),
(53, 'Pince à épiler inox', '10cm', 50.0, 1, 'SOIN', 0.0),
(54, 'Trousse hygiène dentaire', 'Brosse + dentifrice + soie', 150.0, 1, 'SOIN', 0.0),
(55, 'Trousse hygiène dentaire', 'Duo', 280.0, 2, 'SOIN', 0.0),
(56, 'Savon solide multifonction', '30g BIO', 60.0, 1, 'SOIN', 0.0),
(57, 'Savon solide multifonction', 'Pack 2', 120.0, 2, 'SOIN', 0.0),
(58, 'Savon solide multifonction', 'Pack 3', 180.0, 3, 'SOIN', 0.0),
(59, 'Shampooing solide', '30g', 60.0, 1, 'SOIN', 0.0),
(60, 'Lingettes biodégradables', 'Lot x20', 200.0, 1, 'SOIN', 0.0),
(61, 'Lingettes biodégradables', 'Lot x20 - duo', 400.0, 2, 'SOIN', 0.0),
(62, 'Papier toilette compacté', '1 rouleau sans carton', 50.0, 1, 'SOIN', 0.0),
(63, 'Papier toilette compacté', '2 rouleaux', 100.0, 2, 'SOIN', 0.0),
(64, 'Papier toilette compacté', '3 rouleaux', 150.0, 3, 'SOIN', 0.0),
(65, 'Kit de réparation pieds', 'Compeed + talc + pince', 100.0, 1, 'SOIN', 0.0),
(66, 'Masque FFP2', 'Pack 5 unités', 100.0, 1, 'SOIN', 0.0),
(67, 'Gants latex chirurgicaux', 'Paire en sachet', 50.0, 1, 'SOIN', 0.0),
(68, 'Lunettes de soleil', 'Cat.3 légères 20g', 80.0, 1, 'SOIN', 0.0),

-- =====================
-- EAU (masse_a_vide > 0 et < masse_grammes)
-- =====================
(69, 'Gourde souple 1L Platypus', 'Pliable silicone', 1100.0, 1, 'EAU', 100.0),
(70, 'Gourde souple 2L Platypus', 'Pliable silicone', 2100.0, 1, 'EAU', 100.0),
(71, 'Gourde rigide Nalgene 1L', 'Incassable BPA free', 1320.0, 1, 'EAU', 320.0),
(72, 'Gourde isotherme 500ml', 'Inox double paroi', 800.0, 1, 'EAU', 300.0),
(73, 'Gourde isotherme 500ml', 'Duo', 1600.0, 2, 'EAU', 600.0),
(74, 'Poche à eau Hydrapak 2L', 'Tuyau flexible', 2300.0, 1, 'EAU', 300.0),
(75, 'Poche à eau Hydrapak 3L', 'Tuyau flexible', 3400.0, 1, 'EAU', 400.0),
(76, 'Réservoir souple 5L pliant', 'Stockage campement', 4800.0, 2, 'EAU', 300.0),
(77, 'Gourde 750ml titane', 'Ultra légère 100g', 850.0, 1, 'EAU', 100.0),
(78, 'Gourde 750ml titane', 'Duo', 1700.0, 2, 'EAU', 200.0),
(79, 'Filtre Sawyer Squeeze', 'Ultra filtration 0.1μm', 600.0, 1, 'EAU', 100.0),
(80, 'Filtre Sawyer Squeeze', 'Duo', 1200.0, 2, 'EAU', 200.0),
(81, 'Filtre Katadyn BeFree 1L', 'Filtre + poche intégrée', 1100.0, 1, 'EAU', 100.0),
(82, 'Filtre LifeStraw paille', 'Personnel 1P', 200.0, 1, 'EAU', 50.0),
(83, 'Filtre LifeStraw paille', 'Pack duo', 400.0, 2, 'EAU', 100.0),
(84, 'Filtre LifeStraw paille', 'Pack trio', 600.0, 3, 'EAU', 150.0),
(85, 'Bouteille filtrante Grayl Geopress', 'Filtre intégré 710ml', 1420.0, 1, 'EAU', 420.0),
(86, 'Poche à eau 10L pliable', 'Campement ou véhicule', 3500.0, 3, 'EAU', 500.0),
(87, 'Tuyau silicone souple', 'Adaptateur gourde', 200.0, 1, 'EAU', 100.0),
(88, 'Bidon HDPE 1.5L', 'Solide et léger', 1700.0, 1, 'EAU', 200.0),
(89, 'Bidon HDPE 1.5L', 'Duo', 3400.0, 2, 'EAU', 400.0),

-- =====================
-- PROGRESSION
-- =====================
(90, 'Bâtons carbone ultra', '2 bâtons pliables 3 brins', 400.0, 1, 'PROGRESSION', 0.0),
(91, 'Bâtons alu réglables', 'Paires antichoc', 600.0, 1, 'PROGRESSION', 0.0),
(92, 'Bâtons alu réglables', 'Duo 2 paires', 1200.0, 2, 'PROGRESSION', 0.0),
(93, 'Bâtons alu réglables', 'Trio 3 paires', 1800.0, 3, 'PROGRESSION', 0.0),
(94, 'Lampe frontale 400lm', 'Rechargeable USB-C', 140.0, 1, 'PROGRESSION', 0.0),
(95, 'Lampe frontale 400lm', 'Duo', 280.0, 2, 'PROGRESSION', 0.0),
(96, 'Lampe frontale 400lm', 'Trio', 420.0, 3, 'PROGRESSION', 0.0),
(97, 'Lampe frontale 1000lm', 'Haute puissance rechargeable', 250.0, 1, 'PROGRESSION', 0.0),
(98, 'Lampe frontale 1000lm', 'Duo', 500.0, 2, 'PROGRESSION', 0.0),
(99, 'Piles AA recharge', 'Eneloop Pro x4', 120.0, 1, 'PROGRESSION', 0.0),
(100, 'Piles AA recharge', 'Lot 8 duo', 240.0, 2, 'PROGRESSION', 0.0),
(101, 'GPS Garmin inReach Mini', 'Satellite + SOS', 100.0, 1, 'PROGRESSION', 0.0),
(102, 'GPS Garmin Montana 700', 'Cartographie topo', 250.0, 1, 'PROGRESSION', 0.0),
(103, 'Altimètre-boussole Suunto', 'Précision trail', 120.0, 1, 'PROGRESSION', 0.0),
(104, 'Boussole Silva Ranger', 'Miroir + déclinaison', 100.0, 1, 'PROGRESSION', 0.0),
(105, 'Boussole Silva Ranger', 'Duo', 200.0, 2, 'PROGRESSION', 0.0),
(106, 'Carte IGN 1:25000', 'Plastifiée', 100.0, 1, 'PROGRESSION', 0.0),
(107, 'Carte IGN 1:25000', 'Pack 2 cartes', 200.0, 2, 'PROGRESSION', 0.0),
(108, 'Carte IGN 1:25000', 'Pack 3 cartes', 300.0, 3, 'PROGRESSION', 0.0),
(109, 'Sifflet urgence Fox40', 'Sans bille 120dB', 50.0, 1, 'PROGRESSION', 0.0),
(110, 'Sifflet urgence Fox40', 'Pack 3', 150.0, 3, 'PROGRESSION', 0.0),
(111, 'Miroir de signalisation', 'Inox poli', 80.0, 1, 'PROGRESSION', 0.0),
(112, 'Corde légère 4mm 20m', 'Para-cord', 200.0, 1, 'PROGRESSION', 0.0),
(113, 'Corde légère 4mm 30m', 'Secours duo', 300.0, 2, 'PROGRESSION', 0.0),
(114, 'Corde légère 6mm 50m', 'Secours trio', 1100.0, 3, 'PROGRESSION', 0.0),
(115, 'Crampons légers', '10 pointes alu', 400.0, 1, 'PROGRESSION', 0.0),
(116, 'Crampons légers', 'Duo', 800.0, 2, 'PROGRESSION', 0.0),
(117, 'Microspikes', 'Chaînes anti-glisse', 550.0, 1, 'PROGRESSION', 0.0),
(118, 'Guêtres basses', 'Anti-débris trail', 200.0, 1, 'PROGRESSION', 0.0),
(119, 'Guêtres basses', 'Duo', 400.0, 2, 'PROGRESSION', 0.0),
(120, 'Guêtres hautes imperméables', 'Montagne', 350.0, 1, 'PROGRESSION', 0.0),
(121, 'Chausson de bivouac', 'Duvet pour campement', 300.0, 1, 'PROGRESSION', 0.0),
(122, 'Casque vélo/escalade', 'Polyvalent', 350.0, 1, 'PROGRESSION', 0.0),
(123, 'Mousqueton HMS', 'Sécurité verrouillage', 100.0, 1, 'PROGRESSION', 0.0),
(124, 'Mousqueton HMS', 'Duo', 200.0, 2, 'PROGRESSION', 0.0),
(125, 'Mousqueton HMS', 'Trio', 300.0, 3, 'PROGRESSION', 0.0),
(126, 'Longe double 60cm', 'Dyneema', 120.0, 1, 'PROGRESSION', 0.0),
(127, 'Piolet léger', 'Lame acier manche fibre', 500.0, 1, 'PROGRESSION', 0.0),
(128, 'Guirlande LED solaire', 'Signalisation bivouac', 200.0, 1, 'PROGRESSION', 0.0),

-- =====================
-- AUTRE
-- =====================
(129, 'Réchaud gaz MSR PocketRocket', 'Ultra compact 73g', 450.0, 1, 'AUTRE', 0.0),
(130, 'Réchaud gaz Primus Lite+', 'Efficacité élevée', 560.0, 1, 'AUTRE', 0.0),
(131, 'Réchaud alcool Trangia', 'Simple et robuste', 400.0, 1, 'AUTRE', 0.0),
(132, 'Réchaud bois EcoZoom', 'Sans carburant', 900.0, 2, 'AUTRE', 0.0),
(133, 'Cartouche gaz 110g isobutane', '1 cartouche', 220.0, 1, 'AUTRE', 0.0),
(134, 'Cartouche gaz 110g isobutane', 'Pack 2', 440.0, 2, 'AUTRE', 0.0),
(135, 'Cartouche gaz 230g isobutane', '1 cartouche', 380.0, 1, 'AUTRE', 0.0),
(136, 'Cartouche gaz 230g isobutane', 'Pack 2', 760.0, 2, 'AUTRE', 0.0),
(137, 'Cartouche gaz 230g isobutane', 'Pack 3', 1140.0, 3, 'AUTRE', 0.0),
(138, 'Popote titane 700ml', 'Solo ultra légère', 115.0, 1, 'AUTRE', 0.0),
(139, 'Popote alu 900ml', 'Solo', 200.0, 1, 'AUTRE', 0.0),
(140, 'Set popote alu 2P', 'Casserole + couvercle', 450.0, 2, 'AUTRE', 0.0),
(141, 'Set popote alu 3P', 'Trio complet', 650.0, 3, 'AUTRE', 0.0),
(142, 'Tasse titane 450ml', 'Double fond', 80.0, 1, 'AUTRE', 0.0),
(143, 'Tasse titane 450ml', 'Duo', 160.0, 2, 'AUTRE', 0.0),
(144, 'Tasse titane 450ml', 'Trio', 240.0, 3, 'AUTRE', 0.0),
(145, 'Cuillère-fourchette titane', 'Spork compact', 50.0, 1, 'AUTRE', 0.0),
(146, 'Cuillère-fourchette titane', 'Duo', 100.0, 2, 'AUTRE', 0.0),
(147, 'Cuillère-fourchette titane', 'Trio', 150.0, 3, 'AUTRE', 0.0),
(148, 'Couteau multifonctions Victorinox', 'Huntsman 12 outils', 160.0, 1, 'AUTRE', 0.0),
(149, 'Couteau lame fixe 10cm', 'Acier inox manche bois', 150.0, 1, 'AUTRE', 0.0),
(150, 'Couteau pliant', 'Lame 7cm tout-terrain', 100.0, 1, 'AUTRE', 0.0),
(151, 'Ouvre-boîte léger', 'Porte-clé', 50.0, 1, 'AUTRE', 0.0),
(152, 'Serviette microfibre S', '40x80cm absorbante', 100.0, 1, 'AUTRE', 0.0),
(153, 'Serviette microfibre M', '60x120cm', 200.0, 1, 'AUTRE', 0.0),
(154, 'Serviette microfibre M', 'Duo', 400.0, 2, 'AUTRE', 0.0),
(155, 'Serviette microfibre M', 'Trio', 600.0, 3, 'AUTRE', 0.0),
(156, 'Batterie externe 10000mAh', 'USB-A + USB-C', 250.0, 1, 'AUTRE', 0.0),
(157, 'Batterie externe 5000mAh', 'Légère 130g', 180.0, 1, 'AUTRE', 0.0),
(158, 'Batterie externe 10000mAh', 'Duo partagé', 500.0, 2, 'AUTRE', 0.0),
(159, 'Panneau solaire pliable 10W', 'Chargeur solaire', 350.0, 1, 'AUTRE', 0.0),
(160, 'Panneau solaire pliable 20W', 'Duo recharge', 700.0, 2, 'AUTRE', 0.0),
(161, 'Câble USB-C multi-embouts', 'Court 30cm', 50.0, 1, 'AUTRE', 0.0),
(162, 'Câble USB-C multi-embouts', 'Trio pack', 150.0, 3, 'AUTRE', 0.0),
(163, 'Sac étanche 5L drybag', 'Roll-top orange', 120.0, 1, 'AUTRE', 0.0),
(164, 'Sac étanche 5L drybag', 'Duo', 240.0, 2, 'AUTRE', 0.0),
(165, 'Sac étanche 10L drybag', '1 grand sac', 180.0, 1, 'AUTRE', 0.0),
(166, 'Sac étanche 10L drybag', 'Duo', 360.0, 2, 'AUTRE', 0.0),
(167, 'Sac étanche 10L drybag', 'Trio', 540.0, 3, 'AUTRE', 0.0),
(168, 'Sacs zip hermétiques', 'Pack 10 sacs L', 100.0, 1, 'AUTRE', 0.0),
(169, 'Allumettes tempête', 'Imperméables x15', 60.0, 1, 'AUTRE', 0.0),
(170, 'Allumettes tempête', 'Pack 3 boîtes', 180.0, 3, 'AUTRE', 0.0),
(171, 'Briquet Zippo tempête', 'Coupe-vent rechargeable', 80.0, 1, 'AUTRE', 0.0),
(172, 'Briquet gaz classique', 'Type Bic', 50.0, 1, 'AUTRE', 0.0),
(173, 'Firesteel silex', 'Allumage d''urgence', 60.0, 1, 'AUTRE', 0.0),
(174, 'Porte-monnaie imperméable', 'Documents + cartes', 80.0, 1, 'AUTRE', 0.0),
(175, 'Ruban adhésif multi-usages', 'Gaffer 5m', 100.0, 1, 'AUTRE', 0.0),
(176, 'Fil et aiguilles réparation', 'Kit couture minimal', 50.0, 1, 'AUTRE', 0.0),
(177, 'Gel hydroalcoolique', 'Flacon 50ml', 80.0, 1, 'AUTRE', 0.0),
(178, 'Gel hydroalcoolique', 'Duo', 160.0, 2, 'AUTRE', 0.0),
(179, 'Lampe de bivouac LED', 'Lanterne suspendue', 150.0, 1, 'AUTRE', 0.0),
(180, 'Lampe de bivouac LED', 'Duo', 300.0, 2, 'AUTRE', 0.0),
(181, 'Crochet inox multifonction', 'Suspension sac/équipement', 80.0, 1, 'AUTRE', 0.0),
(182, 'Antivol câble acier 1.5m', 'Vélo/équipement', 400.0, 1, 'AUTRE', 0.0),
(183, 'Pince à linge mini', 'Lot 6 - séchage', 60.0, 1, 'AUTRE', 0.0),
(184, 'Corde linge légère 5m', 'Avec tendeurs', 100.0, 1, 'AUTRE', 0.0),
(185, 'Corde linge légère 5m', 'Duo', 200.0, 2, 'AUTRE', 0.0),

-- =====================
-- VÊTEMENT
-- =====================
(186, 'Veste softshell légère', 'Coupe-vent déperlant', 450.0, 1, 'VETEMENT', 0.0),
(187, 'Veste Gore-Tex 3L', 'Imperméable respirant', 500.0, 1, 'VETEMENT', 0.0),
(188, 'Veste duvet léger', 'Duvet synthétique packable', 400.0, 1, 'VETEMENT', 0.0),
(189, 'Doudoune col montant', 'Duvet d''oie 800cuin', 350.0, 1, 'VETEMENT', 0.0),
(190, 'Gilet duvet sans manches', 'Ultra compressible', 220.0, 1, 'VETEMENT', 0.0),
(191, 'Polaire légère 1/4 zip', 'Merinos 150g/m²', 300.0, 1, 'VETEMENT', 0.0),
(192, 'Polaire épaisse full zip', 'Isolation thermique haute', 500.0, 1, 'VETEMENT', 0.0),
(193, 'T-shirt mérinos manches longues', 'Antiodorant naturel', 200.0, 1, 'VETEMENT', 0.0),
(194, 'T-shirt synthétique manches courtes', 'Séchage ultra rapide', 120.0, 1, 'VETEMENT', 0.0),
(195, 'Pantalon imperméable léger', 'Sur-pantalon de pluie', 300.0, 1, 'VETEMENT', 0.0),
(196, 'Short trail running', 'Avec sous-short intégré', 130.0, 1, 'VETEMENT', 0.0),
(197, 'Legging de compression', 'Récupération ou froid', 250.0, 1, 'VETEMENT', 0.0),
(198, 'Sous-vêtement bas laine mérinos', 'Pantalon de base', 220.0, 1, 'VETEMENT', 0.0),
(199, 'Chaussettes trekking Lorpen', 'Laine + coolmax', 100.0, 1, 'VETEMENT', 0.0),
(200, 'Chaussettes trekking mi-mollet', 'Anti-ampoules renforcées', 90.0, 1, 'VETEMENT', 0.0),
(201, 'Chaussettes légères courtes', 'Synthétique respirant', 70.0, 1, 'VETEMENT', 0.0),
(202, 'Guêtres de cheville', 'Anti-gravier', 100.0, 1, 'VETEMENT', 0.0),
(203, 'Gants imperméables', 'Doublure polaire', 180.0, 1, 'VETEMENT', 0.0),
(204, 'Gants liner mince', 'Sous-gants polyester', 80.0, 1, 'VETEMENT', 0.0),
(205, 'Gants mi-saison', 'Softshell touchscreen', 120.0, 1, 'VETEMENT', 0.0),
(206, 'Tour de cou multifonction Buff', 'Polyester microfibre', 60.0, 1, 'VETEMENT', 0.0),
(207, 'Tour de cou mérinos', 'Chaud et naturel', 80.0, 1, 'VETEMENT', 0.0),
(208, 'Bonnet mérinos fin', 'Légère isolation', 70.0, 1, 'VETEMENT', 0.0),
(209, 'Bonnet polaire chaud', 'Hiver froid', 120.0, 1, 'VETEMENT', 0.0),
(210, 'Cagoule balacalava', 'Face complète', 100.0, 1, 'VETEMENT', 0.0),
(211, 'Chapeau soleil large bord', 'Traitement UV50+', 100.0, 1, 'VETEMENT', 0.0),
(212, 'Poncho pluie ultraléger', '70g, compact', 100.0, 1, 'VETEMENT', 0.0),
(213, 'Jambières de pluie', 'Sur-pantalon étanche', 200.0, 1, 'VETEMENT', 0.0),
(214, 'Semelles insoles trail', 'Amorti heel', 150.0, 1, 'VETEMENT', 0.0),
(215, 'Semelles chaudes hiver', 'Isolantes', 200.0, 1, 'VETEMENT', 0.0),
(216, 'Ceinture élastique sport', 'Porte-gourde coureur', 120.0, 1, 'VETEMENT', 0.0),
(217, 'Gilet de trail running', 'Ultra 5L réservoir inclus', 300.0, 1, 'VETEMENT', 0.0);

-- ==========================================
-- 4. POI
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
-- 5. HIKES
-- ==========================================
INSERT INTO hikes (id, libelle, depart_id, arrivee_id, duree_jours, creator_id, is_optimize) VALUES
                                                                                    (1, 'Rando Solo 1 J', 1, 2, 1, 1, TRUE),
                                                                                    (2, 'Week-end Duo', 3, 4, 2, 1, TRUE),
                                                                                    (3, 'Balade Trio', 5, 6, 2, 2, FALSE),
                                                                                    (4, 'Trek Duo 3 J', 7, 8, 3, 2, TRUE),
                                                                                    (5, 'Boucle Solo', 9, 10, 1, 3, FALSE),
                                                                                    (6, 'Littoral Trio', 11, 12, 2, 3, FALSE),
                                                                                    (7, 'Marche Solo', 13, 14, 3, 4, FALSE),
                                                                                    (8, 'Grotte Duo', 15, 16, 2, 4, FALSE),
                                                                                    (9, 'Château Solo', 17, 18, 1, 5, FALSE),
                                                                                    (10, 'Survie Trio', 19, 20, 3, 5, FALSE);

-- ==========================================
-- 6. PARTICIPANTS
-- ==========================================
INSERT INTO participants (id, prenom, nom, age, niveau, morphologie, creator, creator_id, besoin_kcal, besoin_eau_litre, capacite_emport_max_kg) VALUES
                                                                                                                                                     (1, 'Alex', 'M', 25, 'ENTRAINE', 'MOYENNE', TRUE, 1, 3000, 5.0, 0.0),
                                                                                                                                                     (2, 'Alex', 'M', 25, 'ENTRAINE', 'MOYENNE', TRUE, 1, 3000, 5.0, 0.0),
                                                                                                                                                     (3, 'Jean', 'D', 30, 'DEBUTANT', 'FORTE', FALSE, 1, 2000, 3.0, 12.0),
                                                                                                                                                     (4, 'Tony', 'S', 40, 'SPORTIF', 'LEGERE', TRUE, 2, 3000, 5.0, 0.0),
                                                                                                                                                     (5, 'Paul', 'D', 35, 'ENTRAINE', 'MOYENNE', FALSE, 2, 2000, 3.0, 15.0),
                                                                                                                                                     (6, 'Luc', 'B', 28, 'SPORTIF', 'LEGERE', FALSE, 2, 2200, 3.0, 16.0),
                                                                                                                                                     (7, 'Tony', 'S', 40, 'SPORTIF', 'LEGERE', TRUE, 2, 3000, 5.0, 0.0),
                                                                                                                                                     (8, 'Marc', 'N', 45, 'ENTRAINE', 'FORTE', FALSE, 2, 2400, 4.0, 14.0),
                                                                                                                                                     (9, 'Sarah', 'C', 60, 'DEBUTANT', 'FORTE', TRUE, 3, 3000, 5.0, 0.0),
                                                                                                                                                     (10, 'Sarah', 'C', 60, 'DEBUTANT', 'FORTE', TRUE, 3, 3000, 5.0, 0.0),
                                                                                                                                                     (11, 'Emma', 'G', 55, 'DEBUTANT', 'MOYENNE', FALSE, 3, 1800, 2.0, 10.0),
                                                                                                                                                     (12, 'Lea', 'R', 50, 'ENTRAINE', 'LEGERE', FALSE, 3, 2000, 3.0, 12.0),
                                                                                                                                                     (13, 'Frodo', 'B', 32, 'ENTRAINE', 'LEGERE', TRUE, 4, 3000, 5.0, 0.0),
                                                                                                                                                     (14, 'Frodo', 'B', 32, 'ENTRAINE', 'LEGERE', TRUE, 4, 3000, 5.0, 0.0),
                                                                                                                                                     (15, 'Sam', 'G', 33, 'SPORTIF', 'FORTE', FALSE, 4, 2500, 4.0, 20.0),
                                                                                                                                                     (16, 'Lara', 'C', 28, 'SPORTIF', 'MOYENNE', TRUE, 5, 3000, 5.0, 0.0),
                                                                                                                                                     (17, 'Lara', 'C', 28, 'SPORTIF', 'MOYENNE', TRUE, 5, 3000, 5.0, 0.0),
                                                                                                                                                     (18, 'Jon', 'S', 30, 'SPORTIF', 'MOYENNE', FALSE, 5, 2600, 4.0, 18.0),
                                                                                                                                                     (19, 'Arya', 'S', 20, 'ENTRAINE', 'LEGERE', FALSE, 5, 2000, 3.0, 14.0);

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