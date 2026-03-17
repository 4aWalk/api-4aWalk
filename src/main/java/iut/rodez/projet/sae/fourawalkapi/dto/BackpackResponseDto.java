package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.Backpack;
import iut.rodez.projet.sae.fourawalkapi.entity.Participant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Data transfert object utilisé dans les communications d'objet sac à dos avec le client
 */
public class BackpackResponseDto {

    private Long id;
    private double poidsActuelKg;
    private double capaciteMaxKg;
    // La liste contient maintenant directement les équipements
    private List<EquipmentResponseDto> equipements;
    private List<FoodProductResponseDto> nourriture;

    /**
     * Mapper entity to dto
     * @param backpack backpack à mapper en dto
     * @param equipmentOwners Map associant l'ID d'un équipement à son Propriétaire
     */
    public BackpackResponseDto(Backpack backpack, Map<Long, List<Participant>> equipmentOwners) {
        if (backpack == null) return;

        this.id = backpack.getId();
        this.capaciteMaxKg = (backpack.getOwner() != null) ? backpack.getOwner().getCapaciteEmportMaxKg() : 0.0;
        this.poidsActuelKg = backpack.getTotalMassKg();

        this.equipements = new ArrayList<>();
        if (backpack.getEquipmentItems() != null) {
            this.equipements = backpack.getEquipmentItems().stream()
                    .map(item -> {
                        // On récupère la liste des propriétaires, ou liste vide si aucun
                        List<Participant> owners = (equipmentOwners != null)
                                ? equipmentOwners.getOrDefault(item.getId(), List.of())
                                : List.of();
                        return new EquipmentResponseDto(item, owners);
                    })
                    .toList();
        }

        this.nourriture = new ArrayList<>();
        if (backpack.getFoodItems() != null) {
            this.nourriture = backpack.getFoodItems().stream()
                    .map(FoodProductResponseDto::new)
                    .toList();
        }
    }

    // --- Getters ---
    public Long getId() { return id; }

    public double getPoidsActuelKg() { return poidsActuelKg; }

    public double getCapaciteMaxKg() { return capaciteMaxKg; }

    public List<EquipmentResponseDto> getEquipements() { return equipements; }

    public List<FoodProductResponseDto> getNourriture() { return nourriture; }
}