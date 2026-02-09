package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.Backpack;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BackpackResponseDto {

    private Long id;
    private double poidsActuelKg;
    private double capaciteMaxKg;
    private double pourcentageRemplissage;

    // --- SÉPARATION CLAIRE ---
    private List<GroupEquipmentResponseDto> equipements; // Liste de groupes (ex: Cuisine, Dodo...)
    private List<FoodProductResponseDto> nourriture;     // Liste de bouffe

    public BackpackResponseDto() {}

    public BackpackResponseDto(Backpack backpack) {
        if (backpack == null) return;

        this.id = backpack.getId();
        this.capaciteMaxKg = (backpack.getOwner() != null) ? backpack.getOwner().getCapaciteEmportMaxKg() : 0.0;

        // Mise à jour et récupération du poids
        backpack.updateTotalMass();
        this.poidsActuelKg = backpack.getTotalMassKg();

        // Calcul pourcentage
        if (this.capaciteMaxKg > 0) {
            this.pourcentageRemplissage = (this.poidsActuelKg / this.capaciteMaxKg) * 100;
        } else {
            this.pourcentageRemplissage = (this.poidsActuelKg > 0) ? 100 : 0;
        }

        // --- 1. ÉQUIPEMENTS (Map -> List de DTO) ---
        this.equipements = new ArrayList<>();
        if (backpack.getGroupEquipments() != null) {
            // On itère sur les VALEURS de la Map (les objets GroupEquipment)
            this.equipements = backpack.getGroupEquipments().values().stream()
                    .map(GroupEquipmentResponseDto::new)
                    .collect(Collectors.toList());
        }

        // --- 2. NOURRITURE (Set -> List de DTO) ---
        this.nourriture = new ArrayList<>();
        if (backpack.getFoodItems() != null) {
            this.nourriture = backpack.getFoodItems().stream()
                    .map(FoodProductResponseDto::new)
                    .collect(Collectors.toList());
        }
    }

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public double getPoidsActuelKg() { return poidsActuelKg; }
    public void setPoidsActuelKg(double poidsActuelKg) { this.poidsActuelKg = poidsActuelKg; }

    public double getCapaciteMaxKg() { return capaciteMaxKg; }
    public void setCapaciteMaxKg(double capaciteMaxKg) { this.capaciteMaxKg = capaciteMaxKg; }

    public double getPourcentageRemplissage() { return pourcentageRemplissage; }
    public void setPourcentageRemplissage(double pourcentageRemplissage) { this.pourcentageRemplissage = pourcentageRemplissage; }

    public List<GroupEquipmentResponseDto> getEquipements() { return equipements; }
    public void setEquipements(List<GroupEquipmentResponseDto> equipements) { this.equipements = equipements; }

    public List<FoodProductResponseDto> getNourriture() { return nourriture; }
    public void setNourriture(List<FoodProductResponseDto> nourriture) { this.nourriture = nourriture; }
}