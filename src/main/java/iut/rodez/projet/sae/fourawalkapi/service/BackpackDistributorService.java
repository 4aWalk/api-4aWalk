package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Backpack;
import iut.rodez.projet.sae.fourawalkapi.model.Item;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class BackpackDistributorService {

    /**
     * Tente de répartir les items dans les sacs.
     * Modifie directement les instances de Backpack passées en paramètre.
     *
     * @param itemsToPack Liste mixte (Nourriture + Equipement) optimisée.
     * @param backpacks   Liste des sacs des participants.
     * @throws RuntimeException si la répartition est impossible.
     */
    public void distributeBatchesToBackpacks(List<Item> itemsToPack, List<Backpack> backpacks) {

        // 1. Nettoyage préalable (sécurité)
        // On vide les sacs virtuellement pour refaire le calcul proprement
        // (Attention : si tu veux garder de l'eau ou autre, ne fais pas clearContent)
        backpacks.forEach(Backpack::clearContent);

        // 2. TRI DÉCROISSANT SUR LE POIDS TOTAL DU LOT
        // On place les "gros blocs" (ex: Tente 2kg) en premier.
        itemsToPack.sort((i1, i2) -> {
            double totalWeight1 = i1.getMasseGrammes() * i1.getNbItem();
            double totalWeight2 = i2.getMasseGrammes() * i2.getNbItem();
            return Double.compare(totalWeight2, totalWeight1); // Descendant
        });

        // 3. Lancer le Backtracking
        boolean success = solveStrictBinPacking(0, itemsToPack, backpacks);

        // 4. Verdict
        if (!success) {
            throw new RuntimeException("Répartition impossible ! Le volume total dépasse la capacité cumulée ou un objet est trop lourd pour un seul sac.");
        }
    }

    /**
     * Algorithme récursif (Backtracking).
     * @param index Index de l'item à placer dans la liste triée.
     */
    private boolean solveStrictBinPacking(int index, List<Item> items, List<Backpack> backpacks) {

        // CAS DE BASE : Tous les items sont placés avec succès
        if (index >= items.size()) {
            return true;
        }

        Item currentItem = items.get(index);

        // Poids du LOT COMPLET (ex: 3 boites de conserve = 1500g)
        double batchWeightGrammes = currentItem.getMasseGrammes() * currentItem.getNbItem();

        // OPTIMISATION : Trier les sacs pour tester celui qui a le plus de place libre en premier
        // Cela augmente les chances de trouver une solution rapidement ("Best Fit")
        backpacks.sort(Comparator.comparingDouble(Backpack::getSpaceRemainingGrammes).reversed());

        // Boucle sur chaque sac pour tenter d'y mettre l'objet
        for (Backpack backpack : backpacks) {

            // Est-ce que ça rentre ?
            if (backpack.canAddWeightGrammes(batchWeightGrammes)) {

                // DO : On ajoute l'item
                backpack.addItem(currentItem);

                // RECURSE : On essaie de placer l'item suivant
                if (solveStrictBinPacking(index + 1, items, backpacks)) {
                    return true; // Chemin valide trouvé !
                }

                // UNDO (Backtracking) : Ça coince plus loin, on retire l'objet pour tester le sac suivant
                backpack.removeItem(currentItem);
            }
        }

        // Si on a testé tous les sacs sans succès pour cet item
        return false;
    }
}