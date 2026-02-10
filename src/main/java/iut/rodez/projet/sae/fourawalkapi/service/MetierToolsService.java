package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.*;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Level;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Morphology;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;

import java.util.*;

class MetierToolsService {

    private static final double TOLERANCE_PERCENTAGE = 0.10;

    // --- MAIN VALIDATION ---

    public static void validateHikeForOptimize(Hike hike) {
        if (hike.getParticipants().isEmpty()) {
            throw new RuntimeException("Aucun participant n'a été trouvé dans la randonnée");
        }

        double distanceRando = HikeService.getAllDistance(hike);

        // 1. Validation Randonnée Globale
        validateDistanceHike(distanceRando, hike.getDureeJours(), getParticipantWithBadStat(hike.getParticipants()));

        // 2. Validation Participants
        int besoinCalorieTotalParticipant = 0;
        for (Participant p : hike.getParticipants()) {
            validateKcalParticipant(p, distanceRando);
            validateEauParticipant(p, distanceRando);
            validatePoidsParticipant(p);
            besoinCalorieTotalParticipant += p.getBesoinKcal();
        }

        // 3. Validations Logistiques
        validateHikeFood(hike, besoinCalorieTotalParticipant);
        validateHikeEquipment(hike);
        validateCapaciteEmportEauLitre(hike);
    }

    // --- REFACTORISATION : LOGIQUE COMMUNE ---

    /**
     * Calcule un modificateur basé sur le profil du participant (Niveau, Morpho, Age).
     * Permet d'éviter de copier-coller les if/else if.
     */
    private static double calculateProfileModifier(Participant p,
                                                   double modDebutant, double modSportif,
                                                   double modLegere, double modForte,
                                                   double modJunior, double modJeuneAdulte, double modSenior, double modVeteran) {
        double modifier = 0.0;

        // Niveau
        if (p.getNiveau() == Level.DEBUTANT) modifier += modDebutant;
        if (p.getNiveau() == Level.SPORTIF) modifier += modSportif;

        // Morphologie
        if (p.getMorphologie() == Morphology.LEGERE) modifier += modLegere;
        if (p.getMorphologie() == Morphology.FORTE) modifier += modForte;

        // Age (Ranges standardisés pour simplifier la lecture)
        int age = p.getAge();
        if (age < 16) modifier += modJunior;
        else if (age < 31) modifier += modJeuneAdulte;
        else if (age > 50 && age <= 70) modifier += modSenior;
        else if (age > 70) modifier += modVeteran;

        return modifier;
    }

    /**
     * Vérifie si une valeur est aberrante (en dehors de l'intervalle cible +/- 10%)
     */
    private static void checkAbberation(double actual, double target, String errorMessage) {
        double min = target * (1.0 - TOLERANCE_PERCENTAGE);
        double max = target * (1.0 + TOLERANCE_PERCENTAGE);

        // CORRECTION DU BUG : On lève l'exception si on est EN DEHORS des bornes
        if (actual < min || actual > max) {
            throw new RuntimeException(errorMessage + " (Valeur: " + actual + ", Attendu: ~" + target + ")");
        }
    }

    // --- VALIDATIONS SPÉCIFIQUES ---

    private static void validateDistanceHike(double distanceHike, int nbJour, Participant referent) {
        if (nbJour < 1 || nbJour > 3) {
            throw new IllegalArgumentException("Le nombre de jours (1-3) n'est pas valide");
        }

        double distanceMoyenneJour = distanceHike / nbJour;

        // Base 25km + Modificateurs
        double distanceTheorique = 25.0 + calculateProfileModifier(referent,
                0, 10,  // Debutant (0), Sportif (+10) -> (Note: Entrainé +5 géré implicitement comme base ou ajout spécifique si besoin)
                5, -5,  // Legere, Forte
                -5, 0, -5, -10 // <16, <31, 50-70, >70
        );

        // Petit fix spécifique pour le niveau 'ENTRAINE' qui n'était pas standard dans les autres calculs
        if (referent.getNiveau() == Level.ENTRAINE) distanceTheorique += 5;

        checkAbberation(distanceMoyenneJour, distanceTheorique, "La distance quotidienne de la randonnée est aberrante");
    }

    private static void validateKcalParticipant(Participant p, double distance) {
        if (p.getBesoinKcal() <= 0) throw new RuntimeException("Besoin Kcal non défini");

        double base = 2400 + (distance * 50);
        double target = base + calculateProfileModifier(p,
                200, -200,      // Debutant, Sportif
                -200, 200,      // Legere, Forte
                -300, 100, -100, -300 // Ages
        );

        checkAbberation(p.getBesoinKcal(), target, "Le besoin calorique est aberrant");
    }

