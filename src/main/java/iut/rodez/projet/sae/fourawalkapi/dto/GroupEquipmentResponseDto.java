package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.GroupEquipment;
import iut.rodez.projet.sae.fourawalkapi.entity.Participant;

import java.util.List;
import java.util.Map;

/**
 * Data transfert object utilisé dans les communications d'objet groupe équipement avec le client
 */
public class GroupEquipmentResponseDto {
    private Long id;
    private String type;
    private List<EquipmentResponseDto> items;

    /**
     * Constructeur qui prend en compte les propriétaires
     * @param group groupe d'équipement à mapper
     * @param equipmentOwners Map associant l'ID d'un équipement à son Propriétaire (Participant)
     */
    public GroupEquipmentResponseDto(GroupEquipment group, Map<Long, Participant> equipmentOwners) {
        this.id = group.getId();

        if (group.getType() != null) {
            this.type = group.getType().name();
        }

        if (group.getItems() != null) {
            this.items = group.getItems().stream()
                    .map(item -> {
                        // On cherche si cet équipement a un propriétaire dans la Map
                        Participant owner = equipmentOwners != null ? equipmentOwners.get(item.getId()) : null;

                        // On utilise l'autre constructeur de EquipmentResponseDto
                        return new EquipmentResponseDto(item, owner);
                    })
                    .toList();
        }
    }

    // Getters
    public Long getId() { return id; }
    public String getType() { return type; }
    public List<EquipmentResponseDto> getItems() { return items; }
}