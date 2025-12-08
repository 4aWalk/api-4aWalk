package iut.rodez.projet.sae.fourawalkapi.entity;

import iut.rodez.projet.sae.fourawalkapi.model.Item;
import jakarta.persistence.*;

/** Nourriture (Produits alimentaires, implémente Objet) */
@Entity
@Table(name = "food_products")
public class FoodProduct implements Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom; // Dénomination (ex: Gratin dauphinois aux trois fromages Voyager)
    private String description;
    private double masseGrammes;

    // Attributs spécifiques à la nourriture (2.1.4.4)
    private String appellationCourante; // Nom commun (ex: riz)
    private String conditionnement;
    private double apportNutritionnelKcal;
    private double prixEuro;

    public FoodProduct() {}
    public FoodProduct(String nom, String description, double masseGrammes, String appellationCourante, String conditionnement, double apportNutritionnelKcal, double prixEuro) {
        this.nom = nom;
        this.description = description;
        this.masseGrammes = masseGrammes;
        this.appellationCourante = appellationCourante;
        this.conditionnement = conditionnement;
        this.apportNutritionnelKcal = apportNutritionnelKcal;
        this.prixEuro = prixEuro;
    }

    @Override public String getNom() { return nom; }
    @Override public String getDescription() { return description; }
    @Override public double getMasseGrammes() { return masseGrammes; }
}
