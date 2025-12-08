package iut.rodez.projet.sae.fourawalkapi.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

/** Parcours (Historique du tracé réalisé pendant une randonnée - UC012) */
@Document(collection = "courses")
public class Course {

    @Id
    private String id; // Clé MongoDB
    private Long hikeId; // ID de la randonnée MySQL associée
    private LocalDateTime dateRealisation;
    private String depart;
    private String arrivee;
    private boolean isFinished;
    private boolean isPaused;

    // Le tracé réalisé : une liste de coordonnées GPS et leur timestamp
    private List<GeoCoordinate> trajetsRealises;

    public Course() {}
    public Course(Long hikeId, LocalDateTime dateRealisation, String depart, String arrivee, List<GeoCoordinate> trajetsRealises) {
        this.hikeId = hikeId;
        this.dateRealisation = dateRealisation;
        this.depart = depart;
        this.arrivee = arrivee;
        this.trajetsRealises = trajetsRealises;
        this.isFinished = false;
        this.isPaused = false;
    }
}
