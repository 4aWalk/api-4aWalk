package iut.rodez.projet.sae.fourawalkapi.entity;

import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test le comportement de l'entité Hike, notamment la gestion du catalogue
 * d'équipements, de nourriture et les calculs caloriques globaux.
 */
class HikeTest {

    private Hike hike;

    @BeforeEach
    void setUp() {
        // Given: Une randonnée de 2 jours par défaut
        hike = new Hike();
        hike.setLibelle("Rando Test");
        hike.setDureeJours(2);
    }

    /**
     * Test l'ajout d'équipement et le tri automatique par masse.
     */
    @Test
    void addEquipment() {
        // Given: Deux équipements de REPOS de masses différentes
        EquipmentItem heavy = mock(EquipmentItem.class);
        EquipmentItem light = mock(EquipmentItem.class);
        when(heavy.getType()).thenReturn(TypeEquipment.REPOS);
        when(light.getType()).thenReturn(TypeEquipment.REPOS);
        when(heavy.getMasseGrammes()).thenReturn(2000.0);
        when(light.getMasseGrammes()).thenReturn(500.0);

        // When: On ajoute le lourd puis le léger, ou un item null
        hike.addEquipment(heavy);
        hike.addEquipment(light);
        hike.addEquipment(null); // Cas limite null

        // Then: Le groupe REPOS doit exister et être trié (léger en premier)
        GroupEquipment group = hike.getEquipmentGroups().get(TypeEquipment.REPOS);
        assertNotNull(group);
        assertEquals(2, group.getItems().size());
        assertEquals(light, group.getItems().getFirst(), "L'item le plus léger doit être en tête de liste");
    }

    /**
     * Test l'ajout de nourriture et le tri par densité calorique décroissante.
     */
    @Test
    void addFood() {
        // Given: Une nourriture peu dense et une très dense
        FoodProduct lowDensity = mock(FoodProduct.class);
        FoodProduct highDensity = mock(FoodProduct.class);

        // lowDensity: 100kcal / 200g = 0.5
        when(lowDensity.getTotalKcals()).thenReturn(100);
        when(lowDensity.getTotalMasses()).thenReturn(200);

        // highDensity: 500kcal / 100g = 5.0
        when(highDensity.getTotalKcals()).thenReturn(500);
        when(highDensity.getTotalMasses()).thenReturn(100);

        // When: On ajoute les nourritures
        hike.addFood(lowDensity);
        hike.addFood(highDensity);

        // Then: Le catalogue doit placer la plus haute densité en premier
        assertEquals(highDensity, hike.getFoodCatalogue().getFirst());
        assertEquals(2, hike.getFoodCatalogue().size());
    }

    /**
     * Test le retrait d'équipement, y compris pour un type absent.
     */
    @Test
    void removeEquipment() {
        // Given: Un sac contenant un soin
        EquipmentItem care = mock(EquipmentItem.class);
        when(care.getType()).thenReturn(TypeEquipment.SOIN);
        hike.addEquipment(care);

        // When: On retire l'item existant et un item null
        hike.removeEquipment(care);
        hike.removeEquipment(null);

        // Then: Le groupe doit être vide
        assertTrue(hike.getEquipmentGroups().get(TypeEquipment.SOIN).getItems().isEmpty());
    }

    /**
     * Test le retrait de nourriture.
     */
    @Test
    void removeFood() {
        // Given: Une randonnée avec un produit
        FoodProduct food = mock(FoodProduct.class);
        hike.addFood(food);

        // When: On retire la nourriture
        hike.removeFood(food);
        hike.removeFood(null);

        // Then: Le catalogue doit être vide
        assertTrue(hike.getFoodCatalogue().isEmpty());
    }

    /**
     * Test le calcul total des calories du catalogue de nourriture.
     */
    @Test
    void getCalorieRandonne() {
        // Given: Deux produits avec des quantités différentes
        FoodProduct p1 = mock(FoodProduct.class);
        when(p1.getApportNutritionnelKcal()).thenReturn(100.0);
        when(p1.getNbItem()).thenReturn(2); // 200 kcal

        FoodProduct p2 = mock(FoodProduct.class);
        when(p2.getApportNutritionnelKcal()).thenReturn(50.0);
        when(p2.getNbItem()).thenReturn(3); // 150 kcal

        hike.addFood(p1);
        hike.addFood(p2);

        // When: On calcule le total
        double total = hike.getCalorieRandonne();

        // Then: On doit obtenir 350 kcal
        assertEquals(350.0, total);
    }

    /**
     * Test le cumul des besoins caloriques de tous les participants.
     */
    @Test
    void getCaloriesForAllParticipants() {
        // Given: Une rando sans participants au départ, puis deux ajoutés
        assertEquals(0, hike.getCaloriesForAllParticipants());

        Participant p1 = mock(Participant.class);
        when(p1.getBesoinKcal()).thenReturn(2000);
        Participant p2 = mock(Participant.class);
        when(p2.getBesoinKcal()).thenReturn(2500);

        hike.getParticipants().add(p1);
        hike.getParticipants().add(p2);

        // When: On interroge le besoin total
        int total = hike.getCaloriesForAllParticipants();

        // Then: On doit obtenir 4500
        assertEquals(4500, total);
    }

    /**
     * Test la récupération des sacs à dos, en ignorant les participants sans sac.
     */
    @Test
    void getBackpacks() {
        // Given: Un participant avec sac et un participant sans sac (null)
        Participant withBag = mock(Participant.class);
        Backpack bag = mock(Backpack.class);
        when(withBag.getBackpack()).thenReturn(bag);

        Participant withoutBag = mock(Participant.class);
        when(withoutBag.getBackpack()).thenReturn(null);

        hike.getParticipants().add(withBag);
        hike.getParticipants().add(withoutBag);

        // When: On récupère la liste des sacs
        List<Backpack> backpacks = hike.getBackpacks();

        // Then: La liste ne doit contenir qu'un seul sac
        assertEquals(1, backpacks.size());
        assertEquals(bag, backpacks.getFirst());
    }
}