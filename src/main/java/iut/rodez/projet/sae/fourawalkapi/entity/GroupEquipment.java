package iut.rodez.projet.sae.fourawalkapi.entity;

import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "group_equipments")
public class GroupEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_nom", nullable = false)
    private TypeEquipment type;

    // PLUS DE CHAMP HIKE ICI (Relation Unidirectionnelle)
    // C'est géré uniquement par l'entité Hike

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "group_equipment_items",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_id")
    )
    @OrderColumn(name = "item_order")
    private List<EquipmentItem> items = new ArrayList<>();

    public GroupEquipment() {}

    public GroupEquipment(TypeEquipment type) { // Constructeur simplifié
        this.type = type;
    }

    public void addItem(EquipmentItem item) {
        this.items.add(item);
        // Tri automatique
        this.items.sort(Comparator.comparingDouble(e ->
                (double) e.getMasseGrammes()
        ));
    }

    public List<EquipmentItem> getItems() {
        return items;
    }

    public void setItems(List<EquipmentItem> items) {
        this.items = items;
    }

    public Long getId() { return id; }
    public TypeEquipment getType() { return type; }
}