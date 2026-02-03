package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.*;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OptimizerService {

    public OptimizerService(EquipmentItemService es){}

    public static void optimizeFoodV1(Hike hike) {
        /*List<FoodProduct> foodList = new ArrayList<>(hike.getFoodCatalogue());
        List<Participant> participantList = new ArrayList<>(hike.getParticipants());
        if (participantList.isEmpty()) throw new RuntimeException("Aucun particpant trouvé pour l'optimisation");
        int participantIndex = 0;
        boolean allFoodadd = true;

        for (FoodProduct fp : foodList) {
            boolean assigned = false;
            for (int i = 0; i < participantList.size(); i++) {
                int currentIndex = (participantIndex + i) % participantList.size();
                Participant p = participantList.get(currentIndex);

                p.getBackpack().updateAndGetTotalMass();

                if (p.getBackpack().getTotalMassKg() + fp.getWeightKg() <= p.getCapaciteEmportMaxKg()) {

                    p.getBackpack().addFoodItems(fp);

                    participantIndex = (currentIndex + 1) % participantList.size();

                    assigned = true;
                    break;
                }
            }

            if (!assigned) {
                allFoodadd = false;
                break;
            }
        }
        if (!allFoodadd) {throw new RuntimeException("Toutes la nourriture n'as pas pu être placer dans les sac des participant avec l'optimiseur V1");}*/
    }

    public static void optimizeEquipmentV1(Hike hike) {
        /*List<EquipmentItem> equipmentList = new ArrayList<>(hike.getEquipmentRequired());
        List<Participant> participantList = new ArrayList<>(hike.getParticipants());
        if (participantList.isEmpty()) throw new RuntimeException("Aucun particpant trouvé pour l'optimisation");
        int participantIndex = 0;
        boolean allEquipementAdd = true;

        for (EquipmentItem e : equipmentList) {
            boolean assigned = false;
            for (int i = 0; i < participantList.size(); i++) {
                int currentIndex = (participantIndex + i) % participantList.size();
                Participant p = participantList.get(currentIndex);

                p.getBackpack().updateAndGetTotalMass();

                if (p.getBackpack().getTotalMassKg() + e.getWeightKg() <= p.getCapaciteEmportMaxKg()) {

                    p.getBackpack().addEquipmentItems(e);

                    participantIndex = (currentIndex + 1) % participantList.size();

                    assigned = true;
                    break;
                }
            }

            if (!assigned) {
                allEquipementAdd = false;
                break;
            }
        }
        if (!allEquipementAdd) {throw new RuntimeException("Tout les équipements n'ont pas pu être placer dans les sac des participant avec l'optimiseur V1");}
    */}

    /**
     * Algorithme d'optimisation V2 pour l'équipement.
     * Sélectionne la meilleure combinaison d'équipements pour chaque type requis.
     */
    public static List<EquipmentItem> getOptimizeAllEquipmentV2(Hike hike) {
        List<TypeEquipment> typeList = new ArrayList<>(Arrays.asList(TypeEquipment.values()));
        List<EquipmentItem> equipmentOptimized = new ArrayList<>();

        // Suppression des types inutiles
        typeList.remove(TypeEquipment.AUTRE);

        // Règle métier : Pas de repos si rando de 1 jour
        if (hike.getDureeJours() == 1) {
            typeList.remove(TypeEquipment.REPOS);
        }

        for (TypeEquipment type : typeList) {
            // CORRECTION MAJEURE : On récupère la liste directement depuis l'entité Hike
            // Cela évite un appel inutile à la base de données via un service
            List<EquipmentItem> itemsDispo = hike.getOptimizedList(type);

            // Si aucun équipement de ce type n'est dispo dans le catalogue de la rando, on passe
            if (itemsDispo.isEmpty()) {
                continue;
                // Ou throw new RuntimeException("Aucun équipement disponible pour le type " + type);
            }

            // Appel de l'algo récursif
            List<EquipmentItem> bestItemsForType = sortBestEquipmentV2(
                    itemsDispo,
                    new ArrayList<>(),
                    hike.getParticipants().size(), // Objectif : Couvrir tous les participants
                    0
            );

            if (bestItemsForType != null) {
                equipmentOptimized.addAll(bestItemsForType);
            } else {
                throw new RuntimeException("Impossible de trouver une combinaison valide pour le type : " + type);
            }
        }

        return equipmentOptimized;
    }

    /**
     * Moteur récursif (Backtracking) pour l'équipement.
     * Cherche à atteindre la couverture (nbParticipant) avec le moins d'objets possible.
     */
    private static List<EquipmentItem> sortBestEquipmentV2(
            List<EquipmentItem> candidats,
            List<EquipmentItem> currentSelection,
            int nbParticipant,
            int index) {

        int couverture = currentSelection.stream().mapToInt(EquipmentItem::getNbItem).sum();

        // 1. CAS DE BASE : SUCCÈS
        if (couverture >= nbParticipant) {
            return new ArrayList<>(currentSelection);
        }

        // 2. CAS DE BASE : ECHEC (Fin de liste)
        if (index >= candidats.size()) {
            return null;
        }

        EquipmentItem item = candidats.get(index);

        // 3. BRANCHE 1 : PRENDRE L'OBJET
        currentSelection.add(item);
        List<EquipmentItem> solutionTake = sortBestEquipmentV2(candidats, currentSelection, nbParticipant, index + 1);

        // Backtracking (On retire pour tester l'autre branche)
        currentSelection.remove(currentSelection.size() - 1);

        // 4. BRANCHE 2 : SAUTER L'OBJET
        List<EquipmentItem> solutionSkip = sortBestEquipmentV2(candidats, currentSelection, nbParticipant, index + 1);

        // 5. COMPARAISON DES SOLUTIONS
        if (solutionTake == null) return solutionSkip;
        if (solutionSkip == null) return solutionTake;

        // Critère : On privilégie la solution avec le MOINS d'items (pour éviter d'avoir 15 petites tentes)
        if (solutionTake.size() < solutionSkip.size()) {
            return solutionTake;
        } else if (solutionSkip.size() < solutionTake.size()) {
            return solutionSkip;
        }

        // Si égalité de taille, on privilégie 'Take' car la liste 'candidats' est supposée triée par qualité/rentabilité
        return solutionTake;
    }

    /**
     * Algorithme d'optimisation V2 pour la nourriture.
     * Maximise les calories tout en minimisant le poids transporté.
     */
    public static List<FoodProduct> getOptimizeAllFoodV2(Hike hike) {
        // 1. Objectif : Besoin Total (Participants * Jours)
        int targetKcal = hike.getCaloriesForAllParticipants();

        if (targetKcal == 0) return new ArrayList<>();

        // 2. Récupérer et convertir le Set en List
        List<FoodProduct> allFood = new ArrayList<>(hike.getFoodCatalogue());

        // 3. TRI PAR DENSITÉ ÉNERGÉTIQUE (Kcal / Poids)
        // Les aliments les plus "rentables" (légers mais caloriques) en premier
        allFood.sort((f1, f2) -> {
            double density1 = (double) f1.getTotalKcals() / f1.getTotalMasses();
            double density2 = (double) f2.getTotalKcals() / f2.getTotalMasses();
            return Double.compare(density2, density1);
        });

        // 4. Lancement Récursif
        List<FoodProduct> optimizedList = sortBestFoodRecursive(
                allFood,
                new ArrayList<>(),
                targetKcal,
                0
        );

        if (optimizedList == null) {
            System.out.println("Attention : Pas assez de nourriture dans le catalogue pour couvrir " + targetKcal + " kcal.");
            return new ArrayList<>(); // Ou retourner tout ce qu'on a, selon ton besoin
        }

        return optimizedList;
    }

    /**
     * Moteur récursif pour la nourriture.
     * Cherche à atteindre targetKcal avec le poids total minimum.
     */
    private static List<FoodProduct> sortBestFoodRecursive(
            List<FoodProduct> candidats,
            List<FoodProduct> currentSelection,
            int targetKcal,
            int index) {

        // --- A. SUCCÈS ? ---
        int currentKcal = currentSelection.stream()
                .mapToInt(FoodProduct::getTotalKcals)
                .sum();

        if (currentKcal >= targetKcal) {
            return new ArrayList<>(currentSelection);
        }

        // --- B. IMPASSE ? ---
        if (index >= candidats.size()) {
            return null;
        }

        FoodProduct item = candidats.get(index);

        // --- C. BRANCHE 1 : PRENDRE ---
        currentSelection.add(item);
        List<FoodProduct> solutionTake = sortBestFoodRecursive(candidats, currentSelection, targetKcal, index + 1);

        // Backtrack
        currentSelection.remove(currentSelection.size() - 1);

        // --- D. BRANCHE 2 : SKIP ---
        List<FoodProduct> solutionSkip = sortBestFoodRecursive(candidats, currentSelection, targetKcal, index + 1);

        // --- E. ARBITRAGE (LE PLUS LÉGER) ---
        if (solutionTake == null) return solutionSkip;
        if (solutionSkip == null) return solutionTake;

        // Comparaison des masses totales pour minimiser le portage
        int massTake = solutionTake.stream().mapToInt(FoodProduct::getTotalMasses).sum();
        int massSkip = solutionSkip.stream().mapToInt(FoodProduct::getTotalMasses).sum();

        return (massTake <= massSkip) ? solutionTake : solutionSkip;
    }
}
