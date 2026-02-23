package iut.rodez.projet.sae.fourawalkapi.entity;

import iut.rodez.projet.sae.fourawalkapi.model.Item;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
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

    /* Groupe d'équipement que le sac contient */
    @ManyToMany
    @JoinTable(
            name = "backpack_group_equipment",
            joinColumns = @JoinColumn(name = "backpack_id"),
            inverseJoinColumns = @JoinColumn(name = "group_equipment_id"))
    @MapKey(name = "type")
    private Map<TypeEquipment, GroupEquipment> groupEquipments = new EnumMap<>(TypeEquipment.class);

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

    public Map<TypeEquipment, GroupEquipment> getGroupEquipments() { return groupEquipments; }
    public void setGroupEquipments(Map<TypeEquipment, GroupEquipment> groupEquipments) {
        this.groupEquipments = groupEquipments;
    }


    /**
     * Recalcule le poids total du sac (Nourriture + Équipement)
     */
    public void updateTotalMass() {
        double mass = 0.0;
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
            // Créer le groupe si il s'agit du premier équipement d'un groupe ajouté
            this.groupEquipments.computeIfAbsent(equip.getType(), type -> {
                GroupEquipment newGroup = new GroupEquipment();
                newGroup.setType(type);
                return newGroup;
            }).addItem(equip);
        }
    }

    /**
     * Retire un item du sac à dos
     * Item peu être soit une nourriture ou un équipement
     * @param item nourriture ou équipement à retirer du sac
     */
    public void removeItem(Item item) {
        if (item instanceof FoodProduct) {
            this.foodItems.remove(item);
        } else if (item instanceof EquipmentItem equip) {
            GroupEquipment groupEquipment = this.groupEquipments.get(equip.getType());
            if (groupEquipment != null) {
                groupEquipment.getItems().remove(equip);
                if (groupEquipment.getItems().isEmpty()) {
                    this.groupEquipments.remove(equip.getType());
                }
            }
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