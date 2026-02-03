package iut.rodez.projet.sae.fourawalkapi.controller;

import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.service.ParticipantService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/participants")
public class ParticipantController {

    private final ParticipantService participantService;

    public ParticipantController(ParticipantService participantService) {
        this.participantService = participantService;
    }

    /**
     * GET /participants/my
     * Récupère la liste de tous les participants créés par l'utilisateur connecté.
     * Utile pour proposer une liste de "Favoris" ou "Déjà utilisés" dans le frontend.
     */
    @GetMapping("/my")
    public ResponseEntity<List<Participant>> getMyParticipants(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        // L'ID est stocké en tant que Principal (Long) grâce à ton JwtAuthenticationFilter
        Long userId = (Long) authentication.getPrincipal();

        List<Participant> participants = participantService.getMyParticipants(userId);

        return ResponseEntity.ok(participants);
    }
}