package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.GroupEquipment;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data transfert object utilisé dans les communications d'objet groupe équipement avec le client
 */
public class GroupEquipmentResponseDto {
    private Long id;
    private String type;
    private List<EquipmentResponseDto> items;

    /**
     * Mapper entity to dto
     * @param group groupe d'équipement à mapper
     */
    public GroupEquipmentResponseDto(GroupEquipment group) {
        this.id = group.getId();

        if (group.getType() != null) {
            this.type = group.getType().name();
        }
        if (group.getItems() != null) {
            this.items = group.getItems().stream()
                    .map(EquipmentResponseDto::new)
                    .collect(Collectors.toList());
        }
    }

    // Getters
    public Long getId() { return id; }
    public String getType() { return type; }
    public List<EquipmentResponseDto> getItems() { return items; }
}