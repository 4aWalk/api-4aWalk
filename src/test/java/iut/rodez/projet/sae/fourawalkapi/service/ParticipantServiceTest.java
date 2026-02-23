package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.entity.User;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Morphology;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Level;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.HikeRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.ParticipantRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Classe de test pour le service ParticipantService.
 * Vérifie la logique métier liée à la gestion des participants d'une randonnée :
 * limites de capacité, règles physiologiques, droits d'accès et synchronisation
 * avec le profil Utilisateur (Créateur).
 */
class ParticipantServiceTest {

    private HikeRepository hikeRepository;
    private ParticipantRepository participantRepository;
    private UserRepository userRepository;
    private ParticipantService participantService;

    private User creatorUser;
    private Hike mockHike;

    /**
     * Initialisation des bouchons (mocks) et des données de référence avant chaque test.
     */
    @BeforeEach
    void setUp() {
        hikeRepository = mock(HikeRepository.class);
        participantRepository = mock(ParticipantRepository.class);
        userRepository = mock(UserRepository.class);
        participantService = new ParticipantService(hikeRepository, participantRepository, userRepository);

        // --- Initialisation des entités de base pour simuler la BDD ---
        creatorUser = new User();
        creatorUser.setId(10L);

        mockHike = new Hike();
        mockHike.setId(100L);
        mockHike.setCreator(creatorUser);
        // On initialise la collection pour éviter les NullPointerException lors des ajouts
        mockHike.setParticipants(new HashSet<>());
    }

    /**
     * Méthode utilitaire (Helper) pour générer rapidement un participant
     * respectant toutes les règles métier (âge, kcal, eau, etc.).
     * Permet de garder les tests propres et lisibles (principe DRY).
     *
     * @return Un Participant valide prêt à être testé.
     */
    private Participant createValidParticipant() {
        Participant p = new Participant();
        p.setNom("Doe");
        p.setPrenom("Jane");
        p.setAge(25);
        p.setNiveau(mock(Level.class));
        p.setMorphologie(mock(Morphology.class));
        p.setBesoinKcal(2500);
        p.setBesoinEauLitre(3);
        p.setCapaciteEmportMaxKg(10.0);
        return p;
    }

    // ==========================================
    // TESTS : LECTURE DES PARTICIPANTS
    // ==========================================

    /**
     * Teste la récupération des participants créés par un utilisateur,
     * en excluant son propre profil de créateur.
     */
    @Test
    void getMyParticipants() {
        // Given : L'utilisateur (ID 10) a 2 participants secondaires en base
        Long userId = 10L;
        List<Participant> expectedList = List.of(new Participant(), new Participant());
        when(participantRepository.findByCreatorIdAndCreatorFalse(userId)).thenReturn(expectedList);

        // When : On interroge le service
        List<Participant> result = participantService.getMyParticipants(userId);

        // Then : La liste retournée contient bien les 2 éléments attendus
        assertEquals(2, result.size());
        verify(participantRepository).findByCreatorIdAndCreatorFalse(userId);
    }

    // ==========================================
    // TESTS : AJOUT DE PARTICIPANTS
    // ==========================================

