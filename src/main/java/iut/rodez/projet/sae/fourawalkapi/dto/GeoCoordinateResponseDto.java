package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.document.GeoCoordinate;

/**
 * Data transfert object utilisé dans les communications d'objet point de localisation avec le client
 */
public class GeoCoordinateResponseDto {

    private double latitude;
    private double longitude;


    /**
     * Mapper entity to dto
     * @param geoCoordinate point de localisation à mapper
     */
    public GeoCoordinateResponseDto(GeoCoordinate geoCoordinate) {
        if (geoCoordinate != null && geoCoordinate.getGeojson() != null) {
            this.latitude = geoCoordinate.getGeojson().getY();
            this.longitude = geoCoordinate.getGeojson().getX();
        }
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}