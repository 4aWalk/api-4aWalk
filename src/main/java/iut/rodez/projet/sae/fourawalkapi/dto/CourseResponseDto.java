package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.document.Course;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data transfert object utilisé dans les communications d'objet parcours avec le client
 */
public class CourseResponseDto {

    private String id;
    private Long hikeId;
    private LocalDateTime dateRealisation;
    private PointOfInterestResponseDto depart;
    private PointOfInterestResponseDto arrivee;
    private boolean isFinished;
    private boolean isPaused;

    // Remplacement par la classe interne définie plus bas
    private List<CoordinateDto> path;

    /**
     * Mapper entity to dto
     * @param course parcours à mapper
     */
    public CourseResponseDto(Course course) {
        this.id = course.getId();
        this.hikeId = course.getHikeId();
        this.dateRealisation = course.getDateRealisation();
        if (course.getDepart() != null) {
            this.depart = new PointOfInterestResponseDto(course.getDepart());
        }
        if (course.getArrivee() != null) {
            this.arrivee = new PointOfInterestResponseDto(course.getArrivee());
        }

        this.isFinished = course.isFinished();
        this.isPaused = course.isPaused();

        if (course.getTrajetsRealises() != null) {
            // On extrait les coordonnées du GeoJsonLineString et on les transforme
            this.path = course.getTrajetsRealises().getCoordinates().stream()
                    // Rappel : dans Spring Point, Y = Latitude et X = Longitude
                    .map(p -> new CoordinateDto(p.getY(), p.getX()))
                    .toList();
        } else {
            this.path = new ArrayList<>();
        }
    }

    // --- Getters & Setters ---
    public String getId() { return id; }

    public Long getHikeId() { return hikeId; }

    public LocalDateTime getDateRealisation() { return dateRealisation; }

    public PointOfInterestResponseDto getDepart() { return depart; }

    public PointOfInterestResponseDto getArrivee() { return arrivee; }

    public boolean getIsFinished() { return isFinished; }

    public boolean getIsPaused() { return isPaused; }

    public List<CoordinateDto> getPath() { return path; }

    // ==========================================
    // CLASSE INTERNE POUR LE FORMATTAGE JSON
    // ==========================================
    public static class CoordinateDto {
        private double latitude;
        private double longitude;

        public CoordinateDto(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
    }
}