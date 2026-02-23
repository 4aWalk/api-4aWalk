package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.*;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires de l'OptimizerService.
 * Valide les algorithmes de sélection d'équipement et de nourriture via Backtracking.
 * Adapté aux calculs dynamiques des entités (Hike, FoodProduct).
 */
class OptimizerServiceTest {

    private OptimizerService optimizerService;
    private Hike testHike;

    @BeforeEach
    void setUp() {
        optimizerService = new OptimizerService();
        testHike = new Hike();
        testHike.setParticipants(new HashSet<>());
        testHike.setFoodCatalogue(new ArrayList<>());
    }

    // ==========================================
    // TESTS : OPTIMISATION DE L'ÉQUIPEMENT
    // ==========================================

    @Test
    void getOptimizeAllEquipment_NominalCase_ShouldFindBestCombination() {
        // GIVEN : Une randonnée de 2 jours pour 3 participants.
        setupHikeParticipants(3, 2000); // 3 personnes
        testHike.setDureeJours(2); // Dure > 1 jour, donc le type REPOS est requis

        // GIVEN : Un catalogue d'équipement avec 2 options pour le REPOS.
        Map<TypeEquipment, GroupEquipment> groups = new EnumMap<>(TypeEquipment.class);
        GroupEquipment reposGroup = new GroupEquipment();

        // Option 1 : 1 Tente de 3 places (Meilleure car 1 seul élément dans la liste finale)
        reposGroup.addItem(createEquip("Tente 3 places", 3, TypeEquipment.REPOS));
        // Option 2 : 3 Tentes de 1 place (Moins bonne car 3 éléments)
        reposGroup.addItem(createEquip("Tente 1 place", 1, TypeEquipment.REPOS));
        reposGroup.addItem(createEquip("Tente 1 place", 1, TypeEquipment.REPOS));
        reposGroup.addItem(createEquip("Tente 1 place", 1, TypeEquipment.REPOS));

        groups.put(TypeEquipment.REPOS, reposGroup);
        fillOtherRequiredTypes(groups, 3); // Remplissage bouchon pour éviter le crash des autres types
        testHike.setEquipmentGroups(groups);

        // WHEN : On lance l'optimisation des équipements
        List<EquipmentItem> result = optimizerService.getOptimizeAllEquipmentV2(testHike);

        // THEN : L'algorithme choisit l'Option 1 (la tente 3 places) pour minimiser la taille de la liste
        boolean hasTente3Places = result.stream().anyMatch(e -> e.getNbItem() == 3);
        assertTrue(hasTente3Places, "L'algorithme doit privilégier la tente 3 places (1 seul item)");
    }

    @Test
    void getOptimizeAllEquipment_OneDayHike_ShouldSkipRepos() {
        // GIVEN : Une randonnée de 1 JOUR pour 2 personnes.
        setupHikeParticipants(2, 2000);
        testHike.setDureeJours(1);

        // GIVEN : Un catalogue qui ne contient AUCUN équipement de type REPOS.
        Map<TypeEquipment, GroupEquipment> groups = new EnumMap<>(TypeEquipment.class);
        fillOtherRequiredTypes(groups, 2);
        groups.remove(TypeEquipment.REPOS); // On s'assure qu'il n'y a rien
        testHike.setEquipmentGroups(groups);

        // WHEN : On lance l'optimisation
        // THEN : Pas d'exception levée, car la durée de 1 jour exclut le besoin de REPOS.
        assertDoesNotThrow(() -> optimizerService.getOptimizeAllEquipmentV2(testHike));
    }

