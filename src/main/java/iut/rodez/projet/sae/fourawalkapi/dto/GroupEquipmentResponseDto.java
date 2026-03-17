package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.GroupEquipment;
import iut.rodez.projet.sae.fourawalkapi.entity.Participant;

import java.util.List;
import java.util.Map;

public class GroupEquipmentResponseDto {
    private Long id;
    private String type;
    private List<EquipmentResponseDto> items;

    public GroupEquipmentResponseDto(GroupEquipment group, Map<Long, List<Participant>> equipmentOwners) {
        this.id = group.getId();

        if (group.getType() != null) {
            this.type = group.getType().name();
        }

        if (group.getItems() != null) {
            this.items = group.getItems().stream()
                    .map(item -> {
                        List<Participant> owners = (equipmentOwners != null)
                                ? equipmentOwners.getOrDefault(item.getId(), List.of())
                                : List.of();
                        return new EquipmentResponseDto(item, owners);
                    })
                    .toList();
        }
    }

    public Long getId() { return id; }
    public String getType() { return type; }
    public List<EquipmentResponseDto> getItems() { return items; }
}