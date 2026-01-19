package iut.rodez.projet.sae.fourawalkapi.entity;

import iut.rodez.projet.sae.fourawalkapi.error.HikeException;
import jakarta.persistence.*;

import java.util.HashSet;
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
        this.participants = new HashSet<Participant>();
        this.optionalPoints = new HashSet<PointOfInterest>();
    }

    public void setLibelle(String libelle) {this.libelle = libelle;}

    public String getLibelle() { return this.libelle; }
    public void setDepart(String depart) {this.depart = depart;}
    public String getDepart() { return this.depart; }
    public void setArrivee(String arrivee) {this.arrivee = arrivee;}
    public String getArrivee() { return this.arrivee; }
    public int getDureeJours() {return this.dureeJours;}
    public void setDureeJours(int  dureeJours) {this.dureeJours = dureeJours;}

    public User getCreator() {return this.creator;}

    public Set<Participant> getParticipants() {return this.participants;}
    public void addParticipant(Participant participant) throws HikeException {
        if (this.participants.size() > 1) {
            throw new HikeException();
        }
        this.participants.add(participant);
    }
    public void removeParticipant(Participant participant) throws HikeException {

    }

    public Long getId() {return this.id;}
}
