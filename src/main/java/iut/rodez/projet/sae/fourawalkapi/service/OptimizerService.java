package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.*;
import iut.rodez.projet.sae.fourawalkapi.exception.BusinessValidationException;
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
     * Point de vigilance, l'optimisation des vêtements ne présente pas de décisionnel ils sont tous ajouté
     *
     * @param hike La randonnée contenant les participants et le catalogue d'équipements.
     * @return Une liste plate des équipements optimisés à emporter.
     * @throws BusinessValidationException Si une catégorie obligatoire ne peut pas être satisfaite.
     */
    public List<EquipmentItem> getOptimizeAllEquipment(Hike hike) {
        List<TypeEquipment> typeList = new ArrayList<>(Arrays.asList(TypeEquipment.values()));
        List<EquipmentItem> equipmentOptimized = new ArrayList<>();

        /* Skip de repos si non nécessaire (Rando d'un jour) */
        if (hike.getDureeJours() <= 1) {
            typeList.remove(TypeEquipment.REPOS);
        }

        for (TypeEquipment type : typeList) {
            GroupEquipment group = hike.getEquipmentGroups().get(type);

            // Traitement uniquement si le groupe existe
            if (group != null) {
                List<EquipmentItem> itemsDispo = new ArrayList<>(group.getItems());

                // Les vêtementset autre équipe ont déjà été optimisé par l'utilisateur
                if (type == TypeEquipment.VETEMENT || type == TypeEquipment.AUTRE) {
                    equipmentOptimized.addAll(itemsDispo);
                }
                // Cherche du meilleur de la meilleure combinaison
                else {
                    List<EquipmentItem> bestItemsForType = sortBestEquipment(
                            itemsDispo,
                            new ArrayList<>(),
                            hike.getParticipants().size(),
                            0
                    );

                    if (bestItemsForType != null) {
                        equipmentOptimized.addAll(bestItemsForType);
                    } else {
                        // On garde l'exception ici car c'est une sortie fatale, pas un simple saut
                        throw new BusinessValidationException("Impossible de couvrir les besoins pour : " + type);
                    }
                }
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
    public List<EquipmentItem> sortBestEquipment(
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
        List<EquipmentItem> solutionTake = sortBestEquipment(candidats, currentSelection, nbParticipant, index + 1);

        // Backtrack : Retrait de l'item pour explorer l'autre branche
        currentSelection.removeLast();

        // Exploration de la branche : Exclusion de l'item
        List<EquipmentItem> solutionSkip = sortBestEquipment(candidats, currentSelection, nbParticipant, index + 1);

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
     * Cherche à atteindre l'objectif calorique en minimisant le poids,
     * tout en garantissant qu'aucun aliment n'est surreprésenté.
     * Renvoie une liste vide si l'objectif est de 0 ou si aucune solution n'est trouvée.
     *
     * @param hike La randonnée contenant le catalogue et les participants.
     * @return La liste des aliments sélectionnés, ou une liste vide si impossible.
     */
    public List<FoodProduct> getOptimizeAllFood(Hike hike) {
        int targetKcal = hike.getCaloriesForAllParticipants();

        // Fast-exit
        if (targetKcal <= 0) {
            return new ArrayList<>();
        }

        int nbParticipants = hike.getParticipants().size();

        List<FoodProduct> allFood = new ArrayList<>(hike.getFoodCatalogue());

        // Recherche récursive de la liste de nourriture optimisée
        List<FoodProduct> optimizedList = sortBestFoodRecursive(
                allFood,
                new ArrayList<>(),
                new HashMap<>(),
                targetKcal,
                nbParticipants,
                0
        );

        // Échec return d'une liste vide
        if (optimizedList == null) {
            return new ArrayList<>();
        }

        return optimizedList;
    }

    /**
     * Moteur récursif (Glouton exhaustif avec élagage).
     * @param candidats nourritures encore non explorées
     * @param currentSelection nourritures retenues
     * @param usedAppellations appellations "consommées"
     * @param targetKcal consommation à couvrir
     * @param maxPerAppel Nombre de participant
     * @param index curseur de lecture
     * @return Liste de la nourriture optimisée
     */
    private List<FoodProduct> sortBestFoodRecursive(
            List<FoodProduct> candidats,
            List<FoodProduct> currentSelection,
            Map<String, Integer> usedAppellations,
            int targetKcal,
            int maxPerAppel,
            int index) {

        int currentKcal = currentSelection.stream().mapToInt(FoodProduct::getTotalKcals).sum();

        // Condition d'arrêt 1 : Objectif calorique atteint
        if (currentKcal >= targetKcal) {
            return new ArrayList<>(currentSelection);
        }

        // Condition d'arrêt 2 : Fin de la liste atteinte sans succès
        if (index >= candidats.size()) {
            return null;
        }

        FoodProduct item = candidats.get(index);
        String label = item.getAppellationCourante();
        int currentCountForLabel = usedAppellations.getOrDefault(label, 0);

        List<FoodProduct> solutionTake = null;

        // si on ne dépasse pas le nombre de participants
        if (currentCountForLabel + item.getNbItem() <= maxPerAppel) {
            currentSelection.add(item);
            usedAppellations.put(label, currentCountForLabel + item.getNbItem());

            solutionTake = sortBestFoodRecursive(candidats, currentSelection, usedAppellations, targetKcal, maxPerAppel, index + 1);

            // Backtracking : on annule l'action pour explorer l'autre branche
            currentSelection.removeLast();
            usedAppellations.put(label, currentCountForLabel);
        }

        // BRANCHE EXCLUSION : On ignore cet item et on passe au suivant
        List<FoodProduct> solutionSkip = sortBestFoodRecursive(candidats, currentSelection, usedAppellations, targetKcal, maxPerAppel, index + 1);

        // Évaluation des résultats
        if (solutionTake == null) return solutionSkip;
        if (solutionSkip == null) return solutionTake;

        // Si les deux branches ont trouvé une solution, on garde la plus légère
        int massTake = solutionTake.stream().mapToInt(FoodProduct::getTotalMasses).sum();
        int massSkip = solutionSkip.stream().mapToInt(FoodProduct::getTotalMasses).sum();

        return (massTake <= massSkip) ? solutionTake : solutionSkip;
    }
}