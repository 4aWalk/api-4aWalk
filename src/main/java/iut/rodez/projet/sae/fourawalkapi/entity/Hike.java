package iut.rodez.projet.sae.fourawalkapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import iut.rodez.projet.sae.fourawalkapi.advice.HikeException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Représente une randonnée planifiée.
 * Gère l'itinéraire, les participants et les points d'intérêt (UC004).
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
    @JoinColumn(name = "hike_id") // La colonne sera dans la table POI
    private Set<PointOfInterest> optionalPoints = new HashSet<>();
    @ManyToMany
    @JoinTable(
            name = "hike_food_products",
            joinColumns = @JoinColumn(name = "hike_id"),
            inverseJoinColumns = @JoinColumn(name = "food_product_id")
    )
    private Set<FoodProduct> foodCatalogue = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "hike_equipment",
            joinColumns = @JoinColumn(name = "hike_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_id")
    )
    private Set<EquipmentItem> equipmentRequired = new HashSet<>();

    // --- Constructeurs ---

    public Hike() {}

    public Hike(String libelle, PointOfInterest depart, PointOfInterest arrivee, int dureeJours, User creator) {
        this.libelle = libelle;
        this.depart = depart;
        this.arrivee = arrivee;
        setDureeJours(dureeJours); // Utilise le setter pour valider la règle
        this.creator = creator;
    }

    // --- Logique métier de bas niveau (Entity Logic) ---

    /**
     * Ajoute un participant à la randonnée.
     * @throws HikeException si le participant est nul ou déjà présent.
     */
    public void addParticipant(Participant participant) throws HikeException {
        if (participant == null) {
            throw new HikeException("Le participant ne peut pas être nul.");
        }
        if (!this.participants.add(participant)) {
            throw new HikeException("Ce participant est déjà inscrit à cette randonnée.");
        }
    }

    /**
     * Retire un participant de la randonnée.
     * Utilise equals() basé sur l'ID du participant.
     */
    public void removeParticipant(Participant participant) throws HikeException {
        if (!this.participants.remove(participant)) {
            throw new HikeException("Le participant n'a pas été trouvé dans cette randonnée.");
        }
    }

    /**
     * Ajoute un point d'intérêt et maintient la cohérence bidirectionnelle.
     */
    public void addPointOfInterest(PointOfInterest poi) {
        this.optionalPoints.add(poi);
    }

    // --- Overrides Standards ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hike hike = (Hike) o;
        return Objects.equals(id, hike.id) || Objects.equals(libelle, hike.libelle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, libelle);
    }

    @Override
    public String toString() {
        return String.format("Hike[id=%d, libelle='%s', participants=%d]",
                id, libelle, participants.size());
    }

    // --- Getters et Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public PointOfInterest getDepart() { return depart; }
    public void setDepart(PointOfInterest depart) {
        this.depart = depart;
    }

    public PointOfInterest getArrivee() { return arrivee; }
    public void setArrivee(PointOfInterest arrivee) {
        this.arrivee = arrivee;
    }

    public int getDureeJours() { return dureeJours; }

    /** Valide que la durée est comprise entre 1 et 3 jours */
    public void setDureeJours(int dureeJours) {
        this.dureeJours = dureeJours;
    }

    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }

    public Set<Participant> getParticipants() { return participants; }
    public void setParticipants(Set<Participant> participants) { this.participants = participants; }

    public Set<PointOfInterest> getOptionalPoints() { return optionalPoints; }
    public void setOptionalPoints(Set<PointOfInterest> optionalPoints) { this.optionalPoints = optionalPoints; }

    public Set<FoodProduct> getFoodCatalogue() { return foodCatalogue; }
    public void setFoodCatalogue(Set<FoodProduct> foodCatalogue) { this.foodCatalogue = foodCatalogue; }

    public Set<EquipmentItem> getEquipmentRequired() { return equipmentRequired; }
    public void setEquipmentRequired(Set<EquipmentItem> equipmentRequired) { this.equipmentRequired = equipmentRequired; }
}