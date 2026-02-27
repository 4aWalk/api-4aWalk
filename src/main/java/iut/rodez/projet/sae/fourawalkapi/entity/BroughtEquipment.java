package iut.rodez.projet.sae.fourawalkapi.entity;

import jakarta.persistence.*;

/**
 * Entité de liaison (Relation Ternaire) :
 * Représente le fait qu'un participant a un équipement qui lui appartient pour une randonnée spécifique.
 */
@Entity
@Table(name = "brought_equipment")
public class BroughtEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hike_id", nullable = false)
    private Hike hike;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private EquipmentItem equipment;

    // --- Constructeurs ---

    public BroughtEquipment() {
        // Constructeur vide requis par JPA
    }

    public BroughtEquipment(Hike hike, Participant participant, EquipmentItem equipment) {
        this.hike = hike;
        this.participant = participant;
        this.equipment = equipment;
    }

    // --- Getters et Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Hike getHike() {
        return hike;
    }

    public void setHike(Hike hike) {
        this.hike = hike;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    public EquipmentItem getEquipment() {
        return equipment;
    }

    public void setEquipment(EquipmentItem equipment) {
        this.equipment = equipment;
    }
}