package iut.rodez.projet.sae.fourawalkapi.entity;

import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "group_equipments")
public class GroupEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- CORRECTION MAJEURE ICI ---
    // On mappe le champ Java "type" sur la colonne SQL "type_nom"
    @Enumerated(EnumType.STRING)
    @Column(name = "type_nom", nullable = false, length = 50)
    private TypeEquipment type;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "group_equipment_items",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_id")
    )
    private List<EquipmentItem> items = new ArrayList<>();

    // --- Constructeurs ---
    public GroupEquipment() {}

    public GroupEquipment(TypeEquipment type) {
        this.type = type;
    }

    // --- Méthodes ---
    public void addItem(EquipmentItem item) {
        this.items.add(item);
    }

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TypeEquipment getType() { return type; }
    public void setType(TypeEquipment type) { this.type = type; }

    public List<EquipmentItem> getItems() { return items; }
    public void setItems(List<EquipmentItem> items) { this.items = items; }

    public double getTotalMassesKg() {
        double totalMassesKg = 0;
        for (EquipmentItem item : items) {
            totalMassesKg += item.getTotalMassesKg();
        }
        return totalMassesKg;
    }
}