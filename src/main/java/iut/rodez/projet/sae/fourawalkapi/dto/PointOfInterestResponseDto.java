package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.PointOfInterest;

public class PointOfInterestResponseDto {
    private Long id;
    private String nom;
    private double latitude;
    private double longitude;
    private String description;

    public PointOfInterestResponseDto(PointOfInterest poi) {
        if (poi != null) {
            this.id = poi.getId();
            this.nom = poi.getName();
            this.latitude = poi.getLatitude();
            this.longitude = poi.getLongitude();
            this.description = poi.getDescription();
        }
    }

    // Getters
    public Long getId() { return id; }
    public String getNom() { return nom; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getDescription() { return description; }
}