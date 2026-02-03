package iut.rodez.projet.sae.fourawalkapi.entity;

import iut.rodez.projet.sae.fourawalkapi.model.Person;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Level;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Morphology;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Représente un participant à une randonnée.
 * Gère les capacités physiques et les besoins nutritionnels.
 */
@Entity
@Table(name = "participants")
public class Participant implements Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    // Règle : Age entre 10 et 100 ans
    @NotNull(message = "L'âge est obligatoire")
    @Min(value = 10, message = "L'âge minimum est de 10 ans")
    @Max(value = 100, message = "L'âge maximum est de 100 ans")
    private Integer age;

    @NotNull(message = "Le niveau est obligatoire")
    @Enumerated(EnumType.STRING)
    private Level niveau;

    @NotNull(message = "La morphologie est obligatoire")
    @Enumerated(EnumType.STRING)
    private Morphology morphologie;

    private boolean creator = false;

    @Column(name="creator_id")
    private Long creatorId;

    // Règle : Kcal entre 1700 et 10000
    //@Min(value = 1700, message = "Le besoin calorique minimum est de 1700 kcal")
    //@Max(value = 10000, message = "Le besoin calorique maximum est de 10000 kcal")
    private Integer besoinKcal = 1700;

    // Règle : Eau entre 1 et 8 Litres
    //@Min(value = 1, message = "Le besoin en eau minimum est de 1 Litre")
    //@Max(value = 8, message = "Le besoin en eau maximum est de 8 Litres")
    private Integer besoinEauLitre = 1;

    // Règle : Pas de sac de plus de 30kg (donc capacité max <= 30)
    @PositiveOrZero(message = "La capacité d'emport doit être positive")
    //@Max(value = 30, message = "La capacité d'emport ne peut pas dépasser 30 kg")
    private Double capaciteEmportMaxKg = 0.0;

    /** Le sac à dos attribué au participant */
    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Backpack backpack;

    // --- Constructeurs ---

    public Participant() {}

    public Participant(String prenom, String nom, int age, Level niveau, Morphology morphologie, boolean creator,
                       Long creatorId, int besoinKcal, int besoinEauLitre, double capaciteEmportMaxKg) {
        this.prenom = prenom;
        this.nom = nom;
        this.age = age;
        this.niveau = niveau;
        this.morphologie = morphologie;
        this.creator = creator;
        this.creatorId = creatorId;
        this.besoinKcal = besoinKcal;
        this.besoinEauLitre = besoinEauLitre;
        this.capaciteEmportMaxKg = capaciteEmportMaxKg;
    }

    // --- Logique métier ---

    public boolean isOverloaded() {
        if (this.backpack == null) return false;
        return this.backpack.getTotalMassKg() > this.capaciteEmportMaxKg;
    }

    // --- Implémentation Person ---

    @Override
    public String getPrenom() { return prenom; }
    @Override
    public void setPrenom(String prenom) { this.prenom = prenom; }

    @Override
    public String getNom() {return this.nom;}
    @Override
    public void setNom(String nom) { this.nom = nom; }

    @Override
    public int getAge() { return this.age; }
    @Override
    public void setAge(int age) { this.age = age; }

    @Override
    public Level getNiveau() { return this.niveau; }
    @Override
    public void setNiveau(Level niveau) { this.niveau = niveau; }

    @Override
    public Morphology getMorphologie() { return this.morphologie; }
    @Override
    public void setMorphologie(Morphology morphologie) { this.morphologie = morphologie; }

    // --- Getters et Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public boolean getCreator() { return creator; }
    public void setCreator(boolean isCreator) { this.creator = isCreator; }

    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }

    public int getBesoinKcal() { return besoinKcal; }
    public void setBesoinKcal(int besoinKcal) { this.besoinKcal = besoinKcal; }

    public int getBesoinEauLitre() { return besoinEauLitre; }
    public void setBesoinEauLitre(int besoinEauLitre) { this.besoinEauLitre = besoinEauLitre; }

    public double getCapaciteEmportMaxKg() { return capaciteEmportMaxKg; }
    public void setCapaciteEmportMaxKg(double capaciteEmportMaxKg) { this.capaciteEmportMaxKg = capaciteEmportMaxKg; }

    public Backpack getBackpack() { return backpack; }
    public void setBackpack(Backpack backpack) {
        this.backpack = backpack;
        if (backpack != null && backpack.getOwner() != this) {
            backpack.setOwner(this);
        }
    }
}