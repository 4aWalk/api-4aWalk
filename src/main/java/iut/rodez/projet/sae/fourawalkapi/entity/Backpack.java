package iut.rodez.projet.sae.fourawalkapi.entity;

import iut.rodez.projet.sae.fourawalkapi.model.Item;
import jakarta.persistence.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Représente le chargement du sac à dos d'un participant.
 */
@Entity
@Table(name = "backpacks")
public class Backpack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Poids total réel porté en Kg */
    private double totalMassKg;

    @OneToOne
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant owner;

    @ManyToMany
    @JoinTable(
            name = "backpack_food",
            joinColumns = @JoinColumn(name = "backpack_id"),
            inverseJoinColumns = @JoinColumn(name = "food_id"))
    private Set<FoodProduct> foodItems = new HashSet<>();

    /** * Map des équipements de groupe.
     * Clé : ID de l'équipement (Long)
     * Valeur : L'entité GroupEquipment
     */
    @ManyToMany
    @JoinTable(
            name = "backpack_group_equipment",
            joinColumns = @JoinColumn(name = "backpack_id"),
            inverseJoinColumns = @JoinColumn(name = "group_equipment_id"))
    @MapKey(name = "id") // Utilise le champ 'id' de GroupEquipment comme clé de la Map
    private Map<Long, GroupEquipment> groupEquipments = new HashMap<>();

    // --- Constructeurs ---

    public Backpack() {
        this.totalMassKg = 0.0;
    }

    public Backpack(Participant owner) {
        this();
        this.owner = owner;
    }

    // --- Logique métier ---

    public void clearContent() {
        this.foodItems.clear();
        this.groupEquipments.clear();
        this.totalMassKg = 0.0;
    }

    // --- Getters et Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Participant getOwner() { return owner; }
    public void setOwner(Participant owner) { this.owner = owner; }

    public Set<FoodProduct> getFoodItems() { return foodItems; }
    public void setFoodItems(Set<FoodProduct> foodItems) { this.foodItems = foodItems; }

    public Map<Long, GroupEquipment> getGroupEquipments() { return groupEquipments; }
    public void setGroupEquipments(Map<Long, GroupEquipment> groupEquipments) { this.groupEquipments = groupEquipments; }

    public void addFoodItems(FoodProduct foodItem) {
        this.foodItems.add(foodItem);
        // On suppose que getTotalMasses() retourne des grammes, d'où la division
        this.totalMassKg += (double) foodItem.getTotalMasses() / 1000.0;
    }

    public void addGroupEquipment(GroupEquipment equipment) {
        // Dans une Map, on utilise put(clé, valeur)
        this.groupEquipments.put(equipment.getId(), equipment);
        updateTotalMass();
    }

    /**
     * Recalcule le poids total (Food + GroupEquipment)
     */
    public void updateTotalMass() {
        double mass = 0.0;

        // Pour iterer sur une Map, on prend ses .values()
        if (groupEquipments != null) {
            mass += groupEquipments.values().stream()
                    .mapToDouble(GroupEquipment::getTotalMassesKg)
                    .sum();
        }

        if (foodItems != null) {
            mass += foodItems.stream()
                    .mapToDouble(FoodProduct::getTotalMassesKg)
                    .sum();
        }

        this.totalMassKg = mass;
    }

    public double getTotalMassKg() { return this.totalMassKg; }

    @Transient
    public double getCapacityMaxKg() {
        return owner != null ? owner.getCapaciteEmportMaxKg() : 0.0;
    }

    public boolean canAddWeightGrammes(double weightInGrammes) {
        double weightInKg = weightInGrammes / 1000.0;
        return (this.totalMassKg + weightInKg) <= getCapacityMaxKg();
    }

    /**
     * Méthode générique pour ajouter un Item.
     * Gère maintenant GroupEquipment via la Map.
     */
    public void addItem(Item item) {
        if (item instanceof FoodProduct) {
            this.foodItems.add((FoodProduct) item);
        } else if (item instanceof GroupEquipment) {
            GroupEquipment ge = (GroupEquipment) item;
            this.groupEquipments.put(ge.getId(), ge);
        }

        double itemTotalWeightKg = (item.getMasseGrammes() * item.getNbItem()) / 1000.0;
        this.totalMassKg += itemTotalWeightKg;
    }

    /**
     * Retire un item (Backtracking).
     */
    public void removeItem(Item item) {
        boolean removed = false;

        if (item instanceof FoodProduct) {
            removed = this.foodItems.remove(item);
        } else if (item instanceof GroupEquipment) {
            // Pour retirer d'une map, on remove par la clé (ID)
            GroupEquipment removedItem = this.groupEquipments.remove(((GroupEquipment) item).getId());
            removed = (removedItem != null);
        }

        if (removed) {
            double itemTotalWeightKg = (item.getMasseGrammes() * item.getNbItem()) / 1000.0;
            this.totalMassKg -= itemTotalWeightKg;

            if (this.totalMassKg < 0) this.totalMassKg = 0.0;
        }
    }

    @Transient
    public double getSpaceRemainingGrammes() {
        return (getCapacityMaxKg() - this.totalMassKg) * 1000.0;
    }
}