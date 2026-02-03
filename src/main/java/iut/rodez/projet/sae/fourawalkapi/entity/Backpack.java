package iut.rodez.projet.sae.fourawalkapi.entity;

import iut.rodez.projet.sae.fourawalkapi.model.Item;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Représente le chargement du sac à dos d'un participant.
 * C'est l'entité centrale pour le résultat de l'optimisation du chargement.
 */
@Entity
@Table(name = "backpacks")
public class Backpack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Poids total réel porté en Kg (Somme des équipements et de la nourriture) */
    private double totalMassKg;

    /** Le propriétaire du sac (Lien One-to-One avec Participant) */
    @OneToOne
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant owner;

    /** Liste des produits alimentaires présents dans le sac */
    @ManyToMany
    @JoinTable(
            name = "backpack_food",
            joinColumns = @JoinColumn(name = "backpack_id"),
            inverseJoinColumns = @JoinColumn(name = "food_id"))
    private Set<FoodProduct> foodItems = new HashSet<>();

    /** Liste des équipements présents dans le sac */
    @ManyToMany
    @JoinTable(
            name = "backpack_equipment",
            joinColumns = @JoinColumn(name = "backpack_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_id"))
    private Set<EquipmentItem> equipmentItems = new HashSet<>();

    // --- Constructeurs ---

    public Backpack() {
        this.totalMassKg = 0.0;
    }

    public Backpack(Participant owner) {
        this();
        this.owner = owner;
    }

    // --- Logique métier de bas niveau (Entity Logic) ---


    /** Vide intégralement le contenu du sac */
    public void clearContent() {
        this.foodItems.clear();
        this.equipmentItems.clear();
        this.totalMassKg = 0.0;
    }

    // --- Overrides Standards ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Backpack backpack = (Backpack) o;
        // L'égalité est basée sur l'ID technique ou sur le propriétaire unique
        return Objects.equals(id, backpack.id) || Objects.equals(owner, backpack.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, owner);
    }

    // --- Getters et Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Participant getOwner() { return owner; }
    public void setOwner(Participant owner) { this.owner = owner; }

    public Set<FoodProduct> getFoodItems() { return foodItems; }
    public void setFoodItems(Set<FoodProduct> foodItems) { this.foodItems = foodItems; }

    public Set<EquipmentItem> getEquipmentItems() { return equipmentItems; }
    public void setEquipmentItems(Set<EquipmentItem> equipmentItems) { this.equipmentItems = equipmentItems; }

    public void addFoodItems(FoodProduct foodItem) {
        this.foodItems.add(foodItem);
        this.totalMassKg += (double) foodItem.getTotalMasses() / 1000;
    }

    public void addEquipmentItems(EquipmentItem equipmentItem) {
        this.equipmentItems.add(equipmentItem);
        this.totalMassKg += equipmentItem.getTotalMasses() / 1000;
    }

    /**
     * Recalcule le poids total du sac à partir des masses de chaque item.
     * Cette méthode doit être appelée après chaque modification du contenu.
     */
    public void updateAndGetTotalMass() {
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

    public double getTotalMassKg() { return this.totalMassKg;}
    /**
     * Récupère la capacité max définie par le participant.
     * (Suppose que Participant a une méthode getPoidsMaxSac())
     */
    @Transient
    public double getCapacityMaxKg() {
        // Si ce n'est pas le cas, tu peux mettre une valeur par défaut ou l'ajouter dans Participant
        return owner.getCapaciteEmportMaxKg();
    }

    /**
     * Vérifie si on peut ajouter un poids (en grammes) sans exploser le sac.
     */
    public boolean canAddWeightGrammes(double weightInGrammes) {
        double weightInKg = weightInGrammes / 1000.0;
        return (this.totalMassKg + weightInKg) <= getCapacityMaxKg();
    }

    /**
     * Méthode générique pour ajouter un Item (Food ou Equipment).
     * Gère le polymorphisme et la mise à jour du poids.
     */
    public void addItem(Item item) {
        if (item instanceof FoodProduct) {
            this.foodItems.add((FoodProduct) item);
        } else if (item instanceof EquipmentItem) {
            this.equipmentItems.add((EquipmentItem) item);
        }

        // Mise à jour du poids total (Lot complet)
        // On convertit grammes -> kg
        double itemTotalWeightKg = (item.getMasseGrammes() * item.getNbItem()) / 1000.0;
        this.totalMassKg += itemTotalWeightKg;
    }

    /**
     * CRUCIAL POUR LE BACKTRACKING : Permet de retirer un objet si le chemin est une impasse.
     */
    public void removeItem(Item item) {
        boolean removed = false;

        if (item instanceof FoodProduct) {
            removed = this.foodItems.remove(item);
        } else if (item instanceof EquipmentItem) {
            removed = this.equipmentItems.remove(item);
        }

        if (removed) {
            double itemTotalWeightKg = (item.getMasseGrammes() * item.getNbItem()) / 1000.0;
            this.totalMassKg -= itemTotalWeightKg;

            // Sécurité pour éviter les -0.000001 Kg dus aux arrondis float
            if (this.totalMassKg < 0) this.totalMassKg = 0.0;
        }
    }

    /**
     * Helper pour l'optimisation : Espace restant en grammes
     */
    @Transient
    public double getSpaceRemainingGrammes() {
        return (getCapacityMaxKg() - this.totalMassKg) * 1000.0;
    }
}