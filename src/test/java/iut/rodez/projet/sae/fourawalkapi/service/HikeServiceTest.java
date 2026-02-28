package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.*;
import iut.rodez.projet.sae.fourawalkapi.repository.mongo.CourseRepository;
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
 * Suite de tests unitaires dédiée au {@link HikeService}.
 * <p>
 * Cette classe valide l'intégralité du cycle de vie d'une randonnée :
 * <ul>
 * <li><b>Sécurité :</b> Vérification de la propriété des entités (un utilisateur ne modifie que ses randonnées).</li>
 * <li><b>Orchestration :</b> Délégation correcte aux algorithmes d'optimisation et de répartition.</li>
 * <li><b>Persistance Hybride :</b> Synchronisation des suppressions entre MySQL (Hike) et MongoDB (Course).</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class HikeServiceTest {

    @Mock private HikeRepository hikeRepository;
    @Mock private CourseRepository courseRepository; // Mock pour les opérations MongoDB en cascade
    @Mock private BackpackDistributorServiceV2 backpackDistributor;
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

    /**
     * Initialisation du contexte de test.
     * Met en place un jeu de données "bouchonné" (mocks) standard avec un utilisateur,
     * des points de départ/arrivée et une randonnée valide.
     */
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setPrenom("John");
        testUser.setNom("Doe");

        poiDepart = new PointOfInterest();
        poiDepart.setId(10L);

        poiArrivee = new PointOfInterest();
        poiArrivee.setId(20L);

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

    /**
     * Vérifie qu'un utilisateur légitime (le créateur) peut récupérer avec succès
     * les détails de sa propre randonnée.
     */
    @Test
    void getHikeById_UserIsCreator_ShouldReturnHike() {
        // GIVEN : La randonnée est présente en base et appartient à l'utilisateur effectuant la requête.
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(testHike));

        // WHEN : L'utilisateur tente de consulter la randonnée.
        Hike result = hikeService.getHikeById(100L, 1L);

        // THEN : Le système autorise l'accès et retourne la bonne entité.
        assertNotNull(result);
        assertEquals(100L, result.getId());
    }

    /**
     * Vérifie la politique de cloisonnement des données (Multi-Tenant conceptuel).
     * Un utilisateur ne doit pas pouvoir accéder aux données générées par un autre.
     */
    @Test
    void getHikeById_UserIsNotCreator_ShouldThrowSecurityException() {
        // GIVEN : La randonnée existe, mais elle est la propriété de l'utilisateur ayant l'ID 1.
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(testHike));

        // WHEN & THEN : Une tentative d'accès par l'utilisateur ID 2 doit être bloquée immédiatement.
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> hikeService.getHikeById(100L, 2L));
        assertTrue(ex.getMessage().contains("Accès refusé"),
                "Le message d'erreur doit explicitement mentionner le refus d'accès.");
    }

    /**
     * Valide le comportement du service face à un identifiant inexistant.
     */
    @Test
    void getHikeById_HikeNotFound_ShouldThrowException() {
        // GIVEN : L'identifiant fourni ne correspond à aucune entrée en base.
        when(hikeRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN & THEN : Le système lève une exception signalant l'absence de la ressource.
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> hikeService.getHikeById(999L, 1L));
        assertTrue(ex.getMessage().contains("introuvable"));
    }

    // ==========================================
    // TESTS : CRÉATION DE RANDONNÉE
    // ==========================================

    /**
     * Valide le flux nominal de création d'une randonnée.
     * S'assure notamment que le créateur est automatiquement ajouté à la liste
     * des participants lors de l'initialisation de l'événement.
     */
    @Test
    void createHike_NominalCase_ShouldCreateHikeAndAddCreatorAsParticipant() {
        // GIVEN : Les entités référencées (Utilisateur, POIs) existent, et le nom de la randonnée est disponible.
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(hikeRepository.existsByCreatorIdAndLibelleAndIdNot(1L, "Rando Montagne", -1L)).thenReturn(false);
        when(poiRepository.findById(10L)).thenReturn(Optional.of(poiDepart));
        when(poiRepository.findById(20L)).thenReturn(Optional.of(poiArrivee));

        Participant savedParticipant = new Participant();
        savedParticipant.setId(50L);
        when(participantRepository.save(any(Participant.class))).thenReturn(savedParticipant);
        when(hikeRepository.save(any(Hike.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN : On procède à l'enregistrement de la nouvelle randonnée.
        Hike result = hikeService.createHike(testHike, 1L);

        // THEN : La randonnée est créée, l'utilisateur en est le créateur, et il y figure comme premier participant.
        assertNotNull(result);
        assertEquals(testUser, result.getCreator());
        assertEquals(1, result.getParticipants().size(), "Le créateur doit être automatiquement ajouté aux participants.");
        assertTrue(result.getParticipants().contains(savedParticipant));
        verify(hikeRepository).save(result);
    }

    /**
     * Vérifie le respect de la règle d'unicité métier : un utilisateur ne peut pas
     * posséder deux randonnées actives portant exactement le même nom.
     */
    @Test
    void createHike_LibelleAlreadyExists_ShouldThrowException() {
        // GIVEN : L'utilisateur possède déjà une randonnée nommée "Rando Montagne".
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(hikeRepository.existsByCreatorIdAndLibelleAndIdNot(1L, "Rando Montagne", -1L)).thenReturn(true);

        // WHEN & THEN : La sauvegarde est interrompue pour éviter les doublons.
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> hikeService.createHike(testHike, 1L));
        assertTrue(ex.getMessage().contains("déjà une randonnée nommée"));
    }

    // ==========================================
    // TESTS : CAS LIMITES ET VALIDATION MÉTIER
    // ==========================================

    /**
     * Valide les contraintes de limite temporelle imposées par l'application (max 3 jours).
     */
    @Test
    void validateHike_DurationTooLong_ShouldThrowException() {
        // GIVEN : L'objet en entrée spécifie une durée hors périmètre (4 jours).
        testHike.setDureeJours(4);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // WHEN & THEN : L'orchestrateur de validation rejette la demande avant toute insertion.
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> hikeService.createHike(testHike, 1L));
        assertTrue(ex.getMessage().contains("compris entre 0 e 3"));
    }

    /**
     * Valide que l'application ne tolère pas les voyages temporels négatifs.
     */
    @Test
    void validateHike_DurationNegative_ShouldThrowException() {
        // GIVEN : Une tentative d'injection avec un nombre de jours invalide.
        testHike.setDureeJours(-1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // WHEN & THEN : Le validateur bloque l'opération.
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> hikeService.createHike(testHike, 1L));
        assertTrue(ex.getMessage().contains("compris entre 0 e 3"));
    }

    // ==========================================
    // TESTS : ORCHESTRATION DE L'OPTIMISATION
    // ==========================================

    /**
     * Teste le processus complexe d'optimisation des sacs à dos.
     * S'assure que le service orchestre correctement les différentes étapes :
     * Validation -> Génération de l'équipement -> Génération de la nourriture -> Bin Packing.
     */
    @Test
    void optimizeBackpack_NominalCase_ShouldCallAllServices() {
        // GIVEN : Une randonnée existante à optimiser.
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(testHike));

        List<EquipmentItem> dummyEquip = List.of(new EquipmentItem());
        List<FoodProduct> dummyFood = List.of(new FoodProduct());
        when(optimizerService.getOptimizeAllEquipment(testHike)).thenReturn(dummyEquip);
        when(optimizerService.getOptimizeAllFood(testHike)).thenReturn(dummyFood);

        // WHEN : L'ordre d'optimisation est lancé.
        hikeService.optimizeBackpack(100L, 1L);

        // THEN : Les collaborateurs (services externes) sont appelés de manière séquentielle et cohérente.
        verify(hikeValidatorService).validateHikeForOptimize(testHike); // Vérification de l'état
        verify(optimizerService).getOptimizeAllEquipment(testHike);   // Récupération équipement
        verify(optimizerService).getOptimizeAllFood(testHike);        // Récupération nourriture
        verify(backpackDistributor).distributeBatchesToBackpacks(
                anyList(), eq(testHike.getBackpacks()), eq(testHike.getId())); // Répartition algorithmique
        verify(hikeRepository).save(testHike);                          // Persistance du résultat
    }

    // ==========================================
    // TESTS : CALCUL DE DISTANCE (Méthode Statique)
    // ==========================================

    /**
     * Valide l'algorithme de calcul de la distance totale d'un parcours.
     * Le test s'assure que l'ordre des étapes (sequence) est bien respecté
     * pour additionner les segments géographiques dans le bon sens.
     */
    @Test
    void getAllDistance_WithOptionalPois_ShouldCalculateCorrectly() {
        // GIVEN : Un parcours composé de 4 points (Départ, Étape 2, Étape 1, Arrivée) dont l'ordre interne est désordonné.
        PointOfInterest mockDepart = mock(PointOfInterest.class);
        PointOfInterest mockEtape1 = mock(PointOfInterest.class);
        PointOfInterest mockEtape2 = mock(PointOfInterest.class);
        PointOfInterest mockArrivee = mock(PointOfInterest.class);

        Hike distanceHike = new Hike();
        distanceHike.setDepart(mockDepart);
        distanceHike.setArrivee(mockArrivee);

        when(mockEtape1.getSequence()).thenReturn(2); // Étape censée être en 2ème
        when(mockEtape2.getSequence()).thenReturn(1); // Étape censée être en 1ère

        distanceHike.setOptionalPoints(Set.of(mockEtape1, mockEtape2));

        // Définition des distances de chaque segment dans l'ordre de passage attendu :
        when(mockDepart.distanceTo(anyDouble(), anyDouble())).thenReturn(2.0); // Départ -> Étape 2
        when(mockEtape2.distanceTo(anyDouble(), anyDouble())).thenReturn(3.0); // Étape 2 -> Étape 1
        when(mockEtape1.distanceTo(anyDouble(), anyDouble())).thenReturn(5.0); // Étape 1 -> Arrivée

        // WHEN : On calcule le cheminement complet.
        double totalDistance = HikeService.getAllDistance(distanceHike);

        // THEN : La distance totale est la somme exacte des segments parcourus dans l'ordre (2 + 3 + 5).
        assertEquals(10.0, totalDistance, 0.001);
    }

    /**
     * Vérifie le calcul de distance simple pour une randonnée "directe" (point A vers point B) sans étapes.
     */
    @Test
    void getAllDistance_NoOptionalPois_ShouldCalculateDirectDistance() {
        // GIVEN : Une randonnée sans aucun point d'intérêt intermédiaire.
        PointOfInterest mockDepart = mock(PointOfInterest.class);
        PointOfInterest mockArrivee = mock(PointOfInterest.class);

        Hike distanceHike = new Hike();
        distanceHike.setDepart(mockDepart);
        distanceHike.setArrivee(mockArrivee);
        distanceHike.setOptionalPoints(null);

        when(mockDepart.distanceTo(anyDouble(), anyDouble())).thenReturn(10.0);

        // WHEN : Le calcul de la distance globale est déclenché.
        double totalDistance = HikeService.getAllDistance(distanceHike);

        // THEN : La distance correspond directement au segment unique Départ -> Arrivée.
        assertEquals(10.0, totalDistance, 0.001);
    }

    /**
     * Valide la récupération en lot des randonnées via l'identifiant de leur créateur.
     */
    @Test
    void getHikesByCreator_ShouldReturnList() {
        // GIVEN : La base SQL contient une liste de randonnées pour le créateur spécifié.
        when(hikeRepository.findByCreatorId(1L)).thenReturn(List.of(testHike));

        // WHEN : On interroge le référentiel de l'utilisateur.
        List<Hike> result = hikeService.getHikesByCreator(1L);

        // THEN : La liste est correctement récupérée et renvoyée sans altération.
        assertEquals(1, result.size());
        assertEquals(100L, result.getFirst().getId());
    }

    /**
     * Teste la mise à jour des informations de base d'une randonnée, en vérifiant
     * notamment la résolution automatique des nouveaux Points d'Intérêt en base.
     */
    @Test
    void updateHike_NominalCase_ShouldUpdateFieldsAndResolvePois() {
        // GIVEN : Des modifications valides (nouveau nom, nouvelle durée, nouveaux POIs).
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(testHike));
        when(hikeRepository.existsByCreatorIdAndLibelleAndIdNot(1L, "Nouveau Titre", 100L)).thenReturn(false);
        when(hikeRepository.save(any(Hike.class))).thenAnswer(i -> i.getArgument(0));

        Hike details = new Hike();
        details.setLibelle("Nouveau Titre");
        details.setDureeJours(3);

        PointOfInterest newDepart = new PointOfInterest();
        newDepart.setId(88L);
        details.setDepart(newDepart);

        PointOfInterest newArrivee = new PointOfInterest();
        newArrivee.setId(99L);
        details.setArrivee(newArrivee);

        when(poiRepository.findById(88L)).thenReturn(Optional.of(newDepart));
        when(poiRepository.findById(99L)).thenReturn(Optional.of(newArrivee));

        // WHEN : La mise à jour est traitée par le service.
        Hike result = hikeService.updateHike(100L, details, 1L);

        // THEN : Les champs simples sont modifiés et les entités liées (POIs) sont correctement rattachées.
        assertEquals("Nouveau Titre", result.getLibelle());
        assertEquals(3, result.getDureeJours());
        assertEquals(88L, result.getDepart().getId());
        assertEquals(99L, result.getArrivee().getId());
    }

    /**
     * Vérifie que le processus de mise à jour ignore les vérifications lourdes
     * (recherche en base) si les données d'entrée sont vides ou non fournies.
     */
    @Test
    void updateHike_EmptyLibelleAndNullPois_ShouldSkipChecks() {
        // GIVEN : Une requête de mise à jour avec des chaînes vides et des POIs non identifiés.
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(testHike));
        when(hikeRepository.save(any(Hike.class))).thenAnswer(i -> i.getArgument(0));

        Hike details = new Hike();
        details.setLibelle("   ");
        details.setDureeJours(1);
        details.setDepart(new PointOfInterest()); // Pas d'ID défini
        details.setArrivee(new PointOfInterest()); // Pas d'ID défini

        // WHEN : On lance l'actualisation.
        hikeService.updateHike(100L, details, 1L);

        // THEN : Le service optimise le traitement en ignorant les vérifications inutiles d'unicité et de POI.
        verify(hikeRepository, never()).existsByCreatorIdAndLibelleAndIdNot(anyLong(), anyString(), anyLong());
        verify(poiRepository, never()).findById(anyLong());
    }

    // ==========================================
    // TESTS : SUPPRESSION EN CASCADE (HYBRIDE)
    // ==========================================

    /**
     * Vérifie la logique de suppression complète et hybride (SQL + NoSQL).
     * S'assure de l'effacement préalable des collections relationnelles pour éviter
     * les conflits de contraintes, et valide l'appel vers la base MongoDB.
     */
    @Test
    void deleteHike_ShouldClearCollectionsAndDestroy() {
        // GIVEN : Une randonnée qui possède des données liées (participants, nourriture).
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(testHike));

        testHike.setParticipants(new HashSet<>(List.of(new Participant())));
        testHike.setFoodCatalogue(new ArrayList<>(List.of(new FoodProduct())));

        // WHEN : On demande la suppression définitive de la randonnée.
        hikeService.deleteHike(100L, 1L);

        // THEN : Les relations Hibernate (MySQL) sont vidées pour libérer les Foreign Keys.
        assertTrue(testHike.getParticipants().isEmpty(), "Les participants doivent être retirés de la liste avant suppression");
        assertTrue(testHike.getFoodCatalogue().isEmpty(), "Le catalogue de nourriture doit être vidé avant suppression");

        // 2. L'entité MySQL est mise à jour (vidée) puis détruite.
        verify(hikeRepository).save(testHike);
        verify(hikeRepository).delete(testHike);

        // 3. Le dépôt MongoDB est averti pour supprimer le suivi GPS orphelin.
        verify(courseRepository).deleteByHikeId(100L);
    }
}