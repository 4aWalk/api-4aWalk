package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HikeValidationOrchestratorTest {

    @Mock
    private ParticipantPhysiologyService physiologyService;

    @Mock
    private LogisticsValidationService logisticsService;

    private HikeValidationOrchestrator orchestrator;
    private Hike standardHike;
    private Participant p1;

    @BeforeEach
    void setUp() {
        // L'orchestrateur est instancié avec les mocks injectés
        orchestrator = new HikeValidationOrchestrator(physiologyService, logisticsService);

        standardHike = new Hike();
        standardHike.setDureeJours(2);

        p1 = new Participant();
        p1.setPrenom("Alice");
        p1.setBesoinKcal(2000);

        Participant p2 = new Participant();
        p2.setPrenom("Bob");
        p2.setBesoinKcal(2500);

        Set<Participant> participants = new HashSet<>();
        participants.add(p1);
        participants.add(p2);
        standardHike.setParticipants(participants);
    }

    // ==========================================
    // TESTS DU FLUX NOMINAL ET LIMITES
    // ==========================================

    @Test
    void validateHikeForOptimize_NominalFlow_ShouldCallAllServicesCorrectly() {
        // On mock la méthode statique HikeService.getAllDistance pour qu'elle renvoie 20.0 km
        try (MockedStatic<HikeService> mockedHikeService = mockStatic(HikeService.class)) {
            mockedHikeService.when(() -> HikeService.getAllDistance(standardHike)).thenReturn(20000.0);

            // On configure le mock de physiologie pour qu'il renvoie p1 comme maillon faible
            when(physiologyService.getParticipantWithBadStat(standardHike.getParticipants())).thenReturn(p1);

            // Exécution de l'orchestrateur
            assertDoesNotThrow(() -> orchestrator.validateHikeForOptimize(standardHike));

            // VÉRIFICATIONS (Les "Verify")
            // 1. Vérifie que la distance a été validée avec le maillon faible (p1)
            verify(physiologyService).validateDistanceHike(20000.0, 2, p1);

            // 2. Vérifie que CHAQUE participant a été validé physiologiquement
            for (Participant p : standardHike.getParticipants()) {
                verify(physiologyService).validateKcalParticipant(p, 20000.0);
                verify(physiologyService).validateEauParticipant(p, 20000.0);
                verify(physiologyService).validatePoidsParticipant(p);
            }

            // 3. Vérifie le calcul de la somme des calories (2000 + 2500 = 4500)
            verify(logisticsService).validateHikeFood(standardHike, 4500);

            // 4. Vérifie les validations d'équipement
            verify(logisticsService).validateHikeEquipment(standardHike);
            verify(logisticsService).validateCapaciteEmportEauLitre(standardHike);
        }
    }

    // ==========================================
    // TESTS DES CAS D'ERREUR (ARRÊT D'URGENCE)
    // ==========================================

    @Test
    void validateHikeForOptimize_EmptyParticipants_ShouldThrowExceptionAndStop() {
        // On vide la liste des participants
        standardHike.setParticipants(new HashSet<>());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orchestrator.validateHikeForOptimize(standardHike));

        assertEquals("Validation impossible : Aucun participant dans la randonnée", ex.getMessage());

        // L'arrêt doit être immédiat : AUCUN service ne doit être appelé
        verifyNoInteractions(physiologyService);
        verifyNoInteractions(logisticsService);
    }

    @Test
    void validateHikeForOptimize_PhysiologyError_ShouldStopBeforeLogistics() {
        try (MockedStatic<HikeService> mockedHikeService = mockStatic(HikeService.class)) {
            mockedHikeService.when(() -> HikeService.getAllDistance(standardHike)).thenReturn(20000.0);
            when(physiologyService.getParticipantWithBadStat(any())).thenReturn(p1);

            // On simule une erreur lors de la validation de la distance
            doThrow(new RuntimeException("Distance aberrante"))
                    .when(physiologyService).validateDistanceHike(anyDouble(), anyInt(), any(Participant.class));

            // Exécution
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> orchestrator.validateHikeForOptimize(standardHike));

            assertEquals("Distance aberrante", ex.getMessage());

            // VÉRIFICATION CRUCIALE : Le service de logistique ne DOIT PAS avoir été appelé
            verifyNoInteractions(logisticsService);
        }
    }

    @Test
    void validateHikeForOptimize_LogisticsError_ShouldPropagateException() {
        try (MockedStatic<HikeService> mockedHikeService = mockStatic(HikeService.class)) {
            mockedHikeService.when(() -> HikeService.getAllDistance(standardHike)).thenReturn(20000.0);
            when(physiologyService.getParticipantWithBadStat(any())).thenReturn(p1);

            // On simule une erreur à la TOUTE DERNIÈRE étape (l'eau)
            doThrow(new RuntimeException("Pas assez de gourdes"))
                    .when(logisticsService).validateCapaciteEmportEauLitre(any(Hike.class));

            // Exécution
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> orchestrator.validateHikeForOptimize(standardHike));

            assertEquals("Pas assez de gourdes", ex.getMessage());

            // Vérifie que les validations précédentes ont bien eu lieu avant le crash
            verify(logisticsService).validateHikeFood(eq(standardHike), anyInt());
            verify(logisticsService).validateHikeEquipment(standardHike);
        }
    }
}