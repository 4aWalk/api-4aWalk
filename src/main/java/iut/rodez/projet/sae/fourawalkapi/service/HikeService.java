package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.*;
import iut.rodez.projet.sae.fourawalkapi.model.Item;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service de gestion des randonnées assurant les opérations de persistance,
 * la validation métier et l'orchestration des algorithmes d'optimisation.
 */
@Service
public class HikeService {

    private final HikeRepository hikeRepository;
    private final BackpackDistributorService backpackDistributor;
    private final MetierToolsService metierToolsService;
    private final OptimizerService optimizerService;
    private final UserRepository userRepository;
    private final PointOfInterestRepository poiRepository;
    private final ParticipantRepository participantRepository;

    /**
     * Initialise le service avec les dépendances nécessaires à la gestion des randonnées.
     * @param hr Repository pour l'accès aux données des randonnées.
     * @param bds Service de distribution des items dans les sacs à dos.
     * @param mts Service d'outils de validation métier.
     * @param os Service de sélection optimisée du matériel et de la nourriture.
     * @param ur Repository pour l'accès aux données utilisateurs.
     * @param poiRepo Repository pour la gestion des points d'intérêt.
     * @param pr Repository pour la gestion des participants.
     */
    public HikeService(HikeRepository hr,
                       BackpackDistributorService bds,
                       MetierToolsService mts,
                       OptimizerService os, UserRepository ur,
                       PointOfInterestRepository poiRepo,
                       ParticipantRepository pr) {
        this.hikeRepository = hr;
        this.backpackDistributor = bds;
        this.metierToolsService = mts;
        this.optimizerService = os;
        this.userRepository = ur;
        this.poiRepository = poiRepo;
        this.participantRepository = pr;
    }

    /**
     * Récupère la liste des randonnées créées par un utilisateur spécifique.
     * @param creatorId Identifiant unique du créateur.
     * @return Une liste d'objets Hike appartenant à l'utilisateur.
     */
    public List<Hike> getHikesByCreator(Long creatorId) {
        return hikeRepository.findByCreatorId(creatorId);
    }

    /**
     * Récupère une randonnée par son identifiant après vérification de la propriété.
     * @param hikeId Identifiant de la randonnée recherchée.
     * @param userId Identifiant de l'utilisateur effectuant la requête.
     * @return La randonnée correspondante.
     * @throws RuntimeException Si la randonnée n'existe pas ou si l'utilisateur n'est pas le créateur.
     */
    public Hike getHikeById(Long hikeId, Long userId) {
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException("Randonnée introuvable"));

        if (!hike.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Accès refusé : Vous n'êtes pas le propriétaire de cette randonnée");
        }
        return hike;
    }

    /**
     * Enregistre une nouvelle randonnée et initialise le créateur comme premier participant.
     * @param hike L'objet randonnée à persister.
     * @param creatorId Identifiant de l'utilisateur créateur.
     * @return La randonnée sauvegardée avec ses relations initialisées.
     * @throws RuntimeException Si l'utilisateur est introuvable ou si le libellé est déjà utilisé.
     */
    @Transactional
    public Hike createHike(Hike hike, Long creatorId) {
        User user = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        checkLibelleUniqueness(creatorId, hike.getLibelle());

        hike.setCreator(user);

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
        metierToolsService.validateHikeForOptimize(hike);

        hike.setOptionalPoints(new HashSet<>());
        hike.setFoodCatalogue(new ArrayList<>());
        hike.setEquipmentGroups(new HashMap<>());

        return hikeRepository.save(hike);
    }

    /**
     * Met à jour les informations d'une randonnée existante.
     * @param hikeId Identifiant de la randonnée à modifier.
     * @param details Objet contenant les nouvelles valeurs.
     * @param userId Identifiant de l'utilisateur demandeur.
     * @return La randonnée mise à jour.
     */
    @Transactional
    public Hike updateHike(Long hikeId, Hike details, Long userId) {
        Hike hike = getHikeById(hikeId, userId);

        if (details.getLibelle() != null && !details.getLibelle().equals(hike.getLibelle())) {
            checkLibelleUniqueness(userId, details.getLibelle());
            hike.setLibelle(details.getLibelle());
        }

        if (details.getDureeJours() > 0) hike.setDureeJours(details.getDureeJours());
        if (details.getDepart() != null) hike.setDepart(details.getDepart());
        if (details.getArrivee() != null) hike.setArrivee(details.getArrivee());

        resolvePois(hike);
        metierToolsService.validateHikeForOptimize(hike);

        return hikeRepository.save(hike);
    }

    /**
     * Supprime définitivement une randonnée et rompt ses associations avec les autres entités.
     * * @param hikeId Identifiant de la randonnée à supprimer.
     * @param userId Identifiant de l'utilisateur demandeur.
     */
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

    /**
     * Vérifie la disponibilité d'un libellé pour les randonnées d'un utilisateur donné.
     * @param userId Identifiant de l'utilisateur.
     * @param libelle Nom de la randonnée à tester.
     * @throws RuntimeException Si le libellé est déjà utilisé par ce même utilisateur.
     */
    private void checkLibelleUniqueness(Long userId, String libelle) {
        if (libelle == null || libelle.trim().isEmpty()) return;
        if (hikeRepository.existsByCreatorIdAndLibelle(userId, libelle)) {
            throw new RuntimeException("Vous avez déjà une randonnée nommée : " + libelle);
        }
    }

    /**
     * Synchronise les points de départ et d'arrivée avec les données de la base de données.
     * @param hike La randonnée dont les points d'intérêt doivent être résolus.
     * @throws RuntimeException Si l'un des points d'intérêt est introuvable.
     */
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

    /**
     * Exécute les algorithmes de sélection de matériel et de répartition dans les sacs à dos.
     * @param hikeId Identifiant de la randonnée à optimiser.
     * @param userId Identifiant de l'utilisateur demandeur.
     */
    @Transactional
    public void optimizeBackpack(Long hikeId, Long userId) {
        Hike hike = getHikeById(hikeId, userId);

        metierToolsService.validateHikeForOptimize(hike);

        List<EquipmentItem> optimizedEquipment = optimizerService.getOptimizeAllEquipmentV2(hike);
        List<FoodProduct> optimizedFood = optimizerService.getOptimizeAllFoodV2(hike);

        List<Item> itemsToPack = new ArrayList<>();
        itemsToPack.addAll(optimizedEquipment);
        itemsToPack.addAll(optimizedFood);

        List<Backpack> backpacks = hike.getBackpacks();

        backpackDistributor.distributeBatchesToBackpacks(itemsToPack, backpacks);

        hikeRepository.save(hike);
    }

    /**
     * Calcule la distance cumulée entre le départ, les points optionnels triés et l'arrivée.
     * @param hike La randonnée pour laquelle calculer la distance.
     * @return La distance totale exprimée dans l'unité de mesure du système.
     */
    public static double getAllDistance(Hike hike) {
        double distance = 0.0;
        Set<PointOfInterest> setPoi = hike.getOptionalPoints();
        List<PointOfInterest> sortedPois = new ArrayList<>();

        if (setPoi != null) {
            sortedPois.addAll(setPoi);
            sortedPois.sort(Comparator.comparingInt(PointOfInterest::getSequence));
        }

        PointOfInterest currentPoint = hike.getDepart();

        for (PointOfInterest nextPoi : sortedPois) {
            distance += currentPoint.distanceTo(nextPoi.getLatitude(), nextPoi.getLongitude());
            currentPoint = nextPoi;
        }

        return distance + currentPoint.distanceTo(hike.getArrivee().getLatitude(), hike.getArrivee().getLongitude());
    }
}