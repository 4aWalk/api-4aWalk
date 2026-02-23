package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.*;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires du LogisticsValidationService.
 * Adapté aux vraies entités Hike et Participant (calculs dynamiques des calories et de l'eau).
 */
class LogisticsValidationServiceTest {

    private LogisticsValidationService logisticsService;
    private Hike standardHike;

    @BeforeEach
    void setUp() {
        logisticsService = new LogisticsValidationService();
        standardHike = new Hike();
        standardHike.setParticipants(new HashSet<>());
        standardHike.setFoodCatalogue(new ArrayList<>());
        standardHike.setEquipmentGroups(new HashMap<>());

        // Création de 2 participants réels pour définir les bases
        Participant p1 = new Participant();
        p1.setPrenom("Alice");
        p1.setBesoinKcal(2000);
        p1.setBesoinEauLitre(2); // C'est bien un Integer dans ton entité

        Participant p2 = new Participant();
        p2.setPrenom("Bob");
        p2.setBesoinKcal(2000);
        p2.setBesoinEauLitre(3);

        standardHike.getParticipants().add(p1);
        standardHike.getParticipants().add(p2);

        // À ce stade :
        // hike.getCaloriesForAllParticipants() == 4000
        // Besoin en eau total == 5.0L
    }

    // ==========================================
    // TESTS : VALIDATION DE LA NOURRITURE
    // ==========================================

    @Test
    void validateHikeFood_ValidFood_ShouldPass() {
        // Given : 4000 kcal requises (2000 + 2000). Max par item = 1000 kcal.
        // On ajoute pour 4500 kcal de nourriture valide
        standardHike.getFoodCatalogue().add(createFood("Pomme", 500, 3)); // 1500 kcal
        standardHike.getFoodCatalogue().add(createFood("Pates", 1000, 3)); // 3000 kcal (pile à la limite des 25%)

        // When & Then : La randonnée couvre les 4000 kcal sans dépasser le seuil par item
        assertDoesNotThrow(() -> logisticsService.validateHikeFood(standardHike, standardHike.getCaloriesForAllParticipants()));
    }

    @Test
    void validateHikeFood_DuplicateFood_ShouldThrowException() {
        // Given : Deux objets FoodProduct différents mais avec la même appellation courante
        standardHike.getFoodCatalogue().add(createFood("Riz", 500, 5)); // 2500 kcal
        standardHike.getFoodCatalogue().add(createFood("Riz", 500, 5)); // Doublon !

        // When & Then : L'exception de variété (Set processedFoods) est déclenchée
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> logisticsService.validateHikeFood(standardHike, standardHike.getCaloriesForAllParticipants()));
        assertTrue(ex.getMessage().contains("Doublon de type de nourriture détecté"));
    }

    @Test
    void validateHikeFood_ItemTooCaloric_ShouldThrowException() {
        // Given : Les participants demandent 4000 kcal (max 1000 kcal/item).
        // On introduit un aliment unitaire à 1200 kcal.
        standardHike.getFoodCatalogue().add(createFood("Ration Survie", 1200, 4)); // Total 4800, mais 1200 unitaire

        // When & Then : L'aliment est rejeté car sa densité est trop forte
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> logisticsService.validateHikeFood(standardHike, standardHike.getCaloriesForAllParticipants()));
        assertTrue(ex.getMessage().contains("Nourriture trop calorique"));
    }

    @Test
    void validateHikeFood_TotalCaloriesInsufficient_ShouldThrowException() {
        // Given : 4000 kcal requises. On ne fournit que 3000 kcal.
        standardHike.getFoodCatalogue().add(createFood("Barre Céréale", 500, 6)); // 3000 kcal au total

        // When & Then : La rando manque de nourriture
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> logisticsService.validateHikeFood(standardHike, standardHike.getCaloriesForAllParticipants()));
        assertTrue(ex.getMessage().contains("Nourriture insuffisante"));
    }

    // ==========================================
    // TESTS : VALIDATION DE L'ÉQUIPEMENT
    // ==========================================

    @Test
    void validateHikeEquipment_MissingMandatoryEquipment_ShouldThrowException() {
        // Given : Une rando d'un jour (1). 2 participants. Aucun équipement configuré.
        standardHike.setDureeJours(1);

        // When & Then : Le code va chercher les Trousse de secours, etc. (Type != AUTRE, != REPOS)
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> logisticsService.validateHikeEquipment(standardHike));
        assertTrue(ex.getMessage().contains("Couverture insuffisante pour le type"));
    }

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

    // ==========================================
    // TESTS : CAPACITÉ D'EMPORT D'EAU
    // ==========================================

    @Test
    void validateCapaciteEmportEauLitre_SufficientWater_ShouldPass() {
        // Given : Besoin total de 5L (2L + 3L d'après setUp).
        // On fournit 2 gourdes dont le delta (Pleine - Vide) est de 3000g = 3L. Total = 6L.
        GroupEquipment groupeEau = createGroupEquipment(TypeEquipment.EAU, 2, 3100, 100);
        standardHike.getEquipmentGroups().put(TypeEquipment.EAU, groupeEau);

        // When & Then : 6L >= 5L, la validation passe en douceur.
        assertDoesNotThrow(() -> logisticsService.validateCapaciteEmportEauLitre(standardHike));
    }

    @Test
    void validateCapaciteEmportEauLitre_InsufficientWater_ShouldThrowException() {
        // Given : Besoin total de 5L.
        // On ne fournit qu'une gourde de 1L (1100g pleine, 100g vide).
        GroupEquipment groupeEau = createGroupEquipment(TypeEquipment.EAU, 1, 1100, 100);
        standardHike.getEquipmentGroups().put(TypeEquipment.EAU, groupeEau);

        // When & Then : Les gourdes ne suffisent pas, le code doit planter.
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> logisticsService.validateCapaciteEmportEauLitre(standardHike));
        assertTrue(ex.getMessage().contains("Pas assez de gourdes"));
    }

    @Test
    void validateCapaciteEmportEauLitre_ExactWater_ShouldPass() {
        // Given : Besoin total de 5L.
        // On fournit une poche à eau de 5L tout pile (5100g pleine, 100g vide).
        GroupEquipment groupeEau = createGroupEquipment(TypeEquipment.EAU, 1, 5100, 100);
        standardHike.getEquipmentGroups().put(TypeEquipment.EAU, groupeEau);

        // When & Then : 5.0L n'est pas "strictement inférieur" à 5.0L, donc ça doit passer !
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
        food.setNbItem(nbItem); // Assure-toi que cette méthode existe bien dans Item/FoodProduct
        return food;
    }

    /**
     * Remplit la randonnée avec le strict minimum d'équipements pour passer la validation.
     */
    private void populateEquipment(Hike hike, int nbParticipants, boolean includeRepos) {
        for (TypeEquipment type : TypeEquipment.values()) {
            if (type == TypeEquipment.AUTRE) continue;
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
        item.setMasseGrammes(massePleine); // Poids total
        item.setMasseAVide(masseVide);     // Tare

        group.setItems(new ArrayList<>(java.util.List.of(item))); // Utilisation d'une List comme demandé
        return group;
    }
}