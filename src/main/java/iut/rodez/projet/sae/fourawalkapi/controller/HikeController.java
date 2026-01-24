package iut.rodez.projet.sae.fourawalkapi.controller;

import iut.rodez.projet.sae.fourawalkapi.controller.dto.*;
import iut.rodez.projet.sae.fourawalkapi.entity.*;
import iut.rodez.projet.sae.fourawalkapi.security.JwtTokenProvider;
import iut.rodez.projet.sae.fourawalkapi.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/hikes")
public class HikeController {

    private final HikeService hikeService;
    private final FoodService foodService;
    private final EquipmentService equipmentService;
    private final ParticipantService participantService;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    public HikeController(HikeService hs, FoodService fs, EquipmentService es,
                          ParticipantService ps, JwtTokenProvider tp, UserService us) {
        this.hikeService = hs;
        this.foodService = fs;
        this.equipmentService = es;
        this.participantService = ps;
        this.tokenProvider = tp;
        this.userService = us;
    }

    @GetMapping("/my")
    public ResponseEntity<List<HikeResponseDto>> getMyHikes(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);

        Long userId = tokenProvider.getUserId(jwt);

        return ResponseEntity.ok(hikeService.getHikesByCreator(userId).stream()
                .map(HikeResponseDto::new)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HikeResponseDto> getHike(@PathVariable Long id) {
        return hikeService.getHikeById(id).map(h -> ResponseEntity.ok(new HikeResponseDto(h)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<HikeResponseDto> create(@RequestHeader("Authorization") String token,
                                                  @RequestBody Hike hike) {
        // On récupère l'ID directement via le tokenProvider
        Long userId = getUserIdFromToken(token);

        // On crée la rando en passant l'ID du créateur au service
        Hike createdHike = hikeService.createHike(hike, userId);

        return ResponseEntity.ok(new HikeResponseDto(createdHike));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HikeResponseDto> updateHike(@RequestHeader("Authorization") String token,
                                                      @PathVariable Long id,
                                                      @RequestBody Hike hike) {
        Long userId = getUserIdFromToken(token);
        return ResponseEntity.ok(new HikeResponseDto(hikeService.updateHike(id, hike, userId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHike(@RequestHeader("Authorization") String token,
                                           @PathVariable Long id) {
        Long userId = getUserIdFromToken(token);
        hikeService.deleteHike(id, userId);
        return ResponseEntity.noContent().build();
    }

    // --- Participants ---
    @PostMapping("/{hikeId}/participants")
    public ResponseEntity<HikeResponseDto> addParticipant(@PathVariable Long hikeId, @RequestBody Participant p) {
        return ResponseEntity.ok(new HikeResponseDto(hikeService.addParticipantToHike(hikeId, p)));
    }

    @PutMapping("/{hikeId}/participants/{pId}")
    public ResponseEntity<Participant> updateParticipant(
            @RequestHeader("Authorization") String token,
            @PathVariable Long hikeId,
            @PathVariable Long pId,
            @RequestBody Participant p) {

        Long userId = getUserIdFromToken(token);

        // On passe par le HikeService pour vérifier que l'utilisateur possède bien la rando
        Participant updated = hikeService.updateParticipantInHike(hikeId, pId, p, userId);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{hikeId}/participants/{pId}")
    public ResponseEntity<Void> removeParticipant(@PathVariable Long hikeId, @PathVariable Long pId) {
        hikeService.removeParticipantFromHike(hikeId, pId);
        return ResponseEntity.noContent().build();
    }

    // --- Food & Equipment ---
    @PostMapping("/{id}/food/{fId}")
    public ResponseEntity<HikeResponseDto> addFood(@PathVariable Long id, @PathVariable Long fId) {
        return ResponseEntity.ok(new HikeResponseDto(hikeService.addFoodToHike(id, fId)));
    }

    @DeleteMapping("/{id}/food/{fId}")
    public ResponseEntity<HikeResponseDto> removeFood(@PathVariable Long id, @PathVariable Long fId) {
        return ResponseEntity.ok(new HikeResponseDto(hikeService.removeFoodFromHike(id, fId)));
    }

    @PostMapping("/{id}/equipment/{eId}")
    public ResponseEntity<HikeResponseDto> addEquip(@PathVariable Long id, @PathVariable Long eId) {
        return ResponseEntity.ok(new HikeResponseDto(hikeService.addEquipmentToHike(id, eId)));
    }

    @DeleteMapping("/{id}/equipment/{eId}")
    public ResponseEntity<HikeResponseDto> removeEquip(@PathVariable Long id, @PathVariable Long eId) {
        return ResponseEntity.ok(new HikeResponseDto(hikeService.removeEquipmentFromHike(id, eId)));
    }

    // --- Catalogues ---
    @GetMapping("/catalog/food")
    public ResponseEntity<List<FoodProduct>> getFoodCatalog() {
        return ResponseEntity.ok(foodService.findAll());
    }

    @GetMapping("/catalog/equipment")
    public ResponseEntity<List<EquipmentItem>> getEquipCatalog() {
        return ResponseEntity.ok(equipmentService.findAll());
    }

    private Long getUserIdFromToken(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            String jwt = header.substring(7); // On enlève "Bearer "
            return tokenProvider.getUserId(jwt); // On appelle le provider
        }
        throw new RuntimeException("Header Authorization invalide");
    }
}