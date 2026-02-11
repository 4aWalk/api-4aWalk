package iut.rodez.projet.sae.fourawalkapi.controller;

import iut.rodez.projet.sae.fourawalkapi.document.Course;
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
/**
 * Controlleur gérant tous les end points de gestion des parcours
 */
public class CourseController {

    private final CourseService courseService;

    // Injection du service parcours
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * Récupère l'ID de l'utilisateur à partir du token d'authentification
     * @param auth token d'authentification
     * @return Id de l'utiilisateur
     */
    private Long getUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) { // Utilisateur non reconnu
            throw new RuntimeException("Utilisateur non connecté");
        }
        return (Long) auth.getPrincipal();
    }

    /**
     * Endpoint de récupération de tous les parcours d'un utilisateur
     * @param auth token d'authentification
     * @return La liste de tous les parcours de l'utilisateur authentifié
     */
    @GetMapping("/my")
    public ResponseEntity<List<CourseResponseDto>> getMyCourses(Authentication auth) {
        Long userId = getUserId(auth);
        return ResponseEntity.ok(courseService.getCoursesByUser(userId));
    }

    /**
     * Endpoint de récupération des détails d'un parcours précis
     * @param id identifiant du parcours
     * @return les détails d'une course
     */
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponseDto> getCourseById(@PathVariable String id) {
        CourseResponseDto course = courseService.getCourseById(id);
        if (course == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(course);
    }

    /**
     * Endpoint de création d'un parcours
     * @param courseDto Object parcours devant être créer
     * @return le parcours créer
     */
    @PostMapping
    public ResponseEntity<CourseResponseDto> createCourse(@RequestBody CourseResponseDto courseDto) {
        CourseResponseDto created = courseService.createCourse(courseDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Endpoint d'ajout de coordonné à un parcours
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

    @PutMapping("/{id}/finish")
    public ResponseEntity<Course> finishCourse(@PathVariable String id) {
        Course updatedCourse = courseService.finishCourse(id);
        return ResponseEntity.ok(updatedCourse);
    }

    // 2. Endpoint pour le STATUT (Pause / Reprise)
    // URL pour mettre en pause : PUT /api/courses/{id}/state?paused=true
    // URL pour reprendre :       PUT /api/courses/{id}/state?paused=false
    @PutMapping("/{id}/state")
    public ResponseEntity<Course> setCourseState(
            @PathVariable String id,
            @RequestParam("paused") boolean paused) {

        Course updatedCourse = courseService.setPauseState(id, paused);
        return ResponseEntity.ok(updatedCourse);
    }

}