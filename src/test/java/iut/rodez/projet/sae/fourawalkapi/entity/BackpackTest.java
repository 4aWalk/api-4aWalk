package iut.rodez.projet.sae.fourawalkapi.entity;

import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test le comportement logique de l'entité Backpack, incluant les cas nominaux,
 * les limites de capacité et la robustesse face aux données absentes.
 */
class BackpackTest {

    private Backpack backpack;
    private Participant mockOwner;

    @BeforeEach
    void setUp() {
        // Given: Un sac à dos lié à un participant dont la capacité est par défaut de 10kg
        mockOwner = mock(Participant.class);
        when(mockOwner.getCapaciteEmportMaxKg()).thenReturn(10.0);
        backpack = new Backpack(mockOwner);
    }

    /**
     * Test le vidage d'un sac déjà vide.
     */
    @Test
    void clearContent_AlreadyEmpty() {
        // Given: Un sac n'ayant aucun contenu
        assertTrue(backpack.getFoodItems().isEmpty());

        // When: On tente de vider le sac
        backpack.clearContent();

        // Then: Le sac reste vide sans générer d'erreur
        assertTrue(backpack.getFoodItems().isEmpty());
        assertEquals(0.0, backpack.getTotalMassKg());
    }

    /**
     * Test la mise à jour de la masse sur un sac vide.
     */
    @Test
    void updateTotalMass_EmptyBackpack() {
        // Given: Un sac vide

        // When: On force la mise à jour de la masse
        backpack.updateTotalMass();

        // Then: La masse calculée doit être exactement 0.0
        assertEquals(0.0, backpack.getTotalMassKg());
    }

    /**
     * Test la récupération de capacité quand le participant est absent.
     */
    @Test
    void getCapacityMaxKg_NoOwner() {
        // Given: Un sac sans propriétaire (owner = null)
        Backpack ghostBackpack = new Backpack(null);

        // When: On interroge la capacité maximale
        double capacity = ghostBackpack.getCapacityMaxKg();

        // Then: La capacité doit être de 0.0 au lieu de lever une exception
        assertEquals(0.0, capacity);
    }

    /**
     * Test l'ajout d'items multiples et le calcul du poids cumulé.
     */
    @Test
    void addItem_And_CheckTotalMass() {
        // Given: Un item de nourriture (1.5kg) et un équipement de REPOS (2kg)
        FoodProduct food = mock(FoodProduct.class);
        when(food.getTotalMassesKg()).thenReturn(1.5);

        EquipmentItem equip = mock(EquipmentItem.class);
        when(equip.getType()).thenReturn(TypeEquipment.REPOS);
        when(equip.getTotalMassesKg()).thenReturn(2.0);

        // When: On ajoute les deux au sac
        backpack.addItem(food);
        backpack.addItem(equip);

        // Then: La masse totale doit être de 3.5kg
        assertEquals(3.5, backpack.getTotalMassKg());
    }

    /**
     * Test le retrait d'un objet qui n'a jamais été ajouté au sac.
     */
    @Test
    void removeItem_NotPresent() {
        // Given: Un sac contenant une pomme, et un objet "Banane" non présent
        FoodProduct apple = mock(FoodProduct.class);
        FoodProduct banana = mock(FoodProduct.class);
        backpack.addItem(apple);

        // When / Then: On tente de retirer la banane, cela ne doit pas lever d'exception
        assertDoesNotThrow(() -> backpack.removeItem(banana));
        assertEquals(1, backpack.getFoodItems().size());
    }

    /**
     * Test la suppression d'un équipement dont le type n'existe pas dans le sac.
     */
    @Test
    void removeEquipmentItem_TypeNotPresent() {
        // Given: Un sac vide de tout équipement
        EquipmentItem medic = mock(EquipmentItem.class);
        when(medic.getType()).thenReturn(TypeEquipment.SOIN);

        // When / Then: On tente de retirer un soin, l'application doit rester stable
        assertDoesNotThrow(() -> backpack.removeItem(medic),
                "La suppression d'un type d'équipement absent ne doit pas lancer de NullPointerException");
    }

    /**
     * Test le calcul de l'espace restant avec une charge dépassant la capacité.
     */
    @Test
    void getSpaceRemainingGrammes_NegativeSpace() {
        // Given: Un sac de 10kg chargé avec 12kg (cas de surcharge possible via setters)
        when(mockOwner.getCapaciteEmportMaxKg()).thenReturn(10.0);
        FoodProduct heavyLoad = mock(FoodProduct.class);
        when(heavyLoad.getTotalMassesKg()).thenReturn(12.0);
        backpack.addItem(heavyLoad);

        // When: On vérifie l'espace restant
        double remaining = backpack.getSpaceRemainingGrammes();

        // Then: Le résultat doit être négatif (-2000g)
        assertEquals(-2000.0, remaining);
    }
}