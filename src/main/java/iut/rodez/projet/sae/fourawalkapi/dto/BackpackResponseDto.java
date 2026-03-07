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
    private List<GroupEquipmentResponseDto> equipements;
    private List<FoodProductResponseDto> nourriture;


    /**
     * Mapper entity to dto
     * @param backpack backpack à mapper en dto
     * @param equipmentOwners Map associant l'ID d'un équipement à son Propriétaire
     */
    public BackpackResponseDto(Backpack backpack, Map<Long, Participant> equipmentOwners) {
        // Possibilité que le sac soit vide ou que le particpant n'en est pas
        if (backpack == null) return;

        this.id = backpack.getId();
        this.capaciteMaxKg = (backpack.getOwner() != null) ? backpack.getOwner().getCapaciteEmportMaxKg() : 0.0;
        this.poidsActuelKg = backpack.getTotalMassKg();

        // Conversion de Map d'équipment en List
        this.equipements = new ArrayList<>();
        if (backpack.getGroupEquipments() != null) {
            this.equipements = backpack.getGroupEquipments().values().stream()
                    // NOUVEAUTÉ : On passe la map au groupe
                    .map(group -> new GroupEquipmentResponseDto(group, equipmentOwners))
                    .toList();
        }

        this.nourriture = new ArrayList<>();
        if (backpack.getFoodItems() != null) {
            this.nourriture = backpack.getFoodItems().stream()
                    .map(FoodProductResponseDto::new)
                    .toList();
        }
    }

    // --- Getters & Setters ---
    public Long getId() { return id; }

    public double getPoidsActuelKg() { return poidsActuelKg; }

    public double getCapaciteMaxKg() { return capaciteMaxKg; }

    public List<GroupEquipmentResponseDto> getEquipements() { return equipements; }

    public List<FoodProductResponseDto> getNourriture() { return nourriture; }
}