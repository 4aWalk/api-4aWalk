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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du BackpackDistributorServiceV3.
 * Vérifie l'approche Branch & Bound, le Fail-Fast et l'élagage.
 */
class BackpackDistributorServiceV3Test {

    private BackpackDistributorServiceV3 distributorService;
    private BackpackService backpackServiceMock;
    private List<Backpack> backpacks;
    private List<Item> items;
    private long idCounter = 1L; // Compteur pour générer des IDs uniques évitant les NullPointerException

    @BeforeEach
    void setUp() {
        backpackServiceMock = mock(BackpackService.class);
        distributorService = new BackpackDistributorServiceV3(backpackServiceMock);
        backpacks = new ArrayList<>();
        items = new ArrayList<>();
        idCounter = 1L; // Réinitialisation à chaque test
    }

    // ==========================================
    // TESTS DU FLUX NOMINAL
    // ==========================================

    /**
     * Teste le flux nominal de répartition des équipements.
     * S'assure que si la capacité totale est suffisante et qu'aucun objet n'est bloquant,
     * l'algorithme distribue tout sans dépasser la capacité des sacs.
     */
    @Test
    void distributeBatches_NominalCase_ShouldDistributeProperly() {
        // Given : 2 sacs de 10 kg chacun (total 20 kg)
        backpacks.add(createTestBackpack("Alice", 10.0));
        backpacks.add(createTestBackpack("Bob", 10.0));

        // Objets pour un total de 18 kg
        items.add(createEquipmentItem(4000.0, 1, TypeEquipment.REPOS));
        items.add(createEquipmentItem(1000.0, 4, TypeEquipment.EAU));
        items.add(createEquipmentItem(5000.0, 2, TypeEquipment.AUTRE));

        // When : On lance la distribution (Ajout du hikeId factice 1L)
        assertDoesNotThrow(() -> distributorService.distributeBatchesToBackpacks(items, backpacks, 1L));

        // Then : Les sacs ne doivent pas dépasser leur capacité
        assertTrue(backpacks.get(0).getSpaceRemainingGrammes() >= 0);
        assertTrue(backpacks.get(1).getSpaceRemainingGrammes() >= 0);
    }

    // ==========================================
    // TESTS DU BRANCH & BOUND (PERFORMANCE ET LOGIQUE)
    // ==========================================

