package iut.rodez.projet.sae.fourawalkapi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Point d'intêret est un lieu à visiter pendant la randonnée
 */
@Entity
@Table(name = "points_of_interest")
public class PointOfInterest {

    /* Identifiant du point d'intêret */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Nom du point d'intêret */
    @NotBlank(message = "Le nom du POI est obligatoire")
    @Column(nullable = false)
    private String nom;

    /* Latitude du poi */
    @NotNull(message = "La latitude est requise")
    @Min(value = -90, message = "La latitude doit être entre -90 et 90")
    @Max(value = 90, message = "La latitude doit être entre -90 et 90")
    private Double latitude;

    /* Longitude du poi */
    @NotNull(message = "La longitude est requise")
    @Min(value = -180, message = "La longitude doit être entre -180 et 180")
    @Max(value = 180, message = "La longitude doit être entre -180 et 180")
    private Double longitude;

    /* Description du point d'intêret */
    private String description;

    /* Ordre de visite du point d'intêret */
    private int sequence;

    // --- Constructeurs ---

    public PointOfInterest() {}

    public PointOfInterest(String nom, double latitude, double longitude, String description, int order) {
        this.nom = nom;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.sequence = order;
    }

    // --- Logique métier de bas niveau ---

    /**
     * Calcule la distance entre ce POI et une coordonnée GPS donnée
     */
    public double distanceTo(double lat, double lon) {
        final int R = 6371000; // Rayon de la Terre en mètres
        double latDistance = Math.toRadians(lat - this.latitude);
        double lonDistance = Math.toRadians(lon - this.longitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(lat))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // --- Getters et Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return nom; }
    public void setName(String name) { this.nom = name; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getSequence() { return sequence; }
    public void setSequence(int order) { this.sequence = order; }
}