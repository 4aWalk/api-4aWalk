package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Backpack;
import iut.rodez.projet.sae.fourawalkapi.entity.EquipmentItem;
import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.model.Item;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires du BackpackDistributorService.
 * Adapté à la vraie logique de l'entité Backpack (Capacité liée au Participant,
 * tri automatique via addItem).
 */
class BackpackDistributorServiceTest {

    private BackpackDistributorServiceV2 distributorService;
    private List<Backpack> backpacks;
    private List<Item> items;

    @BeforeEach
    void setUp() {
        distributorService = new BackpackDistributorServiceV2();
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

        // Objets pour un total de 18 kg (en grammes)
        items.add(createEquipmentItem(4000.0, 1, TypeEquipment.REPOS));
        items.add(createEquipmentItem(1000.0, 4, TypeEquipment.EAU)); // Poids total = 4000.0
        items.add(createEquipmentItem(5000.0, 2, TypeEquipment.AUTRE)); // Poids total = 10000.0

        // When : On lance la distribution
        assertDoesNotThrow(() -> distributorService.distributeBatchesToBackpacks(items, backpacks));

        // Then : Les sacs ne doivent pas dépasser leur capacité (Espace restant >= 0)
        assertTrue(backpacks.get(0).getSpaceRemainingGrammes() >= 0);
        assertTrue(backpacks.get(1).getSpaceRemainingGrammes() >= 0);
    }

    // ==========================================
    // TESTS DES CAS LIMITES (BACKTRACKING INTENSIF)
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
    void distributeBatches_EmptyItems_ShouldClearBackpacksAndPass() {
        // Given : Un sac de 5 kg
        Backpack b1 = createTestBackpack("Charlie", 5.0);

        // On simule qu'il y a déjà quelque chose dedans via la méthode métier
        b1.addItem(createEquipmentItem(2000.0, 1, TypeEquipment.AUTRE));
        backpacks.add(b1);

        // When : On lance la distribution (items est vide)
        distributorService.distributeBatchesToBackpacks(items, backpacks);

        // Then : Le sac doit avoir été vidé (clearContent) et l'espace doit être revenu à 5000 grammes (5kg)
        assertEquals(5000.0, backpacks.getFirst().getSpaceRemainingGrammes());
        assertTrue(backpacks.getFirst().getGroupEquipments().isEmpty()); // Vérifie que la map est bien vide
    }

    // ==========================================
    // TESTS DES CAS D'ERREUR
    // ==========================================

    @Test
    void distributeBatches_TotalCapacityInsufficient_ShouldThrowException() {
        // Given : 1 sac de 5 kg
        backpacks.add(createTestBackpack("Petit Porteur", 5.0));

        // Objets pour un total de 6 kg
        items.add(createEquipmentItem(6000.0, 1, TypeEquipment.REPOS));

        // When & Then : La répartition est impossible
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> distributorService.distributeBatchesToBackpacks(items, backpacks));
        assertTrue(ex.getMessage().contains("Répartition impossible"));
    }

    @Test
    void distributeBatches_ItemTooHeavyForSingleBackpack_ShouldThrowException() {
        // Given : 2 sacs de 5 kg (Capacité totale = 10 kg)
        backpacks.add(createTestBackpack("Porteur 1", 5.0));
        backpacks.add(createTestBackpack("Porteur 2", 5.0));

        // 1 seul objet indivisible de 6 kg.
        items.add(createEquipmentItem(6000.0, 1, TypeEquipment.REPOS));

        // When & Then : L'objet ne peut être coupé en deux !
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> distributorService.distributeBatchesToBackpacks(items, backpacks));
        assertTrue(ex.getMessage().contains("Répartition impossible"));
    }

    @Test
    void distributeBatches_BatchTooHeavy_ShouldThrowException() {
        // Given : 2 sacs de 5 kg (Capacité totale = 10 kg)
        backpacks.add(createTestBackpack("Porteur 1", 5.0));
        backpacks.add(createTestBackpack("Porteur 2", 5.0));

        // Un lot indissociable de 6 gourdes d'1kg (le fameux pack sous film plastique !)
        items.add(createEquipmentItem(1000.0, 6, TypeEquipment.EAU));

        // When & Then : Le pack ne rentre entier dans aucun sac individuel
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> distributorService.distributeBatchesToBackpacks(items, backpacks));
        assertTrue(ex.getMessage().contains("Répartition impossible"));
    }

    // ==========================================
    // UTILITAIRES DE TEST ADAPTÉS À L'ARCHITECTURE
    // ==========================================

    /**
     * Crée un faux équipement.
     * Le TypeEquipment est obligatoire pour éviter un NullPointerException
     * lors du "computeIfAbsent" dans le addItem du Backpack.
     */
    private Item createEquipmentItem(double masseGrammes, int quantite, TypeEquipment type) {
        EquipmentItem item = new EquipmentItem();
        item.setMasseGrammes(masseGrammes);
        item.setNbItem(quantite);
        item.setType(type);
        return item;
    }

    /**
     * Crée un sac à dos valide en passant par son Participant (Owner).
     * @param ownerName Nom du participant
     * @param capaciteMaxKg Capacité de portage du participant en Kg
     */
    private Backpack createTestBackpack(String ownerName, double capaciteMaxKg) {
        // 1. Création du propriétaire
        Participant owner = new Participant();
        owner.setPrenom(ownerName);
        owner.setCapaciteEmportMaxKg(capaciteMaxKg);

        // 2. Création du sac et liaison
        Backpack backpack = new Backpack();
        backpack.setOwner(owner); // Le getCapacityMaxKg du sac tapera là-dedans !

        return backpack;
    }
}