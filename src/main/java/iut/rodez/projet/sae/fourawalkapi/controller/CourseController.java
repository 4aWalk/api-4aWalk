package iut.rodez.projet.sae.fourawalkapi.controller;

import iut.rodez.projet.sae.fourawalkapi.document.Course;
import iut.rodez.projet.sae.fourawalkapi.dto.CourseResponseDto;
import iut.rodez.projet.sae.fourawalkapi.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static iut.rodez.projet.sae.fourawalkapi.security.SecurityUtils.getUserId;

/**
 * Controlleur gérant tous les end points de gestion des parcours
 */
@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    /**
     * Injection du parcours service
     * @param courseService service parcours
     */
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
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
     * Crée un nouveau parcours.
     * @param dto Les infos du parcours (doit contenir hikeId)
     * @param auth Le token de sécurité
     * @return Le parcours créé
     */
    @PostMapping
    public ResponseEntity<CourseResponseDto> createCourse(@RequestBody CourseResponseDto dto, Authentication auth) {
        CourseResponseDto created = courseService.createCourse(dto, getUserId(auth));
        return ResponseEntity.ok(created);
    }

    /**
     * Ajout de coordonné à un parcours
     * @param id identifiant du parcours
     * @param newPoints ensemble de points de géolocalisation à ajouter au parcours
     * @return le parcours avec les nouveaux points
     */
    @PutMapping("/{id}")
    public ResponseEntity<CourseResponseDto> addPointsToCourse(
            @PathVariable String id,
            @RequestBody List<CourseResponseDto.CoordinateDto> newPoints,
            Authentication auth) {

        CourseResponseDto updated = courseService.addPointsToCourse(id, newPoints, getUserId(auth));
        return ResponseEntity.ok(updated);
    }

    /**
     * Mettre fin à la randonné
     * @param id identifiant du parcours
     * @return le parcours mise à jour
     */
    @PutMapping("/{id}/finish")
    public ResponseEntity<Course> finishCourse(@PathVariable String id) {
        Course updatedCourse = courseService.finishCourse(id);
        return ResponseEntity.ok(updatedCourse);
    }

    /**
     * Mise en pause / cours d'un parcours
     * @param id du parcours
     * @return le parcours avec le statut à jour
     */
    @PutMapping("/{id}/state")
    public ResponseEntity<Course> setCourseState(
            @PathVariable String id) {

        Course updatedCourse = courseService.setPauseState(id);
        return ResponseEntity.ok(updatedCourse);
    }

}