package iut.rodez.projet.sae.fourawalkapi.controller;

import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.security.JwtTokenProvider;
import iut.rodez.projet.sae.fourawalkapi.service.HikeService;
import iut.rodez.projet.sae.fourawalkapi.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hikes")
public class HikeController {

    private final HikeService hikeService;
    private final JwtTokenProvider tokenProvider; // Adapté selon ton image
    private final UserService userService;

    public HikeController(HikeService hikeService, JwtTokenProvider tokenProvider, UserService userService) {
        this.hikeService = hikeService;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    private Long getCurrentUserId(String token) {
        String jwt = token.substring(7);
        // On utilise la méthode de ton JwtTokenProvider (souvent getUsernameFromToken ou similaire)
        String email = tokenProvider.getUsername(jwt);
        return userService.findByMail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"))
                .getId();
    }

    // GET /api/v1/hikes/my -> Liste des randonnées de l'utilisateur connecté
    @GetMapping("/my")
    public ResponseEntity<List<Hike>> getMyHikes(@RequestHeader("Authorization") String token) {
        Long userId = getCurrentUserId(token);
        return ResponseEntity.ok(hikeService.getHikesByCreator(userId));
    }

    // GET /api/v1/hikes/{id} -> Détails d'une randonnée
    @GetMapping("/{id}")
    public ResponseEntity<Hike> getHikeById(@PathVariable Long id) {
        return hikeService.getHikeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/v1/hikes/{id} -> Mise à jour (Update)
    @PostMapping("/{id}")
    public ResponseEntity<Hike> updateHike(@PathVariable Long id, @RequestBody Hike hikeDetails) {
        return hikeService.updateHike(id, hikeDetails)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/v1/hikes/{id} -> Suppression
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHike(@PathVariable Long id) {
        hikeService.deleteHike(id);
        return ResponseEntity.noContent().build();
    }
}