    /**
     * Teste l'ajout réussi d'un participant valide à une randonnée par son créateur.
     */
    @Test
    void addParticipant_Success() {
        // Given : Une randonnée existante et un nouveau participant valide
        Participant newParticipant = createValidParticipant();
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));
        when(participantRepository.save(any(Participant.class))).thenReturn(newParticipant);

        // When : L'utilisateur créateur (ID 10) ajoute le participant
        Participant saved = participantService.addParticipant(100L, newParticipant, 10L);

        // Then : Le participant est sauvegardé, tagué avec le bon ID créateur, et ajouté à la randonnée
        assertEquals(10L, saved.getCreatorId());
        assertFalse(saved.getCreator(), "Un participant ajouté manuellement ne doit pas être le profil créateur");
        assertTrue(mockHike.getParticipants().contains(saved));
        verify(hikeRepository).save(mockHike);
    }

    /**
     * Vérifie la règle métier limitant la taille d'un groupe (Max 3 participants).
     */
    @Test
    void addParticipant_HikeFull_ThrowsException() {
        // Given : Une randonnée contenant déjà 3 participants (la limite max)
        mockHike.getParticipants().add(new Participant());
        mockHike.getParticipants().add(new Participant());
        mockHike.getParticipants().add(new Participant());

        Participant newParticipant = createValidParticipant();
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));

        // When & Then : L'ajout d'un 4ème participant est bloqué par le système
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> participantService.addParticipant(100L, newParticipant, 10L));
        assertEquals("Hike complète (Max 3)", exception.getMessage());
    }

    /**
     * Vérifie la sécurité empêchant un utilisateur de modifier la randonnée d'un autre.
     */
    @Test
    void addParticipant_WrongUserAccess_ThrowsException() {
        // Given : La randonnée appartient à l'utilisateur ID=10
        Participant newParticipant = createValidParticipant();
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));

        // When & Then : L'utilisateur ID=99 tente d'ajouter un participant -> Accès refusé
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> participantService.addParticipant(100L, newParticipant, 99L));
        assertEquals("Acces_refused", exception.getMessage());
    }

    /**
     * Vérifie la validation des règles physiologiques (ex: âge minimum).
     * Permet de s'assurer que les données absurdes n'atteignent jamais la base de données.
     */
    @Test
    void addParticipant_InvalidData_ThrowsException() {
        // Given : Un participant avec un âge invalide (< 10 ans)
        Participant invalidParticipant = createValidParticipant();
        invalidParticipant.setAge(8);

        // When & Then : L'exception est levée immédiatement lors de la validation
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> participantService.addParticipant(100L, invalidParticipant, 10L));
        assertTrue(exception.getMessage().contains("L'âge doit être compris entre 10 et 100"));

        // Sécurité : On s'assure que la base de données n'est jamais sollicitée
        verify(hikeRepository, never()).findById(any());
    }

    // ==========================================
    // TESTS : MISE À JOUR DE PARTICIPANTS
    // ==========================================

    /**
     * Teste la modification d'un participant "standard" (qui n'est pas le créateur).
     */
    @Test
    void updateParticipant_Success_NotCreator() {
        // Given : Un participant classique présent dans la randonnée
        Participant existingParticipant = createValidParticipant();
        existingParticipant.setId(5L);
        existingParticipant.setCreator(false);
        mockHike.getParticipants().add(existingParticipant);

        // Nouvelles données entrantes
        Participant updateDetails = createValidParticipant();
        updateDetails.setNom("NouveauNom");

        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));
        when(participantRepository.findById(5L)).thenReturn(Optional.of(existingParticipant));
        when(participantRepository.save(any(Participant.class))).thenReturn(existingParticipant);

        // When : L'utilisateur met à jour le participant
        Participant updated = participantService.updateParticipant(100L, 5L, updateDetails, 10L);

        // Then : Le nom est mis à jour, et l'entité User n'est PAS touchée (car creator=false)
        assertEquals("NouveauNom", updated.getNom());
        verify(userRepository, never()).findById(any());
    }

    /**
     * Teste la modification du participant "Créateur".
     * Vérifie que les changements (ex: âge) se répercutent bien sur le compte Utilisateur global.
     */
    @Test
    void updateParticipant_Success_IsCreator() {
        // Given : Le participant à modifier est le profil du Créateur
        Participant creatorParticipant = createValidParticipant();
        creatorParticipant.setId(5L);
        creatorParticipant.setCreator(true);
        mockHike.getParticipants().add(creatorParticipant);

        // Nouvelles données (changement d'âge)
        Participant updateDetails = createValidParticipant();
        updateDetails.setAge(40);

        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));
        when(participantRepository.findById(5L)).thenReturn(Optional.of(creatorParticipant));
        when(userRepository.findById(10L)).thenReturn(Optional.of(creatorUser));
        when(participantRepository.save(any(Participant.class))).thenReturn(creatorParticipant);

        // When : On lance la mise à jour
        participantService.updateParticipant(100L, 5L, updateDetails, 10L);

        // Then : L'âge du compte User (créateur) doit avoir été synchronisé
        assertEquals(40, creatorUser.getAge());
        verify(userRepository).save(creatorUser);
    }

    /**
     * Vérifie la cohérence des données : interdit de modifier un participant
     * via l'URL d'une randonnée à laquelle il n'appartient pas.
     */
    @Test
    void updateParticipant_ParticipantNotInHike_ThrowsException() {
        // Given : Un participant existe en base, mais n'est pas lié à "mockHike"
        Participant existingParticipant = createValidParticipant();
        existingParticipant.setId(5L);

        Participant updateDetails = createValidParticipant();

        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));
        when(participantRepository.findById(5L)).thenReturn(Optional.of(existingParticipant));

        // When & Then : L'opération est rejetée
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> participantService.updateParticipant(100L, 5L, updateDetails, 10L));
        assertEquals("Ce participant n'appartient pas à la randonnée spécifiée !", exception.getMessage());
    }

    // ==========================================
    // TESTS : SUPPRESSION DE PARTICIPANTS
    // ==========================================

    /**
     * Teste le retrait réussi d'un participant d'une randonnée.
     */
    @Test
    void deleteParticipant_Success() {
        // Given : Un participant "standard" lié à la randonnée
        Participant p = new Participant();
        p.setId(5L);
        p.setCreator(false);
        mockHike.getParticipants().add(p);

        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));
        when(participantRepository.findById(5L)).thenReturn(Optional.of(p));

        // When : On demande la suppression
        participantService.deleteParticipant(100L, 5L, 10L);

        // Then : Il est retiré de la collection de la randonnée ET supprimé de la base de données
        assertFalse(mockHike.getParticipants().contains(p));
        verify(participantRepository).delete(p);
    }

    /**
     * Vérifie la règle métier critique : le profil créateur ne peut jamais être supprimé
     * d'une randonnée (il en est le propriétaire indispensable).
     */
    @Test
    void deleteParticipant_CannotDeleteCreator_ThrowsException() {
        // Given : Le participant ciblé est le profil créateur
        Participant creatorParticipant = new Participant();
        creatorParticipant.setId(5L);
        creatorParticipant.setCreator(true);
        mockHike.getParticipants().add(creatorParticipant);

        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));
        when(participantRepository.findById(5L)).thenReturn(Optional.of(creatorParticipant));

        // When & Then : La tentative de suppression échoue
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> participantService.deleteParticipant(100L, 5L, 10L));
        assertEquals("Impossible de supprimer le créateur", exception.getMessage());

        // Sécurité maximale : On vérifie que la méthode delete n'a absolument pas été appelée
        verify(participantRepository, never()).delete(any());
    }
}