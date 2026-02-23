package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.EquipmentItem;
import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.entity.User;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.EquipmentItemRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.HikeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Classe de test pour EquipmentService.
 * Valide les règles métier de gestion du matériel (poids limites),
 * ainsi que la sécurité des opérations d'association entre un équipement et une randonnée.
 */
class EquipmentServiceTest {

    private EquipmentItemRepository equipmentRepository;
    private HikeRepository hikeRepository;
    private EquipmentService equipmentService;

    private Hike mockHike;
    private EquipmentItem mockEquipment;

    /**
     * Préparation du contexte de test (Mocks et données factices) avant chaque exécution.
     */
    @BeforeEach
    void setUp() {
        equipmentRepository = mock(EquipmentItemRepository.class);
        hikeRepository = mock(HikeRepository.class);
        equipmentService = new EquipmentService(equipmentRepository, hikeRepository);

        // --- Utilisateur propriétaire ---
        User creatorUser = new User();
        creatorUser.setId(10L);

        // --- Randonnée cible ---
        mockHike = mock(Hike.class); // On mock la Hike pour vérifier l'appel à addEquipment (DDD)
        when(mockHike.getId()).thenReturn(100L);
        when(mockHike.getCreator()).thenReturn(creatorUser);

        // --- Équipement standard ---
        mockEquipment = new EquipmentItem();
        mockEquipment.setId(500L);
        mockEquipment.setMasseGrammes(1000); // 1kg (Poids valide)
    }

    // ==========================================
    // TESTS : CRÉATION ET VALIDATION (RÈGLES MÉTIER)
    // ==========================================

    /**
     * Teste la création d'un équipement valide (poids compris entre 50g et 5kg).
     */
    @Test
    void createEquipment_Success() {
        // Given : Un équipement avec un poids valide (1000g)
        when(equipmentRepository.save(any(EquipmentItem.class))).thenReturn(mockEquipment);

        // When : On tente de le créer
        EquipmentItem result = equipmentService.createEquipment(mockEquipment);

        // Then : La validation passe et le repository est appelé
        assertNotNull(result);
        verify(equipmentRepository).save(mockEquipment);
    }

    /**
     * Vérifie la limite basse : un équipement ne peut pas peser moins de 50g.
     */
    @Test
    void createEquipment_TooLight_ThrowsException() {
        // Given : Un équipement de 49g (sous la limite)
        EquipmentItem lightItem = new EquipmentItem();
        lightItem.setMasseGrammes(49);

        // When & Then : La création est bloquée par la validation métier
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> equipmentService.createEquipment(lightItem));
        assertTrue(ex.getMessage().contains("entre 50g et 5kg"));

        // Sécurité : On s'assure que l'équipement invalide ne touche jamais la base
        verify(equipmentRepository, never()).save(any());
    }

    /**
     * Vérifie la limite haute : un équipement ne peut pas peser plus de 5kg.
     */
    @Test
    void createEquipment_TooHeavy_ThrowsException() {
        // Given : Un équipement de 5001g (au-dessus de la limite)
        EquipmentItem heavyItem = new EquipmentItem();
        heavyItem.setMasseGrammes(5001);

        // When & Then : L'exception est levée
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> equipmentService.createEquipment(heavyItem));
        assertTrue(ex.getMessage().contains("entre 50g et 5kg"));

        verify(equipmentRepository, never()).save(any());
    }

    // ==========================================
    // TESTS : LECTURE ET SUPPRESSION
    // ==========================================

    /**
     * Teste la récupération du catalogue complet.
     */
    @Test
    void getAllEquipment_Success() {
        // Given : Une base contenant 2 équipements
        when(equipmentRepository.findAll()).thenReturn(List.of(new EquipmentItem(), new EquipmentItem()));

        // When
        List<EquipmentItem> result = equipmentService.getAllEquipment();

        // Then
        assertEquals(2, result.size());
        verify(equipmentRepository).findAll();
    }

    /**
     * Teste la suppression d'un équipement.
     */
    @Test
    void deleteEquipment_Success() {
        // Given : L'ID d'un équipement (500L)

        // When : On appelle la suppression
        equipmentService.deleteEquipment(500L);

        // Then : Le repository reçoit bien l'ordre de suppression
        verify(equipmentRepository).deleteById(500L);
    }

    // ==========================================
    // TESTS : AJOUT AU SAC À DOS (ASSOCIATION)
    // ==========================================

    /**
     * Teste le cas passant d'un ajout d'équipement dans une randonnée par son propriétaire.
     */
    @Test
    void addEquipmentToHike_Success() {
        // Given : La randonnée et l'équipement existent en base
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));
        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(mockEquipment));

        // When : Le propriétaire (ID=10L) ajoute l'équipement
        equipmentService.addEquipmentToHike(100L, 500L, 10L);

        // Then : La logique DDD est respectée (appel de hike.addEquipment) et la rando est sauvegardée
        verify(mockHike).addEquipment(mockEquipment);
        verify(hikeRepository).save(mockHike);
    }

    /**
     * Vérifie la faille de sécurité (IDOR) : un utilisateur ne peut pas modifier la rando d'un autre.
     */
    @Test
    void addEquipmentToHike_WrongUser_ThrowsException() {
        // Given : Une randonnée appartenant à l'utilisateur 10L
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));

        // When & Then : L'utilisateur 99L essaie d'ajouter un équipement -> Accès refusé
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> equipmentService.addEquipmentToHike(100L, 500L, 99L));
        assertEquals("Accès refusé : Vous n'êtes pas le propriétaire de cette randonnée", ex.getMessage());

        // La méthode interne de l'entité ne doit jamais être appelée
        verify(mockHike, never()).addEquipment(any());
    }

    /**
     * Vérifie le comportement si l'équipement demandé n'existe pas dans le catalogue.
     */
    @Test
    void addEquipmentToHike_EquipmentNotFound_ThrowsException() {
        // Given : La randonnée existe, mais l'ID de l'équipement (999L) n'existe pas
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));
        when(equipmentRepository.findById(999L)).thenReturn(Optional.empty()); // Introuvable

        // When & Then : La transaction est interrompue
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> equipmentService.addEquipmentToHike(100L, 999L, 10L));
        assertEquals("Équipement introuvable", ex.getMessage());

        verify(hikeRepository, never()).save(any());
    }

    // ==========================================
    // TESTS : RETRAIT DU SAC À DOS (DISSOCIATION)
    // ==========================================

    /**
     * Teste le cas passant du retrait d'un équipement du sac à dos.
     */
    @Test
    void removeEquipmentFromHike_Success() {
        // Given : La randonnée et l'équipement existent
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));
        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(mockEquipment));

        // When : Le propriétaire retire l'équipement
        equipmentService.removeEquipmentFromHike(100L, 500L, 10L);

        // Then : L'entité gère le retrait, puis l'état est sauvegardé
        verify(mockHike).removeEquipment(mockEquipment);
        verify(hikeRepository).save(mockHike);
    }

    /**
     * Vérifie la sécurité lors du retrait d'un équipement.
     */
    @Test
    void removeEquipmentFromHike_WrongUser_ThrowsException() {
        // Given
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));

        // When & Then : Utilisateur non autorisé
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> equipmentService.removeEquipmentFromHike(100L, 500L, 99L));
        assertEquals("Accès refusé", ex.getMessage());
    }
}