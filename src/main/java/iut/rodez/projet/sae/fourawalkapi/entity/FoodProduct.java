package iut.rodez.projet.sae.fourawalkapi.entity;

import iut.rodez.projet.sae.fourawalkapi.model.Item;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Nourriture consomable par un randonneur
 */
@Entity
@Table(name = "food_products")
public class FoodProduct implements Item {

    /* identifiant de la nourriture */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Nom de la nourriture */
    @NotBlank(message = "Le nom de l'aliment est obligatoire")
    @Column(nullable = false)
    private String nom;

    /* Poids en gramme de la nourriture par unité */
    @NotNull(message = "Le poids est requis")
    @Min(value = 50, message = "Le poids minimum est de 50g")
    @Max(value = 5000, message = "Le poids maximum est de 5kg (5000g)")
    private Double masseGrammes;

    /* Appellation courante de la nourriture */
    private String appellationCourante;

    /* Conditionnement de la nourriture */
    private String conditionnement;

    // Apport calorique de la nourriture
    @Min(value = 50, message = "L'apport calorique minimum est de 50 kcal")
    @Max(value = 3000, message = "L'apport calorique maximum est de 3000 kcal")
    private Double apportNutritionnelKcal;

    /* Prix de la nourriture */
    @PositiveOrZero
    private Double prixEuro;

    /* Quantité de la nourriture */
    @Min(value = 1, message = "Un lot de nourriture doit comporté au mois 1 nourriture")
    @Max(value = 3, message = "Un lot de nourriture doit comporté au plus 3 nourritures")
    private int nbItem;

    // --- Constructeurs ---
    public FoodProduct() {}

    public FoodProduct(String nom, double masseGrammes, String appellationCourante,
                       String conditionnement, double apportNutritionnelKcal, double prixEuro, int nbItem) {
        this.nom = nom;
        this.masseGrammes = masseGrammes;
        this.appellationCourante = appellationCourante;
        this.conditionnement = conditionnement;
        this.apportNutritionnelKcal = apportNutritionnelKcal;
        this.prixEuro = prixEuro;
        this.nbItem = nbItem;
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



    public String getAppellationCourante() { return appellationCourante; }
    public void setAppellationCourante(String appellationCourante) { this.appellationCourante = appellationCourante; }

    public String getConditionnement() { return conditionnement; }
    public void setConditionnement(String conditionnement) { this.conditionnement = conditionnement; }

    public double getApportNutritionnelKcal() { return apportNutritionnelKcal;}
    public void setApportNutritionnelKcal(double apportNutritionnelKcal) {
        this.apportNutritionnelKcal = apportNutritionnelKcal;
    }

    public double getPrixEuro() { return prixEuro; }
    public void setPrixEuro(double prixEuro) { this.prixEuro = prixEuro; }

    public int getTotalMasses() { return (int) (this.masseGrammes * this.nbItem); }

    public double getTotalMassesKg(){ return this.masseGrammes * this.nbItem / 1000; }

    public int getTotalKcals() { return (int) (this.apportNutritionnelKcal * this.nbItem); }
}