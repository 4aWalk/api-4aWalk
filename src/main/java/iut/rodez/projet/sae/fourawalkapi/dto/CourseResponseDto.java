package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.document.Course;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CourseResponseDto {

    private String id;
    private Long hikeId;
    private LocalDateTime dateRealisation;
    private PointOfInterestResponseDto depart;
    private PointOfInterestResponseDto arrivee;
    private boolean isFinished;
    private boolean isPaused;
    private List<CoordinateDto> path;

    public CourseResponseDto() {
    }

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
            this.path = course.getTrajetsRealises().getCoordinates().stream()
                    .map(p -> new CoordinateDto(p.getY(), p.getX()))
                    .toList();
        } else {
            this.path = new ArrayList<>();
        }
    }

    // Getters existants...
    public String getId() { return id; }
    public Long getHikeId() { return hikeId; }
    public LocalDateTime getDateRealisation() { return dateRealisation; }
    public PointOfInterestResponseDto getDepart() { return depart; }
    public PointOfInterestResponseDto getArrivee() { return arrivee; }
    public boolean getIsFinished() { return isFinished; }
    public boolean getIsPaused() { return isPaused; }
    public List<CoordinateDto> getPath() { return path; }
    public void setHikeId(Long hikeId) { this.hikeId = hikeId; }
    public void setPath(List<CoordinateDto> path) { this.path = path; }

    // ==========================================
    // CLASSE INTERNE
    // ==========================================
    public static class CoordinateDto {
        private double latitude;
        private double longitude;

        public CoordinateDto() {
        }

        public CoordinateDto(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }

        public void setLatitude(double latitude) { this.latitude = latitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
    }
}