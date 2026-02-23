package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.entity.PointOfInterest;
import iut.rodez.projet.sae.fourawalkapi.entity.User;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.HikeRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.PointOfInterestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Classe de test pour le service PointOfInterestService.
 * Valide l'ajout et la suppression des Points d'Intérêt (POI) sur une carte/randonnée,
 * en insistant sur la sécurité (IDOR) et l'intégrité transactionnelle.
 */
class PointOfInterestServiceTest {

    private HikeRepository hikeRepository;
    private PointOfInterestRepository poiRepository;
    private PointOfInterestService poiService;

    private User creatorUser;
    private Hike mockHike;
    private PointOfInterest validPoi;

    /**
     * Préparation des données factices et des bouchons (Mocks) avant chaque test.
     */
    @BeforeEach
    void setUp() {
        hikeRepository = mock(HikeRepository.class);
        poiRepository = mock(PointOfInterestRepository.class);
        poiService = new PointOfInterestService(hikeRepository, poiRepository);

        // --- Utilisateur Créateur ---
        creatorUser = new User();
        creatorUser.setId(10L);

        // --- Randonnée Cible ---
        mockHike = new Hike();
        mockHike.setId(100L);
        mockHike.setCreator(creatorUser);
        // Important : On simule l'initialisation de la collection pour éviter le NullPointerException
        mockHike.setOptionalPoints(new HashSet<>());

        // --- Point d'Intérêt (POI) à manipuler ---
        validPoi = new PointOfInterest();
        validPoi.setId(50L);
        validPoi.setName("Cascade de Salles-la-Source");
    }

    // ==========================================
    // TESTS : AJOUT D'UN POINT D'INTÉRÊT (POI)
    // ==========================================

    /**
     * Teste l'ajout nominal d'un POI sur une randonnée par son propriétaire légitime.
     */
    @Test
    void addPoiToHike_Success() {
        // Given : La randonnée existe, et le repository est prêt à sauvegarder le POI
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));
        when(poiRepository.save(validPoi)).thenReturn(validPoi);

        // When : Le propriétaire (ID=10L) ajoute le point
        PointOfInterest result = poiService.addPoiToHike(100L, validPoi, 10L);

        // Then : Le POI est sauvegardé, ajouté à la liste, et la rando est mise à jour
        assertNotNull(result);
        assertTrue(mockHike.getOptionalPoints().contains(validPoi), "Le POI doit être ajouté à la collection de la rando");

        verify(poiRepository).save(validPoi);
        verify(hikeRepository).save(mockHike);
    }

    /**
     * Vérifie la faille de sécurité (IDOR) : empêche l'ajout de POI sur la carte d'un autre utilisateur.
     */
    @Test
    void addPoiToHike_WrongUserAccess_ThrowsException() {
        // Given : Une randonnée existante appartenant au User 10L
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));

        // When & Then : Un utilisateur malveillant (ID=99L) tente d'ajouter un point
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> poiService.addPoiToHike(100L, validPoi, 99L));
        assertEquals("Accès refusé", ex.getMessage());

        // Sécurité : On vérifie que les données n'ont pas été altérées en base
        verify(poiRepository, never()).save(any());
        verify(hikeRepository, never()).save(any());
    }

    /**
     * Vérifie le comportement lorsque la randonnée spécifiée n'existe pas.
     */
    @Test
    void addPoiToHike_HikeNotFound_ThrowsException() {
        // Given : Le repository ne trouve aucune randonnée pour l'ID 999L
        when(hikeRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then : L'appel de .orElseThrow() lève une NoSuchElementException
        assertThrows(NoSuchElementException.class,
                () -> poiService.addPoiToHike(999L, validPoi, 10L));
    }

    // ==========================================
    // TESTS : SUPPRESSION D'UN POINT D'INTÉRÊT
    // ==========================================

    /**
     * Teste la suppression réussie d'un POI de la carte.
     */
    @Test
    void removePoiFromHike_Success() {
        // Given : La randonnée contient déjà le POI
        mockHike.getOptionalPoints().add(validPoi);
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));
        when(poiRepository.findById(50L)).thenReturn(Optional.of(validPoi));

        // When : Le propriétaire demande la suppression
        poiService.removePoiFromHike(100L, 50L, 10L);

        // Then : Le point est retiré de la collection, la rando est sauvegardée, et le POI est détruit
        assertFalse(mockHike.getOptionalPoints().contains(validPoi), "Le POI doit être retiré de la collection");

        verify(hikeRepository).save(mockHike);
        verify(poiRepository).delete(validPoi);
    }

    /**
     * Vérifie que l'on ne peut pas supprimer un POI sur la randonnée d'un autre.
     */
    @Test
    void removePoiFromHike_WrongUserAccess_ThrowsException() {
        // Given : Une randonnée existante (Créateur = 10L)
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));

        // When & Then : L'utilisateur 99L essaie de supprimer un point
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> poiService.removePoiFromHike(100L, 50L, 99L));
        assertEquals("Accès refusé", ex.getMessage());

        verify(poiRepository, never()).delete(any());
    }

    /**
     * Vérifie la gestion d'erreur lorsqu'un utilisateur tente de supprimer un POI qui n'existe plus (ou mauvais ID).
     */
    @Test
    void removePoiFromHike_PoiNotFound_ThrowsException() {
        // Given : La randonnée est trouvée, mais le POI (ID=888L) n'existe pas en base
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));
        when(poiRepository.findById(888L)).thenReturn(Optional.empty());

        // When & Then : L'appel de .orElseThrow() sur le POI lève l'exception
        assertThrows(NoSuchElementException.class,
                () -> poiService.removePoiFromHike(100L, 888L, 10L));

        verify(hikeRepository, never()).save(any());
        verify(poiRepository, never()).delete(any());
    }
}