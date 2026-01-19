package iut.rodez.projet.sae.fourawalkapi.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Représente le parcours réel (historique GPS) réalisé pendant une randonnée.
 * Document MongoDB - UC012 (Suivi de randonnée).
 */
@Document(collection = "courses")
public class Course {

    @Id
    private String id;

    private Long hikeId;

    private LocalDateTime dateRealisation;

    private String depart;

    private String arrivee;

    private boolean isFinished;

    private boolean isPaused;

    /** Liste ordonnée des relevés GPS */
    private List<GeoCoordinate> trajetsRealises;

    // --- Constructeurs ---

    public Course() {
        this.trajetsRealises = new ArrayList<>();
        this.dateRealisation = LocalDateTime.now();
        this.isFinished = false;
        this.isPaused = false;
    }

    public Course(Long hikeId, String depart) {
        this();
        this.hikeId = hikeId;
        this.depart = depart;
    }

    // --- Logique métier de bas niveau (Entity Logic) ---

    /**
     * Ajoute un point GPS et gère la sécurité d'état.
     */
    public void addCoordinate(GeoCoordinate point) {
        if (isFinished) {
            throw new IllegalStateException("Le parcours est terminé, impossible d'ajouter des points.");
        }
        if (!isPaused) {
            this.trajetsRealises.add(point);
        }
    }

    /**
     * Calcule la distance totale parcourue en mètres en cumulant
     * les distances entre chaque point successif.
     */
    public double calculateTotalDistance() {
        if (trajetsRealises == null || trajetsRealises.size() < 2) {
            return 0.0;
        }
        double total = 0.0;
        for (int i = 0; i < trajetsRealises.size() - 1; i++) {
            total += trajetsRealises.get(i).distanceTo(trajetsRealises.get(i + 1));
        }
        return total;
    }

    public void togglePause() {
        this.isPaused = !this.isPaused;
    }

    public void terminate(String arriveeFinale) {
        this.isFinished = true;
        this.isPaused = false;
        this.arrivee = arriveeFinale;
    }

    // --- Overrides Standards ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        // On compare l'ID MongoDB si présent, sinon le hikeId et la date
        return Objects.equals(id, course.id) ||
                (Objects.equals(hikeId, course.hikeId) && Objects.equals(dateRealisation, course.dateRealisation));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, hikeId, dateRealisation);
    }

    @Override
    public String toString() {
        return String.format("Course[id=%s, hikeId=%d, points=%d, status=%s]",
                id, hikeId, trajetsRealises.size(), isFinished ? "FINISHED" : (isPaused ? "PAUSED" : "ACTIVE"));
    }

    // --- Getters et Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getHikeId() { return hikeId; }
    public void setHikeId(Long hikeId) { this.hikeId = hikeId; }

    public LocalDateTime getDateRealisation() { return dateRealisation; }
    public void setDateRealisation(LocalDateTime dateRealisation) { this.dateRealisation = dateRealisation; }

    public String getDepart() { return depart; }
    public void setDepart(String depart) { this.depart = depart; }

    public String getArrivee() { return arrivee; }
    public void setArrivee(String arrivee) { this.arrivee = arrivee; }

    public boolean isFinished() { return isFinished; }
    public void setFinished(boolean finished) { isFinished = finished; }

    public boolean isPaused() { return isPaused; }
    public void setPaused(boolean paused) { isPaused = paused; }

    public List<GeoCoordinate> getTrajetsRealises() { return trajetsRealises; }
    public void setTrajetsRealises(List<GeoCoordinate> trajetsRealises) { this.trajetsRealises = trajetsRealises; }
}