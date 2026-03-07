package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.*;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.BelongEquipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du LogisticsValidationService.
 * Adapté aux vraies entités Hike et Participant (calculs dynamiques des calories et de l'eau).
 * Inclut la validation de l'appartenance des équipements via le BelongEquipmentRepository.
 */
class LogisticsValidationServiceTest {

    private LogisticsValidationService logisticsService;
    private BelongEquipmentRepository belongEquipmentRepositoryMock;
    private Hike standardHike;

    @BeforeEach
    void setUp() {
        // Initialisation du mock du repository pour la gestion des propriétaires
        belongEquipmentRepositoryMock = mock(BelongEquipmentRepository.class);
        logisticsService = new LogisticsValidationService(belongEquipmentRepositoryMock);

        standardHike = new Hike();
        standardHike.setId(1L); // Important pour simuler les appels BDD
        standardHike.setParticipants(new HashSet<>());
        standardHike.setFoodCatalogue(new ArrayList<>());
        standardHike.setEquipmentGroups(new HashMap<>());

        // Création de 2 participants réels pour définir les bases
        Participant p1 = new Participant();
        p1.setPrenom("Alice");
        p1.setBesoinKcal(2000);
        p1.setBesoinEauLitre(2);

        Participant p2 = new Participant();
        p2.setPrenom("Bob");
        p2.setBesoinKcal(2000);
        p2.setBesoinEauLitre(3);

        standardHike.getParticipants().add(p1);
        standardHike.getParticipants().add(p2);
    }

    // ==========================================
    // TESTS : VALIDATION DE LA NOURRITURE
    // ==========================================

    /**
     * Teste que la validation passe si le catalogue de nourriture est suffisant
     * et qu'aucun aliment n'est trop calorique individuellement.
     */
    @Test
    void validateHikeFood_ValidFood_ShouldPass() {
        // Given : 4000 kcal requises. Max par lot = 1000 kcal.
        // On ajoute 5 lots différents pour atteindre 4500 kcal sans dépasser le plafond
        standardHike.getFoodCatalogue().add(createFood("Pomme", 200, 5));     // 1000 kcal
        standardHike.getFoodCatalogue().add(createFood("Pates", 250, 4));     // 1000 kcal
        standardHike.getFoodCatalogue().add(createFood("Riz", 200, 5));       // 1000 kcal
        standardHike.getFoodCatalogue().add(createFood("Amandes", 500, 2));   // 1000 kcal
        standardHike.getFoodCatalogue().add(createFood("Chocolat", 250, 2));  // 500 kcal

        // When & Then : La validation ne doit lever aucune exception
        assertDoesNotThrow(() -> logisticsService.validateHikeFood(standardHike, standardHike.getCaloriesForAllParticipants()));
    }

    /**
     * Teste que la validation échoue si un seul aliment dépasse la limite
     * calorique autorisée par unité.
     */
    @Test
    void validateHikeFood_ItemTooCaloric_ShouldThrowException() {
        // Given : Les participants demandent 4000 kcal (max 1000 kcal/item).
        // On introduit un aliment unitaire à 1200 kcal.
        standardHike.getFoodCatalogue().add(createFood("Ration Survie", 1200, 4));

        // When & Then : L'aliment est rejeté car sa densité est trop forte
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> logisticsService.validateHikeFood(standardHike, standardHike.getCaloriesForAllParticipants()));
        assertTrue(ex.getMessage().contains("Nourriture trop calorique"));
    }

    /**
     * Teste que la validation échoue si la somme des calories du catalogue
     * est inférieure au besoin total des participants.
     */
    @Test
    void validateHikeFood_TotalCaloriesInsufficient_ShouldThrowException() {
        // Given : 4000 kcal requises. On ne fournit que 800 kcal au total.
        standardHike.getFoodCatalogue().add(createFood("Barre Céréale", 200, 4));

        // When & Then : L'exception de nourriture insuffisante est levée
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> logisticsService.validateHikeFood(standardHike, standardHike.getCaloriesForAllParticipants()));
        assertTrue(ex.getMessage().contains("Nourriture insuffisante"));
    }

    // ==========================================
    // TESTS : VALIDATION DE L'ÉQUIPEMENT
    // ==========================================

    /**
     * Teste que la validation échoue si des catégories d'équipement obligatoires
     * sont manquantes dans la randonnée.
     */
    @Test
    void validateHikeEquipment_MissingMandatoryEquipment_ShouldThrowException() {
        // Given : Une rando d'un jour (1). 2 participants. Aucun équipement configuré.
        standardHike.setDureeJours(1);

        // When & Then : L'exception de couverture insuffisante est levée
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> logisticsService.validateHikeEquipment(standardHike));
        assertTrue(ex.getMessage().contains("Couverture insuffisante pour le type"));
    }

    /**
     * Teste que la validation échoue si la randonnée dure plus d'un jour
     * et qu'aucun équipement de type REPOS n'est fourni.
     */
    @Test
    void validateHikeEquipment_TwoDayHike_MissingRepos_ShouldThrowException() {
        // Given : Randonnée de 2 jours. On fournit tout l'équipement OBLIGATOIRE sauf le type REPOS.
        standardHike.setDureeJours(2);
        populateEquipment(standardHike, 2, false); // false = omettre le REPOS

        // When & Then : À partir de 2 jours, le REPOS est requis
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> logisticsService.validateHikeEquipment(standardHike));
        assertTrue(ex.getMessage().contains("Couverture insuffisante pour le type : REPOS"));
    }

    /**
     * Teste que la validation passe si tous les équipements requis (y compris
     * les vêtements) ont bien un propriétaire assigné en base de données.
     */
    @Test
    void validateHikeEquipment_VetementAndReposWithOwner_ShouldPass() {
        // Given : Rando de 2 jours, l'équipement de base est présent
        standardHike.setDureeJours(2);
        populateEquipment(standardHike, 2, true);

        // On assigne un ID à l'item REPOS créé par la méthode utilitaire
        EquipmentItem reposItem = standardHike.getEquipmentGroups().get(TypeEquipment.REPOS).getItems().getFirst();
        reposItem.setId(10L);

        // On ajoute le groupe VETEMENT
        GroupEquipment vetementGroup = createGroupEquipment(TypeEquipment.VETEMENT, 1, 500, 0);
        EquipmentItem vetementItem = vetementGroup.getItems().getFirst();
        vetementItem.setId(20L);
        standardHike.getEquipmentGroups().put(TypeEquipment.VETEMENT, vetementGroup);

        // On simule que la base de données trouve bien un propriétaire pour ces objets
        when(belongEquipmentRepositoryMock.getIfExistParticipantForEquipmentAndHike(standardHike.getId(), 10L)).thenReturn(99L);
        when(belongEquipmentRepositoryMock.getIfExistParticipantForEquipmentAndHike(standardHike.getId(), 20L)).thenReturn(88L);

        // When & Then : La validation doit passer sans encombre
        assertDoesNotThrow(() -> logisticsService.validateHikeEquipment(standardHike));
    }

    // ==========================================
    // TESTS : CAPACITÉ D'EMPORT D'EAU
    // ==========================================

    /**
     * Teste que la validation de la capacité d'emport d'eau passe
     * si le volume total des gourdes est supérieur au besoin.
     */
    @Test
    void validateCapaciteEmportEauLitre_SufficientWater_ShouldPass() {
        // Given : Besoin total de 5L (2L + 3L d'après setUp).
        // On fournit 2 gourdes dont le delta (Pleine - Vide) est de 3000g = 3L. Total = 6L.
        GroupEquipment groupeEau = createGroupEquipment(TypeEquipment.EAU, 2, 3100, 100);
        standardHike.getEquipmentGroups().put(TypeEquipment.EAU, groupeEau);

        // When & Then : 6L >= 5L, la validation passe.
        assertDoesNotThrow(() -> logisticsService.validateCapaciteEmportEauLitre(standardHike));
    }

    /**
     * Teste que la validation de la capacité d'emport d'eau échoue
     * si le volume total des gourdes est inférieur au besoin.
     */
    @Test
    void validateCapaciteEmportEauLitre_InsufficientWater_ShouldThrowException() {
        // Given : Besoin total de 5L.
        // On ne fournit qu'une gourde de 1L (1100g pleine, 100g vide).
        GroupEquipment groupeEau = createGroupEquipment(TypeEquipment.EAU, 1, 1100, 100);
        standardHike.getEquipmentGroups().put(TypeEquipment.EAU, groupeEau);

        // When & Then : L'exception de gourdes insuffisantes est levée.
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> logisticsService.validateCapaciteEmportEauLitre(standardHike));
        assertTrue(ex.getMessage().contains("Pas assez de gourdes"));
    }

    /**
     * Teste que la validation de la capacité d'emport d'eau passe
     * si le volume total des gourdes est exactement égal au besoin.
     */
    @Test
    void validateCapaciteEmportEauLitre_ExactWater_ShouldPass() {
        // Given : Besoin total de 5L.
        // On fournit une poche à eau de 5L tout pile (5100g pleine, 100g vide).
        GroupEquipment groupeEau = createGroupEquipment(TypeEquipment.EAU, 1, 5100, 100);
        standardHike.getEquipmentGroups().put(TypeEquipment.EAU, groupeEau);

        // When & Then : La validation passe.
        assertDoesNotThrow(() -> logisticsService.validateCapaciteEmportEauLitre(standardHike));
    }

    // ==========================================
    // MÉTHODES UTILITAIRES POUR LES TESTS
    // ==========================================

    /**
     * Crée une entité FoodProduct mockée.
     * @param nom Nom usuel et appellation
     * @param kcalUnitaire Les calories pour UN item
     * @param nbItem Le nombre d'items de ce type dans le stock
     */
    private FoodProduct createFood(String nom, double kcalUnitaire, int nbItem) {
        FoodProduct food = new FoodProduct();
        food.setAppellationCourante(nom);
        food.setNom(nom);
        food.setApportNutritionnelKcal(kcalUnitaire);
        food.setNbItem(nbItem);
        return food;
    }

    /**
     * Remplit la randonnée avec le strict minimum d'équipements pour passer la validation.
     */
    private void populateEquipment(Hike hike, int nbParticipants, boolean includeRepos) {
        for (TypeEquipment type : TypeEquipment.values()) {
            if (type == TypeEquipment.AUTRE || type == TypeEquipment.VETEMENT) continue;
            if (type == TypeEquipment.REPOS && !includeRepos) continue;

            hike.getEquipmentGroups().put(type, createGroupEquipment(type, nbParticipants, 1000, 100));
        }
    }

    /**
     * Crée un groupe d'équipement avec un item générique.
     */
    private GroupEquipment createGroupEquipment(TypeEquipment type, int nbItem, double massePleine, double masseVide) {
        GroupEquipment group = new GroupEquipment();
        group.setType(type);

        EquipmentItem item = new EquipmentItem();
        item.setType(type);
        item.setNbItem(nbItem);
        item.setMasseGrammes(massePleine);
        item.setMasseAVide(masseVide);

        group.setItems(new ArrayList<>(java.util.List.of(item)));
        return group;
    }
}