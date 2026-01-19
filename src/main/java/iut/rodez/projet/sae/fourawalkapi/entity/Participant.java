package iut.rodez.projet.sae.fourawalkapi.entity;

import iut.rodez.projet.sae.fourawalkapi.model.Person;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Level;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Morphology;
import jakarta.persistence.*;
import java.util.Objects;

/**
 * Représente un participant à une randonnée.
 * Gère les capacités physiques et les besoins nutritionnels (UC 2.1.4.2 & 2.1.4.3).
 */
@Entity
@Table(name = "participants")
public class Participant implements Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomComplet;

    private int age;

    @Enumerated(EnumType.STRING)
    private Level niveau;

    @Enumerated(EnumType.STRING)
    private Morphology morphologie;

    /** Besoins énergétiques quotidiens en Kcal */
    private int besoinKcal;

    /** Besoins en eau en Litres */
    private int besoinEauLitre;

    /** * Capacité de charge maximale en Kg.
     * Si 0, le participant ne peut pas porter de sac.
     */
    private double capaciteEmportMaxKg;

    /** Le sac à dos attribué au participant */
    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Backpack backpack;

    // --- Constructeurs ---

    public Participant() {}

    public Participant(String nomComplet, int age, Level niveau, Morphology morphologie,
                       int besoinKcal, int besoinEauLitre, double capaciteEmportMaxKg) {
        this.nomComplet = nomComplet;
        this.age = age;
        this.niveau = niveau;
        this.morphologie = morphologie;
        this.besoinKcal = besoinKcal;
        this.besoinEauLitre = besoinEauLitre;
        this.capaciteEmportMaxKg = capaciteEmportMaxKg;
    }

    // --- Logique métier de bas niveau (Entity Logic) ---

    /**
     * Vérifie si le participant est en surcharge par rapport à sa capacité maximale.
     * @return true si le poids du sac dépasse la capacité.
     */
    public boolean isOverloaded() {
        if (this.backpack == null) return false;
        return this.backpack.getTotalMassKg() > this.capaciteEmportMaxKg;
    }

    /** Calcule le pourcentage de charge actuel par rapport au max */
    public double getLoadPercentage() {
        if (capaciteEmportMaxKg <= 0 || backpack == null) return 0.0;
        return (backpack.getTotalMassKg() / capaciteEmportMaxKg) * 100;
    }

    // --- Implémentation de l'interface Person ---

    @Override
    public String getNom() {
        return this.nomComplet;
    }

    @Override
    public int getAge() {
        return this.age;
    }

    @Override
    public Level getNiveau() {
        return this.niveau;
    }

    @Override
    public Morphology getMorphologie() {
        return this.morphologie;
    }

    // --- Overrides Standards ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participant that = (Participant) o;
        // L'égalité repose sur l'ID technique s'il existe, sinon sur le nom complet
        return Objects.equals(id, that.id) || (id == null && Objects.equals(nomComplet, that.nomComplet));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nomComplet);
    }

    @Override
    public String toString() {
        return String.format("%s (%d ans, %s, %s)", nomComplet, age, niveau, morphologie);
    }

    // --- Getters et Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }

    public void setAge(int age) { this.age = age; }

    public void setNiveau(Level niveau) { this.niveau = niveau; }

    public void setMorphologie(Morphology morphologie) { this.morphologie = morphologie; }

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