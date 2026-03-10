package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.*;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.BelongEquipmentRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.EquipmentItemRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.HikeRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.ParticipantRepository;
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
    private ParticipantRepository participantRepository;
    private BelongEquipmentRepository belongEquipmentRepository;

    private Hike mockHike;
    private EquipmentItem mockEquipment;

    /**
     * Préparation du contexte de test (Mocks et données factices) avant chaque exécution.
     */
    @BeforeEach
    void setUp() {
        equipmentRepository = mock(EquipmentItemRepository.class);
        hikeRepository = mock(HikeRepository.class);
        participantRepository = mock(ParticipantRepository.class);
        belongEquipmentRepository = mock(BelongEquipmentRepository.class);

        // Injection des mocks dans le service
        equipmentService = new EquipmentService(equipmentRepository, hikeRepository, participantRepository, belongEquipmentRepository);

        // --- Utilisateur propriétaire ---
        User creatorUser = new User();
        creatorUser.setId(10L);

        // --- Randonnée cible ---
        mockHike = mock(Hike.class);
        when(mockHike.getId()).thenReturn(100L);
        when(mockHike.getCreator()).thenReturn(creatorUser);

        // --- Équipement standard ---
        mockEquipment = new EquipmentItem();
        mockEquipment.setId(500L);
        mockEquipment.setNom("Tente");
        mockEquipment.setMasseGrammes(1000);
        mockEquipment.setNbItem(1);
        mockEquipment.setMasseAVide(0);
    }

    /**
     * Crée un équipement valide de base pour permettre de tester
     * l'échec d'une règle spécifique sans être bloqué par les autres.
     * @return un objet EquipmentItem valide
     */
    private EquipmentItem createBaseValidEquipment() {
        EquipmentItem item = new EquipmentItem();
        item.setNom("Equipement Test");
        item.setMasseGrammes(1000);
        item.setNbItem(1);
        item.setMasseAVide(0);
        return item;
    }

    // ==========================================
    // TESTS : CRÉATION ET VALIDATION (RÈGLES MÉTIER)
    // ==========================================

    /**
     * Teste la création d'un équipement valide (poids compris entre 50g et 5kg).
     */
    @Test
    void createEquipment_Success() {
        // Given : Un équipement avec des valeurs valides
        when(equipmentRepository.save(any(EquipmentItem.class))).thenReturn(mockEquipment);

        // When : On tente de le créer
        EquipmentItem result = equipmentService.createEquipment(mockEquipment);

        // Then : La validation passe et le repository est appelé
        assertNotNull(result);
        verify(equipmentRepository).save(mockEquipment);
    }

    /**
     * Vérifie que le nom est obligatoire.
     */
    @Test
    void createEquipment_NameMissing_ThrowsException() {
        // Given : Un équipement sans nom
        EquipmentItem invalidItem = createBaseValidEquipment();
        invalidItem.setNom(null);

        // When & Then : L'exception est levée
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> equipmentService.createEquipment(invalidItem));
        assertTrue(ex.getMessage().contains("Le nom d'un équipement est obligatoire"));

        verify(equipmentRepository, never()).save(any());
    }

    /**
     * Vérifie la limite basse : un équipement ne peut pas peser moins de 50g.
     */
    @Test
    void createEquipment_TooLight_ThrowsException() {
        // Given : Un équipement de 49g (sous la limite)
        EquipmentItem lightItem = createBaseValidEquipment();
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
        EquipmentItem heavyItem = createBaseValidEquipment();
        heavyItem.setMasseGrammes(5001);

        // When & Then : L'exception est levée
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> equipmentService.createEquipment(heavyItem));
        assertTrue(ex.getMessage().contains("entre 50g et 5kg"));

        verify(equipmentRepository, never()).save(any());
    }

    /**
     * Vérifie que le nombre d'item est compris entre 1 et 3.
     */
    @Test
    void createEquipment_NbItemInvalid_ThrowsException() {
        // Given : Un équipement avec 0 item
        EquipmentItem invalidItem = createBaseValidEquipment();
        invalidItem.setNbItem(0);

        // When & Then : L'exception est levée
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> equipmentService.createEquipment(invalidItem));
        assertTrue(ex.getMessage().contains("peut couvrir 1 à 3 participants"));

        verify(equipmentRepository, never()).save(any());
    }

    /**
     * Vérifie que la masse à vide ne peut pas être supérieure à la masse totale.
     */
    @Test
    void createEquipment_MasseAVideInvalid_ThrowsException() {
        // Given : Un équipement où la masse à vide dépasse la masse totale
        EquipmentItem invalidItem = createBaseValidEquipment();
        invalidItem.setMasseGrammes(1000);
        invalidItem.setMasseAVide(1500);

        // When & Then : L'exception est levée
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> equipmentService.createEquipment(invalidItem));
        assertTrue(ex.getMessage().contains("ne peut pas avoir une masse à vide < 0 ou > à sa masse"));

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
        equipmentService.addEquipmentToHike(100L, 500L, 10L, null);

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
                () -> equipmentService.addEquipmentToHike(100L, 500L, 99L, null));
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
                () -> equipmentService.addEquipmentToHike(100L, 999L, 10L, null));
        assertEquals("Équipement introuvable", ex.getMessage());

        verify(hikeRepository, never()).save(any());
    }

    /**
     * Teste que la validation échoue au moment de l'ajout si un équipement
     * nécessitant un propriétaire (comme un vêtement) est ajouté sans participant assigné.
     */
    @Test
    void addEquipmentToHike_VetementWithoutOwner_ThrowsException() {
        // Given : La randonnée existe et appartient à l'utilisateur 10L
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));

        // Given : On prépare un équipement de type VÊTEMENT
        EquipmentItem vetementItem = new EquipmentItem();
        vetementItem.setId(30L);
        vetementItem.setNom("Veste Imperméable");
        vetementItem.setType(TypeEquipment.VETEMENT);

        when(equipmentRepository.findById(30L)).thenReturn(Optional.of(vetementItem));

        // When & Then : On tente d'ajouter ce vêtement sans spécifier de participant (participantId = null)
        // L'exception de propriétaire non défini doit sauter immédiatement
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> equipmentService.addEquipmentToHike(100L, 30L, 10L, null));

        assertTrue(ex.getMessage().contains("Un propriétaire n'a pas été défini pour l'objet Veste Imperméable"));

        // Sécurité : On s'assure que l'entité n'est jamais modifiée ni sauvegardée en cas d'erreur
        verify(mockHike, never()).addEquipment(any());
        verify(hikeRepository, never()).save(any());
        verify(belongEquipmentRepository, never()).save(any());
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