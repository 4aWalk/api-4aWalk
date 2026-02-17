package iut.rodez.projet.sae.fourawalkapi.document;

import iut.rodez.projet.sae.fourawalkapi.entity.PointOfInterest;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Parcours réalisé par le(s) randonneur(s)
 */
@Document(collection = "courses")
public class Course {

    /* Identifiant du parcours */
    @Id
    private String id;

    /* Identifiant de la randonné lié au parcours */
    private Long hikeId;

    /* Date de réalisation du parcours */
    private LocalDateTime dateRealisation;

    /* Point de départ du parcours */
    private PointOfInterest depart;

    /* Point d'arrivée du parcours */
    private PointOfInterest arrivee;

    /* La finalisation dui parcours */
    private boolean isFinished;

    /* État de pause du parcours */
    private boolean isPaused;

    /** Liste coordonnée des relevés GPS */
    private List<GeoCoordinate> trajetsRealises;

    // --- Constructeurs ---

    public Course() {
        this.trajetsRealises = new ArrayList<>();
        this.dateRealisation = LocalDateTime.now();
        this.isFinished = false;
        this.isPaused = false;
    }

    public Course(Long hikeId, PointOfInterest depart) {
        this();
        this.hikeId = hikeId;
        this.depart = depart;
    }

    // --- Logique métier de bas niveau ---

    public void togglePause() {
        this.isPaused = !this.isPaused;
    }

    // --- Getters et Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getHikeId() { return hikeId; }
    public void setHikeId(Long hikeId) { this.hikeId = hikeId; }

    public LocalDateTime getDateRealisation() { return dateRealisation; }
    public void setDateRealisation(LocalDateTime dateRealisation) { this.dateRealisation = dateRealisation; }

    public PointOfInterest getDepart() { return depart; }
    public void setDepart(PointOfInterest depart) { this.depart = depart; }

    public PointOfInterest getArrivee() { return arrivee; }
    public void setArrivee(PointOfInterest arrivee) { this.arrivee = arrivee; }

    public boolean isFinished() { return isFinished; }
    public void setFinished(boolean finished) { isFinished = finished; }

    public boolean isPaused() { return isPaused; }
    public void setPaused(boolean paused) { isPaused = paused; }

    public List<GeoCoordinate> getTrajetsRealises() { return trajetsRealises; }
    public void setTrajetsRealises(List<GeoCoordinate> trajetsRealises) { this.trajetsRealises = trajetsRealises; }
}