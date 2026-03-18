package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.EquipmentItem;
import iut.rodez.projet.sae.fourawalkapi.entity.FoodProduct;
import iut.rodez.projet.sae.fourawalkapi.entity.GroupEquipment;
import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.exception.BusinessValidationException;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import org.springframework.stereotype.Service;

/**
 * Service de validation des informations de la cohérence des informations d'une randonnée
 */
@Service
public class LogisticsValidationService {

    /**
     * Valide le stock de nourriture :
     * 1. Pas d'item individuel excessivement calorique (Distribution).
     * 2. Le stock total couvre les besoins du groupe.
     * @param hike Randonnée vérifiée
     * @param besoinCalorieTotal Calorie vérifié dans la randonnée
     */
    public void validateHikeFood(Hike hike, int besoinCalorieTotal) {
        // un lot d'aliment ne doit pas représenter plus de 25% des besoins totaux
        double maxCaloriePerItem = hike.getCaloriesForAllParticipants() / 4.0;

        for (FoodProduct food : hike.getFoodCatalogue()) {
            if (food.getApportNutritionnelKcal() * food.getNbItem() > maxCaloriePerItem) {
                throw new BusinessValidationException("Nourriture trop calorique : " + food.getNom());
            }
        }

        // Vérification de la suffisance globale
        if (hike.getCalorieRandonne() < besoinCalorieTotal) {
            throw new BusinessValidationException("Nourriture insuffisante pour la randonnée (" +
                    hike.getCalorieRandonne() + " pour " + besoinCalorieTotal + " requis).");
        }
    }

    /**
     * Valide la couverture en équipement.
     * S'assure qu'il y a au moins 1 item par participant pour chaque catégorie obligatoire.
     * Ignore les catégories optionnelles (AUTRE) ou conditionnelles (REPOS si < 2 jours).
     * @param hike randonnée contrôlée
     */
    public void validateHikeEquipment(Hike hike) {
        if (hike.getEquipmentGroups() == null) return;

        int nbParticipants = hike.getParticipants().size();

        for (TypeEquipment type : TypeEquipment.values()) {

            // Pas de vérification de couverture portant sur les objet de type autre ou vêtement
            boolean isNotAutreOrVetement = type != TypeEquipment.AUTRE && type != TypeEquipment.VETEMENT;
            // Pas besoin de matériel pour le repos si la randonnée dure 1 jour
            boolean needsRepos = !(type == TypeEquipment.REPOS && hike.getDureeJours() < 2);

            if (isNotAutreOrVetement && needsRepos) {
                GroupEquipment group = hike.getEquipmentGroups().get(type);

                // Somme des quantités disponibles dans le groupe d'équipement
                int totalItems = (group != null) ? group.getItems().stream().mapToInt(EquipmentItem::getNbItem).sum() : 0;

                if (totalItems < nbParticipants) {
                    throw new BusinessValidationException("Couverture insuffisante pour le type : " + type);
                }
            }
        }
    }

    /**
     * Vérifie si le groupe dispose d'assez de contenants (gourdes, camelbaks)
     * pour transporter la quantité d'eau requise calculée précédemment.
     * Utilise le delta (Masse Pleine - Masse Vide) pour déduire la capacité en volume.
     * @param hike Randonnée validée
     */
    public void validateCapaciteEmportEauLitre(Hike hike) {
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
            throw new BusinessValidationException(String.format(
                    "Pas assez de gourdes pour couvrir les besoins en eau (Stock: %.2fL, Besoin: %.2fL).",
                    capaciteEmport, besoinTotal
            ));
        }
    }
}