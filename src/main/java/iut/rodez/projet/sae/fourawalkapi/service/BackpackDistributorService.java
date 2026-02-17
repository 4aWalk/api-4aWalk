package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Backpack;
import iut.rodez.projet.sae.fourawalkapi.model.Item;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Service métier responsable de la répartition algorithmique des charges.
 * Il résout le problème du "Bin Packing" (remplissage de sacs) en utilisant une approche
 * gloutonne combinée à du backtracking pour garantir une solution valide si elle existe.
 */
@Service
public class BackpackDistributorService {

    /**
     * Orchestre la distribution des objets dans les sacs à dos disponibles.
     * Prépare les données (nettoyage, tri heuristique) avant de lancer l'algorithme récursif.
     *
     * @param itemsToPack Liste des objets (équipements ou nourriture) à répartir.
     * @param backpacks Liste des sacs à dos (conteneurs) disponibles avec leur capacité max.
     * @throws RuntimeException Si la capacité totale des sacs est insuffisante pour le volume d'objets.
     */
    public void distributeBatchesToBackpacks(List<Item> itemsToPack, List<Backpack> backpacks) {

        // Réinitialisation de l'état des sacs pour garantir un calcul sur une base vide
        backpacks.forEach(Backpack::clearContent);

        // Tri décroissant des objets par poids total (Masse * Quantité)
        itemsToPack.sort((i1, i2) -> {
            double totalWeight1 = i1.getMasseGrammes() * i1.getNbItem();
            double totalWeight2 = i2.getMasseGrammes() * i2.getNbItem();
            return Double.compare(totalWeight2, totalWeight1);
        });

        // Lancement de la résolution récursive (Backtracking)
        boolean success = solveStrictBinPacking(0, itemsToPack, backpacks);

        // Gestion de l'échec de l'algorithme (Contraintes impossibles à satisfaire)
        if (!success) {
            throw new RuntimeException("Répartition impossible : Capacité totale insuffisante " +
                    "ou objets trop volumineux pour les sacs disponibles.");
        }
    }

    /**
     * Algorithme récursif de résolution par retour sur trace (Backtracking).
     * Tente de placer chaque objet dans un sac valide, et revient en arrière si une impasse est atteinte.
     *
     * @param index L'index de l'objet actuel à traiter dans la liste items.
     * @param items La liste complète des objets triés.
     * @param backpacks La liste des sacs (conteneurs).
     * @return true si une répartition valide a été trouvée pour tous les objets restants, false sinon.
     */
    private boolean solveStrictBinPacking(int index, List<Item> items, List<Backpack> backpacks) {

        // Condition d'arrêt (Cas de base) : Si tous les objets ont été parcourus, la solution est valide
        if (index >= items.size()) {
            return true;
        }

        Item currentItem = items.get(index);
        double batchWeightGrammes = currentItem.getMasseGrammes() * currentItem.getNbItem();

        // Optimisation Gloutonne (Best Fit Strategy) :
        // On trie dynamiquement les sacs pour proposer en priorité ceux ayant le plus d'espace libre.
        // Cela permet de "lisser" la charge et d'éviter de saturer un petit sac trop vite.
        backpacks.sort(Comparator.comparingDouble(Backpack::getSpaceRemainingGrammes).reversed());

        // Itération sur les conteneurs candidats
        for (Backpack backpack : backpacks) {
            // Vérification de la contrainte de capacité stricte
            if (backpack.canAddWeightGrammes(batchWeightGrammes)) {

                // Tentative : On ajoute l'objet au sac courant
                backpack.addItem(currentItem);

                // Appel récursif pour tenter de placer l'objet suivant (index + 1)
                if (solveStrictBinPacking(index + 1, items, backpacks)) {
                    return true; // Succès : La branche entière est valide
                }

                // Backtracking (Annulation) :
                // Si l'appel récursif a renvoyé false, cela signifie que ce choix menait à une impasse.
                // On retire l'objet du sac pour tester le sac suivant dans la boucle.
                backpack.removeItem(currentItem);
            }
        }

        // Échec : Aucun sac ne pouvait accepter l'objet courant tout en permettant de placer les suivants.
        return false;
    }
}