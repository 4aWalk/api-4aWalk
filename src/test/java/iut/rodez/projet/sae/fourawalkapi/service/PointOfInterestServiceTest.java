package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.entity.PointOfInterest;
import iut.rodez.projet.sae.fourawalkapi.entity.User;
import iut.rodez.projet.sae.fourawalkapi.exception.BusinessValidationException;
import iut.rodez.projet.sae.fourawalkapi.exception.ResourceNotFoundException;
import iut.rodez.projet.sae.fourawalkapi.exception.UnauthorizedAccessException;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.HikeRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.PointOfInterestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Classe de test pour le service PointOfInterestService.
 * Valide la mise à jour globale et ordonnée des Points d'Intérêt (POI) d'une randonnée,
 * la vérification des coordonnées géographiques, et la sécurité d'accès.
 */
class PointOfInterestServiceTest {

    private HikeRepository hikeRepository;
    private PointOfInterestRepository poiRepository;
    private PointOfInterestService poiService;

    private User creatorUser;
    private Hike mockHike;
    private List<PointOfInterest> newList;

    @BeforeEach
    void setUp() {
        hikeRepository = mock(HikeRepository.class);
        poiRepository = mock(PointOfInterestRepository.class);
        poiService = new PointOfInterestService(hikeRepository, poiRepository);

        creatorUser = new User();
        creatorUser.setId(10L);

        mockHike = new Hike();
        mockHike.setId(100L);
        mockHike.setCreator(creatorUser);
        mockHike.setOptionalPoints(new ArrayList<>());

        newList = new ArrayList<>();
        newList.add(createValidPoi("Départ", 45.0, 2.0));
        newList.add(createValidPoi("Arrivée", 46.0, 3.0));
    }

    /**
     * Méthode utilitaire pour générer un POI valide pour les tests.
     */
    private PointOfInterest createValidPoi(String nom, double latitude, double longitude) {
        PointOfInterest poi = new PointOfInterest();
        poi.setNom(nom);
        poi.setLatitude(latitude);
        poi.setLongitude(longitude);
        return poi;
    }

    // ==========================================
    // TESTS : VALIDATION MÉTIER (validatePointOfInterest)
    // ==========================================

    /**
     * Teste qu'un point d'intérêt avec des données valides ne lève aucune exception.
     */
    @Test
    void validatePointOfInterest_Success() {
        // Given : Un POI valide
        PointOfInterest validPoi = createValidPoi("Sommet", 45.0, 2.0);

        // When & Then : Aucune exception n'est levée
        assertDoesNotThrow(() -> poiService.validatePointOfInterest(validPoi));
    }

    /**
     * Vérifie que le nom est obligatoire.
     */
    @Test
    void validatePointOfInterest_MissingName_ThrowsException() {
        // Given : Un POI sans nom
        PointOfInterest invalidPoi = createValidPoi("", 45.0, 2.0);

        // When : On valide le POI
        // Then : Une exception de validation est levée
        BusinessValidationException ex = assertThrows(BusinessValidationException.class,
                () -> poiService.validatePointOfInterest(invalidPoi));
        assertTrue(ex.getMessage().contains("Le nom d'un point d'interêt est obligatoire"));
    }

    /**
     * Vérifie la limite basse de la latitude (-90).
     */
    @Test
    void validatePointOfInterest_LatitudeTooLow_ThrowsException() {
        // Given : Un POI avec une latitude invalide
        PointOfInterest invalidPoi = createValidPoi("Point A", -91.0, 2.0);

        // When : On valide le POI
        // Then : Une exception est levée
        BusinessValidationException ex = assertThrows(BusinessValidationException.class,
                () -> poiService.validatePointOfInterest(invalidPoi));
        assertTrue(ex.getMessage().contains("La latitude doit être entre -90 et 90"));
    }

