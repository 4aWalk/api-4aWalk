package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.*;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Level;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Morphology;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * Service moteur de règles métiers (Business Logic Layer).
 * Ce composant est responsable de la validation physiologique et logistique d'une randonnée
 * avant de tenter toute optimisation algorithmique. Il s'assure que les données sont cohérentes
 * (pas d'aberrations physiques) et que les ressources (eau, nourriture, matériel) sont suffisantes.
 */
@Component
public class MetierToolsService {

    /* Seuil de tolérance (10%) pour accepter les écarts entre les calculs théoriques et les données saisies */
    private static final double TOLERANCE_PERCENTAGE = 0.10;

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
        validateDistanceHike(distanceRando, hike.getDureeJours(), getParticipantWithBadStat(hike.getParticipants()));

        int besoinCalorieTotalParticipant = 0;

        // Itération sur chaque participant pour valider ses constantes physiologiques personnelles
        for (Participant p : hike.getParticipants()) {
            validateKcalParticipant(p, distanceRando);
            validateEauParticipant(p, distanceRando);
            validatePoidsParticipant(p);

            besoinCalorieTotalParticipant += p.getBesoinKcal();
        }

        // Validation des stocks collectifs (Nourriture, Matériel, Contenants à eau)
        validateHikeFood(hike, besoinCalorieTotalParticipant);
        validateHikeEquipment(hike);
        validateCapaciteEmportEauLitre(hike);
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

    // --- VALIDATIONS SPÉCIFIQUES ---

    /**
     * Vérifie si la distance journalière est réaliste pour le participant de référence (le plus faible).
     */
    private void validateDistanceHike(double distanceHike, int nbJour, Participant referent) {
        if (nbJour < 1 || nbJour > 3) {
            throw new IllegalArgumentException("Le nombre de jours (1-3) n'est pas valide");
        }

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
    private void validateKcalParticipant(Participant p, double distance) {
        if (p.getBesoinKcal() <= 0) throw new RuntimeException("Besoin Kcal non défini pour " + p.getPrenom());

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
    private void validateEauParticipant(Participant p, double distance) {
        if (p.getBesoinEauLitre() <= 0) throw new RuntimeException("Besoin eau non défini pour " + p.getPrenom());

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
    private void validatePoidsParticipant(Participant p) {
        if (p.getCapaciteEmportMaxKg() <= 0) throw new RuntimeException("Capacité emport non définie pour "
                + p.getPrenom());

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

    // --- UTILITAIRES ---

    /**
     * Identifie le participant le moins apte physiquement du groupe.
     * C'est lui qui détermine les limites de la randonnée (vitesse, distance max).
     */
    private Participant getParticipantWithBadStat(Set<Participant> participants) {
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

    /**
     * Valide le stock de nourriture :
     * 1. Pas de doublons de types (Variété).
     * 2. Pas d'item individuel excessivement calorique (Distribution).
     * 3. Le stock total couvre les besoins du groupe.
     */
    private void validateHikeFood(Hike hike, int besoinCalorieTotal) {
        // Seuil arbitraire : un aliment ne doit pas représenter plus de 25% des besoins totaux
        double maxCaloriePerItem = hike.getCaloriesForAllParticipants() / 4.0;
        Set<String> processedFoods = new HashSet<>();

        for (FoodProduct food : hike.getFoodCatalogue()) {
            // Vérification de l'unicité via l'appellation courante
            if (!processedFoods.add(food.getAppellationCourante())) {
                throw new RuntimeException("Doublon de type de nourriture détecté : " + food.getAppellationCourante());
            }
            if (food.getApportNutritionnelKcal() > maxCaloriePerItem) {
                throw new RuntimeException("Nourriture trop calorique : " + food.getNom());
            }
        }

        // Vérification de la suffisance globale
        if (hike.getCalorieRandonne() < besoinCalorieTotal) {
            throw new RuntimeException("Nourriture insuffisante pour la randonnée (" +
                    hike.getCalorieRandonne() + " vs " + besoinCalorieTotal + " requis).");
        }
    }

    /**
     * Valide la couverture en équipement.
     * S'assure qu'il y a au moins 1 item par participant pour chaque catégorie obligatoire.
     * Ignore les catégories optionnelles (AUTRE) ou conditionnelles (REPOS si < 2 jours).
     */
    private void validateHikeEquipment(Hike hike) {
        if (hike.getEquipmentGroups() == null) return;

        int nbParticipants = hike.getParticipants().size();

        for (TypeEquipment type : TypeEquipment.values()) {
            // Ignore les équipement autre pour la V2
            boolean isNotAutre = type != TypeEquipment.AUTRE;
            // Pas besoin de tente/sac de couchage pour une randonnée à la journée
            boolean needsRepos = !(type == TypeEquipment.REPOS && hike.getDureeJours() < 2);

            if (isNotAutre && needsRepos) {
                GroupEquipment group = hike.getEquipmentGroups().get(type);

                // Somme des quantités disponibles dans le groupe d'équipement
                int totalItems = (group != null) ? group.getItems().stream().mapToInt(EquipmentItem::getNbItem).sum() : 0;

                if (totalItems < nbParticipants) {
                    throw new IllegalStateException("Couverture insuffisante pour le type : " + type);
                }
            }
        }
    }

    /**
     * Vérifie si le groupe dispose d'assez de contenants (gourdes, camelbaks)
     * pour transporter la quantité d'eau requise calculée précédemment.
     * Utilise le delta (Masse Pleine - Masse Vide) pour déduire la capacité en volume.
     */
    private void validateCapaciteEmportEauLitre(Hike hike) {
        double besoinTotal = hike.getParticipants().stream()
                .mapToDouble(Participant::getBesoinEauLitre)
                .sum();

        GroupEquipment groupeEau = hike.getEquipmentGroups().get(TypeEquipment.EAU);
        double capaciteEmport = 0.0;

        if (groupeEau != null) {
            // Calcul : Volume = (Poids total - Poids à vide) / 1000 [Conversion g -> L d'eau]
            capaciteEmport = groupeEau.getItems().stream()
                    .mapToDouble(item -> ((item.getMasseGrammes() - item.getMasseAVide()) / 1000.0) *
                            item.getNbItem())
                    .sum();
        }

        if (capaciteEmport < besoinTotal) {
            throw new RuntimeException("Pas assez de gourdes pour couvrir les besoins en eau (Stock: " +
                    capaciteEmport + "L, Besoin: " + besoinTotal + "L).");
        }
    }
}