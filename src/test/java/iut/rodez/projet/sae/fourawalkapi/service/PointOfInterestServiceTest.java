package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.entity.PointOfInterest;
import iut.rodez.projet.sae.fourawalkapi.entity.User;
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
 * en vérifiant la gestion de la séquence et la sécurité d'accès.
 */
class PointOfInterestServiceTest {

    private HikeRepository hikeRepository;
    private PointOfInterestRepository poiRepository;
    private PointOfInterestService poiService;

    private User creatorUser;
    private Hike mockHike;
    private List<PointOfInterest> newList;

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
        // Initialisation avec une liste vide (ArrayList)
        mockHike.setOptionalPoints(new ArrayList<>());

        // --- Nouvelle liste de POIs à synchroniser ---
        newList = new ArrayList<>();
        PointOfInterest p1 = new PointOfInterest();
        p1.setNom("Départ");
        PointOfInterest p2 = new PointOfInterest();
        p2.setNom("Arrivée");

        newList.add(p1);
        newList.add(p2);
    }

    /**
     * Teste la mise à jour nominale de tous les POIs d'une randonnée.
     * Vérifie que les anciens points sont supprimés et que les nouveaux sont
     * sauvegardés avec la bonne séquence (0, 1, 2...).
     */
    @Test
    void updateAllPois_Success() {
        // Given : La randonnée existe et contient déjà un ancien point
        PointOfInterest oldPoi = new PointOfInterest();
        oldPoi.setId(1L);
        mockHike.getOptionalPoints().add(oldPoi);

        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));
        // Simulation de la sauvegarde : on retourne l'objet passé en paramètre
        when(poiRepository.save(any(PointOfInterest.class))).thenAnswer(i -> i.getArguments()[0]);

        // When : Le propriétaire met à jour sa liste
        List<PointOfInterest> result = poiService.updateAllPois(100L, newList, 10L);

        // Then : Vérifications de l'état final
        assertNotNull(result);
        assertEquals(2, result.size());

        // Vérification de la séquence
        assertEquals(0, result.get(0).getSequence());
        assertEquals(1, result.get(1).getSequence());

        // Vérification des appels repositories
        verify(poiRepository).deleteAll(anyList()); // Suppression des anciens
        verify(poiRepository, times(2)).save(any(PointOfInterest.class)); // Sauvegarde des 2 nouveaux
        verify(hikeRepository).save(mockHike);
    }

    /**
     * Vérifie la faille de sécurité (IDOR) : empêche un utilisateur tiers de
     * modifier la liste des POIs d'une randonnée qui ne lui appartient pas.
     */
    @Test
    void updateAllPois_WrongUserAccess_ThrowsException() {
        // Given : La randonnée appartient à l'ID 10L
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));

        // When & Then : L'utilisateur 99L tente une modification
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> poiService.updateAllPois(100L, newList, 99L));

        assertTrue(ex.getMessage().contains("Accès refusé"));

        // Sécurité : rien ne doit être supprimé ou sauvegardé
        verify(poiRepository, never()).deleteAll(any());
        verify(poiRepository, never()).save(any());
    }

    /**
     * Teste le comportement lorsque la liste envoyée est vide.
     * La randonnée doit se retrouver avec une liste de points vide.
     */
    @Test
    void updateAllPois_EmptyList_ClearsAll() {
        // Given : Une randonnée avec des points existants
        mockHike.getOptionalPoints().add(new PointOfInterest());
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));

        // When : On envoie une liste vide
        List<PointOfInterest> result = poiService.updateAllPois(100L, new ArrayList<>(), 10L);

        // Then : La liste de la randonnée doit être vide
        assertTrue(result.isEmpty());
        verify(poiRepository).deleteAll(anyList());
        verify(hikeRepository).save(mockHike);
    }

    /**
     * Vérifie la gestion d'erreur lorsque la randonnée n'existe pas.
     */
    @Test
    void updateAllPois_HikeNotFound_ThrowsException() {
        // Given : ID inexistant
        when(hikeRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class,
                () -> poiService.updateAllPois(999L, newList, 10L));
    }
}