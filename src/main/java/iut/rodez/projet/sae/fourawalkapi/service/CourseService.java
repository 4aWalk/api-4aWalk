package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.dto.CourseResponseDto;
import iut.rodez.projet.sae.fourawalkapi.dto.GeoCoordinateResponseDto;
import iut.rodez.projet.sae.fourawalkapi.document.Course;
import iut.rodez.projet.sae.fourawalkapi.document.GeoCoordinate;
import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.entity.PointOfInterest;
import iut.rodez.projet.sae.fourawalkapi.repository.mongo.CourseRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.HikeRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service métier responsable de la gestion des parcours (sessions de marche).
 * Architecture Hybride :
 * - Utilise MySQL (HikeRepository) pour vérifier l'existence des randonnées de référence.
 * - Utilise MongoDB (CourseRepository) pour stocker les données volumineuses de géolocalisation (le tracé).
 */
@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final HikeRepository hikeRepository;

    /**
     * Injection des dépendances pour permettre l'interaction Cross-Store (SQL + NoSQL).
     */
    public CourseService(CourseRepository courseRepository, HikeRepository hikeRepository) {
        this.courseRepository = courseRepository;
        this.hikeRepository = hikeRepository;
    }

    // --- LECTURE ---

    /**
     * Récupère un parcours spécifique par son identifiant MongoDB.
     * Convertit le document brut en DTO pour l'exposition API.
     *
     * @param id L'identifiant unique du parcours (ObjectId stringifié).
     * @return Le DTO contenant les détails du parcours ou null si introuvable.
     */
    public CourseResponseDto getCourseById(String id) {
        return courseRepository.findById(id)
                .map(this::mapToDto)
                .orElse(null);
    }

    /**
     * Récupère l'historique de tous les parcours effectués par un utilisateur.
     * Stratégie :
     * 1. Récupération des randonnées (Hike) créées par l'utilisateur via MySQL.
     * 2. Pour chaque randonnée, interrogation de MongoDB pour trouver les traces associées.
     *
     * @param userId L'ID de l'utilisateur (MySQL).
     * @return Une liste agrégée de tous les parcours DTO.
     */
    public List<CourseResponseDto> getCoursesByUser(Long userId) {
        List<Hike> userHikes = hikeRepository.findByCreatorId(userId);
        List<CourseResponseDto> userCourses = new ArrayList<>();

        for (Hike hike : userHikes) {
            // Jointure logique applicative entre l'ID SQL (Hike) et le champ hikeId dans Mongo
            List<Course> coursesForHike = courseRepository.findByHikeId(hike.getId());
            userCourses.addAll(coursesForHike.stream()
                    .map(this::mapToDto)
                    .toList());
        }
        return userCourses;
    }

    // --- ECRITURE ---

    /**
     * Initialise une nouvelle session de suivi (Course).
     * Vérifie l'intégrité référentielle avec MySQL avant de créer le document MongoDB.
     * Règle métier : Le premier point du tracé devient automatiquement le Point de Départ.
     *
     * @param dto Les données initiales du parcours.
     * @return Le parcours créé avec son ID MongoDB généré.
     */
    public CourseResponseDto createCourse(CourseResponseDto dto, Long userId) {

        if (dto.getHikeId() == null) {
            throw new IllegalArgumentException("Impossible de créer un parcours sans l'ID de la randonnée associée.");
        }

        Hike hike = hikeRepository.findById(dto.getHikeId())
                .orElseThrow(() -> new RuntimeException("Erreur intégrité : Randonnée introuvable avec l'ID " +
                        dto.getHikeId()));

        if (!hike.getCreator().getId().equals(userId)) {
            throw new SecurityException("Accès refusé : Vous ne pouvez pas démarrer un parcours sur une randonnée" +
                    " qui ne vous appartient pas.");
        }

        Course course = new Course();
        course.setHikeId(hike.getId());

        if (dto.getDateRealisation() != null) {
            course.setDateRealisation(dto.getDateRealisation());
        }

        // Extraction de la trace GPS initiale
        List<GeoCoordinate> coordinates = new ArrayList<>();
        if (dto.getPath() != null && !dto.getPath().isEmpty()) {
            coordinates = dto.getPath().stream()
                    .map(p -> new GeoCoordinate(p.getLatitude(), p.getLongitude()))
                    .toList();
        }
        course.setTrajetsRealises(coordinates);

        if (course.getTrajetsRealises().isEmpty()) {
            throw new IllegalArgumentException("Impossible de créer un parcours " +
                    "sans au moins un point de géolocalisation initial.");
        }

        GeoCoordinate startCoord = course.getTrajetsRealises().getFirst();
        course.setDepart(createPoiFromGeo(startCoord, "Départ"));

        course.setFinished(false);
        course.setPaused(false);

        Course savedCourse = courseRepository.save(course);

        return mapToDto(savedCourse);
    }

    /**
     * Ajoute un lot de coordonnées GPS à un parcours en cours.
     * Cette méthode est appelée périodiquement par le client mobile pour mettre à jour le tracé.
     *
     * @param courseId L'identifiant du parcours à mettre à jour.
     * @param newPointsDto La liste des nouveaux points capturés.
     * @return Le parcours mis à jour.
     */
    public CourseResponseDto addPointsToCourse(String courseId, List<GeoCoordinateResponseDto> newPointsDto,
                                               Long userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Parcours introuvable"));

        Hike hike = hikeRepository.findById(course.getHikeId())
                .orElseThrow(() -> new RuntimeException("Randonnée associée introuvable"));

        if (!hike.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Accès refusé : Ce parcours ne vous appartient pas.");
        }

        if (newPointsDto != null && !newPointsDto.isEmpty()) {
            // Transformation des DTOs légers en objets métier GeoCoordinate
            List<GeoCoordinate> newCoordinates = newPointsDto.stream()
                    .map(p -> new GeoCoordinate(p.getLatitude(), p.getLongitude()))
                    .toList();

            // Ajout à la collection existante (append-only logic)
            if (course.getTrajetsRealises() == null) {
                course.setTrajetsRealises(new ArrayList<>());
            }
            course.getTrajetsRealises().addAll(newCoordinates);

            course = courseRepository.save(course);
        }

        return mapToDto(course);
    }

    /**
     * Clôture définitivement une session de marche.
     * Règle métier : Le dernier point du tracé devient automatiquement le Point d'Arrivée.
     * Une fois terminée, une course ne peut plus être modifiée ni mise en pause.
     */
    public Course finishCourse(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course non trouvée"));

        // Règle métier : Idempotence et sécurité d'état
        if (course.isFinished()) {
            throw new RuntimeException("Action illégale : Cette course est déjà terminée.");
        }

        // Gestion du point d'arrivée
        List<GeoCoordinate> path = course.getTrajetsRealises();
        if (path != null && !path.isEmpty()) {
            // On récupère le dernier point connu
            GeoCoordinate lastPoint = path.getLast();
            course.setArrivee(createPoiFromGeo(lastPoint, "Arrivée"));
        }

        course.setFinished(true);
        course.setPaused(false); // Force la fin de la pause si elle était active

        return courseRepository.save(course);
    }

    /**
     * Bascule l'état de pause du parcours (Toggle).
     * Permet d'interrompre l'enregistrement du temps ou des calculs de vitesse moyenne.
     * @param courseId identifiant du parcours
     * @return parcours mis à jour
     */
    public Course setPauseState(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course non trouvée"));

        // Sécurité : Impossible de modifier l'état d'une archive
        if (course.isFinished()) {
            throw new RuntimeException("Action illégale : Impossible de modifier une course terminée.");
        }

        course.togglePause();

        return courseRepository.save(course);
    }

    // --- MAPPERS (Conversion de Données) ---
    private CourseResponseDto mapToDto(Course entity) {
        return new CourseResponseDto(entity);
    }

    /**
     * Méthode utilitaire pour créer un POI à partir d'une coordonnée GPS.
     * Utilise l'objet GeoJSON interne pour récupérer X et Y.
     */
    private PointOfInterest createPoiFromGeo(GeoCoordinate geo, String defaultName) {
        PointOfInterest poi = new PointOfInterest();

        if (geo.getGeojson() != null) {
            poi.setLatitude(geo.getGeojson().getX());
            poi.setLongitude(geo.getGeojson().getY());
        }

        poi.setName(defaultName);
        poi.setDescription("Point généré automatiquement lors du suivi.");
        return poi;
    }
}