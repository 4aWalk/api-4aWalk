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
    private final PointOfInterestRepository poiRepository;
    private final UserService userService;
    private final ParticipantService participantService;

    public HikeService(HikeRepository hr, ParticipantRepository pr,
                       FoodProductRepository fr, EquipmentItemRepository er,
                       PointOfInterestRepository poiRepo,
                       UserService us, ParticipantService ps) {
        this.hikeRepository = hr;
        this.participantRepository = pr;
        this.foodRepository = fr;
        this.equipmentRepository = er;
        this.poiRepository = poiRepo;
        this.userService = us;
        this.participantService = ps;
    }

    public List<Hike> getHikesByCreator(Long creatorId) {
        return hikeRepository.findByCreatorId(creatorId);
    }

    public Optional<Hike> getHikeById(Long hikeId) {
        return hikeRepository.findById(hikeId);
    }

    // ===================================================================================
    // CRUD PRINCIPAL
    // ===================================================================================

    @Transactional
    public Hike createHike(Hike hike, Long creatorId) {
        // 1. Liaison avec l'utilisateur créateur
        User user = userService.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        hike.setCreator(user);

        // 2. Création automatique du Participant "Créateur"
        Participant pCreator = new Participant();
        pCreator.setCreator(true); // C'est le chef
        // (Optionnel : set Morphologie/Age ici si disponible)

        Participant savedCreator = participantRepository.save(pCreator);
        hike.getParticipants().add(savedCreator);

        // 3. Résolution des POI (Départ / Arrivée)
        if (hike.getDepart() != null && hike.getDepart().getId() != null) {
            hike.setDepart(poiRepository.findById(hike.getDepart().getId())
                    .orElseThrow(() -> new RuntimeException("Départ introuvable")));
        }
        if (hike.getArrivee() != null && hike.getArrivee().getId() != null) {
            hike.setArrivee(poiRepository.findById(hike.getArrivee().getId())
                    .orElseThrow(() -> new RuntimeException("Arrivée introuvable")));
        }

        validateHike(hike);
        return hikeRepository.save(hike);
    }

    @Transactional
    public Hike updateHike(Long hikeId, Hike details, Long userId) {
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException("Randonnée non trouvée"));

        checkOwnership(hike, userId);

        updateBasicInfoAndPois(hike, details);

        // Mise à jour des listes
        updateOptionalPoints(hike, details.getOptionalPoints());
        updateFoodCatalogue(hike, details.getFoodCatalogue());
        updateEquipmentRequired(hike, details.getEquipmentRequired());

        // ATTENTION : C'est ici qu'on appelle la version sécurisée
        updateParticipants(hike, details.getParticipants());

        validateHike(hike);
        return hikeRepository.save(hike);
    }

    @Transactional
    public void deleteHike(Long id, Long userId) {
        Hike hike = hikeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Randonnée introuvable"));

        checkOwnership(hike, userId);

        hikeRepository.delete(hike);
    }

    // ===================================================================================
    // GESTION DES PARTICIPANTS (Spécifique)
    // ===================================================================================

    @Transactional
    public Hike addParticipantToHike(Long hikeId, Participant p) {
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException("Randonnée non trouvée"));

        if (hike.getParticipants().size() >= 3) {
            throw new RuntimeException("La randonnée est déjà complète (max 3).");
        }

        participantService.validateParticipant(p);

        // SÉCURITÉ : On force à false, seul createHike peut mettre true
        p.setCreator(false);

        Participant saved = participantRepository.save(p);
        hike.getParticipants().add(saved);

        return hikeRepository.save(hike);
    }

    @Transactional
    public void removeParticipantFromHike(Long hikeId, Long participantId) {
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException("Randonnée non trouvée"));

        Participant pToRemove = hike.getParticipants().stream()
                .filter(p -> p.getId().equals(participantId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Participant non trouvé dans cette randonnée"));

        // SÉCURITÉ : Interdiction de virer le créateur
        if (pToRemove.getCreator()) {
            throw new RuntimeException("Interdit : Vous ne pouvez pas supprimer le créateur.");
        }

        hike.getParticipants().remove(pToRemove);
        hikeRepository.save(hike);
        participantService.deleteParticipant(participantId);
    }

    // Ajout pour mettre à jour un participant spécifique (comme discuté précédemment)
    @Transactional
    public Participant updateParticipantInHike(Long hikeId, Long participantId, Participant details, Long userId) {
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException("Randonnée non trouvée"));

        checkOwnership(hike, userId);

        Participant participant = hike.getParticipants().stream()
                .filter(p -> p.getId().equals(participantId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Le participant n'appartient pas à cette randonnée"));

        return participantService.updateParticipantDetails(participantId, details);
    }

    // ===================================================================================
    // MÉTHODES PRIVÉES DE MISE À JOUR (Helpers)
    // ===================================================================================

    /**
     * Méthode CRITIQUE : Met à jour la liste des participants sans effacer le créateur.
     */
    private void updateParticipants(Hike hike, Set<Participant> newParticipants) {
        if (newParticipants == null) return;

        // 1. On sauvegarde le créateur actuel
        Participant creatorParticipant = hike.getParticipants().stream()
                .filter(p -> p.getCreator()) // Utilise ton getter getCreator()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Erreur critique de données : Aucun créateur trouvé dans la randonnée existante"));

        // 2. On vide la liste
        hike.getParticipants().clear();

        // 3. On remet OBLIGATOIREMENT le créateur
        hike.getParticipants().add(creatorParticipant);

        // 4. On ajoute les autres (jusqu'à atteindre 3 max)
        for (Participant p : newParticipants) {
            if (hike.getParticipants().size() >= 3) break;

            // Si le JSON renvoie le créateur (flag true), on l'ignore pour ne pas l'avoir en double
            if (p.getCreator()) continue;
            // Si c'est le même ID que le créateur (cas où le flag serait false mais l'ID identique)
            if (p.getId() != null && p.getId().equals(creatorParticipant.getId())) continue;

            if (p.getId() != null) {
                Participant realP = participantRepository.findById(p.getId())
                        .orElseThrow(() -> new RuntimeException("Participant introuvable ID " + p.getId()));
                hike.getParticipants().add(realP);
            }
        }
    }

    private void updateOptionalPoints(Hike hike, Set<PointOfInterest> newPoints) {
        if (newPoints == null) return;
        hike.getOptionalPoints().clear();
        for (PointOfInterest poi : newPoints) {
            if (poi.getId() != null) {
                PointOfInterest existing = poiRepository.findById(poi.getId())
                        .orElseThrow(() -> new RuntimeException("POI introuvable"));
                hike.getOptionalPoints().add(existing);
            } else {
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

    private void updateBasicInfoAndPois(Hike target, Hike source) {
        if (source.getLibelle() != null) target.setLibelle(source.getLibelle());
        if (source.getDureeJours() > 0) target.setDureeJours(source.getDureeJours());

        if (source.getDepart() != null && source.getDepart().getId() != null) {
            target.setDepart(poiRepository.findById(source.getDepart().getId())
                    .orElseThrow(() -> new RuntimeException("Point de départ introuvable")));
        }
        if (source.getArrivee() != null && source.getArrivee().getId() != null) {
            target.setArrivee(poiRepository.findById(source.getArrivee().getId())
                    .orElseThrow(() -> new RuntimeException("Point d'arrivée introuvable")));
        }
    }

    // ===================================================================================
    // ALIMENTATION & EQUIPEMENT (Unitaires)
    // ===================================================================================

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

    // ===================================================================================
    // VALIDATIONS & UTILITAIRES
    // ===================================================================================

    private void checkOwnership(Hike hike, Long userId) {
        if (hike.getCreator() == null || !hike.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Accès refusé : vous n'êtes pas le créateur de cette randonnée");
        }
    }

    public void validateHike(Hike hike) {
        // Validation Participant (Min 1 créateur, Max 3)
        int nb = hike.getParticipants().size();
        if (nb < 1) throw new IllegalArgumentException("La randonnée doit avoir au moins 1 participant (le créateur)");
        if (nb > 3) throw new IllegalArgumentException("La randonnée ne peut pas dépasser 3 participants");

        // Validation Durée
        if (hike.getDureeJours() < 1 || hike.getDureeJours() > 3)
            throw new IllegalArgumentException("La durée doit être comprise entre 1 et 3 jours");

        // Validation Points GPS
        if (hike.getDepart() == null || hike.getArrivee() == null)
            throw new IllegalArgumentException("Les points de départ et d'arrivée sont requis");

        if (Double.compare(hike.getDepart().getLongitude(), hike.getArrivee().getLongitude()) == 0 &&
                Double.compare(hike.getDepart().getLatitude(), hike.getArrivee().getLatitude()) == 0) {
            throw new IllegalArgumentException("Le point de départ et d'arrivée sont identiques.");
        }
    }
}