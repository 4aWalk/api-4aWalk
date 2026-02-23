package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.*;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires du HikeService avec Mockito.
 * Valide l'orchestration, la sécurité (propriété des entités), les validations métier
 * et la délégation aux algorithmes.
 */
@ExtendWith(MockitoExtension.class)
class HikeServiceTest {

    @Mock private HikeRepository hikeRepository;
    @Mock private BackpackDistributorService backpackDistributor;
    @Mock private HikeValidationOrchestrator hikeValidatorService;
    @Mock private OptimizerService optimizerService;
    @Mock private UserRepository userRepository;
    @Mock private PointOfInterestRepository poiRepository;
    @Mock private ParticipantRepository participantRepository;

    @InjectMocks
    private HikeService hikeService;

    private User testUser;
    private Hike testHike;
    private PointOfInterest poiDepart;
    private PointOfInterest poiArrivee;

    @BeforeEach
    void setUp() {
        // Initialisation d'un utilisateur de test
        testUser = new User();
        testUser.setId(1L);
        testUser.setPrenom("John");
        testUser.setNom("Doe");

        // Initialisation de POIs de test
        poiDepart = new PointOfInterest();
        poiDepart.setId(10L);

        poiArrivee = new PointOfInterest();
        poiArrivee.setId(20L);

        // Initialisation d'une randonnée de base
        testHike = new Hike();
        testHike.setId(100L);
        testHike.setLibelle("Rando Montagne");
        testHike.setDureeJours(2);
        testHike.setCreator(testUser);
        testHike.setDepart(poiDepart);
        testHike.setArrivee(poiArrivee);
    }

    // ==========================================
    // TESTS : SÉCURITÉ ET RÉCUPÉRATION
    // ==========================================

    @Test
    void getHikeById_UserIsCreator_ShouldReturnHike() {
        // GIVEN : La randonnée existe et l'utilisateur 1 en est le créateur
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(testHike));

        // WHEN : L'utilisateur 1 demande sa randonnée
        Hike result = hikeService.getHikeById(100L, 1L);

