package iut.rodez.projet.sae.fourawalkapi.controller.dto;

import java.util.List;

public class CourseResponseDto {

    private String id;        // ID Mongo (String)
    private Long hikeId;      // ID de la rando SQL liée
    private List<PointDto> path; // Liste simplifiée des points

    // Constructeurs
    public CourseResponseDto() {}

    public CourseResponseDto(String id, Long hikeId, List<PointDto> path) {
        this.id = id;
        this.hikeId = hikeId;
        this.path = path;
    }

    // --- Getters & Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getHikeId() { return hikeId; }
    public void setHikeId(Long hikeId) { this.hikeId = hikeId; }

    public List<PointDto> getPath() { return path; }
    public void setPath(List<PointDto> path) { this.path = path; }

    /**
     * Petite classe statique interne pour représenter un point simple
     * (C'est ce que le Frontend va envoyer)
     */
    public static class PointDto {
        private double latitude;
        private double longitude;

        public PointDto() {}
        public PointDto(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }

        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
    }
}