    @Test
    void getOptimizeAllEquipment_InsufficientCoverage_ShouldThrowException() {
        // GIVEN : 4 participants à couvrir pour 2 jours.
        setupHikeParticipants(4, 2000);
        testHike.setDureeJours(2);

        // GIVEN : Le catalogue ne propose qu'une tente de 2 places pour le REPOS.
        Map<TypeEquipment, GroupEquipment> groups = new EnumMap<>(TypeEquipment.class);
        GroupEquipment reposGroup = new GroupEquipment();
        reposGroup.addItem(createEquip("Petite tente", 2, TypeEquipment.REPOS));
        groups.put(TypeEquipment.REPOS, reposGroup);
        testHike.setEquipmentGroups(groups);

        // WHEN : On lance l'optimisation
        // THEN : L'algorithme échoue et remonte l'erreur demandée par le service.
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> optimizerService.getOptimizeAllEquipmentV2(testHike));
        assertTrue(ex.getMessage().contains("Impossible de trouver une combinaison valide"));
    }

    // ==========================================
    // TESTS : OPTIMISATION DE LA NOURRITURE
    // ==========================================

    @Test
    void getOptimizeAllFood_NominalCase_ShouldMinimizeWeight() {
        // GIVEN : 1 participant avec un besoin de 2000 Kcal (Donc Hike target = 2000 Kcal)
        setupHikeParticipants(1, 2000);

        // GIVEN : Un catalogue de nourriture avec plusieurs combinaisons possibles
        List<FoodProduct> catalogue = new ArrayList<>();

        // Option 1 : Atteint 2000 kcal mais pèse LOURD (1 unité de 1000g, apporte 2000 Kcal)
        catalogue.add(createFood("Ration Lourde", 2000, 1000, 1));

        // Option 2 : Atteint 2000 kcal et pèse LÉGER (1 unité de 400g, apporte 2000 Kcal) -> DOIT GAGNER
        catalogue.add(createFood("Barre Magique", 2000, 400, 1));

        // Option 3 : Ne suffit pas tout seul (1 unité de 200g, apporte 1000 Kcal)
        catalogue.add(createFood("Pomme", 1000, 200, 1));

        testHike.setFoodCatalogue(catalogue);

        // WHEN : On lance l'optimisation alimentaire
        List<FoodProduct> result = optimizerService.getOptimizeAllFoodV2(testHike);

        // THEN : L'algorithme a choisi la barre magique (400g) pour minimiser le poids
        assertEquals(1, result.size());
        assertEquals("Barre Magique", result.getFirst().getNom());
    }

    @Test
    void getOptimizeAllFood_ZeroTarget_ShouldReturnEmptyList() {
        // GIVEN : Un besoin calorique total de 0 Kcal (Participant configuré à 0)
        setupHikeParticipants(1, 0);

        List<FoodProduct> catalogue = new ArrayList<>();
        catalogue.add(createFood("Pomme", 100, 100, 1));
        testHike.setFoodCatalogue(catalogue);

        // WHEN : On lance l'optimisation
        List<FoodProduct> result = optimizerService.getOptimizeAllFoodV2(testHike);

        // THEN : La liste est vide, pas besoin de s'encombrer pour 0 kcal
        assertTrue(result.isEmpty());
    }

    @Test
    void getOptimizeAllFood_InsufficientFood_ShouldHandleGracefully() {
        // GIVEN : Un besoin énorme de 5000 Kcal
        setupHikeParticipants(1, 5000);

        // GIVEN : Le catalogue n'a que 1000 Kcal en stock (1 biscuit)
        List<FoodProduct> catalogue = new ArrayList<>();
        catalogue.add(createFood("Biscuit", 1000, 200, 1));
        testHike.setFoodCatalogue(catalogue);

        // WHEN : L'algorithme cherche une solution
        List<FoodProduct> result = optimizerService.getOptimizeAllFoodV2(testHike);

        // THEN : Renvoie une liste vide suite à l'échec
        assertTrue(result.isEmpty());
    }

    // ==========================================
    // UTILITAIRES DE TEST (ADAPTÉS AUX ENTITÉS)
    // ==========================================

    /**
     * Crée des participants et les ajoute à la randonnée pour fixer l'objectif calorique et la couverture équipement.
     */
    private void setupHikeParticipants(int count, int kcalPerParticipant) {
        Set<Participant> participants = new HashSet<>();
        for (int i = 0; i < count; i++) {
            Participant p = new Participant();
            p.setBesoinKcal(kcalPerParticipant);
            participants.add(p);
        }
        testHike.setParticipants(participants);
    }

    /**
     * Crée un faux équipement avec un type spécifique.
     */
    private EquipmentItem createEquip(String nom, int capacite, TypeEquipment type) {
        EquipmentItem item = new EquipmentItem();
        item.setNom(nom);
        item.setNbItem(capacite);
        item.setType(type);
        return item;
    }

    /**
     * Crée un produit alimentaire avec calcul automatique des totaux via la classe.
     */
    private FoodProduct createFood(String nom, double apportKcalUnitaire, double masseGrammesUnitaire, int quantite) {
        FoodProduct food = new FoodProduct();
        food.setNom(nom);
        food.setApportNutritionnelKcal(apportKcalUnitaire);
        food.setMasseGrammes(masseGrammesUnitaire);
        food.setNbItem(quantite);
        return food;
    }

    /**
     * Ajoute des équipements bidons pour tous les types requis par l'algorithme,
     * pour éviter un crash (NullPointerException) lors du parcours de l'Enum.
     */
    private void fillOtherRequiredTypes(Map<TypeEquipment, GroupEquipment> groups, int nbParticipants) {
        for (TypeEquipment type : TypeEquipment.values()) {
            if (type != TypeEquipment.REPOS && type != TypeEquipment.AUTRE) {
                GroupEquipment group = new GroupEquipment();
                group.addItem(createEquip("Generic " + type, nbParticipants, type));
                groups.put(type, group);
            }
        }
    }
}