package iut.rodez.projet.sae.fourawalkapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Level;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Morphology;
import iut.rodez.projet.sae.fourawalkapi.model.Person;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Utilisateur de l'application
 */
@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User implements Person {

    /* identifiant de l'utilisateur */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Nom de l'utilisateur */
    @Column(nullable = false)
    private String nom;

    /* Prénom de l'utilisateur */
    @Column(nullable = false)
    private String prenom;

    /* Mail de l'utilisateur */
    @Column(unique = true, nullable = false)
    private String mail;

    /* Mot de passe de l'utilisateur */
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /* Adresse de l'utilisateur */
    private String adresse;

    /* Age de l'utilisateur */
    @Min(value = 10, message = "L'âge ne peut pas être inférieur à 10 ans")
    @Max(value = 100, message = "L'âge ne peut pas être supérieur à 100 ans")
    private int age;

    /* Niveau de l'utilisateur */
    @Enumerated(EnumType.STRING)
    private Level niveau;

    /* Morphologie de l'utilisateur */
    @Enumerated(EnumType.STRING)
    private Morphology morphologie;

    // --- Constructeurs ---

    public User() {}

    public User(String nom, String prenom, String mail, String password, String adresse,
                int age, Level niveau, Morphology morphologie) {
        this.nom = nom;
        this.prenom = prenom;
        this.mail = mail;
        this.password = password;
        this.adresse = adresse;
        this.age = age;
        this.niveau = niveau;
        this.morphologie = morphologie;
    }

    // --- Implémentation de l'interface ---

    @Override
    public String getPrenom() { return prenom; }
    @Override
    public void setPrenom(String prenom) { this.prenom = prenom; }

    @Override
    public String getNom() {return this.nom;}
    @Override
    public void setNom(String nom) { this.nom = nom; }

    @Override
    public int getAge() {
        return this.age;
    }

    @Override
    public void setAge(int age) { this.age = age; }

    @Override
    public Level getNiveau() {
        return this.niveau;
    }

    @Override
    public void setNiveau(Level niveau) { this.niveau = niveau; }

    @Override
    public Morphology getMorphologie() {
        return this.morphologie;
    }

    @Override
    public void setMorphologie(Morphology morphologie) { this.morphologie = morphologie; }


    // --- Getters et Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }


}