package iut.rodez.projet.sae.fourawalkapi.service;


import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.HikeRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.ParticipantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HikeService {

    private final HikeRepository hikeRepository;

    private final ParticipantRepository participantRepository;

    public HikeService(HikeRepository hikeRepository, ParticipantRepository participantRepository) {
        this.hikeRepository = hikeRepository;
        this.participantRepository = participantRepository;
    }

    // --- 1. Consultation des randonnées créées par l'utilisateur ---
    public List<Hike> getHikesByCreator(Long creatorId) {
        return hikeRepository.findByCreatorId(creatorId);
    }

    // --- 2. Consultation des détails d'une randonnée ---
    public Optional<Hike> getHikeById(Long hikeId) {
        return null;
    }

    // --- 3. Création d'une randonnée ---
    public Hike createHike(Hike newHike) {
        // TODOValidation des données si nécessaire (distance > 0, date valide, etc.)
        return hikeRepository.save(newHike);
    }

    // --- 4. Mise à jour d'une randonnée ---
    public Optional<Hike> updateHike(Long hikeId, Hike updatedDetails) {

        Optional<Hike> existingHike = hikeRepository.findById(hikeId);

        if (existingHike.isPresent()) {
            Hike hikeToUpdate = existingHike.get();

            // Mise à jour des champs
            hikeToUpdate.setLibelle(updatedDetails.getLibelle());
            hikeToUpdate.setDepart(updatedDetails.getDepart());
            hikeToUpdate.setArrivee(updatedDetails.getArrivee());
            hikeToUpdate.setDureeJours(updatedDetails.getDureeJours());

            // Vérification sécuritaire du createur.
            if (hikeToUpdate.getCreator().equals(updatedDetails.getCreator())) { return Optional.empty(); }

            return Optional.of(hikeRepository.save(hikeToUpdate));
        } else {
            return Optional.empty(); // Randonnée non trouvée
        }
    }
    // --- Ajouter un participant à une randonnée ---
    public Hike addParticipantToHike(Long hikeId, Long participantId) {
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException("Randonnée non trouvée"));
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant non trouvé"));

        hike.getParticipants().add(participant);
        return hikeRepository.save(hike);
    }

    // --- Retirer un participant d'une randonnée ---
    public Hike removeParticipantFromHike(Long hikeId, Long participantId) {
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException("Randonnée non trouvée"));

        hike.getParticipants().removeIf(p -> p.getId().equals(participantId));
        return hikeRepository.save(hike);
    }

    public void deleteHike(Long id) {

    }

    // [TODO : Logique de sécurité pour s'assurer que l'utilisateur modifie SES randonnées]
}