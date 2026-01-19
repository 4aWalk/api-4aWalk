package iut.rodez.projet.sae.fourawalkapi.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/** * Représente le chargement du sac à dos d'un participant.
 * Cette classe est le cœur de l'optimisation.
 */
@Entity
@Table(name = "backpacks")
public class Backpack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Poids total réel porté (calculé lors de l'optimisation)
    private double totalMassKg;

    // Le propriétaire du sac.
    // On utilise 'owner' comme nom de variable Java.
    @OneToOne
    @JoinColumn(name = "participant_id")
    private Participant owner;

    // Contenu : Liste des produits alimentaires.
    // Relation N-N car une référence de produit peut être dans plusieurs sacs.
    @ManyToMany
    @JoinTable(
            name = "backpack_food",
            joinColumns = @JoinColumn(name = "backpack_id"),
            inverseJoinColumns = @JoinColumn(name = "food_id"))
    private Set<FoodProduct> foodItems = new HashSet<>();

    // Contenu : Liste de l'équipement.
    @ManyToMany
    @JoinTable(
            name = "backpack_equipment",
            joinColumns = @JoinColumn(name = "backpack_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_id"))
    private Set<EquipmentItem> equipmentItems = new HashSet<>();

    // Constructeurs
    public Backpack() {}

    public Backpack(Participant owner) {
        this.owner = owner;
    }

    // Getters et Setters
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getTotalMassKg() {
        return totalMassKg;
    }

    public void setTotalMassKg(double totalMassKg) {
        this.totalMassKg = totalMassKg;
    }

    public Participant getOwner() {
        return owner;
    }

    public void setOwner(Participant owner) {
        this.owner = owner;
    }

    public Set<FoodProduct> getFoodItems() {
        return foodItems;
    }

    public void setFoodItems(Set<FoodProduct> foodItems) {
        this.foodItems = foodItems;
    }

    public Set<EquipmentItem> getEquipmentItems() {
        return equipmentItems;
    }

    public void setEquipmentItems(Set<EquipmentItem> equipmentItems) {
        this.equipmentItems = equipmentItems;
    }
}