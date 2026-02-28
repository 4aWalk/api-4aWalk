package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Backpack;
import iut.rodez.projet.sae.fourawalkapi.model.Item;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Service métier responsable de la répartition algorithmique des charges.
 * Il résout le problème du "Bin Packing" (remplissage de sacs) en utilisant une approche
 * gloutonne combinée à du backtracking pour garantir une solution valide si elle existe.
 */
@Service
public class BackpackDistributorServiceV2 {

    private final BackpackService backpackService;

    /**
     * Injection de dépendance
     * @param backpackService service sac à dos
     */
    public BackpackDistributorServiceV2(BackpackService backpackService) {
        this.backpackService = backpackService;
    }

    /**
     * Orchestre la distribution des objets dans les sacs à dos disponibles.
     * Prépare les données (nettoyage, tri heuristique) avant de lancer l'algorithme récursif.
     *
     * @param itemsToPack Liste des objets (équipements ou nourriture) à répartir.
     * @param backpacks Liste des sacs à dos (conteneurs) disponibles.
     * @param hikeId Identifiant de la randonnée (nécessaire pour retrouver les propriétaires).
     */
    public void distributeBatchesToBackpacks(List<Item> itemsToPack, List<Backpack> backpacks, Long hikeId) {

        // Réinitialisation de l'état des sacs
        backpacks.forEach(Backpack::clearContent);

        // Tri décroissant des objets par poids total (Masse * Quantité)
        itemsToPack.sort((i1, i2) -> {
            double totalWeight1 = i1.getMasseGrammes() * i1.getNbItem();
            double totalWeight2 = i2.getMasseGrammes() * i2.getNbItem();
            return Double.compare(totalWeight2, totalWeight1);
        });

        // Lancement de la résolution récursive avec l'ID de la randonnée
        boolean success = solveStrictBinPacking(0, itemsToPack, backpacks, hikeId);

        if (!success) {
            throw new RuntimeException("Répartition impossible : Capacité totale insuffisante " +
                    "ou objets trop volumineux pour les sacs disponibles.");
        }
    }

    /**
     * Algorithme récursif de résolution par retour sur trace (Backtracking).
     */
    private boolean solveStrictBinPacking(int index, List<Item> items, List<Backpack> backpacks, Long hikeId) {

        // Condition d'arrêt
        if (index >= items.size()) {
            return true;
        }

        Item currentItem = items.get(index);
        double batchWeightGrammes = currentItem.getMasseGrammes() * currentItem.getNbItem();

        // 1. Chercher le sac prioritaire (propriétaire) si VETEMENT ou REPOS
        Backpack preferredBackpack = backpackService.getPreferredOwnerBackpack(currentItem, backpacks, hikeId);

        // 2. Créer une liste de candidats triée par espace disponible (Optimisation gloutonne)
        List<Backpack> candidateBackpacks = new ArrayList<>(backpacks);
        candidateBackpacks.sort(Comparator.comparingDouble(Backpack::getSpaceRemainingGrammes).reversed());

        // 3. Forcer le sac du propriétaire en TOUTE PREMIÈRE position des tentatives
        if (preferredBackpack != null) {
            candidateBackpacks.remove(preferredBackpack); // On l'enlève de sa position actuelle
            candidateBackpacks.add(0, preferredBackpack); // On le met tout devant !
        }

        // 4. Itération sur les conteneurs candidats
        for (Backpack backpack : candidateBackpacks) {

            // Vérification de la contrainte de capacité stricte
            if (backpack.canAddWeightGrammes(batchWeightGrammes)) {

                // Tentative : On ajoute l'objet au sac courant
                backpack.addItem(currentItem);

                // Appel récursif pour tenter de placer l'objet suivant
                if (solveStrictBinPacking(index + 1, items, backpacks, hikeId)) {
                    return true;
                }

                // Backtracking (Annulation) si impasse
                backpack.removeItem(currentItem);
            }
        }

        return false;
    }


}