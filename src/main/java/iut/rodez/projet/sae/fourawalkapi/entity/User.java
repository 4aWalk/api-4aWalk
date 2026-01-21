package iut.rodez.projet.sae.fourawalkapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Level;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Morphology;
import iut.rodez.projet.sae.fourawalkapi.model.Person;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Utilisateur principal du système.
 * Gère le compte, l'authentification et les randonnées créées (UC001, UC002).
 */
@Entity
@Table(name = "users")
public class User implements Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(unique = true, nullable = false)
    private String mail;

    @Column(nullable = false)
    private String password; // Stocké sous forme de hash (BCrypt par exemple)

    private String adresse;

    private int age;

    @Enumerated(EnumType.STRING)
    private Level niveau;

    @Enumerated(EnumType.STRING)
    private Morphology morphologie;

    /** Liste des randonnées dont cet utilisateur est l'organisateur */
    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Hike> createdHikes = new HashSet<>();

    // --- Constructeurs ---

    public User() {}

    /** Constructeur complet (sans ID car géré par la BDD) */
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

    // --- Logique métier de bas niveau ---

    /** Retourne le nom complet formaté */
    public String getFullName() {
        return prenom + " " + nom.toUpperCase();
    }

    /** Ajoute une randonnée créée et assure la cohérence du lien */
    public void addCreatedHike(Hike hike) {
        this.createdHikes.add(hike);
        hike.setCreator(this);
    }

    // --- Implémentation de l'interface Person ---

    @Override
    public String getNom() {
        return this.nom;
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
        User user = (User) o;
        // L'email est unique en base, c'est notre identifiant métier le plus fiable
        return Objects.equals(mail, user.mail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mail);
    }

    @Override
    public String toString() {
        return String.format("User[id=%d, mail='%s', nom='%s']", id, mail, nom);
    }

    // --- Getters et Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public void setNom(String nom) { this.nom = nom; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public void setAge(int age) { this.age = age; }

    public void setNiveau(Level niveau) { this.niveau = niveau; }

    public void setMorphologie(Morphology morphologie) { this.morphologie = morphologie; }

    public Set<Hike> getCreatedHikes() { return createdHikes; }
    public void setCreatedHikes(Set<Hike> createdHikes) { this.createdHikes = createdHikes; }
}