    /**
     * Teste l'efficacité du backtracking.
     * Vérifie que l'algorithme est capable de revenir sur ses choix initiaux (dépiler l'arbre)
     * pour trouver l'unique combinaison permettant un remplissage exact à 100%.
     */
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
        assertDoesNotThrow(() -> distributorService.distributeBatchesToBackpacks(items, backpacks, 1L));
    }

    /**
     * Test de performance validant le mécanisme d'élagage (Pruning).
     * En soumettant beaucoup de petits objets, l'arbre de recherche potentiel est gigantesque.
     * Le test s'assure que la condition d'élagage coupe les branches mortes et résout le problème en moins de 500ms.
     */
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
            distributorService.distributeBatchesToBackpacks(items, backpacks, 1L);
        });
    }

    /**
     * S'assure que si la liste d'équipements à traiter est vide, l'algorithme
     * commence bien par vider le contenu résiduel éventuel des sacs avant de terminer avec succès.
     */
    @Test
    void distributeBatches_EmptyItems_ShouldClearBackpacksAndPass() {
        // Given : Un sac de 5 kg
        Backpack b1 = createTestBackpack("Charlie", 5.0);

        // On simule qu'il y a déjà quelque chose dedans
        b1.addItem(createEquipmentItem(2000.0, 1, TypeEquipment.AUTRE));
        backpacks.add(b1);

        // When : On lance la distribution (items est vide)
        distributorService.distributeBatchesToBackpacks(items, backpacks, 1L);

        // Then : Le sac doit avoir été vidé et l'espace revenu à 5000 grammes
        assertEquals(5000.0, backpacks.getFirst().getSpaceRemainingGrammes());
        assertTrue(backpacks.getFirst().getGroupEquipments().isEmpty());
    }

    // ==========================================
    // TESTS DES CAS D'ERREUR ET FAIL-FAST
    // ==========================================

    /**
     * Teste le mécanisme de Fail-Fast initial.
     * Si le poids total des objets excède la capacité maximale totale de tous les sacs réunis,
     * la méthode doit lever une exception immédiatement sans chercher à résoudre le problème.
     */
    @Test
    void distributeBatches_FailFast_TotalCapacityInsufficient_ShouldThrowException() {
        // Given : 1 sac de 5 kg
        backpacks.add(createTestBackpack("Petit Porteur", 5.0));

        // Objets pour un total de 6 kg
        items.add(createEquipmentItem(6000.0, 1, TypeEquipment.REPOS));

        // When & Then : Le Fail-Fast (Pré-calcul) doit stopper le process immédiatement
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> distributorService.distributeBatchesToBackpacks(items, backpacks, 1L));
        assertTrue(ex.getMessage().contains("Le poids total dépasse la capacité max"));
    }

    /**
     * Vérifie que l'algorithme détecte l'impossibilité de placer un objet unique
     * dont le poids est supérieur à la capacité du plus grand sac individuel.
     */
    @Test
    void distributeBatches_BranchAndBound_ItemTooHeavy_ShouldThrowException() {
        // Given : 2 sacs de 5 kg (Capacité totale = 10 kg)
        backpacks.add(createTestBackpack("Porteur 1", 5.0));
        backpacks.add(createTestBackpack("Porteur 2", 5.0));

        // 1 seul objet indivisible de 6 kg (Capacité totale OK, mais aucun sac ne peut l'accepter)
        items.add(createEquipmentItem(6000.0, 1, TypeEquipment.REPOS));

        // When & Then : L'algorithme récursif échoue
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> distributorService.distributeBatchesToBackpacks(items, backpacks, 1L));
        assertTrue(ex.getMessage().contains("Objets trop volumineux pour l'espace des sacs disponibles"));
    }

    /**
     * Vérifie qu'un lot d'objets (plusieurs exemplaires traités comme une seule masse)
     * lève bien une exception s'il ne rentre dans aucun sac individuel.
     */
    @Test
    void distributeBatches_BatchTooHeavy_ShouldThrowException() {
        // Given : 2 sacs de 5 kg (Capacité totale = 10 kg)
        backpacks.add(createTestBackpack("Porteur 1", 5.0));
        backpacks.add(createTestBackpack("Porteur 2", 5.0));

        // Un lot indissociable de 6kg
        items.add(createEquipmentItem(1000.0, 6, TypeEquipment.EAU));

        // When & Then : Le pack ne rentre entier dans aucun sac individuel
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> distributorService.distributeBatchesToBackpacks(items, backpacks, 1L));
        assertTrue(ex.getMessage().contains("Objets trop volumineux pour l'espace des sacs disponibles"));
    }

    // ==========================================
    // NOUVEAU TEST : PRIORISATION DU PROPRIÉTAIRE
    // ==========================================

    /**
     * Teste la règle métier de placement prioritaire.
     * Pour les équipements de type Vêtement ou Repos, l'algorithme doit forcer
     * l'attribution dans le sac du propriétaire de l'équipement si celui-ci est défini.
     */
    @Test
    void distributeBatches_ShouldPrioritizeOwnerBackpackForClothesAndRest() {
        // Given : 2 sacs de 10 kg
        Backpack aliceBackpack = createTestBackpack("Alice", 10.0);
        aliceBackpack.getOwner().setId(101L);

        Backpack bobBackpack = createTestBackpack("Bob", 10.0);
        bobBackpack.getOwner().setId(102L);

        backpacks.add(aliceBackpack);
        backpacks.add(bobBackpack);

        // Un vêtement (1kg) qu'on attribue spécifiquement à Bob
        EquipmentItem veste = (EquipmentItem) createEquipmentItem(1000.0, 1, TypeEquipment.VETEMENT);
        veste.setId(99L);
        items.add(veste);

        // Mock du repository : La veste (ID 99) appartient à Bob (ID 102) sur la rando 1
        when(backpackServiceMock.getPreferredOwnerBackpack(eq(veste), eq(backpacks), eq(1L)))
                .thenReturn(bobBackpack);

        // When
        assertDoesNotThrow(() -> distributorService.distributeBatchesToBackpacks(items, backpacks, 1L));

        // Then : Même si Alice est la première dans la liste, l'algo doit avoir forcé le rangement chez Bob
        assertTrue(bobBackpack.getGroupEquipments().containsKey(TypeEquipment.VETEMENT));
        assertFalse(aliceBackpack.getGroupEquipments().containsKey(TypeEquipment.VETEMENT));
    }

    // ==========================================
    // UTILITAIRES DE TEST ADAPTÉS À L'ARCHITECTURE
    // ==========================================

    /**
     * Crée un faux équipement pour les tests.
     */
    private Item createEquipmentItem(double masseGrammes, int quantite, TypeEquipment type) {
        EquipmentItem item = new EquipmentItem();
        item.setMasseGrammes(masseGrammes);
        item.setNbItem(quantite);
        item.setType(type);
        return item;
    }

    /**
     * Crée un faux sac avec son propriétaire pour les tests.
     * Assigne automatiquement un identifiant unique (ID) au propriétaire pour éviter les NullPointerException.
     */
    private Backpack createTestBackpack(String ownerName, double capaciteMaxKg) {
        Participant owner = new Participant();
        owner.setId(idCounter++); // <-- CORRECTION DE L'ERREUR NPE ICI
        owner.setPrenom(ownerName);
        owner.setCapaciteEmportMaxKg(capaciteMaxKg);

        Backpack backpack = new Backpack();
        backpack.setOwner(owner);

        return backpack;
    }
}