package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.PointOfInterest;

/**
 * Data transfert object utilisé dans les communications d'objet point d'interêt avec le client
 */
public class PointOfInterestResponseDto {

    private Long id;
    private String nom;
    private String description;
    private double latitude;
    private double longitude;

    /**
     * Mapper entity to dto
     * @param poi poi à mapper
     */
    public PointOfInterestResponseDto(PointOfInterest poi) {
        if (poi != null) {
            this.id = poi.getId();
            this.nom = poi.getName();
            this.description = poi.getDescription();
            this.latitude = poi.getLatitude();
            this.longitude = poi.getLongitude();
        }
    }

    // Getters
    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getDescription() { return description; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}