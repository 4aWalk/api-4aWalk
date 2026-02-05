package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.dto.CourseResponseDto;
import iut.rodez.projet.sae.fourawalkapi.dto.GeoCoordinateResponseDto; // Import du nouveau DTO
import iut.rodez.projet.sae.fourawalkapi.document.Course;
import iut.rodez.projet.sae.fourawalkapi.document.GeoCoordinate;
import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.repository.mongo.CourseRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.HikeRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final HikeRepository hikeRepository;

    public CourseService(CourseRepository courseRepository, HikeRepository hikeRepository) {
        this.courseRepository = courseRepository;
        this.hikeRepository = hikeRepository;
    }

    // --- LECTURE ---

    public CourseResponseDto getCourseById(String id) {
        return courseRepository.findById(id)
                .map(this::mapToDto)
                .orElse(null);
    }

    public List<CourseResponseDto> getCoursesByUser(Long userId) {
        List<Hike> userHikes = hikeRepository.findByCreatorId(userId);
        List<CourseResponseDto> userCourses = new ArrayList<>();

        for (Hike hike : userHikes) {
            List<Course> coursesForHike = courseRepository.findByHikeId(hike.getId());
            userCourses.addAll(coursesForHike.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList()));
        }
        return userCourses;
    }

    // --- ECRITURE ---

    /**
     * Crée un nouveau parcours lié à une randonnée.
     */
    public CourseResponseDto createCourse(CourseResponseDto dto) {
        // Validation : Vérifier que la Rando existe
        if (dto.getHikeId() != null) {
            hikeRepository.findById(dto.getHikeId())
                    .orElseThrow(() -> new RuntimeException("Randonnée introuvable avec l'ID " + dto.getHikeId()));
        }

        Course course = mapToEntity(dto);

        // Initialisation de la liste pour éviter les erreurs null plus tard
        if (course.getTrajetsRealises() == null) {
            course.setTrajetsRealises(new ArrayList<>());
        }

        Course savedCourse = courseRepository.save(course);
        return mapToDto(savedCourse);
    }

    /**
     * Ajoute une liste de nouveaux points à un parcours existant.
     * Met à jour la signature pour utiliser GeoCoordinateResponseDto.
     */
    public CourseResponseDto addPointsToCourse(String courseId, List<GeoCoordinateResponseDto> newPointsDto) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Parcours introuvable"));

        if (newPointsDto != null && !newPointsDto.isEmpty()) {
            // Conversion DTO -> Document (Entity)
            List<GeoCoordinate> newCoordinates = newPointsDto.stream()
                    .map(p -> new GeoCoordinate(p.getLatitude(), p.getLongitude())) // Utilise le constructeur GeoCoordinate(lat, lon)
                    .collect(Collectors.toList());

            // Ajout à la liste existante
            if (course.getTrajetsRealises() == null) {
                course.setTrajetsRealises(new ArrayList<>());
            }
            course.getTrajetsRealises().addAll(newCoordinates);

            course = courseRepository.save(course);
        }

        return mapToDto(course);
    }

    // --- MAPPERS ---

    /**
     * Transforme l'Entité Mongo en DTO.
     * La logique a été déplacée dans le constructeur de CourseResponseDto pour être plus propre.
     */
    private CourseResponseDto mapToDto(Course entity) {
        return new CourseResponseDto(entity);
    }

    /**
     * Transforme le DTO en Entité Mongo.
     */
    private Course mapToEntity(CourseResponseDto dto) {
        Course course = new Course();
        course.setId(dto.getId());
        course.setHikeId(dto.getHikeId());

        // Mapping manuel inversé pour le path
        List<GeoCoordinate> coordinates = new ArrayList<>();
        if (dto.getPath() != null) {
            // On convertit chaque DTO (lat/lon) en objet GeoCoordinate (qui contient le GeoJsonPoint interne)
            coordinates = dto.getPath().stream()
                    .map(p -> new GeoCoordinate(p.getLatitude(), p.getLongitude()))
                    .collect(Collectors.toList());
        }
        course.setTrajetsRealises(coordinates);

        return course;
    }
}