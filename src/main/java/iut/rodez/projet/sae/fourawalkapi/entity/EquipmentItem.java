package iut.rodez.projet.sae.fourawalkapi.entity;

import iut.rodez.projet.sae.fourawalkapi.model.Item;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Équipement pouvant être utilisé par un randonneur
 */
@Entity
@Table(name = "equipment_items")
public class EquipmentItem implements Item {

    /* identifiant de l'équipement */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Nom de l'équipement */
    @NotBlank(message = "Le nom de l'équipement est obligatoire")
    @Column(nullable = false)
    private String nom;

    /* Description de l'équipement */
    private String description;

    /* Masse de l'équipement en gramme par unité */
    @NotNull(message = "Le poids est requis")
    @Min(value = 50, message = "Le poids minimum est de 50g")
    @Max(value = 5000, message = "Le poids maximum est de 5kg (5000g)")
    private Double masseGrammes;

    /* Nombre d'équipement dans le lot */
    @Min(value = 1, message = "Un lot d'équipement doit comporté au mois 1 équipement")
    @Max(value = 3, message = "Un lot d'équipement doit comporté au plus 3 équipements")
    private int nbItem;

    /* Type de l'équipement */
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private TypeEquipment type;

    /* Masse à vide pour une meilleur précision */
    @Column(name = "masse_a_vide")
    private double masseAVide;

    // --- Constructeurs ---
    public EquipmentItem() {}

    public EquipmentItem(String nom,
                         String description,
                         double masseGrammes,
                         int nbItem,
                         TypeEquipment type,
                         double masseAVide) {
        this.nom = nom;
        this.description = description;
        this.masseGrammes = masseGrammes;
        this.nbItem = nbItem;
        this.type = type;
        this.masseAVide = masseAVide;
    }

    // Override de l'interface

    @Override
    public String getNom() { return nom; }

    @Override
    public double getMasseGrammes() { return masseGrammes; }

    @Override
    public int getNbItem() { return nbItem; }

    @Override
    public void setNom(String nom) { this.nom = nom; }

    @Override
    public void setMasseGrammes(double masseGrammes) { this.masseGrammes = masseGrammes; }

    @Override
    public void setNbItem(int nbItem) { this.nbItem = nbItem; }


    // --- Getters et Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TypeEquipment getType() { return type; }
    public void setType(TypeEquipment type){ this.type = type;}

    public double getMasseAVide() { return masseAVide; }
    public void setMasseAVide(double masseAVide) {this.masseAVide = masseAVide;}

    public double getTotalMasses() { return this.masseGrammes - this.masseAVide * this.nbItem; }
    public double getTotalMassesKg() {return this.getTotalMasses() / 1000;}
}