    /**
     * Vérifie la limite haute de la latitude (90).
     */
    @Test
    void validatePointOfInterest_LatitudeTooHigh_ThrowsException() {
        // Given : Un POI avec une latitude invalide
        PointOfInterest invalidPoi = createValidPoi("Point A", 91.0, 2.0);

        // When : On valide le POI
        // Then : Une exception est levée
        BusinessValidationException ex = assertThrows(BusinessValidationException.class,
                () -> poiService.validatePointOfInterest(invalidPoi));
        assertTrue(ex.getMessage().contains("La latitude doit être entre -90 et 90"));
    }

    /**
     * Vérifie la limite basse de la longitude (-180).
     */
    @Test
    void validatePointOfInterest_LongitudeTooLow_ThrowsException() {
        // Given : Un POI avec une longitude invalide
        PointOfInterest invalidPoi = createValidPoi("Point A", 45.0, -181.0);

        // When : On valide le POI
        // Then : Une exception est levée
        BusinessValidationException ex = assertThrows(BusinessValidationException.class,
                () -> poiService.validatePointOfInterest(invalidPoi));
        assertTrue(ex.getMessage().contains("La longitude doit être entre -180 et 180"));
    }

    // ==========================================
    // TESTS : MISE À JOUR DES POI (updateAllPois)
    // ==========================================

    /**
     * Teste la mise à jour nominale de tous les POIs d'une randonnée.
     */
    @Test
    void updateAllPois_Success() {
        // Given : La randonnée existe et contient déjà un ancien point
        PointOfInterest oldPoi = createValidPoi("Ancien", 0.0, 0.0);
        oldPoi.setId(1L);
        mockHike.getOptionalPoints().add(oldPoi);

        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));
        when(poiRepository.save(any(PointOfInterest.class))).thenAnswer(i -> i.getArguments()[0]);

        // When : Le propriétaire met à jour sa liste
        List<PointOfInterest> result = poiService.updateAllPois(100L, newList, 10L);

        // Then : Vérifications de l'état final et de la séquence
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(0, result.get(0).getSequence());
        assertEquals(1, result.get(1).getSequence());

        verify(poiRepository).deleteAll(anyList());
        verify(poiRepository, times(2)).save(any(PointOfInterest.class));
        verify(hikeRepository).save(mockHike);
    }

    /**
     * Vérifie la faille de sécurité (IDOR) lors de la mise à jour des POIs.
     */
    @Test
    void updateAllPois_WrongUserAccess_ThrowsException() {
        // Given : La randonnée appartient à l'utilisateur 10L
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));

        // When : L'utilisateur 99L tente une modification
        // Then : Une exception de sécurité est levée
        UnauthorizedAccessException ex = assertThrows(UnauthorizedAccessException.class,
                () -> poiService.updateAllPois(100L, newList, 99L));

        assertTrue(ex.getMessage().contains("Accès refusé"));

        verify(poiRepository, never()).deleteAll(any());
        verify(poiRepository, never()).save(any());
    }

    /**
     * Teste le nettoyage complet de la liste des POIs.
     */
    @Test
    void updateAllPois_EmptyList_ClearsAll() {
        // Given : Une randonnée avec des points existants
        mockHike.getOptionalPoints().add(createValidPoi("Ancien", 0.0, 0.0));
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));

        // When : On envoie une liste vide
        List<PointOfInterest> result = poiService.updateAllPois(100L, new ArrayList<>(), 10L);

        // Then : La liste de la randonnée est complètement vidée
        assertTrue(result.isEmpty());
        verify(poiRepository).deleteAll(anyList());
        verify(hikeRepository).save(mockHike);
    }

    /**
     * Vérifie la gestion d'erreur lorsque la randonnée n'existe pas.
     */
    @Test
    void updateAllPois_HikeNotFound_ThrowsException() {
        // Given : Un ID de randonnée inexistant en base
        when(hikeRepository.findById(999L)).thenReturn(Optional.empty());

        // When : On tente de mettre à jour les POIs
        // Then : Une exception est levée
        assertThrows(ResourceNotFoundException.class,
                () -> poiService.updateAllPois(999L, newList, 10L));
    }
}