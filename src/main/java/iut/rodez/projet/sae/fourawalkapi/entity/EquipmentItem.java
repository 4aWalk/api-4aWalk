package iut.rodez.projet.sae.fourawalkapi.entity;

import iut.rodez.projet.sae.fourawalkapi.model.Item;
import jakarta.persistence.*;

/** Équipement (Matériel indispensable, implémente Objet) */
@Entity
@Table(name = "equipment_items")
public class EquipmentItem implements Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String description;
    private double masseGrammes;
    private boolean permetRepos; // Critère de l'équipement (2.1.4.1)

    public EquipmentItem() {}
    public EquipmentItem(String nom, String description, double masseGrammes, boolean permetRepos) {
        this.nom = nom;
        this.description = description;
        this.masseGrammes = masseGrammes;
        this.permetRepos = permetRepos;
    }

    @Override public String getNom() { return nom; }
    @Override public String getDescription() { return description; }
    @Override public double getMasseGrammes() { return masseGrammes; }
}
