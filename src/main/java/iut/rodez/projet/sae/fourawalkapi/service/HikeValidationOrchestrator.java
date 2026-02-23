package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import org.springframework.stereotype.Service;

/**
 * Service moteur de règles métiers (Business Logic Layer).
 * Ce composant est responsable de la validation physiologique et logistique d'une randonnée
 * avant de tenter toute optimisation algorithmique. Il s'assure que les données sont cohérentes
 * (pas d'aberrations physiques) et que les ressources (eau, nourriture, matériel) sont suffisantes.
 */
@Service
public class HikeValidationOrchestrator {

    private final ParticipantPhysiologyService physiologyService;
    private final LogisticsValidationService logisticsService;

    public HikeValidationOrchestrator(ParticipantPhysiologyService physiologyService,
                                      LogisticsValidationService logisticsService) {
        this.physiologyService = physiologyService;
        this.logisticsService = logisticsService;
    }

    // --- MAIN VALIDATION ---

    /**
     * Point d'entrée principal de la validation.
     * Orchestre l'ensemble des vérifications : faisabilité physique, besoins nutritionnels,
     * couverture en eau et équipements. Bloque le processus si une incohérence critique est détectée.
     *
     * @param hike La randonnée configurée à valider.
     * @throws RuntimeException Si une contrainte métier n'est pas respectée.
     */
    public void validateHikeForOptimize(Hike hike) {
        if (hike.getParticipants().isEmpty()) {
            throw new RuntimeException("Validation impossible : Aucun participant dans la randonnée");
        }

        // Calcul de la distance totale via les points de passage
        double distanceRando = HikeService.getAllDistance(hike);

        // Validation de la faisabilité de la distance basée sur le participant le plus faible ("Maillon faible")
        physiologyService.validateDistanceHike(distanceRando, hike.getDureeJours(), physiologyService.getParticipantWithBadStat(hike.getParticipants()));

        int besoinCalorieTotalParticipant = 0;

        // Itération sur chaque participant pour valider ses constantes physiologiques personnelles
        for (Participant p : hike.getParticipants()) {
            physiologyService.validateKcalParticipant(p, distanceRando);
            physiologyService.validateEauParticipant(p, distanceRando);
            physiologyService.validatePoidsParticipant(p);

            besoinCalorieTotalParticipant += p.getBesoinKcal();
        }

        // Validation des stocks collectifs (Nourriture, Matériel, Contenants à eau)
        logisticsService.validateHikeFood(hike, besoinCalorieTotalParticipant);
        logisticsService.validateHikeEquipment(hike);
        logisticsService.validateCapaciteEmportEauLitre(hike);
    }
}