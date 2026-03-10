package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Backpack;
import iut.rodez.projet.sae.fourawalkapi.entity.EquipmentItem;
import iut.rodez.projet.sae.fourawalkapi.entity.FoodProduct;
import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.BelongEquipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Classe de test pour BackpackService.
 * Vérifie l'attribution logique des sacs à dos en fonction du type d'équipement
 * (restriction aux vêtements et au matériel de repos) et du propriétaire assigné.
 */
class BackpackServiceTest {

    private BelongEquipmentRepository belongEquipmentRepository;
    private BackpackService backpackService;

    private EquipmentItem mockEquipment;
    private Backpack targetBackpack;
    private List<Backpack> backpacks;
    private final Long HIKE_ID = 100L;
    private final Long OWNER_ID = 10L;

    @BeforeEach
    void setUp() {
        belongEquipmentRepository = mock(BelongEquipmentRepository.class);
        backpackService = new BackpackService(belongEquipmentRepository);

        // --- Préparation du propriétaire ---
        Participant owner = mock(Participant.class);
        when(owner.getId()).thenReturn(OWNER_ID);

        Participant otherParticipant = mock(Participant.class);
        when(otherParticipant.getId()).thenReturn(99L);

        // --- Préparation des sacs à dos ---
        targetBackpack = mock(Backpack.class);
        when(targetBackpack.getOwner()).thenReturn(owner);

        Backpack otherBackpack = mock(Backpack.class);
        when(otherBackpack.getOwner()).thenReturn(otherParticipant);

        backpacks = List.of(otherBackpack, targetBackpack);

        // --- Préparation de l'équipement de base ---
        mockEquipment = new EquipmentItem();
        mockEquipment.setId(500L);
    }

    // ==========================================
    // TESTS : SUCCÈS (RECHERCHE FRUCTUEUSE)
    // ==========================================

    /**
     * Teste que la méthode retourne bien le sac du propriétaire si l'équipement
     * est un vêtement et qu'il est correctement assigné.
     */
    @Test
    void getPreferredOwnerBackpack_Success_WithVetement() {
        // Given : Un équipement de type VÊTEMENT et un propriétaire connu
        mockEquipment.setType(TypeEquipment.VETEMENT);
        when(belongEquipmentRepository.getIfExistParticipantForEquipmentAndHike(HIKE_ID, mockEquipment.getId()))
                .thenReturn(OWNER_ID);

        // When : On demande à trouver le sac préférentiel
        Backpack result = backpackService.getPreferredOwnerBackpack(mockEquipment, backpacks, HIKE_ID);

        // Then : Le sac du propriétaire est renvoyé
        assertNotNull(result);
        assertEquals(targetBackpack, result);
        verify(belongEquipmentRepository).getIfExistParticipantForEquipmentAndHike(HIKE_ID, mockEquipment.getId());
    }

    /**
     * Teste que la méthode retourne bien le sac du propriétaire si l'équipement
     * est du matériel de repos et qu'il est correctement assigné.
     */
    @Test
    void getPreferredOwnerBackpack_Success_WithRepos() {
        // Given : Un équipement de type REPOS et un propriétaire connu
        mockEquipment.setType(TypeEquipment.REPOS);
        when(belongEquipmentRepository.getIfExistParticipantForEquipmentAndHike(HIKE_ID, mockEquipment.getId()))
                .thenReturn(OWNER_ID);

        // When : On demande à trouver le sac préférentiel
        Backpack result = backpackService.getPreferredOwnerBackpack(mockEquipment, backpacks, HIKE_ID);

        // Then : Le sac du propriétaire est renvoyé
        assertNotNull(result);
        assertEquals(targetBackpack, result);
        verify(belongEquipmentRepository).getIfExistParticipantForEquipmentAndHike(HIKE_ID, mockEquipment.getId());
    }

    // ==========================================
    // TESTS : ÉCHECS ET CAS AUX LIMITES
    // ==========================================

    /**
     * Teste que l'algorithme ignore les équipements dont le type (ex: EAU)
     * n'est pas censé forcer une appartenance stricte à un sac.
     */
    @Test
    void getPreferredOwnerBackpack_WrongType_ReturnsNull() {
        // Given : Un équipement dont le type EAU ne nécessite pas le sac de son propriétaire
        mockEquipment.setType(TypeEquipment.EAU);

        // When : On tente de lui attribuer un sac préférentiel
        Backpack result = backpackService.getPreferredOwnerBackpack(mockEquipment, backpacks, HIKE_ID);

        // Then : La méthode retourne null (aucun sac préférentiel défini)
        assertNull(result);

        // Sécurité/Optimisation : la base de données ne doit même pas être interrogée
        verify(belongEquipmentRepository, never()).getIfExistParticipantForEquipmentAndHike(anyLong(), anyLong());
    }

    /**
     * Teste le cas où l'item fourni n'est pas un équipement (ex: c'est un FoodProduct direct).
     */
    @Test
    void getPreferredOwnerBackpack_NotAnEquipmentItem_ReturnsNull() {
        // Given : Un objet de type FoodProduct, qui n'est pas un EquipmentItem
        FoodProduct foodItem = new FoodProduct();
        foodItem.setId(300L);

        // When : On demande un sac préférentiel pour cet item
        Backpack result = backpackService.getPreferredOwnerBackpack(foodItem, backpacks, HIKE_ID);

        // Then : La méthode retourne null sans faire d'appel au repo
        assertNull(result);
        verify(belongEquipmentRepository, never()).getIfExistParticipantForEquipmentAndHike(anyLong(), anyLong());
    }

    /**
     * Teste le comportement si un équipement éligible n'a pas encore de propriétaire assigné en BDD.
     */
    @Test
    void getPreferredOwnerBackpack_NoOwnerAssigned_ReturnsNull() {
        // Given : Un vêtement, mais qui n'a pas de propriétaire en base
        mockEquipment.setType(TypeEquipment.VETEMENT);
        when(belongEquipmentRepository.getIfExistParticipantForEquipmentAndHike(HIKE_ID, mockEquipment.getId()))
                .thenReturn(null);

        // When : On recherche son sac
        Backpack result = backpackService.getPreferredOwnerBackpack(mockEquipment, backpacks, HIKE_ID);

        // Then : Impossible de trouver un sac, retour de null
        assertNull(result);
    }

    /**
     * Teste le cas où le propriétaire de l'équipement est bien trouvé en base,
     * mais qu'il ne possède aucun sac dans la liste qui nous a été fournie.
     */
    @Test
    void getPreferredOwnerBackpack_OwnerNotInBackpackList_ReturnsNull() {
        // Given : Un équipement de repos appartenant à un participant (ID 777L)
        mockEquipment.setType(TypeEquipment.REPOS);
        when(belongEquipmentRepository.getIfExistParticipantForEquipmentAndHike(HIKE_ID, mockEquipment.getId()))
                .thenReturn(777L);

        // When : On cherche le sac de cet utilisateur (qui n'est pas dans la liste 'backpacks')
        Backpack result = backpackService.getPreferredOwnerBackpack(mockEquipment, backpacks, HIKE_ID);

        // Then : Le sac n'est pas trouvé dans la liste, on retourne null
        assertNull(result);
    }
}