package iut.rodez.projet.sae.fourawalkapi.entity;

import jakarta.persistence.*;

/** Point d'Intérêt (Points optionnels sur l'itinéraire) */
@Entity
@Table(name = "points_of_interest")
public class PointOfInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private double latitude;
    private double longitude;
    private String description;

    // La randonnée à laquelle ce POI est attaché
    @ManyToOne
    @JoinColumn(name = "hike_id")
    private Hike hike;

    public PointOfInterest() {}
    public PointOfInterest(String name, double latitude, double longitude, String description, Hike hike) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.hike = hike;
    }
}
