package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Level;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Morphology;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Set;

@Service
public class ParticipantPhysiologyService {

    /* Seuil de tolérance (10%) pour accepter les écarts entre les calculs théoriques et les données saisies */
    private static final double TOLERANCE_PERCENTAGE = 0.10;

    // --- VALIDATIONS SPÉCIFIQUES ---

    /**
     * Vérifie si la distance journalière est réaliste pour le participant de référence (le plus faible).
     */
    public void validateDistanceHike(double distanceHike, int nbJour, Participant referent) {
        double distanceMoyenneJour = distanceHike / nbJour;

        // Base théorique : 25km/jour ajustée selon le profil
        double distanceTheorique = 25.0 + calculateProfileModifier(referent,
                0, 10,  // Pas de malus débutant, bonus sportif
                5, -5,  // Bonus léger, malus fort
                -5, 0, -5, -10 // Malus selon l'âge
        );

        // Conversion km en m
        distanceTheorique *= 1000;

        checkAbberation(distanceMoyenneJour, distanceTheorique,
                "La distance quotidienne de la randonnée est aberrante");
    }

    /**
     * Vérifie la cohérence du besoin calorique calculé.
     * Formule : Base métabolique (2400) + Effort (50kcal/km) + Modificateurs.
     */
    public void validateKcalParticipant(Participant p, double distance) {

        double base = 2400 + (distance * 0.001 * 50);
        double target = base + calculateProfileModifier(p,
                200, -200,
                -200, 200,
                -300, 100, -100, -300
        );

        checkAbberation(p.getBesoinKcal(), target, "Le besoin calorique est aberrant pour " + p.getPrenom());
    }

    /**
     * Vérifie la cohérence du besoin en hydratation.
     * Formule : Base (2L) + Effort (0.1L/km) + Modificateurs.
     */
    public void validateEauParticipant(Participant p, double distance) {

        double base = 2.0 + (distance * 0.1);
        double target = base + calculateProfileModifier(p,
                0.5, -0.25,
                -0.25, 0.5,
                -0.5, -0.25, 0.25, 0.5
        );

        checkAbberation(p.getBesoinEauLitre(), target, "Le besoin en eau est aberrant pour " + p.getPrenom());
    }

    /**
     * Vérifie la capacité de portage maximale.
     * Formule : Base (15kg) + Modificateurs de force physique.
     */
    public void validatePoidsParticipant(Participant p) {
        if (p.getCapaciteEmportMaxKg() != 0.0) {
            double base = 15.0;
            double target = base + calculateProfileModifier(p,
                    -3, 3,
                    -3, 3,
                    -5, -3, 0, -5
            );

            // Bonus pour la tranche d'âge "force de l'âge" (31-50 ans)
            if (p.getAge() >= 31 && p.getAge() <= 50) target += 1;

            checkAbberation(p.getCapaciteEmportMaxKg(), target, "La capacité d'emport est aberrante pour "
                    + p.getPrenom());
        }
    }

    // --- UTILITAIRES ---

    /**
     * Identifie le participant le moins apte physiquement du groupe.
     * C'est lui qui détermine les limites de la randonnée (vitesse, distance max).
     */
    public Participant getParticipantWithBadStat(Set<Participant> participants) {
        return participants.stream()
                .min(Comparator.comparingDouble(this::calculateWeaknessScore))
                .orElseThrow(() -> new RuntimeException("Aucun participant trouvé"));
    }

    /**
     * Calcule un score de performance (plus c'est bas, plus le participant est "faible").
     * Départ à 1.0, puis application de pénalités.
     */
    private double calculateWeaknessScore(Participant p) {
        double score = 1.0;

        // Pénalités de niveau et morphologie
        if (p.getNiveau() == Level.DEBUTANT) score -= 0.2;
        if (p.getNiveau() == Level.ENTRAINE) score -= 0.1;
        if (p.getMorphologie() == Morphology.MOYENNE) score -= 0.1;
        if (p.getMorphologie() == Morphology.FORTE) score -= 0.2;

        // Pénalités d'âge extrêmes
        int age = p.getAge();
        if (age < 16 || age > 70) score -= 0.3;
        else if (age > 50) score -= 0.2;

        return score;
    }

    // --- LOGIQUE COMMUNE ---

    /**
     * Calcule la somme des ajustements physiologiques applicables à un participant
     * en fonction de son profil sportif, de sa morphologie et de sa tranche d'âge.
     *
     * @param p Le participant dont les caractéristiques sont analysées.
     * @param modDebutant Valeur de l'ajustement pour un niveau débutant.
     * @param modSportif Valeur de l'ajustement pour un niveau sportif.
     * @param modLegere Valeur de l'ajustement pour une morphologie légère.
     * @param modForte Valeur de l'ajustement pour une morphologie forte.
     * @param modJunior Valeur de l'ajustement pour les moins de 16 ans.
     * @param modJeuneAdulte Valeur de l'ajustement pour la tranche 16-30 ans.
     * @param modSenior Valeur de l'ajustement pour la tranche 51-70 ans.
     * @param modVeteran Valeur de l'ajustement pour les plus de 70 ans.
     * @return Le modificateur total cumulé sous forme de valeur décimale.
     */
    private double calculateProfileModifier(Participant p,
                                            double modDebutant, double modSportif,
                                            double modLegere, double modForte,
                                            double modJunior, double modJeuneAdulte,
                                            double modSenior, double modVeteran) {
        double modifier = 0.0;

        if (p.getNiveau() == Level.DEBUTANT) {
            modifier += modDebutant;
        }
        if (p.getNiveau() == Level.SPORTIF) {
            modifier += modSportif;
        }

        if (p.getMorphologie() == Morphology.LEGERE) {
            modifier += modLegere;
        }
        if (p.getMorphologie() == Morphology.FORTE) {
            modifier += modForte;
        }

        int age = p.getAge();
        if (age < 16) {
            modifier += modJunior;
        } else if (age < 31) {
            modifier += modJeuneAdulte;
        } else if (age > 50 && age <= 70) {
            modifier += modSenior;
        } else if (age > 70) {
            modifier += modVeteran;
        }

        return modifier;
    }

    /**
     * Vérification statistique d'aberration.
     * S'assure que la valeur réelle ne sort pas de l'intervalle [Cible - 10%, Cible + 10%].
     */
    private void checkAbberation(double actual, double target, String errorMessage) {
        double min = target * (1.0 - TOLERANCE_PERCENTAGE);
        double max = target * (1.0 + TOLERANCE_PERCENTAGE);

        if (actual < min || actual > max) {
            // Le formatage %.2f permet d'afficher 2 décimales dans le message d'erreur
            throw new RuntimeException(String.format("%s (Valeur: %.2f, Attendu: ~%.2f)", errorMessage, actual, target));
        }
    }
}