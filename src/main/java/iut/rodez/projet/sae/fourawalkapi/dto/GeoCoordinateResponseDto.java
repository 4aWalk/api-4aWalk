package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.document.GeoCoordinate;

public class GeoCoordinateResponseDto {

    private double latitude;
    private double longitude;

    public GeoCoordinateResponseDto() {
    }

    // Constructeur qui convertit l'entité GeoCoordinate en DTO
    public GeoCoordinateResponseDto(GeoCoordinate geoCoordinate) {
        if (geoCoordinate != null && geoCoordinate.getGeojson() != null) {
            // Rappel : Avec GeoJsonPoint, Y = Latitude, X = Longitude
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