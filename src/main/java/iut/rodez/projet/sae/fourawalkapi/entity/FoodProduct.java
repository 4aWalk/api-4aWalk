package iut.rodez.projet.sae.fourawalkapi.entity;

import iut.rodez.projet.sae.fourawalkapi.model.Item;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.Objects;

@Entity
@Table(name = "food_products")
public class FoodProduct implements Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de l'aliment est obligatoire")
    @Column(nullable = false)
    private String nom;

    // Règle : Poids entre 50g et 5000g (5kg)
    @NotNull(message = "Le poids est requis")
    @Min(value = 50, message = "Le poids minimum est de 50g")
    @Max(value = 5000, message = "Le poids maximum est de 5kg (5000g)")
    private Double masseGrammes;

    private String appellationCourante;
    private String conditionnement;

    // Règle : Kcal entre 50 et 3000
    @Min(value = 50, message = "L'apport calorique minimum est de 50 kcal")
    @Max(value = 3000, message = "L'apport calorique maximum est de 3000 kcal")
    private Double apportNutritionnelKcal;

    @PositiveOrZero
    private Double prixEuro;

    @Min(1) @Max(3)
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

    // --- Interface Item ---

    @Override
    public String getNom() { return nom; }

    @Override
    public double getMasseGrammes() { return masseGrammes; }

    @Override
    public int getNbItem() { return nbItem; }

    public double getEnergyDensity() {
        if (masseGrammes <= 0) return 0;
        return apportNutritionnelKcal / masseGrammes;
    }

    // --- Overrides ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FoodProduct that = (FoodProduct) o;
        return Objects.equals(id, that.id) || Objects.equals(nom, that.nom);
    }

    @Override
    public int hashCode() { return Objects.hash(id, nom); }

    @Override
    public String toString() {
        return String.format("%s [%s] (%.0f Kcal, %.2f€)", nom, conditionnement, apportNutritionnelKcal, prixEuro);
    }

    // --- Getters et Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public void setNom(String nom) { this.nom = nom; }

    public void setMasseGrammes(double masseGrammes) { this.masseGrammes = masseGrammes; }

    public String getAppellationCourante() { return appellationCourante; }
    public void setAppellationCourante(String appellationCourante) { this.appellationCourante = appellationCourante; }

    public String getConditionnement() { return conditionnement; }
    public void setConditionnement(String conditionnement) { this.conditionnement = conditionnement; }

    public double getApportNutritionnelKcal() { return apportNutritionnelKcal;}
    public void setApportNutritionnelKcal(double apportNutritionnelKcal) { this.apportNutritionnelKcal = apportNutritionnelKcal; }

    public double getPrixEuro() { return prixEuro; }
    public void setPrixEuro(double prixEuro) { this.prixEuro = prixEuro; }

    public int getTotalMasses() { return (int) (this.masseGrammes * this.nbItem); }

    public double getTotalMassesKg(){ return this.masseGrammes / 1000; }

    public int getTotalKcals() { return (int) (this.apportNutritionnelKcal * this.nbItem); }
}