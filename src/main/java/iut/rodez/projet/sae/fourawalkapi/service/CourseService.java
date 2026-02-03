package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.controller.dto.CourseResponseDto;
import iut.rodez.projet.sae.fourawalkapi.document.Course;
import iut.rodez.projet.sae.fourawalkapi.document.GeoCoordinate;
import iut.rodez.projet.sae.fourawalkapi.repository.mongo.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    /**
     * Récupère le parcours d'une randonnée spécifique
     */
    public CourseResponseDto getCourseByHikeId(Long hikeId) {
        List<Course> courses = courseRepository.findByHikeId(hikeId);

        if (courses.isEmpty()) {
            return null;
        }

        return mapToDto(courses.get(0));
    }

    /**
     * Crée ou met à jour un parcours
     */
    public CourseResponseDto saveCourse(CourseResponseDto dto) {
        Course course = mapToEntity(dto);
        Course savedCourse = courseRepository.save(course);
        return mapToDto(savedCourse);
    }

    // --- MAPPERS (Conversion DTO <-> Entity) ---

    private CourseResponseDto mapToDto(Course entity) {
        List<CourseResponseDto.PointDto> points = new ArrayList<>();

        if (entity.getTrajetsRealises() != null) {
            points = entity.getTrajetsRealises().stream()
                    // CORRECTION ICI : on instancie PointDto, pas CourseResponseDto
                    .map(geo -> new CourseResponseDto.PointDto(geo.getLatitude(), geo.getLongitude()))
                    .collect(Collectors.toList());
        }

        return new CourseResponseDto(
                entity.getId(),
                entity.getHikeId(),
                points
        );
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