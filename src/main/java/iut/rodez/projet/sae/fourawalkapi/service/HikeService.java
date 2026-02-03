package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.*;
import iut.rodez.projet.sae.fourawalkapi.model.Item;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Service
public class HikeService {

    private final HikeRepository hikeRepository;
    private final BackpackDistributorService backpackDistributor;
    private final UserService userService;
    private final PointOfInterestRepository poiRepository;
    private final ParticipantRepository participantRepository;

    public HikeService(HikeRepository hr, BackpackDistributorService bds, UserService us, PointOfInterestRepository poiRepo, ParticipantRepository pr) {
        this.hikeRepository = hr;
        this.backpackDistributor = bds;
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

        // Sécurité : on vérifie que c'est bien le créateur qui accède
        if (!hike.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Accès refusé");
        }
        return hike;
    }

    @Transactional
    public Hike createHike(Hike hike, Long creatorId) {
        User user = userService.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // 1. Vérification unicité du nom (Libellé) pour cet utilisateur
        checkLibelleUniqueness(creatorId, hike.getLibelle());

        hike.setCreator(user);

        // Création du Participant "Chef" depuis le User
        Participant pCreator = new Participant(
                user.getPrenom(),
                user.getNom(),
                user.getAge(),
                user.getNiveau(),
                user.getMorphologie(),
                true, user.getId(), 0, 0, 0.0
        );

        hike.setParticipants(new HashSet<>());
        Participant savedCreator = participantRepository.save(pCreator);
        hike.getParticipants().add(savedCreator);

        resolvePois(hike);
        validateHike(hike);

        hike.setOptionalPoints(new HashSet<>());
        hike.setFoodCatalogue(new HashSet<>());
        hike.setEquipmentGroups(new HashMap<>());

        return hikeRepository.save(hike);
    }

    @Transactional
    public Hike updateHike(Long hikeId, Hike details, Long userId) {
        Hike hike = getHikeById(hikeId, userId);

        // 1. Vérification unicité SEULEMENT si le libellé change
        if (details.getLibelle() != null && !details.getLibelle().equals(hike.getLibelle())) {
            checkLibelleUniqueness(userId, details.getLibelle());
            hike.setLibelle(details.getLibelle());
        }

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

        hike.getFoodCatalogue().clear();
        hike.getEquipmentGroups().clear();
        hike.getOptionalPoints().clear();
        hike.getParticipants().clear();

        hikeRepository.save(hike);
        hikeRepository.delete(hike);
    }

    // --- Méthodes Privées ---

    /**
     * Vérifie si une randonnée avec ce nom existe déjà pour cet utilisateur.
     * Throws RuntimeException si c'est le cas.
     */
    private void checkLibelleUniqueness(Long userId, String libelle) {
        if (libelle == null || libelle.trim().isEmpty()) return; // Ou lever une erreur si le nom vide est interdit

        if (hikeRepository.existsByCreatorIdAndLibelle(userId, libelle)) {
            throw new RuntimeException("Vous avez déjà une randonnée nommée : " + libelle);
        }
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
        // on ne vérifie plus la distance entre départ et arrivé car une randonné peut être une boucle
    }

    @Transactional
    public void optimizeBackpack(Long hikeId, Long userId) {
        Hike hike = getHikeById(hikeId, userId);
        MetierToolsService.validateHikeForOptimize(hike);

        // Attention : Assure-toi que OptimizerService existe et possède ces méthodes statiques
        // Sinon il faut l'injecter via le constructeur comme backpackDistributor
        List<EquipmentItem> optimizedEquipment = OptimizerService.getOptimizeAllEquipmentV2(hike);
        List<FoodProduct> optimizedFood = OptimizerService.getOptimizeAllFoodV2(hike);

        List<Item> itemsToPack = new ArrayList<>();
        itemsToPack.addAll(optimizedEquipment);
        itemsToPack.addAll(optimizedFood);

        List<Backpack> backpacks = hike.getBackpacks();

        backpackDistributor.distributeBatchesToBackpacks(itemsToPack, backpacks);

        hikeRepository.save(hike);
    }
}