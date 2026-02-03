package iut.rodez.projet.sae.fourawalkapi.controller;

import iut.rodez.projet.sae.fourawalkapi.dto.HikeResponseDto;
import iut.rodez.projet.sae.fourawalkapi.dto.ParticipantResponseDto;
import iut.rodez.projet.sae.fourawalkapi.dto.PointOfInterestResponseDto;
import iut.rodez.projet.sae.fourawalkapi.entity.*;
import iut.rodez.projet.sae.fourawalkapi.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/hikes")
public class HikeController {

    private final HikeService hikeService;
    private final ParticipantService participantService;
    private final PointOfInterestService poiService;
    private final FoodProductService foodService;
    private final EquipmentItemService equipmentService;

    public HikeController(HikeService hs, ParticipantService ps, PointOfInterestService pois,
                          FoodProductService fs, EquipmentItemService es) {
        this.hikeService = hs;
        this.participantService = ps;
        this.poiService = pois;
        this.foodService = fs;
        this.equipmentService = es;
    }

    private Long getUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non connecté");
        }
        return (Long) auth.getPrincipal();
    }

    // --- SCOPE HIKE ---

    @GetMapping("/my")
    public List<HikeResponseDto> getMyHikes(Authentication auth) {
        List<Hike> hikes = hikeService.getHikesByCreator(getUserId(auth));

        // Conversion de la liste d'Entités en liste de DTOs
        return hikes.stream()
                .map(HikeResponseDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HikeResponseDto> getHike(@PathVariable Long id, Authentication auth) {
        Hike hike = hikeService.getHikeById(id, getUserId(auth));
        return ResponseEntity.ok(new HikeResponseDto(hike));
    }

    @PostMapping
    public ResponseEntity<HikeResponseDto> createHike(@RequestBody Hike hike, Authentication auth) {
        Hike savedHike = hikeService.createHike(hike, getUserId(auth));
        return ResponseEntity.ok(new HikeResponseDto(savedHike));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HikeResponseDto> updateHike(@PathVariable Long id, @RequestBody Hike hike, Authentication auth) {
        Hike updatedHike = hikeService.updateHike(id, hike, getUserId(auth));
        return ResponseEntity.ok(new HikeResponseDto(updatedHike));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHike(@PathVariable Long id, Authentication auth) {
        hikeService.deleteHike(id, getUserId(auth));
        return ResponseEntity.noContent().build();
    }

    // --- SCOPE PARTICIPANT ---

    @PostMapping("/{hikeId}/participants")
    public ParticipantResponseDto addParticipant(@PathVariable Long hikeId, @RequestBody Participant p, Authentication auth) {
        Participant savedParticipant = participantService.addParticipant(hikeId, p, getUserId(auth));
        return new ParticipantResponseDto(savedParticipant);
    }

    @PutMapping("/{hikeId}/participants/{pId}")
    public ParticipantResponseDto updateParticipant(@PathVariable Long hikeId, @PathVariable Long pId, @RequestBody Participant p, Authentication auth) {
        Participant updatedParticipant = participantService.updateParticipant(hikeId, pId, p, getUserId(auth));
        return new ParticipantResponseDto(updatedParticipant);
    }

    @DeleteMapping("/{hikeId}/participants/{pId}")
    public ResponseEntity<Void> deleteParticipant(@PathVariable Long hikeId, @PathVariable Long pId, Authentication auth) {
        participantService.deleteParticipant(hikeId, pId, getUserId(auth));
        return ResponseEntity.noContent().build();
    }

    // --- SCOPE POI ---

    @PostMapping("/{hikeId}/poi")
    public PointOfInterestResponseDto addPoi(@PathVariable Long hikeId, @RequestBody PointOfInterest poi, Authentication auth) {
        PointOfInterest savedPoi = poiService.addPoiToHike(hikeId, poi, getUserId(auth));
        return new PointOfInterestResponseDto(savedPoi);
    }

    @DeleteMapping("/{hikeId}/poi/{poiId}")
    public ResponseEntity<Void> deletePoi(@PathVariable Long hikeId, @PathVariable Long poiId, Authentication auth) {
        poiService.removePoiFromHike(hikeId, poiId, getUserId(auth));
        return ResponseEntity.noContent().build();
    }

    // --- SCOPE FOOD (Liaison) ---

    @PostMapping("/{hikeId}/food")
    public ResponseEntity<Void> addFoodToHike(@PathVariable Long hikeId, @RequestBody FoodProduct food, Authentication auth) {
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

    // --- SCOPE OPTIMISATION ---

    @PostMapping("/{hikeId}/optimize")
    public ResponseEntity<HikeResponseDto> optimizeBackpacks(@PathVariable Long hikeId, Authentication auth) {
        hikeService.optimizeBackpack(hikeId, getUserId(auth));

        // On récupère la rando fraîchement optimisée et on la renvoie en DTO
        Hike optimizedHike = hikeService.getHikeById(hikeId, getUserId(auth));
        return ResponseEntity.ok(new HikeResponseDto(optimizedHike));
    }
}