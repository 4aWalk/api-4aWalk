package iut.rodez.projet.sae.fourawalkapi.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Randonnée (Planification de l'itinéraire et des participants - UC004) */
@Entity
@Table(name = "hikes")
public class Hike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String libelle;
    private String depart; // Coordonnées ou lieu
    private String arrivee; // Coordonnées ou lieu
    private int dureeJours; // 1 à 3 jours

    // L'utilisateur qui a créé la randonnée
    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    // Liste des participants de CETTE randonnée
    @ManyToMany
    @JoinTable(name = "hike_participants",
            joinColumns = @JoinColumn(name = "hike_id"),
            inverseJoinColumns = @JoinColumn(name = "participant_id"))
    private Set<Participant> participants;

    // Points d'intérêt optionnels (relation One-to-Many)
    @OneToMany(mappedBy = "hike")
    private Set<PointOfInterest> optionalPoints;

    public Hike() {}
    public Hike(String libelle, String depart, String arrivee, int dureeJours, User creator) {
        this.libelle = libelle;
        this.depart = depart;
        this.arrivee = arrivee;
        this.dureeJours = dureeJours;
        this.creator = creator;
    }
    public Long getId() { return id; }
    public String getLibelle() {return libelle;}
    public String getDepart() {return depart;}
    public String getArrivee() {return arrivee;}
    public int getDureeJours() {return dureeJours;}
    public User getCreator() {return creator;}

    public void setLibelle(String libelle) {this.libelle = libelle;}
    public void setDepart(String depart) {this.depart = depart;}
    public void setArrivee(String arrivee) {this.arrivee = arrivee;}
    public void setDureeJours(int dureeJours) {this.dureeJours = dureeJours;}
    public void setCreator(User creator) {this.creator = creator;}


    public List<Participant> getParticipants() {
        if (participants == null) { return new ArrayList<>(); }
        return (List<Participant>) this.participants;
    }

    public void setParticipants(Set<Participant> participants) {this.participants = participants;}
}
