package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.*;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service dédié à l'optimisation combinatoire.
 * Résout des problèmes de type "Sac à dos" et "Couverture d'ensemble"
 * pour sélectionner le matériel et la nourriture les plus adaptés aux contraintes.
 */
@Service
public class OptimizerService {
    /**
     * Algorithme d'optimisation pour l'équipement.
     * Parcourt chaque catégorie d'équipement requise et sélectionne la combinaison
     * la plus légère permettant de couvrir tous les participants.
     *
     * @param hike La randonnée contenant les participants et le catalogue d'équipements.
     * @return Une liste plate des équipements optimisés à emporter.
     * @throws RuntimeException Si une catégorie obligatoire ne peut pas être satisfaite.
     */
    public List<EquipmentItem> getOptimizeAllEquipmentV2(Hike hike) {
        List<TypeEquipment> typeList = new ArrayList<>(Arrays.asList(TypeEquipment.values()));
        List<EquipmentItem> equipmentOptimized = new ArrayList<>();

        typeList.remove(TypeEquipment.AUTRE);

        if (hike.getDureeJours() == 1) {
            typeList.remove(TypeEquipment.REPOS);
        }

        for (TypeEquipment type : typeList) {
            GroupEquipment group = hike.getEquipmentGroups().get(type);
            List<EquipmentItem> itemsDispo = (group != null) ? new ArrayList<>(group.getItems()) : new ArrayList<>();

            List<EquipmentItem> bestItemsForType = sortBestEquipmentV2(
                    itemsDispo,
                    new ArrayList<>(),
                    hike.getParticipants().size(),
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
     * Moteur récursif (Backtracking) pour la sélection d'équipement.
     * Explore l'arbre des possibilités binaires (prendre ou ne pas prendre l'item).
     *
     * @param candidats Liste des équipements disponibles pour ce type.
     * @param currentSelection Liste des équipements actuellement sélectionnés dans cette branche.
     * @param nbParticipant Nombre de personnes à équiper.
     * @param index Index de l'item en cours d'évaluation.
     * @return La liste d'équipements optimale (la plus petite taille) ou null si aucune solution n'est trouvée.
     */
    private List<EquipmentItem> sortBestEquipmentV2(
            List<EquipmentItem> candidats,
            List<EquipmentItem> currentSelection,
            int nbParticipant,
            int index) {

        int couverture = currentSelection.stream().mapToInt(EquipmentItem::getNbItem).sum();

        // Cas de base : Succès, la couverture est suffisante
        if (couverture >= nbParticipant) {
            return new ArrayList<>(currentSelection);
        }

        // Cas de base : Échec, fin de la liste des candidats sans atteindre l'objectif
        if (index >= candidats.size()) {
            return null;
        }

        EquipmentItem item = candidats.get(index);

        // Exploration de la branche : Inclusion de l'item
        currentSelection.add(item);
        List<EquipmentItem> solutionTake = sortBestEquipmentV2(candidats, currentSelection, nbParticipant, index + 1);

        // Backtrack : Retrait de l'item pour explorer l'autre branche
        currentSelection.removeLast();

        // Exploration de la branche : Exclusion de l'item
        List<EquipmentItem> solutionSkip = sortBestEquipmentV2(candidats, currentSelection, nbParticipant, index + 1);

        // Comparaison des résultats des deux branches
        if (solutionTake == null) return solutionSkip;
        if (solutionSkip == null) return solutionTake;

        // Priorité à la solution comportant le plus d'items distincts
        if (solutionTake.size() < solutionSkip.size()) {
            return solutionSkip;
        } else if (solutionSkip.size() < solutionTake.size()) {
            return solutionTake;
        }

        return solutionTake;
    }

    /**
     * Algorithme d'optimisation pour la nourriture.
     * Cherche à atteindre l'objectif calorique total en minimisant le poids transporté.
     *
     * @param hike La randonnée contenant le catalogue de nourriture et les besoins caloriques.
     * @return La liste des aliments sélectionnés pour le voyage.
     */
    public List<FoodProduct> getOptimizeAllFoodV2(Hike hike) {
        int targetKcal = hike.getCaloriesForAllParticipants();

        if (targetKcal == 0) return new ArrayList<>();

        List<FoodProduct> allFood = new ArrayList<>(hike.getFoodCatalogue());

        List<FoodProduct> optimizedList = sortBestFoodRecursive(
                allFood,
                new ArrayList<>(),
                targetKcal,
                0
        );

        if (optimizedList == null) {
            return new ArrayList<>();
        }

        return optimizedList;
    }

    /**
     * Moteur récursif pour la sélection de nourriture.
     * Minimise le poids pour une valeur calorique cible atteinte.
     *
     * @param candidats Liste des aliments disponibles.
     * @param currentSelection Sélection courante.
     * @param targetKcal Objectif calorique à atteindre.
     * @param index Index courant.
     * @return La liste d'aliments la plus légère satisfaisant les besoins caloriques.
     */
    private List<FoodProduct> sortBestFoodRecursive(
            List<FoodProduct> candidats,
            List<FoodProduct> currentSelection,
            int targetKcal,
            int index) {

        int currentKcal = currentSelection.stream().mapToInt(FoodProduct::getTotalKcals).sum();

        if (currentKcal >= targetKcal) {
            return new ArrayList<>(currentSelection);
        }

        if (index >= candidats.size()) {
            return null;
        }

        FoodProduct item = candidats.get(index);

        // Branche inclusion
        currentSelection.add(item);
        List<FoodProduct> solutionTake = sortBestFoodRecursive(candidats, currentSelection, targetKcal, index + 1);
        currentSelection.removeLast();

        // Branche exclusion
        List<FoodProduct> solutionSkip = sortBestFoodRecursive(candidats, currentSelection, targetKcal, index + 1);

        if (solutionTake == null) return solutionSkip;
        if (solutionSkip == null) return solutionTake;

        // Comparaison sur le critère du poids total
        int massTake = solutionTake.stream().mapToInt(FoodProduct::getTotalMasses).sum();
        int massSkip = solutionSkip.stream().mapToInt(FoodProduct::getTotalMasses).sum();

        return (massTake <= massSkip) ? solutionTake : solutionSkip;
    }
}