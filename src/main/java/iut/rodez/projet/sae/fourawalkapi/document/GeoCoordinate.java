package iut.rodez.projet.sae.fourawalkapi.document;

import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

/**
 * Wrapper de l'objet GeoJsonPoint
 */
public class GeoCoordinate {

    /* Point de coordonnée avec une latitude et une longitude */
    private GeoJsonPoint geojson;

    // --- Constructeurs ---

    public GeoCoordinate() {
    }

    /**
     * Constructeur qui prend lat/lon pour crée l'objet standard GeoJsonPoint.
     * @param latitude (Y)
     * @param longitude (X)
     */
    public GeoCoordinate(double latitude, double longitude) {
        this.geojson = new GeoJsonPoint(longitude, latitude);
    }


    // --- Getter / Setter ---

    public GeoJsonPoint getGeojson() {
        return geojson;
    }

    public void setGeojson(GeoJsonPoint geojson) {
        this.geojson = geojson;
    }
}