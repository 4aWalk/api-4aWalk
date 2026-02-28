package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Backpack;
import iut.rodez.projet.sae.fourawalkapi.entity.EquipmentItem;
import iut.rodez.projet.sae.fourawalkapi.model.Item;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.BroughtEquipmentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service métier V3 : Répartition algorithmique optimisée (Branch & Bound).
 * Approche gloutonne couplée à un backtracking élagué pour des performances maximales.
 */
@Service
public class BackpackDistributorServiceV3 {

    private final BroughtEquipmentRepository broughtEquipmentRepository;

    public BackpackDistributorServiceV3(BroughtEquipmentRepository broughtEquipmentRepository) {
        this.broughtEquipmentRepository = broughtEquipmentRepository;
    }

    public void distributeBatchesToBackpacks(List<Item> itemsToPack, List<Backpack> backpacks, Long hikeId) {

        backpacks.forEach(Backpack::clearContent);

        // 1. Pré-calcul pour le Fail-Fast
        double totalItemsWeight = itemsToPack.stream()
                .mapToDouble(i -> i.getMasseGrammes() * i.getNbItem())
                .sum();

        double totalBackpacksCapacity = backpacks.stream()
                .mapToDouble(Backpack::getSpaceRemainingGrammes)
                .sum();

        // Si le poids total dépasse la capacité max combinée, on stoppe net
        if (totalItemsWeight > totalBackpacksCapacity) {
            throw new RuntimeException("Répartition impossible : Le poids total dépasse la capacité max des sacs.");
        }

        // Tri décroissant des objets (Heuristique First-Fit Decreasing)
        // Les objets les plus lourds sont les plus difficiles à placer, on les gère en premier.
        itemsToPack.sort((i1, i2) -> {
            double w1 = i1.getMasseGrammes() * i1.getNbItem();
            double w2 = i2.getMasseGrammes() * i2.getNbItem();
            return Double.compare(w2, w1);
        });

        // 2. Lancement de la résolution optimisée avec passage du poids restant
        boolean success = solveBranchAndBound(0, itemsToPack, backpacks, totalItemsWeight, hikeId);

        if (!success) {
            throw new RuntimeException("Répartition impossible : Objets trop volumineux pour l'espace des sacs disponibles.");
        }
    }

    /**
     * Algorithme récursif avec élagage (Branch and Bound).
     * * @param index L'index de l'objet actuel.
     * @param items Liste des objets triés.
     * @param backpacks Liste des sacs.
     * @param remainingWeight Poids total des objets qu'il reste à placer.
     * @return true si une solution est trouvée.
     */
    private boolean solveBranchAndBound(int index, List<Item> items, List<Backpack> backpacks, double remainingWeight, Long hikeId) {

        // Cas de base : tout est placé
        if (index >= items.size()) {
            return true;
        }

        // --- OPTIMISATION CRUCIALE : Élagage (Branch & Bound) ---
        // On calcule l'espace total actuellement libre dans tous les sacs.
        double currentAvailableSpace = backpacks.stream()
                .mapToDouble(Backpack::getSpaceRemainingGrammes)
                .sum();

        // Si l'espace libre total est devenu strictement inférieur au poids qu'il nous reste à placer,
        // c'est une impasse (Dead-end). Inutile de continuer à creuser cette branche !
        if (currentAvailableSpace < remainingWeight) {
            return false;
        }

        Item currentItem = items.get(index);
        double batchWeight = currentItem.getMasseGrammes() * currentItem.getNbItem();

        // Ajout : Récupération du sac prioritaire si c'est un vêtement ou repos
        Backpack preferredBackpack = getPreferredOwnerBackpack(currentItem, backpacks, hikeId);

        // Création de la liste des candidats à tester.
        // Si on a un sac prioritaire, on le place en tête de liste, sinon on utilise la liste telle quelle.
        List<Backpack> candidateBackpacks = backpacks;
        if (preferredBackpack != null) {
            candidateBackpacks = new ArrayList<>(backpacks);
            candidateBackpacks.remove(preferredBackpack);
            candidateBackpacks.add(0, preferredBackpack);
        }

        // Itération classique (First-Fit)
        // Contrairement à la V2, on NE TRIE PAS les sacs ici. Le coût CPU d'un tri à chaque appel est trop lourd.
        for (Backpack backpack : candidateBackpacks) {

            if (backpack.canAddWeightGrammes(batchWeight)) {

                // On place l'objet
                backpack.addItem(currentItem);

                // Appel récursif en déduisant le poids de l'objet qu'on vient de placer
                if (solveBranchAndBound(index + 1, items, backpacks, remainingWeight - batchWeight, hikeId)) {
                    return true;
                }

                // Backtracking : on retire l'objet pour tester une autre combinaison
                backpack.removeItem(currentItem);
            }
        }

        return false; // Échec pour cette branche
    }

    /**
     * Détermine si l'objet a un propriétaire et retourne son sac.
     * Restreint aux équipements de type VETEMENT et REPOS.
     */
    private Backpack getPreferredOwnerBackpack(Item item, List<Backpack> backpacks, Long hikeId) {
        // On vérifie que c'est bien un équipement (et non de la nourriture)
        if (item instanceof EquipmentItem equipmentItem) {
            TypeEquipment type = equipmentItem.getType();

            // Si c'est un vêtement ou du repos
            if (type == TypeEquipment.VETEMENT || type == TypeEquipment.REPOS) {
                Long ownerId = broughtEquipmentRepository.getIfExistParticipantForEquipmentAndHike(hikeId, item.getId());

                if (ownerId != null) {
                    for(Backpack backpack : backpacks) {
                        if(backpack.getOwner().getId().equals(ownerId)) {return backpack;}
                    }
                }
            }
        }
        return null; // Aucun propriétaire ou type non éligible
    }
}