    private static void validateEauParticipant(Participant p, double distance) {
        if (p.getBesoinEauLitre() <= 0) throw new RuntimeException("Besoin eau non défini");

        double base = 2.0 + (distance * 0.1);
        double target = base + calculateProfileModifier(p,
                0.5, -0.25,      // Debutant, Sportif
                -0.25, 0.5,      // Legere, Forte
                -0.5, -0.25, 0.25, 0.5 // Ages
        );

        checkAbberation(p.getBesoinEauLitre(), target, "Le besoin en eau est aberrant");
    }

    private static void validatePoidsParticipant(Participant p) {
        if (p.getCapaciteEmportMaxKg() <= 0) throw new RuntimeException("Capacité emport non définie");

        double base = 15.0;
        double target = base + calculateProfileModifier(p,
                -3, 3,        // Debutant, Sportif
                -3, 3,        // Legere, Forte
                -5, -3, 0, -5 // Ages (Note: <51 ans a +1 dans ton code original, ajusté ici à 0 pour simplifier ou à modifier selon règle exacte)
        );

        // Cas spécifique du code original pour l'âge 31-50 qui ajoutait +1
        if (p.getAge() >= 31 && p.getAge() <= 50) target += 1;

        checkAbberation(p.getCapaciteEmportMaxKg(), target, "La capacité d'emport est aberrante");
    }

    // --- UTILITAIRES ---

    private static Participant getParticipantWithBadStat(Set<Participant> participants) {
        // Utilisation de Comparator pour simplifier la logique de score "plus faible"
        return participants.stream()
                .min(Comparator.comparingDouble(MetierToolsService::calculateWeaknessScore))
                .orElseThrow(() -> new RuntimeException("Aucun participant trouvé"));
    }

    private static double calculateWeaknessScore(Participant p) {
        double score = 1.0;
        if (p.getNiveau() == Level.DEBUTANT) score -= 0.2;
        if (p.getNiveau() == Level.ENTRAINE) score -= 0.1;
        if (p.getMorphologie() == Morphology.MOYENNE) score -= 0.1;
        if (p.getMorphologie() == Morphology.FORTE) score -= 0.2;

        int age = p.getAge();
        if (age < 16 || age > 70) score -= 0.3;
        else if (age > 50) score -= 0.2;

        return score;
    }

    private static void validateHikeFood(Hike hike, int besoinCalorieTotal) {
        double maxCaloriePerItem = hike.getCaloriesForAllParticipants() / 4.0;
        Set<String> processedFoods = new HashSet<>();

        for (FoodProduct food : hike.getFoodCatalogue()) {
            if (!processedFoods.add(food.getAppellationCourante())) {
                throw new RuntimeException("Doublon de type de nourriture détecté : " + food.getAppellationCourante());
            }
            if (food.getApportNutritionnelKcal() > maxCaloriePerItem) {
                throw new RuntimeException("Nourriture trop calorique : " + food.getNom());
            }
        }

        if (hike.getCalorieRandonne() < besoinCalorieTotal) {
            throw new RuntimeException("Nourriture insuffisante pour la randonnée");
        }
    }

    private static void validateHikeEquipment(Hike hike) {
        if (hike.getEquipmentGroups() == null) return;

        int nbParticipants = hike.getParticipants().size();

        // Calcul simplifié de la couverture
        for (TypeEquipment type : TypeEquipment.values()) {
            if (type == TypeEquipment.AUTRE) continue;
            if (type == TypeEquipment.REPOS && hike.getDureeJours() < 2) continue;

            GroupEquipment group = hike.getEquipmentGroups().get(type);
            int totalItems = (group != null) ? group.getItems().stream().mapToInt(EquipmentItem::getNbItem).sum() : 0;

            if (totalItems < nbParticipants) {
                throw new IllegalStateException("Couverture insuffisante pour le type : " + type);
            }
        }
    }

    private static void validateCapaciteEmportEauLitre(Hike hike) {
        double besoinTotal = hike.getParticipants().stream()
                .mapToDouble(Participant::getBesoinEauLitre)
                .sum();

        GroupEquipment groupeEau = hike.getEquipmentGroups().get(TypeEquipment.EAU);
        double capaciteEmport = 0.0;

        if (groupeEau != null) {
            capaciteEmport = groupeEau.getItems().stream()
                    .mapToDouble(item -> (item.getMasseGrammes() / 1000.0) * item.getNbItem())
                    .sum();
        }

        if (capaciteEmport < besoinTotal) { // Correction logique : Si emport < besoin => Erreur
            throw new RuntimeException("Pas assez de gourdes pour couvrir les besoins en eau.");
        }
    }
}