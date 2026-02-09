package iut.rodez.projet.sae.fourawalkapi.controller;

import iut.rodez.projet.sae.fourawalkapi.dto.CourseResponseDto;
import iut.rodez.projet.sae.fourawalkapi.dto.GeoCoordinateResponseDto; // <--- Nouvel import
import iut.rodez.projet.sae.fourawalkapi.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    private Long getUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non connecté");
        }
        return (Long) auth.getPrincipal();
    }

    @GetMapping("/my")
    public ResponseEntity<List<CourseResponseDto>> getMyCourses(Authentication auth) {
        Long userId = getUserId(auth);
        return ResponseEntity.ok(courseService.getCoursesByUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponseDto> getCourseById(@PathVariable String id) {
        CourseResponseDto course = courseService.getCourseById(id);
        if (course == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(course);
    }

    /**
     * POST /courses
     * Crée un nouveau suivi de parcours (Mongo) lié à un Hike ID.
     * Body attendu : { "hikeId": 12, "path": [] }
     */
    @PostMapping
    public ResponseEntity<CourseResponseDto> createCourse(@RequestBody CourseResponseDto courseDto) {
        CourseResponseDto created = courseService.createCourse(courseDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * PUT /courses/{id}
     * Ajoute des points à un parcours existant.
     * Body attendu : Liste de points [ {"latitude": 44.3, "longitude": 2.5}, ... ]
     */
    @PutMapping("/{id}")
    public ResponseEntity<CourseResponseDto> addPointsToCourse(
            @PathVariable String id,
            @RequestBody List<GeoCoordinateResponseDto> newPoints) {

        // Note : Idéalement, il faudrait vérifier ici que le parcours appartient bien à l'utilisateur connecté
        // via getUserId(auth) avant d'autoriser la modification.

        CourseResponseDto updated = courseService.addPointsToCourse(id, newPoints);
        return ResponseEntity.ok(updated);
    }
}