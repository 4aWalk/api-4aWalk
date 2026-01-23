package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.*;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class HikeService {

    private final HikeRepository hikeRepository;
    private final ParticipantRepository participantRepository;
    private final FoodProductRepository foodRepository;
    private final EquipmentItemRepository equipmentRepository;
    // AJOUT IMPORTANT : Il nous faut le repo des POI pour charger les coords
    private final PointOfInterestRepository poiRepository;
    private final UserService userService;
    private final ParticipantService participantService;

    public HikeService(HikeRepository hr, ParticipantRepository pr,
                       FoodProductRepository fr, EquipmentItemRepository er,
                       PointOfInterestRepository poiRepo, // Injection ici
                       UserService us, ParticipantService ps) {
        this.hikeRepository = hr;
        this.participantRepository = pr;
        this.foodRepository = fr;
        this.equipmentRepository = er;
        this.poiRepository = poiRepo; // Assignation
        this.userService = us;
        this.participantService = ps;
    }

    public List<Hike> getHikesByCreator(Long creatorId) {
        return hikeRepository.findByCreatorId(creatorId);
    }

    public Optional<Hike> getHikeById(Long hikeId) {
        return hikeRepository.findById(hikeId);
    }

    @Transactional
    public Hike createHike(Hike hike, Long creatorId) {
        User creator = userService.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Créateur non trouvé"));
        hike.setCreator(creator);

        if (hike.getDepart() != null && hike.getDepart().getId() != null) {
            PointOfInterest realDepart = poiRepository.findById(hike.getDepart().getId())
                    .orElseThrow(() -> new RuntimeException("Point de départ introuvable (ID " + hike.getDepart().getId() + ")"));
            hike.setDepart(realDepart);
        }

        if (hike.getArrivee() != null && hike.getArrivee().getId() != null) {
            PointOfInterest realArrivee = poiRepository.findById(hike.getArrivee().getId())
                    .orElseThrow(() -> new RuntimeException("Point d'arrivée introuvable (ID " + hike.getArrivee().getId() + ")"));
            hike.setArrivee(realArrivee);
        }

        validateHike(hike);

        return hikeRepository.save(hike);
    }

    @Transactional
    public Hike updateHike(Long hikeId, Hike details, Long userId) {
        // 1. Chargement de l'entité existante
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException("Randonnée non trouvée"));

        checkOwnership(hike, userId);

        // 2. Mise à jour des champs simples et POI uniques (Départ/Arrivée)
        updateBasicInfoAndPois(hike, details);

        // 3. Mise à jour des Listes (Collections) via des sous-méthodes dédiées
        updateOptionalPoints(hike, details.getOptionalPoints());
        updateFoodCatalogue(hike, details.getFoodCatalogue());
        updateEquipmentRequired(hike, details.getEquipmentRequired());
        updateParticipants(hike, details.getParticipants());

        // 4. Validation finale sur l'objet complet et à jour
        validateHike(hike);

        return hikeRepository.save(hike);
    }

    @Transactional
    public void deleteHike(Long id, Long userId) { // Ajout du userId
        Hike hike = hikeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Randonnée introuvable"));

        checkOwnership(hike, userId);

        hikeRepository.delete(hike);
    }

    // --- Participants ---
    @Transactional
    public Hike addParticipantToHike(Long hikeId, Participant p) {
        Hike hike = hikeRepository.findById(hikeId).orElseThrow();
        participantService.validateParticipant(p);
        Participant saved = participantRepository.save(p);
        hike.getParticipants().add(saved);
        return hikeRepository.save(hike);
    }

    @Transactional
    public void removeParticipantFromHike(Long hikeId, Long participantId) {
        Hike hike = hikeRepository.findById(hikeId).orElseThrow();
        hike.getParticipants().removeIf(p -> p.getId().equals(participantId));
        hikeRepository.save(hike);
        participantService.deleteParticipant(participantId);
    }

    // --- Food & Equipment ---
    @Transactional
    public Hike addFoodToHike(Long hikeId, Long foodId) {
        Hike hike = hikeRepository.findById(hikeId).orElseThrow();
        FoodProduct fp = foodRepository.findById(foodId).orElseThrow();
        hike.getFoodCatalogue().add(fp);
        return hikeRepository.save(hike);
    }

    @Transactional
    public Hike removeFoodFromHike(Long hikeId, Long foodId) {
        Hike hike = hikeRepository.findById(hikeId).orElseThrow();
        hike.getFoodCatalogue().removeIf(f -> f.getId().equals(foodId));
        return hikeRepository.save(hike);
    }

    @Transactional
    public Hike addEquipmentToHike(Long hikeId, Long equipId) {
        Hike hike = hikeRepository.findById(hikeId).orElseThrow();
        EquipmentItem ei = equipmentRepository.findById(equipId).orElseThrow();
        hike.getEquipmentRequired().add(ei);
        return hikeRepository.save(hike);
    }

    @Transactional
    public Hike removeEquipmentFromHike(Long hikeId, Long equipId) {
        Hike hike = hikeRepository.findById(hikeId).orElseThrow();
        hike.getEquipmentRequired().removeIf(e -> e.getId().equals(equipId));
        return hikeRepository.save(hike);
    }

    private void updateBasicInfoAndPois(Hike target, Hike source) {
        if (source.getLibelle() != null) {
            target.setLibelle(source.getLibelle());
        }
        if (source.getDureeJours() > 0) {
            target.setDureeJours(source.getDureeJours());
        }

        if (source.getDepart() != null && source.getDepart().getId() != null) {
            PointOfInterest realDepart = poiRepository.findById(source.getDepart().getId())
                    .orElseThrow(() -> new RuntimeException("Point de départ introuvable"));
            // On s'assure que l'entité est bien attachée
            target.setDepart(realDepart);
        }

        if (source.getArrivee() != null && source.getArrivee().getId() != null) {
            PointOfInterest realArrivee = poiRepository.findById(source.getArrivee().getId())
                    .orElseThrow(() -> new RuntimeException("Point d'arrivée introuvable"));
            target.setArrivee(realArrivee);
        }
    }

    private void updateOptionalPoints(Hike hike, Set<PointOfInterest> newPoints) {
        if (newPoints == null) return;

        // On vide la liste : Hibernate va mettre hike_id à NULL en BDD
        hike.getOptionalPoints().clear();

        for (PointOfInterest poi : newPoints) {
            if (poi.getId() != null) {
                // On récupère le point existant pour ne pas créer de doublon "transient"
                PointOfInterest existing = poiRepository.findById(poi.getId())
                        .orElseThrow(() -> new RuntimeException("POI introuvable"));
                hike.getOptionalPoints().add(existing);
            } else {
                // C'est un nouveau point créé dans Postman
                hike.getOptionalPoints().add(poi);
            }
        }
    }

    private void updateFoodCatalogue(Hike hike, Set<FoodProduct> newFood) {
        if (newFood == null) return;

        hike.getFoodCatalogue().clear();
        for (FoodProduct fp : newFood) {
            if (fp.getId() != null) {
                FoodProduct realFp = foodRepository.findById(fp.getId())
                        .orElseThrow(() -> new RuntimeException("Nourriture introuvable ID " + fp.getId()));
                hike.getFoodCatalogue().add(realFp);
            }
        }
    }

    private void updateEquipmentRequired(Hike hike, Set<EquipmentItem> newEquip) {
        if (newEquip == null) return;

        hike.getEquipmentRequired().clear();
        for (EquipmentItem ei : newEquip) {
            if (ei.getId() != null) {
                EquipmentItem realEi = equipmentRepository.findById(ei.getId())
                        .orElseThrow(() -> new RuntimeException("Equipement introuvable ID " + ei.getId()));
                hike.getEquipmentRequired().add(realEi);
            }
        }
    }

    private void updateParticipants(Hike hike, Set<Participant> newParticipants) {
        if (newParticipants == null) return;

        hike.getParticipants().clear();
        for (Participant p : newParticipants) {
            if (p.getId() != null) {
                Participant realP = participantRepository.findById(p.getId())
                        .orElseThrow(() -> new RuntimeException("Participant introuvable ID " + p.getId()));
                hike.getParticipants().add(realP);
            }
        }
    }

    private void checkOwnership(Hike hike, Long userId) {
        if (hike.getCreator() == null || !hike.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Accès refusé : vous n'êtes pas le créateur de cette randonnée");
        }
    }

    public void validateHike(Hike hike) {
        if (hike.getDureeJours() < 1 || hike.getDureeJours() > 3)
            throw new IllegalArgumentException("La durée doit être comprise entre 1 et 3 jours");

        if (hike.getDepart() == null || hike.getArrivee() == null)
            throw new IllegalArgumentException("Les points de départ et d'arrivée sont requis");

        if (Double.compare(hike.getDepart().getLongitude(), hike.getArrivee().getLongitude()) == 0 &&
                Double.compare(hike.getDepart().getLatitude(), hike.getArrivee().getLatitude()) == 0) {
            throw new IllegalArgumentException("Le point de départ et d'arrivée sont identiques (coordonnées GPS confondues)\n" +
                    "[" + hike.getDepart().getLongitude() + "," +
                    hike.getDepart().getLatitude() + "] ; " +
                    "[" + hike.getArrivee().getLongitude() + "," +
                    hike.getArrivee().getLatitude() + "]");
        }
    }
}