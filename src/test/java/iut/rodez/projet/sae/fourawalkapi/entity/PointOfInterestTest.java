package iut.rodez.projet.sae.fourawalkapi.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test la logique de calcul de distance géographique entre points d'intérêt.
 */
class PointOfInterestTest {

    private PointOfInterest poiRodez;

    @BeforeEach
    void setUp() {
        // Given: Un point d'intérêt situé à Rodez (Coordonnées approximatives)
        poiRodez = new PointOfInterest("Cathédrale de Rodez", 44.3516, 2.5734, "Belle cathédrale", 1);
    }

    /**
     * Test la distance entre un point et ses propres coordonnées.
     */
    @Test
    void distanceTo_SamePoint() {
        // When: On calcule la distance vers le même point
        double distance = poiRodez.distanceTo(44.3516, 2.5734);

        // Then: La distance doit être de 0 mètre
        assertEquals(0.0, distance, 0.1, "La distance vers le même point doit être nulle");
    }

    /**
     * Test la distance entre deux points connus.
     * Exemple : Rodez vers Toulouse (environ 120-125km à vol d'oiseau)
     */
    @Test
    void distanceTo_KnownDistance() {
        // Given: Coordonnées de Toulouse
        double latToulouse = 43.6047;
        double lonToulouse = 1.4442;

        // When: On calcule la distance
        double distanceMeters = poiRodez.distanceTo(latToulouse, lonToulouse);
        double distanceKm = distanceMeters / 1000.0;

        // Then: La distance doit être cohérente (environ 122 km)
        // On utilise une marge d'erreur de 1km car les rayons terrestres peuvent varier selon les modèles
        assertTrue(distanceKm > 120 && distanceKm < 125,
                "La distance Rodez-Toulouse devrait être d'environ 122km, obtenue : " + distanceKm);
    }

    /**
     * Test avec des valeurs nulles ou limites de coordonnées.
     */
    @Test
    void distanceTo_EquatorAndGreenwich() {
        // Given: Un point à l'intersection de l'équateur et du méridien de Greenwich
        PointOfInterest center = new PointOfInterest("Center", 0.0, 0.0, "Zero", 0);

        // When: On calcule la distance vers 1 degré de latitude nord
        double distance = center.distanceTo(1.0, 0.0);

        // Then: 1 degré de latitude est égal à environ 111,1 km (111100 mètres)
        assertEquals(111195.0, distance, 500.0, "Un degré de latitude doit correspondre à environ 111km");
    }
}