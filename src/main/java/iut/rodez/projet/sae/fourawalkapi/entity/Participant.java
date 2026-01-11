package iut.rodez.projet.sae.fourawalkapi.entity;

import iut.rodez.projet.sae.fourawalkapi.model.Person;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Level;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Morphology;
import jakarta.persistence.*;

/** Participant (Personne dans le contexte d'une randonnée - Peut être un User ou un invité) */
@Entity
@Table(name = "participants")
public class Participant implements Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nomComplet;

    // Caractéristiques Personne
    private int age;
    @Enumerated(EnumType.STRING)
    private Level niveau;
    @Enumerated(EnumType.STRING)
    private Morphology morphologie;

    // Besoins nutritionnels/eau (Saisie ou calculés - 2.1.4.3)
    private int besoinKcal;
    private int besoinEauLitre;

    // Capacité d'emport (Saisie ou calculée/vérifiée - 2.1.4.2)
    // Une capacité de 0 est équivalent à ne pas avoir de sac
    private double capaciteEmportMaxKg;

    // Sac à dos attribué (Contient la liste finale des objets à emporter)
    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL)
    private Backpack backpack;

    public Participant() {}
    public Participant(String nomComplet, int age, Level niveau, Morphology morphologie, int besoinKcal, int besoinEauLitre, double capaciteEmportMaxKg) {
        this.nomComplet = nomComplet;
        this.age = age;
        this.niveau = niveau;
        this.morphologie = morphologie;
        this.besoinKcal = besoinKcal;
        this.besoinEauLitre = besoinEauLitre;
        this.capaciteEmportMaxKg = capaciteEmportMaxKg;
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

    public Long getId() {return this.id;}
    public String getNomComplet() {return this.nomComplet;}
    public int getBesoinEau() {return this.besoinEauLitre;}
    public double getCapaciteMax() {return  this.capaciteEmportMaxKg;}

    public void setNomComplet(String nomComplet) {this.nomComplet = nomComplet;}
    public void setAge(int age) {this.age = age;}
    public void setNiveau(Level niveau) {this.niveau = niveau;}
    public void setMorphologie(Morphology morphologie){this.morphologie = morphologie;}
    public void setBesoinKcal(int besoinKcal) {this.besoinKcal = besoinKcal;}
    public void setBesoinEau(int eauCl) {this.besoinEauLitre = eauCl;}
    public void setCapaciteEmportMaxKg(double capaciteEmportMaxKg) {
        this.capaciteEmportMaxKg = capaciteEmportMaxKg;
    }

    public Object getNom() {
        return this.nomComplet;
    }
}