        // THEN : La randonnée est retournée avec succès
        assertNotNull(result);
        assertEquals(100L, result.getId());
    }

    @Test
    void getHikeById_UserIsNotCreator_ShouldThrowSecurityException() {
        // GIVEN : La randonnée appartient à l'utilisateur 1
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(testHike));

        // WHEN & THEN : L'utilisateur 2 essaie d'y accéder -> Erreur d'accès refusé
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> hikeService.getHikeById(100L, 2L));
        assertTrue(ex.getMessage().contains("Accès refusé"));
    }

    @Test
    void getHikeById_HikeNotFound_ShouldThrowException() {
        // GIVEN : La randonnée n'existe pas en base
        when(hikeRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN & THEN : Erreur introuvable
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> hikeService.getHikeById(999L, 1L));
        assertTrue(ex.getMessage().contains("introuvable"));
    }

    // ==========================================
    // TESTS : CRÉATION DE RANDONNÉE
    // ==========================================

    @Test
    void createHike_NominalCase_ShouldCreateHikeAndAddCreatorAsParticipant() {
        // GIVEN : Un utilisateur valide, des POIs valides, un nom unique
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(hikeRepository.existsByCreatorIdAndLibelleAndIdNot(1L, "Rando Montagne", -1L)).thenReturn(false);
        when(poiRepository.findById(10L)).thenReturn(Optional.of(poiDepart));
        when(poiRepository.findById(20L)).thenReturn(Optional.of(poiArrivee));

        // On simule la sauvegarde du participant (le créateur)
        Participant savedParticipant = new Participant();
        savedParticipant.setId(50L);
        when(participantRepository.save(any(Participant.class))).thenReturn(savedParticipant);

        // On simule la sauvegarde finale de la rando
        when(hikeRepository.save(any(Hike.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN : On crée la randonnée
        Hike result = hikeService.createHike(testHike, 1L);

        // THEN : La rando est sauvegardée, initialisée, et le créateur est dans les participants
        assertNotNull(result);
        assertEquals(testUser, result.getCreator());
        assertEquals(1, result.getParticipants().size());
        assertTrue(result.getParticipants().contains(savedParticipant));
        verify(hikeRepository).save(result); // Vérifie que le save a bien été appelé
    }

    @Test
    void createHike_LibelleAlreadyExists_ShouldThrowException() {
        // GIVEN : L'utilisateur existe, mais il a déjà une rando avec ce nom
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(hikeRepository.existsByCreatorIdAndLibelleAndIdNot(1L, "Rando Montagne", -1L)).thenReturn(true);

        // WHEN & THEN : La création échoue sur la règle d'unicité
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> hikeService.createHike(testHike, 1L));
        assertTrue(ex.getMessage().contains("déjà une randonnée nommée"));
    }

    // ==========================================
    // TESTS : CAS LIMITES ET VALIDATION MÉTIER
    // ==========================================

    @Test
    void validateHike_DurationTooLong_ShouldThrowException() {
        // GIVEN : Une randonnée de 4 jours (Limites = 0 à 3 selon validateHike)
        testHike.setDureeJours(4);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // WHEN & THEN : La validation échoue dès la création
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> hikeService.createHike(testHike, 1L));
        assertTrue(ex.getMessage().contains("compris entre 0 e 3"));
    }

    // ==========================================
    // TESTS : ORCHESTRATION DE L'OPTIMISATION
    // ==========================================

    @Test
    void optimizeBackpack_NominalCase_ShouldCallAllServices() {
        // GIVEN : Une randonnée valide appartenant à l'utilisateur
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(testHike));

        // On prépare des fausses listes optimisées renvoyées par l'Optimizer
        List<EquipmentItem> dummyEquip = List.of(new EquipmentItem());
        List<FoodProduct> dummyFood = List.of(new FoodProduct());
        when(optimizerService.getOptimizeAllEquipmentV2(testHike)).thenReturn(dummyEquip);
        when(optimizerService.getOptimizeAllFoodV2(testHike)).thenReturn(dummyFood);

        // WHEN : On lance l'optimisation
        hikeService.optimizeBackpack(100L, 1L);

        // THEN : Tous les sous-services doivent avoir été appelés dans le bon ordre
        verify(hikeValidatorService).validateHikeForOptimize(testHike); // 1. Validation
        verify(optimizerService).getOptimizeAllEquipmentV2(testHike);   // 2. Opti Equip
        verify(optimizerService).getOptimizeAllFoodV2(testHike);        // 3. Opti Food
        // 4. Distribution (anyList() car la liste est instanciée dans la méthode)
        verify(backpackDistributor).distributeBatchesToBackpacks(anyList(), eq(testHike.getBackpacks()));
        verify(hikeRepository).save(testHike);                          // 5. Sauvegarde finale
    }

    // ==========================================
    // TESTS : CALCUL DE DISTANCE (Méthode Statique)
    // ==========================================

    @Test
    void getAllDistance_WithOptionalPois_ShouldCalculateCorrectly() {
        // GIVEN : On crée nos mocks
        PointOfInterest mockDepart = mock(PointOfInterest.class);
        PointOfInterest mockEtape1 = mock(PointOfInterest.class);
        PointOfInterest mockEtape2 = mock(PointOfInterest.class);
        PointOfInterest mockArrivee = mock(PointOfInterest.class);

        Hike distanceHike = new Hike();
        distanceHike.setDepart(mockDepart);
        distanceHike.setArrivee(mockArrivee);

        // séquences inversées pour forcer le tri
        when(mockEtape1.getSequence()).thenReturn(2);
        when(mockEtape2.getSequence()).thenReturn(1);

        distanceHike.setOptionalPoints(Set.of(mockEtape1, mockEtape2));

        // L'ordre calculé doit être :
        // Depart -> Etape 2 -> Etape 1 -> Arrivee

        // 1. Depart -> Etape 2
        when(mockDepart.distanceTo(anyDouble(), anyDouble())).thenReturn(2.0);

        // 2. Etape 2 -> Etape 1
        when(mockEtape2.distanceTo(anyDouble(), anyDouble())).thenReturn(3.0);

        // 3. Etape 1 -> Arrivee
        when(mockEtape1.distanceTo(anyDouble(), anyDouble())).thenReturn(5.0);

        // WHEN
        double totalDistance = HikeService.getAllDistance(distanceHike);

        // THEN : 2.0 + 3.0 + 5.0 = 10.0
        assertEquals(10.0, totalDistance, 0.001);
    }

    @Test
    void getAllDistance_NoOptionalPois_ShouldCalculateDirectDistance() {
        // GIVEN : Uniquement un départ et une arrivée
        PointOfInterest mockDepart = mock(PointOfInterest.class);
        PointOfInterest mockArrivee = mock(PointOfInterest.class);

        Hike distanceHike = new Hike();
        distanceHike.setDepart(mockDepart);
        distanceHike.setArrivee(mockArrivee);
        distanceHike.setOptionalPoints(null); // Cas limite : la liste est null

        // Distance directe = 10.0
        when(mockDepart.distanceTo(anyDouble(), anyDouble())).thenReturn(10.0);

        // WHEN
        double totalDistance = HikeService.getAllDistance(distanceHike);

        // THEN
        assertEquals(10.0, totalDistance, 0.001);
    }
}