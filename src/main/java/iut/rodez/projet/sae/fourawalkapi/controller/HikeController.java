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

import static iut.rodez.projet.sae.fourawalkapi.security.SecurityUtils.getUserId;

/**
 * Controleur de tous les endpoints permettant de gérer toutes les randonnées
 */
@RestController
@RequestMapping("/hikes")
public class HikeController {

    private final HikeService hikeService;
    private final ParticipantService participantService;
    private final PointOfInterestService poiService;
    private final FoodService foodService;
    private final EquipmentService equipmentService;

    /**
     * Injection des dépendances
     * @param hs randonnées service
     * @param ps participants service
     * @param pois point d'interet service
     * @param fs nourriture service
     * @param es equipement service
     */
    public HikeController(HikeService hs, ParticipantService ps, PointOfInterestService pois,
                          FoodService fs, EquipmentService es) {
        this.hikeService = hs;
        this.participantService = ps;
        this.poiService = pois;
        this.foodService = fs;
        this.equipmentService = es;
    }

    // --- SCOPE HIKE ---

    /**
     * Liste de toutes les randonnées créer par l'utilisateur
     * @param auth token d'identification
     * @return Liste de toutes les randonnées où l'utilisateur est le créateur
     */
    @GetMapping("/my")
    public List<HikeResponseDto> getMyHikes(Authentication auth) {
        List<Hike> hikes = hikeService.getHikesByCreator(getUserId(auth));

        return hikes.stream()
                .map(HikeResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * Renvoi les détails de la randonnée passer en paramètre
     * @param id identifiant de la randonnées
     * @param auth token d'authentication
     * @return la randonnée trouvée
     */
    @GetMapping("/{id}")
    public ResponseEntity<HikeResponseDto> getHike(@PathVariable Long id, Authentication auth) {
        Hike hike = hikeService.getHikeById(id, getUserId(auth));
        return ResponseEntity.ok(new HikeResponseDto(hike));
    }

    /**
     * Création d'une nouvelle randonnée
     * @param hike la randonnée à créer
     * @param auth le token d'identification
     * @return la randonnée créer
     */
    @PostMapping
    public ResponseEntity<HikeResponseDto> createHike(@RequestBody Hike hike, Authentication auth) {
        Hike savedHike = hikeService.createHike(hike, getUserId(auth));
        return ResponseEntity.ok(new HikeResponseDto(savedHike));
    }

    /**
     * Mise à jour des informations d'une randonnée
     * @param id identifiant de la randonnée à mettre à jour
     * @param hike la randonnée mise à jour
     * @param auth token d'identification
     * @return la randonnée mise à jour
     */
    @PutMapping("/{id}")
    public ResponseEntity<HikeResponseDto> updateHike(@PathVariable Long id, @RequestBody Hike hike,
                                                      Authentication auth) {
        Hike updatedHike = hikeService.updateHike(id, hike, getUserId(auth));
        return ResponseEntity.ok(new HikeResponseDto(updatedHike));
    }

    /**
     * Suppression de la randonnée
     * @param id identifiant de la randonnée à supprimer
     * @param auth token d'ientification
     * @return Code retour de la supression
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHike(@PathVariable Long id, Authentication auth) {
        hikeService.deleteHike(id, getUserId(auth));
        return ResponseEntity.noContent().build();
    }

    // --- SCOPE PARTICIPANT ---

    /**
     * Ajout d'un participant à la randonnée
     * @param hikeId identifiant de la randonnée dans laquelle le participant est ajouté
     * @param p le participant à ajouter à la randonnée
     * @param auth token d'identification
     * @return Le participant créer
     */
    @PostMapping("/{hikeId}/participants")
    public ParticipantResponseDto addParticipant(@PathVariable Long hikeId, @RequestBody Participant p,
                                                 Authentication auth) {
        Participant savedParticipant = participantService.addParticipant(hikeId, p, getUserId(auth));
        return new ParticipantResponseDto(savedParticipant);
    }

    /**
     * Mise à jour des information d'un participants
     * @param hikeId identifiant d'une randonnée
     * @param pId identifiant du participant à mettre à jour
     * @param p participant mis à jour
     * @param auth token d'authentification
     * @return le participant mis à jour
     */
    @PutMapping("/{hikeId}/participants/{pId}")
    public ParticipantResponseDto updateParticipant(@PathVariable Long hikeId, @PathVariable Long pId,
                                                    @RequestBody Participant p, Authentication auth) {
        Participant updatedParticipant = participantService.updateParticipant(hikeId, pId, p, getUserId(auth));
        return new ParticipantResponseDto(updatedParticipant);
    }

    /**
     * Suppresison d'un particpant
     * @param hikeId identifiant de la randonnée où le participant doit être retiré
     * @param pId identifiant du participant à retirer de la randonnée
     * @param auth token d'authentification
     * @return Code retour du retirement du particpant de la randonnée
     */
    @DeleteMapping("/{hikeId}/participants/{pId}")
    public ResponseEntity<Void> deleteParticipant(@PathVariable Long hikeId, @PathVariable Long pId,
                                                  Authentication auth) {
        participantService.deleteParticipant(hikeId, pId, getUserId(auth));
        return ResponseEntity.noContent().build();
    }

    // --- SCOPE POI ---

    /**
     * Ajout d'un poi dans la randonnée
     * @param hikeId identifiant de la randonnée dans laquelle le poi est ajouté
     * @param poi Point d interet à ajouter
     * @param auth token d'identification
     * @return le nouveau poi créer et ajouté
     */
    @PostMapping("/{hikeId}/poi")
    public PointOfInterestResponseDto addPoi(@PathVariable Long hikeId, @RequestBody PointOfInterest poi,
                                             Authentication auth) {
        PointOfInterest savedPoi = poiService.addPoiToHike(hikeId, poi, getUserId(auth));
        return new PointOfInterestResponseDto(savedPoi);
    }

    /**
     * Suppression du point d interet
     * @param hikeId identifiant de la randonnée dans laquelle le poi est supprimer
     * @param poiId identifiant du poi à supprimer
     * @param auth token d'identification
     * @return Code retour de la suppression du poi
     */
    @DeleteMapping("/{hikeId}/poi/{poiId}")
    public ResponseEntity<Void> deletePoi(@PathVariable Long hikeId, @PathVariable Long poiId, Authentication auth) {
        poiService.removePoiFromHike(hikeId, poiId, getUserId(auth));
        return ResponseEntity.noContent().build();
    }

    // --- SCOPE FOOD (Liaison) ---

    /**
     * ajout de la nourriture à une randonnée
     * @param hikeId identifiant de la randonnée dans laquelle la nourriture est ajoutée
     * @param foodId identifiant de la nourriture à ajoutée à la randonnée
     * @param auth token d'identification
     * @return Code retour de l'ajout de la nourriture à la randonné
     */
    @PostMapping("/{hikeId}/food")
    public ResponseEntity<Void> addFoodToHike(@PathVariable Long hikeId, @RequestBody Long foodId,
                                              Authentication auth) {
        foodService.addFoodToHike(hikeId, foodId, getUserId(auth));
        return ResponseEntity.ok().build();
    }

    /**
     * Retire une nourriture d'un randonnée
     * @param hikeId identifiant de la randonnée dans laquelle la nourriture doit être retiré
     * @param foodId identifiant de la nourriture à retirer
     * @param auth token d'identification
     * @return Code retour du retirement de la nourriture de la randonnée
     */
    @DeleteMapping("/{hikeId}/food/{foodId}")
    public ResponseEntity<Void> removeFoodFromHike(@PathVariable Long hikeId, @PathVariable Long foodId,
                                                   Authentication auth) {
        foodService.removeFoodFromHike(hikeId, foodId, getUserId(auth));
        return ResponseEntity.noContent().build();
    }

    // --- SCOPE EQUIPMENT ---

    /**
     * Ajout d'un équipement à une randonnée
     * @param hikeId identifiant de la randonnée dans laquelle l'équipement est ajouté
     * @param equipmentId  identifiant de l'équipement à ajouter à la randonnée
     * @param auth token d'identification
     * @return Code retour de l'ajout de l'équipement
     */
    @PostMapping("/{hikeId}/equipment")
    public ResponseEntity<Void> addEquipmentToHike(@PathVariable Long hikeId, @RequestBody Long equipmentId, Authentication auth) {
        equipmentService.addEquipmentToHike(hikeId, equipmentId, getUserId(auth));
        return ResponseEntity.ok().build();
    }

    /**
     * Retire un équipement d'une randonnée
     * @param hikeId identifiant de la randonnée dans laquelle l'équipement est ajouté
     * @param equipId identifiant de l'équipement
     * @param auth token d'identification
     * @return Code retour de l'enlèvement de l'équipement de la randonnée
     */
    @DeleteMapping("/{hikeId}/equipment/{equipId}")
    public ResponseEntity<Void> removeEquipmentFromHike(@PathVariable Long hikeId, @PathVariable Long equipId, Authentication auth) {
        equipmentService.removeEquipmentFromHike(hikeId, equipId, getUserId(auth));
        return ResponseEntity.noContent().build();
    }

    // --- SCOPE OPTIMISATION ---

    /**
     * Optimisation du sac à dos de tous les participants
     * @param hikeId identifiant de la randonnée
     * @param auth token di'identification
     * @return Les sacs à dos des particpants optimisé, ou non si impossible
     */
    @PostMapping("/{hikeId}/optimize")
    public ResponseEntity<HikeResponseDto> optimizeBackpacks(@PathVariable Long hikeId, Authentication auth) {
        hikeService.optimizeBackpack(hikeId, getUserId(auth));
        Hike optimizedHike = hikeService.getHikeById(hikeId, getUserId(auth));
        return ResponseEntity.ok(new HikeResponseDto(optimizedHike));
    }
}