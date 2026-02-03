package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.dto.CourseResponseDto;
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
    private final HikeRepository hikeRepository; // <--- AJOUT : Nécessaire pour le lien User -> Hike -> Course

    public CourseService(CourseRepository courseRepository, HikeRepository hikeRepository) {
        this.courseRepository = courseRepository;
        this.hikeRepository = hikeRepository;
    }

    /**
     * Récupère un parcours précis via son ID MongoDB (String)
     */
    public CourseResponseDto getCourseById(String id) {
        return courseRepository.findById(id)
                .map(this::mapToDto)
                .orElse(null);
    }

    /**
     * Récupère tous les parcours appartenant à un utilisateur spécifique
     */
    public List<CourseResponseDto> getCoursesByUser(Long userId) {
        // 1. On récupère toutes les randos (SQL) du créateur
        List<Hike> userHikes = hikeRepository.findByCreatorId(userId);

        List<CourseResponseDto> userCourses = new ArrayList<>();

        // 2. Pour chaque rando, on regarde si un parcours (Mongo) existe
        for (Hike hike : userHikes) {
            List<Course> coursesForHike = courseRepository.findByHikeId(hike.getId());

            // On convertit et on ajoute à la liste
            userCourses.addAll(coursesForHike.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList()));
        }

        return userCourses;
    }

    // --- LE RESTE EST INCHANGÉ ---

    public CourseResponseDto getCourseByHikeId(Long hikeId) {
        List<Course> courses = courseRepository.findByHikeId(hikeId);
        if (courses.isEmpty()) return null;
        return mapToDto(courses.get(0));
    }

    public CourseResponseDto saveCourse(CourseResponseDto dto) {
        Course course = mapToEntity(dto);
        Course savedCourse = courseRepository.save(course);
        return mapToDto(savedCourse);
    }

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