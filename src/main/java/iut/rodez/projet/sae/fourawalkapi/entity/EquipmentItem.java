package iut.rodez.projet.sae.fourawalkapi.entity;

import iut.rodez.projet.sae.fourawalkapi.model.Item;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Objects;

/**
 * Représente un équipement (matériel indispensable comme une tente, un duvet, etc.).
 * Implémente l'interface Item pour la logique de calcul de charge.
 */
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

    @NotNull(message = "Le poids est requis")
    @Positive(message = "Le poids doit être strictement positif")
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

    // --- Méthodes de l'interface Item & Logique métier ---

    @Override
    public String getNom() {
        return nom;
    }


    @Override
    public double getMasseGrammes() {
        return masseGrammes;
    }

    // --- Overrides Standards ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EquipmentItem that = (EquipmentItem) o;
        // Si l'ID est présent, on l'utilise, sinon on compare par nom (clé naturelle)
        return Objects.equals(id, that.id) || Objects.equals(nom, that.nom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom);
    }


    // --- Getters et Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public void setNom(String nom) { this.nom = nom; }

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