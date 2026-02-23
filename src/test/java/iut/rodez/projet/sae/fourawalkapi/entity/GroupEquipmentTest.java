package iut.rodez.projet.sae.fourawalkapi.entity;

import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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