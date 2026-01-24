package iut.rodez.projet.sae.fourawalkapi.controller;

import iut.rodez.projet.sae.fourawalkapi.entity.*;
import iut.rodez.projet.sae.fourawalkapi.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hikes")
public class HikeController {

    private final HikeService hikeService;
    private final ParticipantService participantService;
    private final PointOfInterestService poiService;
    private final FoodProductService foodService;
    private final EquipmentItemService equipmentService;

    // PLUS BESOIN de UserService ici !
    public HikeController(HikeService hs, ParticipantService ps, PointOfInterestService pois,
                          FoodProductService fs, EquipmentItemService es) {
        this.hikeService = hs;
        this.participantService = ps;
        this.poiService = pois;
        this.foodService = fs;
        this.equipmentService = es;
    }

    /**
     * Méthode utilitaire pour récupérer l'ID directement depuis le Token décodé
     */
    private Long getUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non connecté");
        }
        // Grâce à ton JwtAuthenticationFilter, le principal EST un Long
        return (Long) auth.getPrincipal();
    }

    // --- SCOPE HIKE ---

    @GetMapping("/my")
    public List<Hike> getMyHikes(Authentication auth) {
        // Renvoie uniquement les randos où creator_id = userId du token
        return hikeService.getHikesByCreator(getUserId(auth));
    }

    @GetMapping("/{id}")
    public Hike getHike(@PathVariable Long id, Authentication auth) {
        return hikeService.getHikeById(id, getUserId(auth));
    }

    @PostMapping
    public Hike createHike(@RequestBody Hike hike, Authentication auth) {
        // Le service va chercher le User en base à partir de cet ID pour faire le lien
        return hikeService.createHike(hike, getUserId(auth));
    }

    @PutMapping("/{id}")
    public Hike updateHike(@PathVariable Long id, @RequestBody Hike hike, Authentication auth) {
        return hikeService.updateHike(id, hike, getUserId(auth));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHike(@PathVariable Long id, Authentication auth) {
        hikeService.deleteHike(id, getUserId(auth));
        return ResponseEntity.noContent().build();
    }

    // --- SCOPE PARTICIPANT ---

    @PostMapping("/{hikeId}/participants")
    public Participant addParticipant(@PathVariable Long hikeId, @RequestBody Participant p, Authentication auth) {
        return participantService.addParticipant(hikeId, p, getUserId(auth));
    }

    @PutMapping("/{hikeId}/participants/{pId}")
    public Participant updateParticipant(@PathVariable Long hikeId, @PathVariable Long pId, @RequestBody Participant p, Authentication auth) {
        return participantService.updateParticipant(hikeId, pId, p, getUserId(auth));
    }

    @DeleteMapping("/{hikeId}/participants/{pId}")
    public ResponseEntity<Void> deleteParticipant(@PathVariable Long hikeId, @PathVariable Long pId, Authentication auth) {
        participantService.deleteParticipant(hikeId, pId, getUserId(auth));
        return ResponseEntity.noContent().build();
    }

    // --- SCOPE POI ---

    @PostMapping("/{hikeId}/poi")
    public PointOfInterest addPoi(@PathVariable Long hikeId, @RequestBody PointOfInterest poi, Authentication auth) {
        return poiService.addPoiToHike(hikeId, poi, getUserId(auth));
    }

    @DeleteMapping("/{hikeId}/poi/{poiId}")
    public ResponseEntity<Void> deletePoi(@PathVariable Long hikeId, @PathVariable Long poiId, Authentication auth) {
        poiService.removePoiFromHike(hikeId, poiId, getUserId(auth));
        return ResponseEntity.noContent().build();
    }

    // --- SCOPE FOOD (Liaison) ---

    @PostMapping("/{hikeId}/food")
    public ResponseEntity<Void> addFoodToHike(@PathVariable Long hikeId, @RequestBody FoodProduct food, Authentication auth) {
        // On attend { "id": X } dans le body
        foodService.addFoodToHike(hikeId, food.getId(), getUserId(auth));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{hikeId}/food/{foodId}")
    public ResponseEntity<Void> removeFoodFromHike(@PathVariable Long hikeId, @PathVariable Long foodId, Authentication auth) {
        foodService.removeFoodFromHike(hikeId, foodId, getUserId(auth));
        return ResponseEntity.noContent().build();
    }

    // --- SCOPE EQUIPMENT (Liaison) ---

    @PostMapping("/{hikeId}/equipment")
    public ResponseEntity<Void> addEquipmentToHike(@PathVariable Long hikeId, @RequestBody EquipmentItem item, Authentication auth) {
        equipmentService.addEquipmentToHike(hikeId, item.getId(), getUserId(auth));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{hikeId}/equipment/{equipId}")
    public ResponseEntity<Void> removeEquipmentFromHike(@PathVariable Long hikeId, @PathVariable Long equipId, Authentication auth) {
        equipmentService.removeEquipmentFromHike(hikeId, equipId, getUserId(auth));
        return ResponseEntity.noContent().build();
    }
}