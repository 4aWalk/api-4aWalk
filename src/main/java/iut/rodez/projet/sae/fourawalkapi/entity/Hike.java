package iut.rodez.projet.sae.fourawalkapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import iut.rodez.projet.sae.fourawalkapi.model.Item;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.*;

/**
 * Randonnée contenant l'itinéraire, les participants, les équipements et la nourriture.
 */
@Entity
@Table(name = "hikes")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Hike {

    /* Identifiant de la randonnée */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Libellé de la randonnée */
    @NotBlank(message = "Le libellé de la randonnée est obligatoire")
    @Column(nullable = false)
    private String libelle;

    /* Point de départ */
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private PointOfInterest depart;

    /* Point d'arrivé' */
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private PointOfInterest arrivee;

    /* Durée de la randonnée entre 1 et 3 */
    @Min(value = 1, message = "La durée minimale est de 1 jour")
    @Max(value = 3, message = "La durée maximale est de 3 jours")
    private int dureeJours;

    /* Utilisateur créateur de la randonnée */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    /* Participant de la randonnée (entre 1 et 3) */
    @ManyToMany
    @JoinTable(
            name = "hike_participants",
            joinColumns = @JoinColumn(name = "hike_id"),
            inverseJoinColumns = @JoinColumn(name = "participant_id")
    )
    private Set<Participant> participants = new HashSet<>();

    /* Liste de tous les points d'intêrets à visiter pendant la randonnée */
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "hike_id")
    private Set<PointOfInterest> optionalPoints = new HashSet<>();

    /* Liste de la nourriture ajouté à la randonnée */
    @ManyToMany
    @JoinTable(
            name = "hike_food_products",
            joinColumns = @JoinColumn(name = "hike_id"),
            inverseJoinColumns = @JoinColumn(name = "food_product_id")
    )
    private List<FoodProduct> foodCatalogue = new ArrayList<>();
    /* Liste de l'ensemble des équipements rajoutés à la randonnée */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "hike_id", nullable = false)
    @MapKey(name = "type")
    private Map<TypeEquipment, GroupEquipment> equipmentGroups = new EnumMap<>(TypeEquipment.class);

    // --- Constructeurs ---

    public Hike() {}

    public Hike(String libelle, PointOfInterest depart, PointOfInterest arrivee, int dureeJours, User creator) {
        this.libelle = libelle;
        this.depart = depart;
        this.arrivee = arrivee;
        setDureeJours(dureeJours);
        this.creator = creator;
    }

    /**
     * Ajoute un équipement à la randonné et ordonne la liste
     * pour pré trier la liste à optimisé
     * @param item équipement à ajouter
     */
    public void addEquipment(EquipmentItem item) {
        if (item == null) return;

        GroupEquipment group = this.equipmentGroups.computeIfAbsent(item.getType(),
                k -> {
                    GroupEquipment newGroup = new GroupEquipment();
                    newGroup.setType(k);
                    return newGroup;
                });

        group.addItem(item);

        // Tri du groupe spécifique
        if (group.getItems() != null) {
            group.getItems().sort(Comparator.comparingDouble(Item::getMasseGrammes));
        }
    }

    /**
     * Ajoute une nourriture à la randonné et ordonne la liste
     * pour pré trier la liste à optimisé
     * @param food nourriture à ajouter
     */
    public void addFood(FoodProduct food) {
        if (food == null) return;

        this.foodCatalogue.add(food);

        this.foodCatalogue.sort((f1, f2) -> {
            // Sécurité pour éviter la division par 0
            double mass1 = f1.getTotalMasses() > 0 ? f1.getTotalMasses() : 1.0;
            double mass2 = f2.getTotalMasses() > 0 ? f2.getTotalMasses() : 1.0;

            double density1 = f1.getTotalKcals() / mass1;
            double density2 = f2.getTotalKcals() / mass2;

            // Compare f2 à f1 pour avoir l'ordre décroissant (plus grand en premier)
            return Double.compare(density2, density1);
        });
    }

    /**
     * Retire un équipement de la randonnée
     * @param item équipement à retirer
     */
    public void removeEquipment(EquipmentItem item) {
        if (item == null) return;

        GroupEquipment group = this.equipmentGroups.get(item.getType());
        if (group != null) {
            group.getItems().remove(item);
        }
    }

    /**
     * Retire une nourriture de la randonnée
     * @param food nourriture à retirer de la randonnée
     */
    public void removeFood(FoodProduct food) {
        if (food == null) return;
        this.foodCatalogue.remove(food);
    }


    // --- Logique Métier : Helpers Calculs ---

    /**
     * Calcule le total des calories fournies par la nourriture présente dans la rando
     * @return calories couvertent par la nourriture ajouté à la randonnée
     */
    public double getCalorieRandonne() {
        double sommeCalorie = 0;
        for(FoodProduct foodProduct : this.foodCatalogue) {
            sommeCalorie += foodProduct.getApportNutritionnelKcal() * foodProduct.getNbItem();
        }
        return sommeCalorie;
    }

    /**
     * Calcule le besoin calorique total de tous les participants
     * @return tous les besoins caloriques de tous les particpants
     */
    public int getCaloriesForAllParticipants() {
        int sommeCalorie = 0;
        for(Participant participant : participants) {
            sommeCalorie += participant.getBesoinKcal();
        }
        return sommeCalorie;
    }

    /**
     * Récupère la liste des sacs à dos de tous les participants.
     * @return la liste des sacs des pacticipants
     */
    public List<Backpack> getBackpacks() {
        List<Backpack> backpacks = new ArrayList<>();
        for(Participant participant : participants) {
            if (participant.getBackpack() != null) {
                backpacks.add(participant.getBackpack());
            }
        }
        return backpacks;
    }

    // --- Getters et Setters---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public PointOfInterest getDepart() { return depart; }
    public void setDepart(PointOfInterest depart) { this.depart = depart; }

    public PointOfInterest getArrivee() { return arrivee; }
    public void setArrivee(PointOfInterest arrivee) { this.arrivee = arrivee; }

    public int getDureeJours() { return dureeJours; }
    public void setDureeJours(int dureeJours) { this.dureeJours = dureeJours; }

    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }

    public Set<Participant> getParticipants() { return participants; }
    public void setParticipants(Set<Participant> participants) { this.participants = participants; }

    public Set<PointOfInterest> getOptionalPoints() { return optionalPoints; }
    public void setOptionalPoints(Set<PointOfInterest> optionalPoints) { this.optionalPoints = optionalPoints; }

    public List<FoodProduct> getFoodCatalogue() { return foodCatalogue; }
    public void setFoodCatalogue(List<FoodProduct> foodCatalogue) { this.foodCatalogue = foodCatalogue; }

    public Map<TypeEquipment, GroupEquipment> getEquipmentGroups() { return equipmentGroups; }
    public void setEquipmentGroups(Map<TypeEquipment, GroupEquipment> equipmentGroups) {
        this.equipmentGroups = equipmentGroups;
    }
}