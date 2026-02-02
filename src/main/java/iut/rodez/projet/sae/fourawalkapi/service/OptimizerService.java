package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.*;
import iut.rodez.projet.sae.fourawalkapi.model.Item;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OptimizerService {

    private static EquipmentItemService equipmentService = null;

    public OptimizerService(EquipmentItemService es) {
        this.equipmentService = es;
    }

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

    public static List<EquipmentItem> getOptimizeAllEquipmentV2(Hike hike) {
        List<TypeEquipment> typeList = new ArrayList<>(Arrays.asList(TypeEquipment.values()));

        List<EquipmentItem> equipmentOptimized = new ArrayList<>();

        // Suppression des types inutiles
        typeList.remove(TypeEquipment.AUTRE);
        if (hike.getDureeJours() == 1) {
            typeList.remove(TypeEquipment.REPOS);
        }

        for (TypeEquipment type : typeList) {
            // Récupération des objets (supposés triés par rentabilité via la requête SQL/Service)
            List<EquipmentItem> itemsDispo = equipmentService.getEquipmentByType(hike.getId(), type);

            List<EquipmentItem> bestItemsForType = sortBestEquipmentV2(
                    itemsDispo,
                    new ArrayList<>(),
                    hike.getParticipants().size(),
                    0
            );

            if (bestItemsForType != null) {
                equipmentOptimized.addAll(bestItemsForType);
            } else {
                throw new RuntimeException("La vérification métier pour le type d'équipement " + type + " n'est pa valide");
            }
        }

        return  equipmentOptimized;
    }

    /**
     * Retourne la combinaison d'objets la plus rentable pour couvrir le besoin.
     * @param index : curseur de lecture (évite le removeFirst)
     */
    private static List<EquipmentItem> sortBestEquipmentV2(
            List<EquipmentItem> candidats,
            List<EquipmentItem> currentSelection,
            int nbParticipant,
            int index) {

        int couverture = currentSelection.stream().mapToInt(EquipmentItem::getNbItem).sum();

        if (couverture >= nbParticipant) {
            // SUCCÈS : On a assez d'objets, on renvoie une COPIE de la solution
            return new ArrayList<>(currentSelection);
        }

        // On a tout parcouru dans cette branche mais couverture insuffisante.
        // Ce chemin est une impasse.
        if (index >= candidats.size()) {
            return null;
        }

        EquipmentItem item = candidats.get(index);

        currentSelection.add(item);
        // On explore la suite AVEC cet objet
        List<EquipmentItem> solutionTake = sortBestEquipmentV2(candidats, currentSelection, nbParticipant, index + 1);

        // BACKTRACKING : On nettoie pour tester l'autre branche proprement
        currentSelection.remove(currentSelection.size() - 1);


        // On explore la suite SANS cet objet
        List<EquipmentItem> solutionSkip = sortBestEquipmentV2(candidats, currentSelection, nbParticipant, index + 1);



        // Si l'option "Prendre" n'a pas abouti (null), on prend l'option "Sauter"
        if (solutionTake == null) return solutionSkip;

        // Si l'option "Sauter" n'a pas abouti (null), on prend l'option "Prendre"
        if (solutionSkip == null) return solutionTake;

        // Si LES DEUX fonctionnent, lequel est le meilleur ?
        // Critère 1 : Le moins d'objets possible (car tu veux "1 à 3 équipements")
        if (solutionTake.size() < solutionSkip.size()) {
            return solutionTake;
        } else if (solutionSkip.size() < solutionTake.size()) {
            return solutionSkip;
        }

        // Critère 2 (Egalité de taille) : On prend 'solutionTake' car ta liste d'entrée
        // est triée par rentabilité, donc les premiers objets sont intrinsèquement meilleurs.
        return solutionTake;
    }

    public static List<FoodProduct> getOptimizeAllFoodV2(Hike hike) {
        // 1. Objectif : Besoin Total (Participants * Jours)
        // On utilise ta méthode corrigée dans Hike (pense à bien vérifier le +=)
        int targetKcal = hike.getCaloriesForAllParticipants();

        // 2. Récupérer et convertir le Set en List
        List<FoodProduct> allFood = new ArrayList<>(hike.getFoodCatalogue());

        // 3. TRI PAR RENTABILITÉ (Densité Énergétique)
        // On utilise tes méthodes : (Total Kcal / Total Masse)
        allFood.sort((f1, f2) -> {
            double density1 = (double) f1.getTotalKcals() / f1.getTotalMasses();
            double density2 = (double) f2.getTotalKcals() / f2.getTotalMasses();
            // Descendant : Le plus dense en premier
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
            // Tu peux throw une exception ou retourner une liste vide selon ton choix
            System.out.println("Attention : Pas assez de nourriture pour couvrir " + targetKcal + " kcal.");
            return new ArrayList<>();
        }

        return optimizedList;
    }

    /**
     * Moteur récursif utilisant getTotalKcals() et getTotalMasses()
     */
    private static List<FoodProduct> sortBestFoodRecursive(
            List<FoodProduct> candidats,
            List<FoodProduct> currentSelection,
            int targetKcal,
            int index) {

        // --- A. SUCCÈS ? ---
        int currentKcal = currentSelection.stream()
                .mapToInt(FoodProduct::getTotalKcals) // Utilisation de ta méthode
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

        // Comparaison des masses totales
        int massTake = solutionTake.stream().mapToInt(FoodProduct::getTotalMasses).sum();
        int massSkip = solutionSkip.stream().mapToInt(FoodProduct::getTotalMasses).sum();

        // On retourne la liste qui pèse le moins lourd
        return (massTake <= massSkip) ? solutionTake : solutionSkip;
    }
}
