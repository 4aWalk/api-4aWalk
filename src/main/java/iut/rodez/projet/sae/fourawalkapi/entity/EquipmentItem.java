package iut.rodez.projet.sae.fourawalkapi.entity;

import iut.rodez.projet.sae.fourawalkapi.model.Item;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Objects;

@Entity
@Table(name = "equipment_items")
public class EquipmentItem implements Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de l'équipement est obligatoire")
    @Column(nullable = false)
    private String nom;

    private String description;

    // Règle : Poids entre 50g et 5000g (5kg)
    @NotNull(message = "Le poids est requis")
    @Min(value = 50, message = "Le poids minimum est de 50g")
    @Max(value = 5000, message = "Le poids maximum est de 5kg (5000g)")
    private Double masseGrammes;

    private int nbItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private TypeEquipment type;

    @Column(name = "masse_a_vide")
    private double masseAVide;

    // --- Constructeurs ---
    public EquipmentItem() {}

    public EquipmentItem(String nom, String description, double masseGrammes, boolean permetRepos, boolean tousLesParticipant, int nbItem, TypeEquipment type, double masseAVide) {
        this.nom = nom;
        this.description = description;
        this.masseGrammes = masseGrammes;
        this.nbItem = nbItem;
        this.type = type;
        this.masseAVide = masseAVide;
    }

    // --- Interface Item ---

    @Override
    public String getNom() { return nom; }

    @Override
    public double getMasseGrammes() { return masseGrammes; }

    // --- Overrides ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EquipmentItem that = (EquipmentItem) o;
        return Objects.equals(id, that.id) || Objects.equals(nom, that.nom);
    }

    @Override
    public int hashCode() { return Objects.hash(id, nom); }

    // --- Getters et Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public void setMasseGrammes(double masseGrammes) { this.masseGrammes = masseGrammes; }

    public int getNbItem() { return nbItem; }
    public void setNbItem(int nbItem) { this.nbItem = nbItem; }

    public TypeEquipment getType() { return type; }
    public void setType(TypeEquipment type){ this.type = type;}

    public double getMasseAVide() { return masseAVide; }
    public void setMasseAVide(double masseAVide) {this.masseAVide = masseAVide;}

    public double getTotalMasses() { return this.masseGrammes - this.masseAVide * this.nbItem; }
    public double getTotalMassesKg() {return this.getTotalMasses() / 1000;}
}