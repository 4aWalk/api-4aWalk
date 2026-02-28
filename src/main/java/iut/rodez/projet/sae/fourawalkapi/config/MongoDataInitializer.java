package iut.rodez.projet.sae.fourawalkapi.config;

import iut.rodez.projet.sae.fourawalkapi.document.Course;
import iut.rodez.projet.sae.fourawalkapi.entity.PointOfInterest;
import iut.rodez.projet.sae.fourawalkapi.repository.mongo.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonLineString;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Launcher qui s'exécute automatiquement au démarrage de l'application.
 * Il initialise les données MongoDB selon la configuration de l'environnement (Dev/Prod).
 */
@Component
public class MongoDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MongoDataInitializer.class);

    private final CourseRepository courseRepository;

    // On récupère la propriété des fichiers properties (par défaut "if-empty" si non trouvée)
    @Value("${app.mongo.init.mode:if-empty}")
    private String initMode;

    public MongoDataInitializer(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        // 1. Si on est en production, on coupe tout de suite
        if ("never".equalsIgnoreCase(initMode)) {
            logger.info("Mode de production détecté : Aucune initialisation MongoDB n'aura lieu.");
            return;
        }

        // 2. Si on est en dev (mode "always"), on vide la collection pour un démarrage propre (OVERRIDE)
        if ("always".equalsIgnoreCase(initMode)) {
            logger.info("Mode 'always' activé : Suppression des anciennes données MongoDB...");
            courseRepository.deleteAll();
        }

        // 3. On insère les données uniquement si la base est vide
        if (courseRepository.count() == 0) {
            logger.info("Création du jeu de données de test MongoDB en cours...");

            // --- 1. Parcours TERMINÉ (hike_id = 1) ---
            Course course1 = new Course(1L, createPoi("D A", "Point de départ 1", 44.1, 2.1));
            course1.setArrivee(createPoi("A A", "Point d'arrivée", 44.2, 2.2));
            course1.setDateRealisation(LocalDateTime.now().minusDays(5));
            course1.setFinished(true);
            course1.setPaused(false);
            course1.setTrajetsRealises(new GeoJsonLineString(List.of(
                    new Point(2.1, 44.1),
                    new Point(2.15, 44.15),
                    new Point(2.2, 44.2)
            )));

            // --- 2. Parcours EN COURS (hike_id = 2) ---
            Course course2 = new Course(2L, createPoi("D B", "Point de départ 2", 45.1, 3.1));
            course2.setDateRealisation(LocalDateTime.now().minusHours(2));
            course2.setFinished(false);
            course2.setPaused(false);
            course2.setTrajetsRealises(new GeoJsonLineString(List.of(
                    new Point(3.1, 45.1),
                    new Point(3.12, 45.11),
                    new Point(3.15, 45.12)
            )));

            // --- 3. Parcours EN PAUSE (hike_id = 1) ---
            Course course3 = new Course(1L, createPoi("D C", "Point de départ 3", 46.1, 4.1));
            course3.setDateRealisation(LocalDateTime.now().minusHours(5));
            course3.setFinished(false);
            course3.setPaused(true);
            course3.setTrajetsRealises(new GeoJsonLineString(List.of(
                    new Point(4.1, 46.1),
                    new Point(4.15, 46.15)
            )));

            // Sauvegarde dans MongoDB
            courseRepository.saveAll(List.of(course1, course2, course3));
            logger.info("Jeu de données MongoDB initialisé avec succès !");

        } else {
            logger.info("Les données MongoDB existent déjà, aucune initialisation requise.");
        }
    }

    /**
     * Méthode utilitaire pour créer rapidement un PointOfInterest
     */
    private PointOfInterest createPoi(String nom, String description, double latitude, double longitude) {
        PointOfInterest poi = new PointOfInterest();
        poi.setName(nom);
        poi.setDescription(description);
        poi.setLatitude(latitude);
        poi.setLongitude(longitude);
        return poi;
    }
}