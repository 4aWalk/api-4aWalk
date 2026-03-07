package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.*;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.BelongEquipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Tests unitaires de l'OptimizerService.
 * Valide les algorithmes de sélection d'équipement et de nourriture via Backtracking.
 * Adapté aux calculs dynamiques des entités (Hike, FoodProduct).
 */
class OptimizerServiceTest {

    private OptimizerService optimizerService;
    private BelongEquipmentRepository belongEquipmentRepositoryMock;
    private Hike testHike;

    @BeforeEach
    void setUp() {
        // Initialisation du mock du repository
        belongEquipmentRepositoryMock = mock(BelongEquipmentRepository.class);
        // Si ton OptimizerService a besoin du repository, on l'injecte ici.
        // (Si le constructeur n'en a pas besoin, tu peux retirer cette injection).
        optimizerService = new OptimizerService();

        testHike = new Hike();
        testHike.setParticipants(new HashSet<>());
        testHike.setFoodCatalogue(new ArrayList<>());
    }

    // ==========================================
    // TESTS : OPTIMISATION DE L'ÉQUIPEMENT
    // ==========================================

    /**
     * Teste que l'optimisation sélectionne la combinaison d'équipements
     * qui minimise le nombre d'éléments transportés.
     */
    @Test
    void getOptimizeAllEquipment_NominalCase_ShouldFindBestCombination() {
        // Given : Une randonnée de 2 jours pour 3 participants (Type REPOS requis).
        setupHikeParticipants(3, 2000);
        testHike.setDureeJours(2);

        // Given : Un catalogue d'équipement avec 2 options pour le REPOS.
        Map<TypeEquipment, GroupEquipment> groups = new EnumMap<>(TypeEquipment.class);
        GroupEquipment reposGroup = new GroupEquipment();

        // Option 1 : 1 Tente de 3 places (Meilleure car 1 seul élément dans la liste finale)
        reposGroup.addItem(createEquip("Tente 3 places", 3, TypeEquipment.REPOS));
        // Option 2 : 3 Tentes de 1 place (Moins bonne car 3 éléments)
        reposGroup.addItem(createEquip("Tente 1 place", 1, TypeEquipment.REPOS));
        reposGroup.addItem(createEquip("Tente 1 place", 1, TypeEquipment.REPOS));
        reposGroup.addItem(createEquip("Tente 1 place", 1, TypeEquipment.REPOS));

        groups.put(TypeEquipment.REPOS, reposGroup);
        fillOtherRequiredTypes(groups, 3);
        testHike.setEquipmentGroups(groups);

        // When : On lance l'optimisation des équipements
        List<EquipmentItem> result = optimizerService.getOptimizeAllEquipment(testHike);

        // Then : L'algorithme choisit l'Option 1 (la tente 3 places)
        boolean hasTente3Places = result.stream().anyMatch(e -> e.getNbItem() == 3);
        assertTrue(hasTente3Places, "L'algorithme doit privilégier la tente 3 places (1 seul item)");
    }

    /**
     * Teste que l'optimisation ne lève pas d'exception si le type REPOS
     * est absent lors d'une randonnée d'un seul jour.
     */
    @Test
    void getOptimizeAllEquipment_OneDayHike_ShouldSkipRepos() {
        // Given : Une randonnée de 1 JOUR pour 2 personnes.
        setupHikeParticipants(2, 2000);
        testHike.setDureeJours(1);

        // Given : Un catalogue qui ne contient AUCUN équipement de type REPOS.
        Map<TypeEquipment, GroupEquipment> groups = new EnumMap<>(TypeEquipment.class);
        fillOtherRequiredTypes(groups, 2);
        groups.remove(TypeEquipment.REPOS);
        testHike.setEquipmentGroups(groups);

        // When & Then : Pas d'exception levée, car la durée exclut le besoin de REPOS.
        assertDoesNotThrow(() -> optimizerService.getOptimizeAllEquipment(testHike));
    }

