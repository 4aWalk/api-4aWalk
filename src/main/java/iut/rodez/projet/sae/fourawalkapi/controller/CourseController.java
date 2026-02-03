package iut.rodez.projet.sae.fourawalkapi.controller;

import iut.rodez.projet.sae.fourawalkapi.dto.CourseResponseDto;
import iut.rodez.projet.sae.fourawalkapi.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses") // Attention : pluriel standard pour les API REST
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * Utilitaire pour récupérer l'ID du user connecté
     */
    private Long getUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non connecté");
        }
        return (Long) auth.getPrincipal();
    }

    /**
     * GET /courses/my
     * Renvoie la liste de tous les parcours de l'utilisateur connecté.
     */
    @GetMapping("/my")
    public ResponseEntity<List<CourseResponseDto>> getMyCourses(Authentication auth) {
        Long userId = getUserId(auth);
        List<CourseResponseDto> myCourses = courseService.getCoursesByUser(userId);
        return ResponseEntity.ok(myCourses);
    }

    /**
     * GET /courses/{id}
     * Renvoie les détails d'un seul parcours via son ID MongoDB.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponseDto> getCourseById(@PathVariable String id) {
        CourseResponseDto course = courseService.getCourseById(id);

        if (course == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(course);
    }
}