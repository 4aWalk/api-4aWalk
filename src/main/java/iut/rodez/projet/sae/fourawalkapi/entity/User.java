package iut.rodez.projet.sae.fourawalkapi.entity;

import iut.rodez.projet.sae.fourawalkapi.model.enums.Level;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Morphology;
import iut.rodez.projet.sae.fourawalkapi.model.Item;
import iut.rodez.projet.sae.fourawalkapi.model.Person;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Level;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Set;

/** Utilisateur (Gère le compte et les données personnelles - UC001, UC002) */
@Entity
@Table(name = "users")
public class User implements Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String prenom;
    @Column(unique = true, nullable = false)
    private String mail;
    private String password; // Doit être hashé
    private String adresse;

    // Caractéristiques Personne
    private int age;
    @Enumerated(EnumType.STRING)
    private Level niveau;
    @Enumerated(EnumType.STRING)
    private Morphology morphologie;

    // Randonnées créées par cet utilisateur
    @OneToMany(mappedBy = "creator")
    private Set<Hike> createdHikes;

    public User() {}
    public User(String nom, String prenom, String mail, String password, String adresse, int age, Level niveau, Morphology morphologie) {
        this.nom = nom;
        this.prenom = prenom;
        this.mail = mail;
        this.password = password;
        this.adresse = adresse;
        this.age = age;
        this.niveau = niveau;
        this.morphologie = morphologie;
    }

    @Override
    public int getAge() {
        return this.age;
    }
    @Override
    public String getNiveau() {
        return this.niveau != null ? this.niveau.toString() : null;
    }
    @Override
    public String getMorphologie() {
        return this.morphologie != null ? this.morphologie.toString() : null;
    }
    public String getPassword() {
        return this.password;
    }
    public String getMail() {
        return this.mail;
    }
    public String getAdresse() {
        return this.adresse;
    }
    public String getNom() {
        return this.nom;
    }
    public String getPrenom() {
        return this.prenom;
    }
}