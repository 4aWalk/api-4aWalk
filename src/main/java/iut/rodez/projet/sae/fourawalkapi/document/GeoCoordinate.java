package iut.rodez.projet.sae.fourawalkapi.document;

import java.time.LocalDateTime;

/** Coordonnée GPS (Classe embarquée dans le document Course) */
public class GeoCoordinate {
    private double latitude;
    private double longitude;
    private LocalDateTime timestamp;

    public GeoCoordinate() {}
    public GeoCoordinate(double latitude, double longitude, LocalDateTime timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }
}
