package iut.rodez.projet.sae.fourawalkapi.entity;

import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test le comportement de l'entité GroupEquipment, notamment l'agrégation
 * d'items et le calcul de la masse cumulée par catégorie.
 */
class GroupEquipmentTest {

    private GroupEquipment groupEquipment;

    @BeforeEach
    void setUp() {
        // Given: Un groupe d'équipement initialisé pour une catégorie spécifique
        groupEquipment = new GroupEquipment(TypeEquipment.PROGRESSION);
    }

    /**
     * Test l'ajout d'un équipement dans la liste du groupe.
     */
    @Test
    void addItem() {
        // Given: Un équipement mocké correspondant au type du groupe
        EquipmentItem mockItem = mock(EquipmentItem.class);

        // When: On ajoute l'item au groupe
        groupEquipment.addItem(mockItem);

        // Then: La liste des items doit contenir l'élément et sa taille doit être de 1
        List<EquipmentItem> items = groupEquipment.getItems();
        assertFalse(items.isEmpty(), "La liste ne devrait pas être vide après l'ajout");
        assertEquals(1, items.size(), "La liste devrait contenir exactement un item");
        assertEquals(mockItem, items.get(0), "L'item récupéré doit être celui qui a été ajouté");
    }

    /**
     * Test le calcul de la masse totale de tous les items du groupe.
     */
    @Test
    void getTotalMassesKg() {
        // Given: Deux équipements mockés ayant des masses respectives de 0.5kg et 1.2kg
        EquipmentItem item1 = mock(EquipmentItem.class);
        EquipmentItem item2 = mock(EquipmentItem.class);

        when(item1.getTotalMassesKg()).thenReturn(0.5);
        when(item2.getTotalMassesKg()).thenReturn(1.2);

        groupEquipment.addItem(item1);
        groupEquipment.addItem(item2);

        // When: On calcule la masse totale du groupe
        double totalMass = groupEquipment.getTotalMassesKg();

        // Then: La somme retournée doit être de 1.7kg
        assertEquals(1.7, totalMass, 0.001, "La masse totale calculée est incorrecte");
    }
}