    /**
     * Teste que l'optimisation échoue si le catalogue ne contient pas
     * assez d'équipements pour couvrir tous les participants.
     */
    @Test
    void getOptimizeAllEquipment_InsufficientCoverage_ShouldThrowException() {
        // Given : 4 participants à couvrir pour 2 jours.
        setupHikeParticipants(4, 2000);
        testHike.setDureeJours(2);

        // Given : Le catalogue ne propose qu'une tente de 2 places pour le REPOS.
        Map<TypeEquipment, GroupEquipment> groups = new EnumMap<>(TypeEquipment.class);
        GroupEquipment reposGroup = new GroupEquipment();
        reposGroup.addItem(createEquip("Petite tente", 2, TypeEquipment.REPOS));
        groups.put(TypeEquipment.REPOS, reposGroup);
        fillOtherRequiredTypes(groups, 4); // On remplit les autres pour être sûr de planter sur le REPOS
        testHike.setEquipmentGroups(groups);

        // When & Then : L'algorithme échoue car 2 places < 4 participants
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> optimizerService.getOptimizeAllEquipment(testHike));
        assertTrue(ex.getMessage().contains("Impossible de couvrir les besoins pour :"));
    }

    // ==========================================
    // TESTS : OPTIMISATION DE LA NOURRITURE
    // ==========================================

    /**
     * Teste que l'optimisation sélectionne la combinaison de nourriture
     * qui couvre les besoins caloriques tout en minimisant le poids total.
     */
    @Test
    void getOptimizeAllFood_NominalCase_ShouldMinimizeWeight() {
        // Given : 1 participant avec un besoin de 2000 Kcal
        setupHikeParticipants(1, 2000);

        // Given : Un catalogue de nourriture avec plusieurs combinaisons possibles
        List<FoodProduct> catalogue = new ArrayList<>();
        catalogue.add(createFood("Ration Lourde", 2000, 1000, 1));
        catalogue.add(createFood("Barre Magique", 2000, 400, 1)); // Option la plus légère
        catalogue.add(createFood("Pomme", 1000, 200, 1));
        testHike.setFoodCatalogue(catalogue);

        // When : On lance l'optimisation alimentaire
        List<FoodProduct> result = optimizerService.getOptimizeAllFood(testHike);

        // Then : L'algorithme a choisi la barre magique
        assertEquals(1, result.size());
        assertEquals("Barre Magique", result.getFirst().getNom());
    }

    /**
     * Teste que l'optimisation retourne une liste vide si l'objectif
     * calorique est de zéro.
     */
    @Test
    void getOptimizeAllFood_ZeroTarget_ShouldReturnEmptyList() {
        // Given : Un besoin calorique total de 0 Kcal
        setupHikeParticipants(1, 0);

        List<FoodProduct> catalogue = new ArrayList<>();
        catalogue.add(createFood("Pomme", 100, 100, 1));
        testHike.setFoodCatalogue(catalogue);

        // When : On lance l'optimisation
        List<FoodProduct> result = optimizerService.getOptimizeAllFood(testHike);

        // Then : La liste est vide
        assertTrue(result.isEmpty());
    }

    /**
     * Teste que l'optimisation échoue silencieusement (retourne une liste vide)
     * si le catalogue ne contient pas assez de calories.
     */
    @Test
    void getOptimizeAllFood_InsufficientFood_ShouldHandleGracefully() {
        // Given : Un besoin énorme de 5000 Kcal
        setupHikeParticipants(1, 5000);

        // Given : Le catalogue n'a que 1000 Kcal en stock
        List<FoodProduct> catalogue = new ArrayList<>();
        catalogue.add(createFood("Biscuit", 1000, 200, 1));
        testHike.setFoodCatalogue(catalogue);

        // When : L'algorithme cherche une solution
        List<FoodProduct> result = optimizerService.getOptimizeAllFood(testHike);

        // Then : Renvoie une liste vide suite à l'échec
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