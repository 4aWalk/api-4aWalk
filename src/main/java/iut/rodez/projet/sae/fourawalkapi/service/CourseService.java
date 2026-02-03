package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.dto.CourseResponseDto;
import iut.rodez.projet.sae.fourawalkapi.document.Course;
import iut.rodez.projet.sae.fourawalkapi.document.GeoCoordinate;
import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.repository.mongo.CourseRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.HikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * Crée un nouveau parcours (généralement vide au départ) lié à une randonnée.
     */
    public CourseResponseDto createCourse(CourseResponseDto dto) {
        // Validation : Vérifier que la Rando existe (Optionnel mais recommandé)
        if (dto.getHikeId() != null) {
            hikeRepository.findById(dto.getHikeId())
                    .orElseThrow(() -> new RuntimeException("Randonnée introuvable avec l'ID " + dto.getHikeId()));
        }

        Course course = mapToEntity(dto);
        // On s'assure que la liste est initialisée pour éviter les NullPointer plus tard
        if (course.getTrajetsRealises() == null) {
            course.setTrajetsRealises(new ArrayList<>());
        }

        Course savedCourse = courseRepository.save(course);
        return mapToDto(savedCourse);
    }

    /**
     * Ajoute une liste de nouveaux points à un parcours existant.
     * Utile pour envoyer les coordonnées GPS par paquets.
     */
    public CourseResponseDto addPointsToCourse(String courseId, List<CourseResponseDto.PointDto> newPoints) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Parcours introuvable"));

        if (newPoints != null && !newPoints.isEmpty()) {
            // Conversion DTO -> Document
            List<GeoCoordinate> newCoordinates = newPoints.stream()
                    .map(p -> new GeoCoordinate(p.getLatitude(), p.getLongitude()))
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

    private CourseResponseDto mapToDto(Course entity) {
        List<CourseResponseDto.PointDto> points = new ArrayList<>();
        if (entity.getTrajetsRealises() != null) {
            points = entity.getTrajetsRealises().stream()
                    .map(geo -> new CourseResponseDto.PointDto(geo.getLatitude(), geo.getLongitude()))
                    .collect(Collectors.toList());
        }
        return new CourseResponseDto(entity.getId(), entity.getHikeId(), points);
    }

    private Course mapToEntity(CourseResponseDto dto) {
        Course course = new Course();
        course.setId(dto.getId());
        course.setHikeId(dto.getHikeId());
        List<GeoCoordinate> coordinates = new ArrayList<>();
        if (dto.getPath() != null) {
            for (CourseResponseDto.PointDto p : dto.getPath()) {
                coordinates.add(new GeoCoordinate(p.getLatitude(), p.getLongitude()));
            }
        }
        course.setTrajetsRealises(coordinates);
        return course;
    }
}