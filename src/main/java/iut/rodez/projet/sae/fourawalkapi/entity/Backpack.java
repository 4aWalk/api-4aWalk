package iut.rodez.projet.sae.fourawalkapi.entity;

import iut.rodez.projet.sae.fourawalkapi.model.Item;
import jakarta.persistence.*;

import java.util.*;

/**
 * Sac à dos rattacher à un participant. Contient l'ensemble de la nourriture et des équipements
 * que doit transporter un participant. Les emports sont contraints par le poids maximal
 * que le participants peut transporter
 */
@Entity
@Table(name = "backpacks")
public class Backpack {

    /* identifiant du sac à dos */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Poids total du sac */
    private double totalMassKg;

    /** Participant possédant le sac */
    @OneToOne
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant owner;

    /* Nourriture que le sac contient */
    @ManyToMany
    @JoinTable(
            name = "backpack_food",
            joinColumns = @JoinColumn(name = "backpack_id"),
            inverseJoinColumns = @JoinColumn(name = "food_id"))
    private Set<FoodProduct> foodItems = new HashSet<>();

    /* Équipements en vrac que le sac contient */
    @ManyToMany
    @JoinTable(
            name = "backpack_equipment",
            joinColumns = @JoinColumn(name = "backpack_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_id"))
    private Set<EquipmentItem> equipmentItems = new HashSet<>();

    // --- Constructeurs ---

    public Backpack() {
    }

    public Backpack(Participant owner) {
        this();
        this.owner = owner;
    }

    // --- Logique métier ---

    public void clearContent() {
        this.foodItems.clear();
        this.equipmentItems.clear();
        this.totalMassKg = 0.0;
    }

    // --- Getters et Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Participant getOwner() { return owner; }
    public void setOwner(Participant owner) { this.owner = owner; }

    public Set<FoodProduct> getFoodItems() { return foodItems; }
    public void setFoodItems(Set<FoodProduct> foodItems) { this.foodItems = foodItems; }

    public Set<EquipmentItem> getEquipmentItems() { return equipmentItems; }
    public void setEquipmentItems(Set<EquipmentItem> equipmentItems) {
        this.equipmentItems = equipmentItems;
    }


    /**
     * Recalcule le poids total du sac (Nourriture + Équipement)
     */
    public void updateTotalMass() {
        double mass = 0.0;
        if (equipmentItems != null) {
            mass += equipmentItems.stream()
                    .mapToDouble(EquipmentItem::getTotalMassesKg)
                    .sum();
        }

        if (foodItems != null) {
            mass += foodItems.stream()
                    .mapToDouble(FoodProduct::getTotalMassesKg)
                    .sum();
        }

        this.totalMassKg = mass;
    }

    /**
     * Récupère le poids total du sac
     * @return poids du sac actuel
     */
    public double getTotalMassKg() {
        updateTotalMass();
        return this.totalMassKg;
    }

    /**
     * Récupère la capacité du sac
     * @return capacité du sac à dos maximal
     */
    @Transient
    public double getCapacityMaxKg() {
        return owner != null ? owner.getCapaciteEmportMaxKg() : 0.0;
    }

    /**
     * Détermine si pour un poids donnée le sac pour le contenir
     * @param weightInGrammes poids à tester
     * @return true si le poids peut être emporté, false sinon
     */
    public boolean canAddWeightGrammes(double weightInGrammes) {
        double weightInKg = weightInGrammes / 1000.0;
        updateTotalMass();
        return (this.totalMassKg + weightInKg) <= getCapacityMaxKg();
    }

    /**
     * Méthode générique pour ajouter un Item
     * Item peu être soit une nourriture ou un équipement
     * @param item nourriture ou équipement à ajouter
     */
    public void addItem(Item item) {
        if (item instanceof FoodProduct food) {
            this.foodItems.add(food);
        }
        else if (item instanceof EquipmentItem equip) {
            this.equipmentItems.add(equip);
        }
    }

    /**
     * Retire un item du sac à dos
     * Item peu être soit une nourriture ou un équipement
     * @param item nourriture ou équipement à retirer du sac
     */
    public void removeItem(Item item) {
        if (item instanceof FoodProduct food) {
            this.foodItems.remove(food);
        } else if (item instanceof EquipmentItem equip) {
            this.equipmentItems.remove(equip); // <-- Directement à la poubelle
        }
    }

    /**
     * Calcul de la capacité restante
     * @return l'espace restant dans le sac en gramme
     */
    @Transient
    public double getSpaceRemainingGrammes() {
        updateTotalMass();
        return (getCapacityMaxKg() - this.totalMassKg) * 1000.0;
    }
}