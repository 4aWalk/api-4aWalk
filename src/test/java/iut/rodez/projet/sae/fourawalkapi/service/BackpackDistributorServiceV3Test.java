package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Backpack;
import iut.rodez.projet.sae.fourawalkapi.entity.EquipmentItem;
import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.model.Item;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires du BackpackDistributorServiceV3.
 * Vérifie l'approche Branch & Bound, le Fail-Fast et l'élagage.
 */
class BackpackDistributorServiceV3Test {

    private BackpackDistributorServiceV3 distributorService;
    private List<Backpack> backpacks;
    private List<Item> items;

    @BeforeEach
    void setUp() {
        distributorService = new BackpackDistributorServiceV3();
        backpacks = new ArrayList<>();
        items = new ArrayList<>();
    }

    // ==========================================
    // TESTS DU FLUX NOMINAL
    // ==========================================

    @Test
    void distributeBatches_NominalCase_ShouldDistributeProperly() {
        // Given : 2 sacs de 10 kg chacun (total 20 kg)
        backpacks.add(createTestBackpack("Alice", 10.0));
        backpacks.add(createTestBackpack("Bob", 10.0));

        // Objets pour un total de 18 kg
        items.add(createEquipmentItem(4000.0, 1, TypeEquipment.REPOS));
        items.add(createEquipmentItem(1000.0, 4, TypeEquipment.EAU));
        items.add(createEquipmentItem(5000.0, 2, TypeEquipment.AUTRE));

        // When : On lance la distribution
        assertDoesNotThrow(() -> distributorService.distributeBatchesToBackpacks(items, backpacks));

        // Then : Les sacs ne doivent pas dépasser leur capacité
        assertTrue(backpacks.get(0).getSpaceRemainingGrammes() >= 0);
        assertTrue(backpacks.get(1).getSpaceRemainingGrammes() >= 0);
    }

    // ==========================================
    // TESTS DU BRANCH & BOUND (PERFORMANCE ET LOGIQUE)
    // ==========================================

    @Test
    void distributeBatches_ComplexBacktracking_ShouldFindSolution() {
        // Given : 2 sacs de 10 kg. Total = 20 kg.
        backpacks.add(createTestBackpack("Alice", 10.0));
        backpacks.add(createTestBackpack("Bob", 10.0));

        // Objets : 6kg, 5kg, 5kg, 4kg (Total = 20kg). Remplissage à 100%.
        items.add(createEquipmentItem(6000.0, 1, TypeEquipment.REPOS));
        items.add(createEquipmentItem(5000.0, 1, TypeEquipment.AUTRE));
        items.add(createEquipmentItem(5000.0, 1, TypeEquipment.REPOS));
        items.add(createEquipmentItem(4000.0, 1, TypeEquipment.PROGRESSION));

        // When & Then : L'algorithme doit réussir à trouver la combinaison parfaite
        assertDoesNotThrow(() -> distributorService.distributeBatchesToBackpacks(items, backpacks));
    }

    @Test
    void distributeBatches_PerformanceTest_ShouldResolveQuicklyWithPruning() {
        // Given : 3 sacs de 15 kg (Total = 45 kg)
        backpacks.add(createTestBackpack("P1", 15.0));
        backpacks.add(createTestBackpack("P2", 15.0));
        backpacks.add(createTestBackpack("P3", 15.0));

        // On ajoute beaucoup de petits objets pour forcer l'arbre de recherche à grandir
        // 40 objets de 1 kg (Total = 40 kg)
        for (int i = 0; i < 40; i++) {
            items.add(createEquipmentItem(1000.0, 1, TypeEquipment.AUTRE));
        }

        // When & Then : La résolution (grâce au Branch & Bound) doit être quasi-instantanée (< 500ms)
        assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
            distributorService.distributeBatchesToBackpacks(items, backpacks);
        });
    }

    @Test
    void distributeBatches_EmptyItems_ShouldClearBackpacksAndPass() {
        // Given : Un sac de 5 kg
        Backpack b1 = createTestBackpack("Charlie", 5.0);

        // On simule qu'il y a déjà quelque chose dedans
        b1.addItem(createEquipmentItem(2000.0, 1, TypeEquipment.AUTRE));
        backpacks.add(b1);

        // When : On lance la distribution (items est vide)
        distributorService.distributeBatchesToBackpacks(items, backpacks);

        // Then : Le sac doit avoir été vidé et l'espace revenu à 5000 grammes
        assertEquals(5000.0, backpacks.getFirst().getSpaceRemainingGrammes());
        assertTrue(backpacks.getFirst().getGroupEquipments().isEmpty());
    }

    // ==========================================
    // TESTS DES CAS D'ERREUR ET FAIL-FAST
    // ==========================================

    @Test
    void distributeBatches_FailFast_TotalCapacityInsufficient_ShouldThrowException() {
        // Given : 1 sac de 5 kg
        backpacks.add(createTestBackpack("Petit Porteur", 5.0));

        // Objets pour un total de 6 kg
        items.add(createEquipmentItem(6000.0, 1, TypeEquipment.REPOS));

        // When & Then : Le Fail-Fast (Pré-calcul) doit stopper le process immédiatement
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> distributorService.distributeBatchesToBackpacks(items, backpacks));
        assertTrue(ex.getMessage().contains("Le poids total dépasse la capacité max"));
    }

    @Test
    void distributeBatches_BranchAndBound_ItemTooHeavy_ShouldThrowException() {
        // Given : 2 sacs de 5 kg (Capacité totale = 10 kg)
        backpacks.add(createTestBackpack("Porteur 1", 5.0));
        backpacks.add(createTestBackpack("Porteur 2", 5.0));

        // 1 seul objet indivisible de 6 kg (Capacité totale OK, mais aucun sac ne peut l'accepter)
        items.add(createEquipmentItem(6000.0, 1, TypeEquipment.REPOS));

        // When & Then : L'algorithme récursif échoue
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> distributorService.distributeBatchesToBackpacks(items, backpacks));
        assertTrue(ex.getMessage().contains("Objets trop volumineux pour l'espace des sacs disponibles"));
    }

    @Test
    void distributeBatches_BatchTooHeavy_ShouldThrowException() {
        // Given : 2 sacs de 5 kg (Capacité totale = 10 kg)
        backpacks.add(createTestBackpack("Porteur 1", 5.0));
        backpacks.add(createTestBackpack("Porteur 2", 5.0));

        // Un lot indissociable de 6kg
        items.add(createEquipmentItem(1000.0, 6, TypeEquipment.EAU));

        // When & Then : Le pack ne rentre entier dans aucun sac individuel
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> distributorService.distributeBatchesToBackpacks(items, backpacks));
        assertTrue(ex.getMessage().contains("Objets trop volumineux pour l'espace des sacs disponibles"));
    }

    // ==========================================
    // UTILITAIRES DE TEST ADAPTÉS À L'ARCHITECTURE
    // ==========================================

    private Item createEquipmentItem(double masseGrammes, int quantite, TypeEquipment type) {
        EquipmentItem item = new EquipmentItem();
        item.setMasseGrammes(masseGrammes);
        item.setNbItem(quantite);
        item.setType(type);
        return item;
    }

    private Backpack createTestBackpack(String ownerName, double capaciteMaxKg) {
        Participant owner = new Participant();
        owner.setPrenom(ownerName);
        owner.setCapaciteEmportMaxKg(capaciteMaxKg);

        Backpack backpack = new Backpack();
        backpack.setOwner(owner);

        return backpack;
    }
}