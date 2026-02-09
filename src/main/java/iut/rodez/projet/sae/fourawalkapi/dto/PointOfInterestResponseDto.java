package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.PointOfInterest;

public class PointOfInterestResponseDto {

    private Long id;
    private String nom;
    private String description;
    private double latitude;
    private double longitude;

    public PointOfInterestResponseDto() {
    }

    // --- 2. Ton constructeur existant (pour convertir depuis l'entité) ---
    public PointOfInterestResponseDto(PointOfInterest poi) {
        if (poi != null) {
            this.id = poi.getId();
            this.nom = poi.getName(); // ou getName() selon ton entité
            this.description = poi.getDescription();
            this.latitude = poi.getLatitude();
            this.longitude = poi.getLongitude();
        }
    }

    // --- 3. GETTERS ET SETTERS (OBLIGATOIRES POUR LE JSON) ---
    // Jackson utilise les Setters pour remplir l'objet à partir du JSON

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return nom; }
    public void setName(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}