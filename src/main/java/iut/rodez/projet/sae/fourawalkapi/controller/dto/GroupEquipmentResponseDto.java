package iut.rodez.projet.sae.fourawalkapi.controller.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.GroupEquipment;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import java.util.List;
import java.util.stream.Collectors;

public class GroupEquipmentResponseDto {

    private TypeEquipment type;
    // On utilise EquipmentResponseDto pour ne pas exposer l'entité brute
    private List<EquipmentResponseDto> items;

    public GroupEquipmentResponseDto(GroupEquipment entity) {
        this.type = entity.getType();
        if (entity.getItems() != null) {
            this.items = entity.getItems().stream()
                    .map(EquipmentResponseDto::new)
                    .collect(Collectors.toList());
        }
    }

    // Getters & Setters
    public TypeEquipment getType() { return type; }
    public void setType(TypeEquipment type) { this.type = type; }
    public List<EquipmentResponseDto> getItems() { return items; }
    public void setItems(List<EquipmentResponseDto> items) { this.items = items; }
}