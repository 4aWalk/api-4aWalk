package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.PointOfInterest;

public class PointOfInterestResponseDto {
    private Long id;
    private String nom;
    private double latitude;
    private double longitude;

    public PointOfInterestResponseDto(PointOfInterest poi) {
        if (poi != null) {
            this.id = poi.getId();
            this.nom = poi.getName();
            this.latitude = poi.getLatitude();
            this.longitude = poi.getLongitude();
        }
    }

    // Getters
    public Long getId() { return id; }
    public String getNom() { return nom; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}