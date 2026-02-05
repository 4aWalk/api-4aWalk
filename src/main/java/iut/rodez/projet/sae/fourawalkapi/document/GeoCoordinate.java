package iut.rodez.projet.sae.fourawalkapi.document;

import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

public class GeoCoordinate {

    private GeoJsonPoint geojson;

    // --- Constructeurs ---

    public GeoCoordinate() {
    }

    /**
     * Constructeur qui prend lat/lon mais crée l'objet standard GeoJsonPoint.
     * @param latitude (Y)
     * @param longitude (X)
     */
    public GeoCoordinate(double latitude, double longitude) {
        this.geojson = new GeoJsonPoint(longitude, latitude);
    }

    // --- Logique métier ---

    public double distanceTo(GeoCoordinate other) {
        if (other == null || other.getGeojson() == null || this.geojson == null) return 0;

        // Avec GeoJsonPoint : getY() = Latitude, getX() = Longitude
        double lat1 = this.geojson.getY();
        double lon1 = this.geojson.getX();

        double lat2 = other.getGeojson().getY();
        double lon2 = other.getGeojson().getX();

        final int R = 6371000; // Rayon Terre en mètres

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    // --- Getter / Setter ---

    public GeoJsonPoint getGeojson() {
        return geojson;
    }

    public void setGeojson(GeoJsonPoint geojson) {
        this.geojson = geojson;
    }

    @Override
    public String toString() {
        return "GeoCoordinate{geojson=" + geojson + '}';
    }
}