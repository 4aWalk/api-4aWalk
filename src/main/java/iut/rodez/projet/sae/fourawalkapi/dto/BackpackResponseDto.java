package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.Backpack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * DTO représentant l'état du sac à dos d'un participant.
 */
public class BackpackResponseDto {
    private Long id; // C'est mieux d'avoir l'ID du sac aussi
    private double poidsActuelKg;
    private double capaciteMaxKg;
    private double pourcentageRemplissage;
    private List<String> itemsContenus; // Liste des noms des objets

    // CONSTRUCTEUR MODIFIÉ : Il prend l'Entité en paramètre
    public BackpackResponseDto(Backpack backpack) {
        this.id = backpack.getId();
        this.capaciteMaxKg = backpack.getOwner().getCapaciteEmportMaxKg();

        // On suppose que l'entité a une méthode pour le poids actuel, sinon on le calcule
        // Si tu as backpack.getCurrentWeight(), utilise-le. Sinon :
        backpack.updateAndGetTotalMass();
        this.poidsActuelKg = backpack.getTotalMassKg();

        // Calcul du pourcentage (éviter division par zéro)
        if (this.capaciteMaxKg > 0) {
            this.pourcentageRemplissage = (this.poidsActuelKg / this.capaciteMaxKg) * 100;
        } else {
            this.pourcentageRemplissage = 100;
        }

        // Fusionner les noms des équipements et de la nourriture pour l'affichage simple
        this.itemsContenus = new ArrayList<>();

        if (backpack.getEquipmentItems() != null) {
            this.itemsContenus.addAll(backpack.getEquipmentItems().stream()
                    .map(item -> item.getNom() + " (Eq)") // Ajout d'un petit tag pour différencier
                    .collect(Collectors.toList()));
        }

        if (backpack.getFoodItems() != null) {
            this.itemsContenus.addAll(backpack.getFoodItems().stream()
                    .map(food -> food.getNom() + " (Nourriture)")
                    .collect(Collectors.toList()));
        }
    }

    public Long getId() { return id; }
    public double getPoidsActuelKg() { return poidsActuelKg; }
    public double getCapaciteMaxKg() { return capaciteMaxKg; }
    public double getPourcentageRemplissage() { return pourcentageRemplissage; }
    public List<String> getItemsContenus() { return itemsContenus; }
}