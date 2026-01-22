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

    private final ParticipantService participantService;
    private final FoodService foodService;
    private final EquipmentService equipmentService;
    private final HikeRepository hikeRepository;
    private final ParticipantRepository participantRepository;

    public HikeService(ParticipantService ps, FoodService fs, EquipmentService es, HikeRepository hr, ParticipantRepository pr) {
        this.participantService = ps;
        this.foodService = fs;
        this.equipmentService = es;
        this.hikeRepository = hr;
        this.participantRepository = pr;
    }

    // --- 1. Consultation des randonnées créées par l'utilisateur ---
    public List<Hike> getHikesByCreator(Long creatorId) {
        return hikeRepository.findByCreatorId(creatorId);
    }

    // --- 2. Consultation des détails d'une randonnée ---
    public Optional<Hike> getHikeById(Long hikeId) {
        return hikeRepository.findById(hikeId);
    }

    // --- 3. Création d'une randonnée ---
    public Hike createHike(Hike newHike) {
        validateHike(newHike);
        return hikeRepository.save(newHike);
    }

    // --- 4. Mise à jour d'une randonnée ---
    public Optional<Hike> updateHike(Long hikeId, Hike updatedDetails) {
        validateHike(updatedDetails);
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
        if (!hikeRepository.existsById(id)) {
            throw new RuntimeException("Impossible de supprimer : Randonnée introuvable avec l'ID " + id);
        }
        hikeRepository.deleteById(id);
    }

    public void validateHike(Hike hike) {
        // 1. Validation Durée
        if (hike.getDureeJours() < 1 || hike.getDureeJours() > 3) {
            throw new IllegalArgumentException("La durée doit être comprise entre 1 et 3 jours.");
        }

        // 2. Validation Départ != Arrivée
        if (hike.getDepart() == null || hike.getArrivee() == null) {
            throw new IllegalArgumentException("Le départ et l'arrivée doivent être renseignés.");
        }
        if (hike.getDepart().equals(hike.getArrivee())) {
            throw new IllegalArgumentException("Le point de départ doit être différent du point d'arrivée.");
        }

        // 3. Validation Participants (Taille 1 à 3 + Validation individuelle)
        if (hike.getParticipants() == null || hike.getParticipants().isEmpty()) {
            throw new IllegalArgumentException("Il faut au moins 1 participant.");
        }
        if (hike.getParticipants().size() > 3) {
            throw new IllegalArgumentException("Maximum 3 participants autorisés.");
        }
        hike.getParticipants().forEach(participantService::validateParticipant);

        // 4. Validation Catalogue Food & Equipment
        if (hike.getFoodCatalogue() != null) {
            hike.getFoodCatalogue().forEach(foodService::validateFoodProduct);
        }
        if (hike.getEquipmentRequired() != null) {
            hike.getEquipmentRequired().forEach(equipmentService::validateEquipment);
        }
    }
}