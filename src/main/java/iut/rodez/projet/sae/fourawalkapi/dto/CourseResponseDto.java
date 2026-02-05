package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.document.Course;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CourseResponseDto {

    private String id;
    private Long hikeId;
    private LocalDateTime dateRealisation;
    private PointOfInterestResponseDto depart;
    private PointOfInterestResponseDto arrivee;
    private boolean isFinished;
    private boolean isPaused;

    // On utilise maintenant le DTO dédié
    private List<GeoCoordinateResponseDto> path;

    // Constructeurs
    public CourseResponseDto() {}

    public CourseResponseDto(Course course) {
        this.id = course.getId();
        this.hikeId = course.getHikeId();
        this.dateRealisation = course.getDateRealisation();

        // Gestion sécurisée des POI (si null)
        if (course.getDepart() != null) {
            this.depart = new PointOfInterestResponseDto(course.getDepart());
        }
        if (course.getArrivee() != null) {
            this.arrivee = new PointOfInterestResponseDto(course.getArrivee());
        }

        this.isFinished = course.isFinished();
        this.isPaused = course.isPaused();

        // CORRECTION MAJEURE ICI :
        // On doit transformer la List<GeoCoordinate> en List<GeoCoordinateResponseDto>
        if (course.getTrajetsRealises() != null) {
            this.path = course.getTrajetsRealises().stream()
                    .map(GeoCoordinateResponseDto::new) // Appelle le constructeur du DTO pour chaque point
                    .collect(Collectors.toList());
        } else {
            this.path = new ArrayList<>();
        }
    }

    // --- Getters & Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getHikeId() { return hikeId; }
    public void setHikeId(Long hikeId) { this.hikeId = hikeId; }

    public LocalDateTime getDateRealisation() { return dateRealisation; }
    public void setDateRealisation(LocalDateTime dateRealisation) { this.dateRealisation = dateRealisation; }

    public PointOfInterestResponseDto getDepart() { return depart; }
    public void setDepart(PointOfInterestResponseDto depart) { this.depart = depart; }

    public PointOfInterestResponseDto getArrivee() { return arrivee; }
    public void setArrivee(PointOfInterestResponseDto arrivee) { this.arrivee = arrivee; }

    public boolean isFinished() { return isFinished; }
    public void setFinished(boolean finished) { isFinished = finished; }

    public boolean isPaused() { return isPaused; }
    public void setPaused(boolean paused) { isPaused = paused; }

    public List<GeoCoordinateResponseDto> getPath() { return path; }
    public void setPath(List<GeoCoordinateResponseDto> path) { this.path = path; }
}