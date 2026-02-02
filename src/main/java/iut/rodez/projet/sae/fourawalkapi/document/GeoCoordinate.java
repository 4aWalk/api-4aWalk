package iut.rodez.projet.sae.fourawalkapi.document;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Coordonnée GPS élémentaire.
 * Cette classe est destinée à être stockée dans une liste au sein du document {@link Course}.
 */
public class GeoCoordinate {
    // JsonGeoPoint
    private double latitude;
    private double longitude;
    private LocalDateTime timestamp;

    // --- Constructeurs ---

    public GeoCoordinate() {}

    /**
     * Crée une coordonnée avec un timestamp automatique à l'instant présent.
     */
    public GeoCoordinate(double latitude, double longitude) {
        this(latitude, longitude, LocalDateTime.now());
    }

    public GeoCoordinate(double latitude, double longitude, LocalDateTime timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    // --- Logique métier de bas niveau ---

    /**
     * Calcule la distance en mètres entre ce point et un autre point GPS.
     * Utilise la formule de Haversine pour prendre en compte la courbure de la Terre.
     */
    public double distanceTo(GeoCoordinate other) {
        if (other == null) return 0;
        final int R = 6371000; // Rayon moyen de la Terre en mètres

        double latDistance = Math.toRadians(other.latitude - this.latitude);
        double lonDistance = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    // --- Overrides Standards ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoCoordinate that = (GeoCoordinate) o;
        // On compare les coordonnées et le timestamp pour l'égalité parfaite
        return Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0 &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, timestamp);
    }

    @Override
    public String toString() {
        return String.format("GPS[lat=%.6f, lon=%.6f, time=%s]", latitude, longitude, timestamp);
    }

    // --- Getters et Setters ---

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}