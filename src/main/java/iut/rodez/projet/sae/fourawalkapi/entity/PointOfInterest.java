package iut.rodez.projet.sae.fourawalkapi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * Représente un point d'intérêt (POI) sur l'itinéraire d'une randonnée.
 * (Ex: Point de vue, source d'eau, refuge optionnel).
 */
@Entity
@Table(name = "points_of_interest")
public class PointOfInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du POI est obligatoire")
    @Column(nullable = false)
    private String nom;

    @NotNull(message = "La latitude est requise")
    @Min(value = -90, message = "La latitude doit être entre -90 et 90")
    @Max(value = 90, message = "La latitude doit être entre -90 et 90")
    private Double latitude;

    @NotNull(message = "La longitude est requise")
    @Min(value = -180, message = "La longitude doit être entre -180 et 180")
    @Max(value = 180, message = "La longitude doit être entre -180 et 180")
    private Double longitude;

    @Column(length = 500)
    private String description;

    private int sequence;

    // --- Constructeurs ---

    public PointOfInterest() {}

    public PointOfInterest(String nom, double latitude, double longitude, String description, Hike hike, int order) {
        this.nom = nom;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.sequence = order;
    }

    // --- Logique métier de bas niveau ---

    /**
     * Calcule la distance entre ce POI et une coordonnée GPS donnée.
     * Utile pour savoir si le randonneur est proche du point.
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

    // --- Overrides Standards ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PointOfInterest that = (PointOfInterest) o;
        // Égalité sur l'ID ou sur le nom et les coordonnées
        return Objects.equals(id, that.id) ||
                (Double.compare(that.latitude, latitude) == 0 &&
                        Double.compare(that.longitude, longitude) == 0 &&
                        Objects.equals(nom, that.nom));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom, latitude, longitude);
    }

    @Override
    public String toString() {
        return String.format("POI[name='%s', lat=%.4f, lon=%.4f]", nom, latitude, longitude);
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