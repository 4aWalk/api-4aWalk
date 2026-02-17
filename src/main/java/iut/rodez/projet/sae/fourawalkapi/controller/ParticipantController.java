package iut.rodez.projet.sae.fourawalkapi.controller;

import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.service.ParticipantService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static iut.rodez.projet.sae.fourawalkapi.security.SecurityUtils.getUserId;

/**
 * Controlleur les endpoints de gestions des participants
 */
@RestController
@RequestMapping("/participants")
public class ParticipantController {

    private final ParticipantService participantService;

    /**
     * Injection de dépendance
     * @param participantService service participant
     */
    public ParticipantController(ParticipantService participantService) {
        this.participantService = participantService;
    }

    /**
     * Récupération de l'ensemble des particiants créer par l'utilisateur identifier à partir du token
     * @param auth token d'identification
     * @return Listes de tous les participants créer par l'utilisateur
     */
    @GetMapping("/my")
    public ResponseEntity<List<Participant>> getMyParticipants(Authentication auth) {
        // Récupération de l'identifiant
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        Long userId = getUserId(auth);

        List<Participant> participants = participantService.getMyParticipants(userId);
        return ResponseEntity.ok(participants);
    }
}