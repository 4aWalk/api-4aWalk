package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.*;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class HikeService {

    private final HikeRepository hikeRepository;
    private final UserService userService;
    private final PointOfInterestRepository poiRepository;
    private final ParticipantRepository participantRepository;

    public HikeService(HikeRepository hr, UserService us, PointOfInterestRepository poiRepo, ParticipantRepository pr) {
        this.hikeRepository = hr;
        this.userService = us;
        this.poiRepository = poiRepo;
        this.participantRepository = pr;
    }

    public List<Hike> getHikesByCreator(Long creatorId) {
        return hikeRepository.findByCreatorId(creatorId);
    }

    public Hike getHikeById(Long hikeId, Long userId) {
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException("Randonnée introuvable"));
        if (!hike.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Accès refusé");
        }
        return hike;
    }

    @Transactional
    public Hike createHike(Hike hike, Long creatorId) {
        User user = userService.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        hike.setCreator(user);

        // Création du Participant "Chef" depuis le User
        Participant pCreator = new Participant(
                user.getAge(),
                user.getNiveau(),
                user.getMorphologie(),
                true, 0, 0, 0.0
        );

        hike.setParticipants(new HashSet<>());
        Participant savedCreator = participantRepository.save(pCreator);
        hike.getParticipants().add(savedCreator);

        // Gestion POI Départ/Arrivée
        resolvePois(hike);
        validateHike(hike);

        // On ignore les listes annexes
        hike.setOptionalPoints(new HashSet<>());
        hike.setFoodCatalogue(new HashSet<>());
        hike.setEquipmentRequired(new HashSet<>());

        return hikeRepository.save(hike);
    }

    @Transactional
    public Hike updateHike(Long hikeId, Hike details, Long userId) {
        Hike hike = getHikeById(hikeId, userId);

        if (details.getLibelle() != null) hike.setLibelle(details.getLibelle());
        if (details.getDureeJours() > 0) hike.setDureeJours(details.getDureeJours());

        if (details.getDepart() != null) hike.setDepart(details.getDepart());
        if (details.getArrivee() != null) hike.setArrivee(details.getArrivee());

        resolvePois(hike);
        validateHike(hike);

        return hikeRepository.save(hike);
    }

    @Transactional
    public void deleteHike(Long hikeId, Long userId) {
        Hike hike = getHikeById(hikeId, userId);

        // Nettoyage manuel des relations avant suppression (si pas de Cascade ALL strict)
        hike.getFoodCatalogue().clear();
        hike.getEquipmentRequired().clear();
        hike.getOptionalPoints().clear();
        hike.getParticipants().clear();

        hikeRepository.save(hike);
        hikeRepository.delete(hike);
    }

    private void resolvePois(Hike hike) {
        if (hike.getDepart() != null && hike.getDepart().getId() != null) {
            hike.setDepart(poiRepository.findById(hike.getDepart().getId())
                    .orElseThrow(() -> new RuntimeException("Départ introuvable")));
        }
        if (hike.getArrivee() != null && hike.getArrivee().getId() != null) {
            hike.setArrivee(poiRepository.findById(hike.getArrivee().getId())
                    .orElseThrow(() -> new RuntimeException("Arrivée introuvable")));
        }
    }

    private void validateHike(Hike hike) {
        if (hike.getDepart().getLatitude() == hike.getArrivee().getLatitude() &&
            hike.getDepart().getLongitude() == hike.getArrivee().getLongitude()) {
            throw new RuntimeException("Le départ et l'arrivée ne peuvent pas être confondu");
        }
    }

    public void optimizeBackpackParticipant(Hike hike) {
        MetierToolsService.validateHikeForOptimize(hike);


    }
}