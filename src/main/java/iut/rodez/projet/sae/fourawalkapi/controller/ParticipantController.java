package iut.rodez.projet.sae.fourawalkapi.controller;

import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.service.HikeService;
import iut.rodez.projet.sae.fourawalkapi.service.ParticipantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/participants")
public class ParticipantController {

    private final ParticipantService participantService;
    private final HikeService hikeService;

    public ParticipantController(ParticipantService participantService, HikeService hikeService) {
        this.participantService = participantService;
        this.hikeService = hikeService;
    }

    // GET /api/v1/participants/{id} -> Infos d'un participant
    @GetMapping("/{id}")
    public ResponseEntity<Participant> getParticipant(@PathVariable Long id) {
        return new ResponseEntity<>(participantService.getById(id), HttpStatus.OK);
    }

    // POST /api/v1/participants -> Étape 1 : Création (Nom/Prénom)
    @PostMapping
    public ResponseEntity<Participant> createParticipant(@RequestBody Participant p) {
        Participant created = participantService.createBasicParticipant();
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // POST /api/v1/participants/{id} -> Étape 2 : Complétion (Niveau, Morpho, etc.)
    @PostMapping("/{id}")
    public ResponseEntity<Participant> completeParticipant(@PathVariable Long id, @RequestBody Participant details) {
        return ResponseEntity.ok(participantService.updateParticipantDetails(id, details));
    }

    // DELETE /api/v1/participants/{id} -> Suppression
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParticipant(@PathVariable Long id) {
        participantService.deleteParticipant(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/v1/participants/hike/{idHike} -> Liste des participants d'une hike
    @GetMapping("/hike/{idHike}")
    public ResponseEntity<?> getParticipantsByHike(@PathVariable Long idHike) {
        return ResponseEntity.of(hikeService.getHikeById(idHike)
                .map(Hike::getParticipants));
    }
}
