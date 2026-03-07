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

/**
 * Tests unitaires du HikeValidationOrchestrator.
 * Vérifie que l'orchestrateur appelle correctement les services de physiologie
 * et de logistique dans le bon ordre, et qu'il s'arrête en cas d'erreur.
 */
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
    // TESTS DU FLUX NOMINAL
    // ==========================================

    /**
     * Teste le flux nominal de validation d'une randonnée.
     * Vérifie que tous les sous-services sont appelés avec les bons paramètres
     * si aucune exception n'est levée en cours de route.
     */
    @Test
    void validateHikeForOptimize_NominalFlow_ShouldCallAllServicesCorrectly() {
        // Given : Une randonnée standard de 20km avec 2 participants
        try (MockedStatic<HikeService> mockedHikeService = mockStatic(HikeService.class)) {
            mockedHikeService.when(() -> HikeService.getAllDistance(standardHike)).thenReturn(20000.0);

            // Given : Le service de physiologie identifie p1 comme le maillon faible
            when(physiologyService.getParticipantWithBadStat(standardHike.getParticipants())).thenReturn(p1);

            // When : On lance la validation globale de la randonnée
            assertDoesNotThrow(() -> orchestrator.validateHikeForOptimize(standardHike));

            // Then : La distance est validée avec le maillon faible (p1)
            verify(physiologyService).validateDistanceHike(20000.0, 2, p1);

            // Then : CHAQUE participant a été validé physiologiquement
            for (Participant p : standardHike.getParticipants()) {
                verify(physiologyService).validateKcalParticipant(p, 20000.0);
                verify(physiologyService).validateEauParticipant(p, 20000.0);
                verify(physiologyService).validatePoidsParticipant(p);
            }

            // Then : La nourriture est validée avec le bon calcul de marge
            // Calcul : (4500 kcal/jour * 2 jours) + 4500 kcal (marge d'1 jour) = 13500
            verify(logisticsService).validateHikeFood(standardHike, 13500);

            // Then : L'équipement et la capacité d'eau sont validés
            verify(logisticsService).validateHikeEquipment(standardHike);
            verify(logisticsService).validateCapaciteEmportEauLitre(standardHike);
        }
    }

    // ==========================================
    // TESTS DES CAS D'ERREUR (ARRÊT D'URGENCE)
    // ==========================================

    /**
     * Teste que la validation s'arrête immédiatement si une erreur
     * physiologique est détectée, sans appeler le service de logistique.
     */
    @Test
    void validateHikeForOptimize_PhysiologyError_ShouldStopBeforeLogistics() {
        // Given : Une randonnée standard de 20km
        try (MockedStatic<HikeService> mockedHikeService = mockStatic(HikeService.class)) {
            mockedHikeService.when(() -> HikeService.getAllDistance(standardHike)).thenReturn(20000.0);
            when(physiologyService.getParticipantWithBadStat(any())).thenReturn(p1);

            // Given : Une erreur critique survient lors de la validation de la distance
            doThrow(new RuntimeException("Distance aberrante"))
                    .when(physiologyService).validateDistanceHike(anyDouble(), anyInt(), any(Participant.class));

            // When : On lance la validation globale
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> orchestrator.validateHikeForOptimize(standardHike));

            // Then : L'exception remonte correctement
            assertEquals("Distance aberrante", ex.getMessage());

            // Then : Le service de logistique n'a JAMAIS été appelé (l'orchestrateur a stoppé le flux)
            verifyNoInteractions(logisticsService);
        }
    }

    /**
     * Teste que la validation propage correctement une exception
     * levée par le service de logistique (en fin de chaîne).
     */
    @Test
    void validateHikeForOptimize_LogisticsError_ShouldPropagateException() {
        // Given : Une randonnée standard de 20km
        try (MockedStatic<HikeService> mockedHikeService = mockStatic(HikeService.class)) {
            mockedHikeService.when(() -> HikeService.getAllDistance(standardHike)).thenReturn(20000.0);
            when(physiologyService.getParticipantWithBadStat(any())).thenReturn(p1);

            // Given : Une erreur survient à la toute dernière étape (capacité d'emport d'eau)
            doThrow(new RuntimeException("Pas assez de gourdes"))
                    .when(logisticsService).validateCapaciteEmportEauLitre(any(Hike.class));

            // When : On lance la validation globale
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> orchestrator.validateHikeForOptimize(standardHike));

            // Then : L'exception remonte correctement
            assertEquals("Pas assez de gourdes", ex.getMessage());

            // Then : Les validations logistiques précédentes ont bien été exécutées avant le crash
            verify(logisticsService).validateHikeFood(eq(standardHike), anyInt());
            verify(logisticsService).validateHikeEquipment(standardHike);
        }
    }
}