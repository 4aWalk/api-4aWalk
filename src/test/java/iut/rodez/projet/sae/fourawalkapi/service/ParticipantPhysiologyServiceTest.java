package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Level;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Morphology;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires du ParticipantPhysiologyService.
 * Objectif : Valider les algorithmes physiologiques (distance, calories, eau, poids)
 * et le respect des tolérances de 10% selon les modificateurs de profil.
 */
class ParticipantPhysiologyServiceTest {

    private ParticipantPhysiologyService physiologyService;

    @BeforeEach
    void setUp() {
        physiologyService = new ParticipantPhysiologyService();
    }

    // ==========================================
    // TESTS : VALIDATION DE LA DISTANCE
    // ==========================================

    @Test
    void validateDistanceHike_ValidDistance_ShouldPass() {
        // Given : Un profil standard (Age 40, Entraîné, Moyenne)
        // Modificateurs calculés : 0 (Entraîné) + 0 (Moyenne) + 0 (Age 31-50) = 0.
        // Cible théorique : 25.0 km -> 25 000 mètres.
        Participant referent = createParticipant("John", 40, Level.ENTRAINE, Morphology.MOYENNE);

        // Randonnée de 2 jours, distance totale 50 000 m (moyenne = 25 000 m / jour)
        double distanceTotaleMeters = 50000.0;

        // When & Then : La distance est exactement la cible, aucune exception.
        assertDoesNotThrow(() -> physiologyService.validateDistanceHike(distanceTotaleMeters, 2, referent));
    }

