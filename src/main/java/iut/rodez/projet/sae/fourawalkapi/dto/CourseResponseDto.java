package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.document.Course;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private List<GeoCoordinateResponseDto> path;

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
            this.path = course.getTrajetsRealises().stream()
                    .map(GeoCoordinateResponseDto::new) // Appelle le constructeur du DTO pour chaque point
                    .collect(Collectors.toList());
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

    public List<GeoCoordinateResponseDto> getPath() { return path; }
}