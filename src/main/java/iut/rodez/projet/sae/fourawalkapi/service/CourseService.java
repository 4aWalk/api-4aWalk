package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.dto.CourseResponseDto;
import iut.rodez.projet.sae.fourawalkapi.dto.GeoCoordinateResponseDto;
import iut.rodez.projet.sae.fourawalkapi.document.Course;
import iut.rodez.projet.sae.fourawalkapi.document.GeoCoordinate;
import iut.rodez.projet.sae.fourawalkapi.dto.PointOfInterestResponseDto;
import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.entity.PointOfInterest;
import iut.rodez.projet.sae.fourawalkapi.repository.mongo.CourseRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.HikeRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                    .collect(Collectors.toList()));
        }
        return userCourses;
    }

    // --- ECRITURE ---

    /**
     * Initialise une nouvelle session de suivi (Course).
     * Vérifie l'intégrité référentielle avec MySQL avant de créer le document MongoDB.
     *
     * @param dto Les données initiales du parcours.
     * @return Le parcours créé avec son ID MongoDB généré.
     */
    public CourseResponseDto createCourse(CourseResponseDto dto) {
        // Validation d'existence : On ne peut pas suivre une randonnée qui n'existe pas en base SQL
        if (dto.getHikeId() != null) {
            hikeRepository.findById(dto.getHikeId())
                    .orElseThrow(() -> new RuntimeException("Erreur intégrité : Randonnée introuvable avec l'ID " + dto.getHikeId()));
        }

        Course course = mapToEntity(dto);

        // Initialisation défensive de la liste de points pour éviter les NullPointerException
        if (course.getTrajetsRealises() == null) {
            course.setTrajetsRealises(new ArrayList<>());
        }

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
    public CourseResponseDto addPointsToCourse(String courseId, List<GeoCoordinateResponseDto> newPointsDto) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Parcours introuvable pour ajout de points"));

        if (newPointsDto != null && !newPointsDto.isEmpty()) {
            // Transformation des DTOs légers en objets métier GeoCoordinate
            List<GeoCoordinate> newCoordinates = newPointsDto.stream()
                    .map(p -> new GeoCoordinate(p.getLatitude(), p.getLongitude()))
                    .collect(Collectors.toList());

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
     * Une fois terminée, une course ne peut plus être modifiée ni mise en pause.
     */
    public Course finishCourse(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course non trouvée"));

        // Règle métier : Idempotence et sécurité d'état
        if (course.isFinished()) {
            throw new RuntimeException("Action illégale : Cette course est déjà terminée.");
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

    /**
     * Transforme l'Entité de persistance (MongoDB) en objet de transfert (DTO).
     * Isole la structure interne de la base de données de l'API exposée.
     * @param entity parcours à mapper
     * @return parcours mappé
     */
    private CourseResponseDto mapToDto(Course entity) {
        return new CourseResponseDto(entity);
    }

    /**
     * Transforme le DTO entrant en Entité métier pour la persistance.
     * Gère le mapping manuel des champs complexes (listes, objets imbriqués) et les valeurs par défaut.
     * @param dto parcours à mapper
     * @return parcours mappé
     */
    private Course mapToEntity(CourseResponseDto dto) {
        Course course = new Course();

        // Mapping des identifiants et références clés
        course.setId(dto.getId());
        course.setHikeId(dto.getHikeId());

        // Gestion de la temporalité : utilise la date fournie ou conserve null
        if (dto.getDateRealisation() != null) {
            course.setDateRealisation(dto.getDateRealisation());
        }

        // Mapping des états (Booleans)
        course.setFinished(dto.getIsFinished());
        course.setPaused(dto.getIsPaused());

        // Mapping des Points d'Intérêt (Conversion DTO -> Entité SQL/Mongo compatible)
        if (dto.getDepart() != null) {
            course.setDepart(mapPoiDtoToEntity(dto.getDepart()));
        }
        if (dto.getArrivee() != null) {
            course.setArrivee(mapPoiDtoToEntity(dto.getArrivee()));
        }

        // Reconstruction du chemin complet (Path)
        List<GeoCoordinate> coordinates = new ArrayList<>();
        if (dto.getPath() != null) {
            coordinates = dto.getPath().stream()
                    .map(p -> new GeoCoordinate(p.getLatitude(), p.getLongitude()))
                    .collect(Collectors.toList());
        }
        course.setTrajetsRealises(coordinates);

        return course;
    }

    /**
     * Helper de mapping pour les Points d'Intérêt (POI).
     * Assure la conversion propre des données géographiques statiques.
     * @param dto dto à convertir en entité
     * @return entité poi construit
     */
    private PointOfInterest mapPoiDtoToEntity(PointOfInterestResponseDto dto) {

        PointOfInterest poi = new PointOfInterest();
        poi.setId(dto.getId());
        poi.setName(dto.getNom());
        poi.setDescription(dto.getDescription());
        poi.setLatitude(dto.getLatitude());
        poi.setLongitude(dto.getLongitude());

        return poi;
    }
}