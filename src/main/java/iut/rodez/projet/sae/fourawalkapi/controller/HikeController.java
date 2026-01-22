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

    public HikeController(HikeService hikeService, FoodService foodService,
                          EquipmentService equipmentService, ParticipantService participantService,
                          JwtTokenProvider tokenProvider, UserService userService) {
        this.hikeService = hikeService;
        this.foodService = foodService;
        this.equipmentService = equipmentService;
        this.participantService = participantService;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    // --- HELPER ---
    private Long getCurrentUserId(String token) {
        String jwt = token.substring(7);
        String email = tokenProvider.getUsername(jwt);
        return userService.findByMail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"))
                .getId();
    }

    // --- HIKES ENDPOINTS ---

    @GetMapping("/my")
    public ResponseEntity<List<HikeResponseDto>> getMyHikes(@RequestHeader("Authorization") String token) {
        Long userId = getCurrentUserId(token);
        List<HikeResponseDto> dtos = hikeService.getHikesByCreator(userId).stream()
                .map(HikeResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HikeResponseDto> getHikeById(@PathVariable Long id) {
        return hikeService.getHikeById(id)
                .map(hike -> ResponseEntity.ok(new HikeResponseDto(hike)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHike(@PathVariable Long id) {
        hikeService.deleteHike(id);
        return ResponseEntity.noContent().build();
    }

    // --- CATALOGUE ENDPOINTS (Services manquants) ---

    /** GET /api/v1/hikes/catalog/food -> Liste tout le catalogue alimentaire */
    @GetMapping("/catalog/food")
    public ResponseEntity<List<FoodProductResponseDto>> getAllFood() {
        return ResponseEntity.ok(foodService.findAll().stream()
                .map(FoodProductResponseDto::new)
                .collect(Collectors.toList()));
    }

    /** GET /api/v1/hikes/catalog/equipment -> Liste tout l'équipement disponible */
    @GetMapping("/catalog/equipment")
    public ResponseEntity<List<EquipmentResponseDto>> getAllEquipment() {
        return ResponseEntity.ok(equipmentService.findAll().stream()
                .map(EquipmentResponseDto::new)
                .collect(Collectors.toList()));
    }

    /** DELETE /api/v1/hikes/participants/{id} -> Supprimer un participant spécifique */
    @DeleteMapping("/participants/{id}")
    public ResponseEntity<Void> deleteParticipant(@PathVariable Long id) {
        participantService.deleteParticipant(id);
        return ResponseEntity.noContent().build();
    }
}