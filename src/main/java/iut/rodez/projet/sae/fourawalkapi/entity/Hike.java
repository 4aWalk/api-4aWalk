package iut.rodez.projet.sae.fourawalkapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.*;

/**
 * Représente une randonnée planifiée.
 * Gère l'itinéraire, les participants, les équipements et la nourriture.
 */
@Entity
@Table(name = "hikes")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Hike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le libellé de la randonnée est obligatoire")
    @Column(nullable = false)
    private String libelle;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private PointOfInterest depart;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private PointOfInterest arrivee;

    @Min(value = 1, message = "La durée minimale est de 1 jour")
    @Max(value = 3, message = "La durée maximale est de 3 jours")
    private int dureeJours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @ManyToMany
    @JoinTable(
            name = "hike_participants",
            joinColumns = @JoinColumn(name = "hike_id"),
            inverseJoinColumns = @JoinColumn(name = "participant_id")
    )
    private Set<Participant> participants = new HashSet<>();

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "hike_id")
    private Set<PointOfInterest> optionalPoints = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "hike_food_products",
            joinColumns = @JoinColumn(name = "hike_id"),
            inverseJoinColumns = @JoinColumn(name = "food_product_id")
    )
    private Set<FoodProduct> foodCatalogue = new HashSet<>();

    // La Map pour ton algo d'optimisation
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "hike_id", nullable = false)
    @MapKey(name = "type") // Utilise le champ 'type' de GroupEquipment comme clé
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

    // --- Logique Métier : Équipement (Map Management) ---

    public void addEquipment(EquipmentItem item) {
        if (item == null) return;

        // 1. Récupère ou crée le groupe pour ce type
        GroupEquipment group = this.equipmentGroups.computeIfAbsent(item.getType(),
                k -> new GroupEquipment(k));

        // 2. Ajoute l'item
        group.addItem(item);
    }

    public void removeEquipment(EquipmentItem item) {
        if (item == null) return;

        GroupEquipment group = this.equipmentGroups.get(item.getType());
        if (group != null) {
            group.getItems().remove(item);
            // Nettoyage si le groupe est vide
            if (group.getItems().isEmpty()) {
                this.equipmentGroups.remove(item.getType());
            }
        }
    }

    public List<EquipmentItem> getOptimizedList(TypeEquipment type) {
        GroupEquipment group = this.equipmentGroups.get(type);
        if (group == null) return new ArrayList<>();
        return group.getItems();
    }

    // --- Logique Métier : Participants ---

    public void addParticipant(Participant participant) throws RuntimeException {
        if (participant == null) throw new RuntimeException("Le participant ne peut pas être nul.");
        if (!this.participants.add(participant)) throw new RuntimeException("Ce participant est déjà inscrit.");
    }

    public void removeParticipant(Participant participant) throws RuntimeException {
        if (!this.participants.remove(participant)) throw new RuntimeException("Participant introuvable.");
    }

    // --- Logique Métier : Helpers Calculs (RESTITUTION) ---

    /**
     * Calcule le total des calories fournies par la nourriture présente dans la rando.
     */
    public double getCalorieRandonne() {
        double sommeCalorie = 0;
        for(FoodProduct foodProduct : this.foodCatalogue) {
            // Correction ici : utiliser += et non =+
            sommeCalorie += foodProduct.getApportNutritionnelKcal() * foodProduct.getNbItem();
        }
        return sommeCalorie;
    }

    /**
     * Calcule le besoin calorique total de tous les participants.
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

    // --- Getters et Setters Standards ---

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

    public Set<FoodProduct> getFoodCatalogue() { return foodCatalogue; }
    public void setFoodCatalogue(Set<FoodProduct> foodCatalogue) { this.foodCatalogue = foodCatalogue; }

    public Map<TypeEquipment, GroupEquipment> getEquipmentGroups() { return equipmentGroups; }
    public void setEquipmentGroups(Map<TypeEquipment, GroupEquipment> equipmentGroups) { this.equipmentGroups = equipmentGroups; }
}