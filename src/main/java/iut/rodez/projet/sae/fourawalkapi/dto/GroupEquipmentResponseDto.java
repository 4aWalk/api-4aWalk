package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.GroupEquipment;
import java.util.List;
import java.util.stream.Collectors;

public class GroupEquipmentResponseDto {
    private Long id;
    private String type; // Le nom de l'enum (ex: "CUISINE", "COUCHAGE")
    private double poidsTotalKg;
    private List<EquipmentResponseDto> items; // La liste détaillée

    public GroupEquipmentResponseDto(GroupEquipment group) {
        this.id = group.getId();

        // On convertit l'Enum en String pour le JSON
        if (group.getType() != null) {
            this.type = group.getType().name();
        } else {
            this.type = "AUTRE";
        }

        // On utilise ta méthode de calcul du poids
        this.poidsTotalKg = group.getTotalMassesKg();

        // On transforme la liste d'entités items en liste de DTOs items
        if (group.getItems() != null) {
            this.items = group.getItems().stream()
                    .map(EquipmentResponseDto::new)
                    .collect(Collectors.toList());
        }
    }

    // Getters / Setters
    public Long getId() { return id; }
    public String getType() { return type; }
    public double getPoidsTotalKg() { return poidsTotalKg; }
    public List<EquipmentResponseDto> getItems() { return items; }
}