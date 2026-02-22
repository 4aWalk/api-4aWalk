package iut.rodez.projet.sae.fourawalkapi.entity;

import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test le comportement logique de l'entité Backpack, notamment les calculs de masse
 * et la gestion du contenu (nourriture et équipements).
 */
class BackpackTest {

    private Backpack backpack;
    private Participant mockOwner;

    @BeforeEach
    void setUp() {
        // Given: Un sac à dos initialisé avec un porteur mocké
        mockOwner = mock(Participant.class);
        backpack = new Backpack(mockOwner);
    }

    /**
     * Test le vidage complet du sac.
     */
    @Test
    void clearContent() {
        // Given: Un sac contenant déjà de la nourriture
        backpack.addItem(mock(FoodProduct.class));

        // When: On demande le nettoyage du contenu
        backpack.clearContent();

        // Then: Les collections doivent être vides et le poids réinitialisé
        assertTrue(backpack.getFoodItems().isEmpty());
        assertTrue(backpack.getGroupEquipments().isEmpty());
        assertEquals(0.0, backpack.getTotalMassKg());
    }

    /**
     * Test la mise à jour forcée de la masse totale.
     */
    @Test
    void updateTotalMass() {
        // Given: Un produit alimentaire pesant 2.5 kg ajouté au sac
        FoodProduct food = mock(FoodProduct.class);
        when(food.getTotalMassesKg()).thenReturn(2.5);
        backpack.addItem(food);

        // When: On déclenche manuellement la mise à jour de la masse
        backpack.updateTotalMass();

        // Then: La masse totale du sac doit correspondre à 2.5 kg
        assertEquals(2.5, backpack.getTotalMassKg());
    }

    /**
     * Test la récupération automatique de la masse totale via le getter.
     */
    @Test
    void getTotalMassKg() {
        // Given: Un produit pesant 1.2 kg présent dans le sac
        FoodProduct food = mock(FoodProduct.class);
        when(food.getTotalMassesKg()).thenReturn(1.2);
        backpack.addItem(food);

        // When: On interroge le getter pour obtenir la masse
        double totalMass = backpack.getTotalMassKg();

        // Then: Le getter doit retourner la valeur correcte après calcul interne
        assertEquals(1.2, totalMass);
    }

    /**
     * Test la récupération de la capacité maximale du sac basée sur le porteur.
     */
    @Test
    void getCapacityMaxKg() {
        // Given: Un porteur capable de porter 15.0 kg
        when(mockOwner.getCapaciteEmportMaxKg()).thenReturn(15.0);

        // When: On vérifie la capacité maximale du sac
        double capacity = backpack.getCapacityMaxKg();

        // Then: La valeur retournée doit être strictement identique à celle définie par le porteur
        assertEquals(15.0, capacity);
    }

    /**
     * Test la vérification de possibilité d'ajout de poids supplémentaire.
     */
    @Test
    void canAddWeightGrammes() {
        // Given: Un sac avec une capacité de 10kg contenant déjà 8kg de charge
        when(mockOwner.getCapaciteEmportMaxKg()).thenReturn(10.0);
        FoodProduct food = mock(FoodProduct.class);
        when(food.getTotalMassesKg()).thenReturn(8.0);
        backpack.addItem(food);

        // When & Then: On vérifie si 1.5kg (valide) et 2.5kg (invalide) peuvent être ajoutés
        assertTrue(backpack.canAddWeightGrammes(1500.0), "On devrait pouvoir ajouter 1.5kg");
        assertFalse(backpack.canAddWeightGrammes(2500.0), "On ne devrait pas pouvoir ajouter 2.5kg");
    }

    /**
     * Test la répartition correcte des items ajoutés selon leur type.
     */
    @Test
    void addItem() {
        // Given: Un produit alimentaire et un équipement de type REPOS
        FoodProduct food = mock(FoodProduct.class);
        EquipmentItem equipment = mock(EquipmentItem.class);
        when(equipment.getType()).thenReturn(TypeEquipment.REPOS);

        // When: On ajoute les deux items au sac
        backpack.addItem(food);
        backpack.addItem(equipment);

        // Then: La nourriture doit être dans le Set et l'équipement dans la Map des groupes
        assertTrue(backpack.getFoodItems().contains(food));
        assertTrue(backpack.getGroupEquipments().containsKey(TypeEquipment.REPOS));
    }

    /**
     * Test la suppression d'un item présent dans le sac.
     */
    @Test
    void removeItem() {
        // Given: Un sac contenant initialement un produit alimentaire
        FoodProduct food = mock(FoodProduct.class);
        backpack.addItem(food);

        // When: On retire ce produit du sac
        backpack.removeItem(food);

        // Then: Le produit ne doit plus figurer dans la liste des aliments
        assertFalse(backpack.getFoodItems().contains(food));
    }

    /**
     * Test le calcul de l'espace libre restant dans le sac.
     */
    @Test
    void getSpaceRemainingGrammes() {
        // Given: Un sac limité à 10kg contenant déjà 4kg
        when(mockOwner.getCapaciteEmportMaxKg()).thenReturn(10.0);
        FoodProduct food = mock(FoodProduct.class);
        when(food.getTotalMassesKg()).thenReturn(4.0);
        backpack.addItem(food);

        // When: On calcule l'espace restant
        double remaining = backpack.getSpaceRemainingGrammes();

        // Then: Le résultat doit être de 6000.0 grammes (10kg - 4kg)
        assertEquals(6000.0, remaining);
    }
}