    @Test
    void validateDistanceHike_DistanceTooHigh_ShouldThrowException() {
        // Given : Même profil cible (25 000 m / jour). Tolérance max = +10% (27 500 m).
        Participant referent = createParticipant("John", 40, Level.ENTRAINE, Morphology.MOYENNE);

        // Randonnée de 1 jour de 30 000 m (aberrant car > 27 500)
        double distanceTotaleMeters = 30000.0;

        // When & Then : L'exception est levée
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> physiologyService.validateDistanceHike(distanceTotaleMeters, 1, referent));
        assertTrue(ex.getMessage().contains("distance quotidienne"));
    }

    // ==========================================
    // TESTS : VALIDATION DES CALORIES (KCAL)
    // ==========================================

    @Test
    void validateKcalParticipant_ValidKcal_ShouldPass() {
        // Given : Distance = 10 000 m. Base effort = 2400 + (10000 * 0.001 * 50) = 2900 kcal.
        // Profil Jeune Sportif Léger :
        // Modificateurs : Sportif (-200), Léger (-200), JeuneAdulte (+100) -> Total = -300 kcal.
        // Cible = 2900 - 300 = 2600 kcal.
        Participant p = createParticipant("Alice", 25, Level.SPORTIF, Morphology.LEGERE);
        p.setBesoinKcal(2600); // Pile sur la cible

        // When & Then : Passe la validation
        assertDoesNotThrow(() -> physiologyService.validateKcalParticipant(p, 10000.0));
    }

    @Test
    void validateKcalParticipant_AberrantKcal_ShouldThrowException() {
        // Given : Cible à 2600 kcal. Tolérance de +/- 10% -> [2340, 2860].
        Participant p = createParticipant("Alice", 25, Level.SPORTIF, Morphology.LEGERE);
        p.setBesoinKcal(3000); // En dehors de l'intervalle (+10%)

        // When & Then : Exception levée
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> physiologyService.validateKcalParticipant(p, 10000.0));
        assertTrue(ex.getMessage().contains("besoin calorique est aberrant"));
    }

    // ==========================================
    // TESTS : VALIDATION DE L'EAU
    // ==========================================

    @Test
    void validateEauParticipant_ValidWater_ShouldPass() {
        // Given : Distance = 10 (Le calcul base = 2.0 + 10 * 0.1 = 3.0 L)
        // Profil Senior Fort Débutant :
        // Modificateurs : Débutant (+0.5), Forte (+0.5), Senior (+0.25) -> Total = +1.25 L.
        // Cible = 3.0 + 1.25 = 4.25 L.
        Participant p = createParticipant("Bob", 60, Level.DEBUTANT, Morphology.FORTE);
        p.setBesoinEauLitre(4); // 4L est dans la tolérance de 10% [3.82L - 4.67L]

        // When & Then
        assertDoesNotThrow(() -> physiologyService.validateEauParticipant(p, 10.0));
    }

    @Test
    void validateEauParticipant_InsufficientWater_ShouldThrowException() {
        // Given : Cible à 4.25 L. Minimum accepté = 3.82 L.
        Participant p = createParticipant("Bob", 60, Level.DEBUTANT, Morphology.FORTE);
        p.setBesoinEauLitre(2); // Vraiment trop bas !

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> physiologyService.validateEauParticipant(p, 10.0));
        assertTrue(ex.getMessage().contains("besoin en eau est aberrant"));
    }

    // ==========================================
    // TESTS : VALIDATION DU POIDS / EMPORT
    // ==========================================

    @Test
    void validatePoidsParticipant_ValidPoidsWithAgeBonus_ShouldPass() {
        // Given : Profil standard "Force de l'âge" (40 ans). Base = 15.0 kg.
        // Modificateurs : Aucun = 0.
        // Bonus spécifique (31-50 ans) : +1.0 kg -> Cible = 16.0 kg.
        Participant p = createParticipant("Charlie", 40, Level.ENTRAINE, Morphology.MOYENNE);
        p.setCapaciteEmportMaxKg(16.0);

        // When & Then
        assertDoesNotThrow(() -> physiologyService.validatePoidsParticipant(p));
    }

    @Test
    void validatePoidsParticipant_ZeroWeight_ShouldSkipValidation() {
        // Given : Un participant avec 0 d'emport (peut-être un enfant qui ne porte rien).
        Participant p = createParticipant("Enfant", 12, Level.DEBUTANT, Morphology.LEGERE);
        p.setCapaciteEmportMaxKg(0.0); // La condition "!= 0.0" doit s'activer

        // When & Then : Ça passe silencieusement sans jeter l'exception d'aberration
        assertDoesNotThrow(() -> physiologyService.validatePoidsParticipant(p));
    }

    @Test
    void validatePoidsParticipant_TooHeavy_ShouldThrowException() {
        // Given : Cible de 16.0 kg. Tolérance max = 17.6 kg.
        Participant p = createParticipant("Charlie", 40, Level.ENTRAINE, Morphology.MOYENNE);
        p.setCapaciteEmportMaxKg(25.0); // 25kg, c'est beaucoup trop lourd

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> physiologyService.validatePoidsParticipant(p));
        assertTrue(ex.getMessage().contains("capacité d'emport est aberrante"));
    }

    // ==========================================
    // TESTS : DÉTECTION DU MAILLON FAIBLE
    // ==========================================

    @Test
    void getParticipantWithBadStat_ShouldFindWeakestParticipant() {
        // Given : 3 participants avec des profils très différents
        // Score P1 (Jeune/Sportif) = 1.0 (Aucun malus)
        Participant p1 = createParticipant("P1", 25, Level.SPORTIF, Morphology.LEGERE);

        // Score P2 (Senior/Débutant/Forte) = 1.0 - 0.2 (Débutant) - 0.2 (Forte) - 0.2 (Senior > 50) = 0.4
        Participant p2 = createParticipant("P2", 65, Level.DEBUTANT, Morphology.FORTE);

        // Score P3 (Vétéran/Entraîné/Moyenne) = 1.0 - 0.1 (Entrainé) - 0.1 (Moyenne) - 0.3 (> 70) = 0.5
        Participant p3 = createParticipant("P3", 75, Level.ENTRAINE, Morphology.MOYENNE);

        Set<Participant> participants = new HashSet<>(Set.of(p1, p2, p3));

        // When : On cherche le plus faible
        Participant weakest = physiologyService.getParticipantWithBadStat(participants);

        // Then : C'est P2 qui a le score le plus bas (0.4)
        assertEquals("P2", weakest.getPrenom(), "P2 devrait être identifié comme le maillon faible");
    }

    @Test
    void getParticipantWithBadStat_EmptySet_ShouldThrowException() {
        // Given : Un set vide
        Set<Participant> participants = new HashSet<>();

        // When & Then : Une RuntimeException est levée
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> physiologyService.getParticipantWithBadStat(participants));
        assertEquals("Aucun participant trouvé", ex.getMessage());
    }

    // ==========================================
    // UTILITAIRE DE TEST
    // ==========================================

    /**
     * Crée un participant avec les données physiologiques de base.
     */
    private Participant createParticipant(String prenom, int age, Level niveau, Morphology morphologie) {
        Participant p = new Participant();
        p.setPrenom(prenom);
        p.setAge(age);
        p.setNiveau(niveau);
        p.setMorphologie(morphologie);
        return p;
    }
}