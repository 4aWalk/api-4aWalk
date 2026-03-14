package iut.rodez.projet.sae.fourawalkapi.controller;

import iut.rodez.projet.sae.fourawalkapi.dto.HikeResponseDto;
import iut.rodez.projet.sae.fourawalkapi.dto.HikeSummaryDto;
import iut.rodez.projet.sae.fourawalkapi.dto.ParticipantResponseDto;
import iut.rodez.projet.sae.fourawalkapi.dto.PointOfInterestResponseDto;
import iut.rodez.projet.sae.fourawalkapi.entity.*;
import iut.rodez.projet.sae.fourawalkapi.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public List<HikeSummaryDto> getMyHikes(Authentication auth) {
        // 1. On récupère les randos (version allégée grâce à ton HikeRepository)
        List<Hike> hikes = hikeService.getHikesByCreator(getUserId(auth));

        // 2. On les transforme instantanément en résumé léger pour le client Android
        return hikes.stream()
                .map(HikeSummaryDto::new)
                .toList();
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
        Map<Long, Participant> owners = equipmentService.getEquipmentOwners(id);
        return ResponseEntity.ok(new HikeResponseDto(hike, owners));
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
        // À la création, aucun équipement n'est encore assigné, on passe une map vide
        return ResponseEntity.ok(new HikeResponseDto(savedHike, Map.of()));
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
        Map<Long, Participant> owners = equipmentService.getEquipmentOwners(id);
        return ResponseEntity.ok(new HikeResponseDto(updatedHike, owners));
    }

    /**
     * Suppression de la randonnée
     * @param id identifiant de la randonnée à supprimer
     * @param auth token d'ientification
     * @return Code retour de la supression
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteHike(@PathVariable Long id, Authentication auth) {
        hikeService.deleteHike(id, getUserId(auth));
        return ResponseEntity.ok("Randonnée supprimée avec succès.");
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
        Map<Long, Participant> owners = equipmentService.getEquipmentOwners(hikeId);
        return new ParticipantResponseDto(savedParticipant, owners);
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
        Map<Long, Participant> owners = equipmentService.getEquipmentOwners(hikeId);
        return new ParticipantResponseDto(updatedParticipant, owners);
    }

    /**
     * Suppresison d'un particpant
     * @param hikeId identifiant de la randonnée où le participant doit être retiré
     * @param pId identifiant du participant à retirer de la randonnée
     * @param auth token d'authentification
     * @return Code retour du retirement du particpant de la randonnée
     */
    @DeleteMapping("/{hikeId}/participants/{pId}")
    public ResponseEntity<String> deleteParticipant(@PathVariable Long hikeId, @PathVariable Long pId,
                                                    Authentication auth) {
        participantService.deleteParticipant(hikeId, pId, getUserId(auth));
        return ResponseEntity.ok("Participant retiré de la randonnée avec succès.");
    }

    // --- SCOPE POI ---

    /**
     * Met à jour la liste complète des points d'intérêt d'une randonnée.
     * Cet endpoint remplace tous les POI existants par la nouvelle liste fournie,
     * en respectant l'ordre d'insertion pour la séquence.
     *
     * @param hikeId  Identifiant de la randonnée à modifier.
     * @param pois    Nouvelle liste des points d'intérêt.
     * @param auth    Token d'authentification.
     * @return La liste des nouveaux points d'intérêt mappée en DTO.
     */
    @PutMapping("/{hikeId}/pois")
    public List<PointOfInterestResponseDto> updatePois(@PathVariable Long hikeId,
                                                       @RequestBody List<PointOfInterest> pois,
                                                       Authentication auth) {
        List<PointOfInterest> updatedPois = poiService.updateAllPois(hikeId, pois, getUserId(auth));

        return updatedPois.stream()
                .map(PointOfInterestResponseDto::new)
                .toList();
    }

    // --- SCOPE FOOD (Liaison) ---

    /**
     * ajout de la nourriture à une randonnée
     * @param hikeId identifiant de la randonnée dans laquelle la nourriture est ajoutée
     * @param foodId identifiant de la nourriture à ajoutée à la randonnée
     * @param auth token d'identification
     * @return Code retour de l'ajout de la nourriture à la randonné
     */
    @PostMapping("/{hikeId}/food/{foodId}")
    public ResponseEntity<Void> addFoodToHike(@PathVariable Long hikeId, @PathVariable Long foodId,
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
    public ResponseEntity<String> removeFoodFromHike(@PathVariable Long hikeId, @PathVariable Long foodId,
                                                     Authentication auth) {
        foodService.removeFoodFromHike(hikeId, foodId, getUserId(auth));
        return ResponseEntity.ok("Nourriture retirée de la randonnée avec succès.");
    }

    // --- SCOPE EQUIPMENT ---

    /**
     * Ajout d'un équipement à une randonnée avec affectation optionnelle d'un propriétaire.
     * @param hikeId identifiant de la randonnée
     * @param equipmentId identifiant de l'équipement
     * @param ownerId identifiant du participant (propriétaire) passé en paramètre d'URL (optionnel)
     * @param auth token d'identification
     */
    @PostMapping("/{hikeId}/equipment/{equipmentId}")
    public ResponseEntity<Void> addEquipmentToHike(
            @PathVariable Long hikeId,
            @PathVariable Long equipmentId,
            @RequestParam(name = "owner", required = false) Long ownerId,
            Authentication auth) {

        equipmentService.addEquipmentToHike(hikeId, equipmentId, getUserId(auth), ownerId);

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
    public ResponseEntity<String> removeEquipmentFromHike(@PathVariable Long hikeId, @PathVariable Long equipId, Authentication auth) {
        equipmentService.removeEquipmentFromHike(hikeId, equipId, getUserId(auth));
        return ResponseEntity.ok("Équipement retiré de la randonnée avec succès.");
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
        Map<Long, Participant> owners = equipmentService.getEquipmentOwners(hikeId);
        return ResponseEntity.ok(new HikeResponseDto(optimizedHike, owners));
    }
}