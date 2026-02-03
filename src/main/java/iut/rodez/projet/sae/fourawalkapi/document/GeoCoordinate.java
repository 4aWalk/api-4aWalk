package iut.rodez.projet.sae.fourawalkapi.document;

import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import java.util.Objects;

/**
 * Wrapper autour d'un GeoJsonPoint.
 * L'ordre des points dans la liste de la classe parente (Course/Hike) définit le trajet.
 */
public class GeoCoordinate {

    private GeoJsonPoint location;

    // --- Constructeurs ---

    public GeoCoordinate() {}

    /**
     * Constructeur pratique.
     * ATTENTION : Dans GeoJSON, l'ordre est (Longitude, Latitude).
     * @param latitude  Latitude (Y)
     * @param longitude Longitude (X)
     */
    public GeoCoordinate(double latitude, double longitude) {
        // x = Longitude, y = Latitude
        this.location = new GeoJsonPoint(longitude, latitude);
    }

    // --- Logique métier ---

    /**
     * Calcule la distance en mètres vers un autre point.
     */
    public double distanceTo(GeoCoordinate other) {
        if (other == null || other.getLocation() == null || this.location == null) return 0;

        final int R = 6371000; // Rayon de la Terre en mètres

        // Récupération correcte depuis GeoJsonPoint (X=Lon, Y=Lat)
        double lat1 = this.location.getY();
        double lon1 = this.location.getX();
        double lat2 = other.getLocation().getY();
        double lon2 = other.getLocation().getX();

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
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
        return Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location);
    }

    @Override
    public String toString() {
        // Affiche Lat, Lon pour être lisible par un humain
        return String.format("GPS[lat=%.6f, lon=%.6f]", getLatitude(), getLongitude());
    }

    // --- Getters et Setters ---

    public GeoJsonPoint getLocation() {
        return location;
    }

    public void setLocation(GeoJsonPoint location) {
        this.location = location;
    }

    // Helpers pour faciliter l'accès (évite de se tromper entre X et Y ailleurs)
    public double getLatitude() {
        return location != null ? location.getY() : 0.0;
    }

    public double getLongitude() {
        return location != null ? location.getX() : 0.0;
    }
}