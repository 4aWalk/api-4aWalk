package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.BelongEquipment;
import iut.rodez.projet.sae.fourawalkapi.entity.EquipmentItem;
import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.exception.BusinessValidationException;
import iut.rodez.projet.sae.fourawalkapi.exception.ResourceNotFoundException;
import iut.rodez.projet.sae.fourawalkapi.exception.UnauthorizedAccessException;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.BelongEquipmentRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.EquipmentItemRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.HikeRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.ParticipantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service de gestion du matériel de randonnée.
 * Responsable du cycle de vie des équipements (CRUD) et de leur association
 * avec les randonnées (ajout/retrait du sac global).
 */
@Service
public class EquipmentService {

    private final EquipmentItemRepository equipmentRepository;
    private final HikeRepository hikeRepository;
    private final ParticipantRepository participantRepository;
    private final BelongEquipmentRepository belongEquipmentRepository;

    /**
     * Injection de dépendances via le constructeur pour assurer l'immutabilité des repositories.
     */
    public EquipmentService(EquipmentItemRepository er, HikeRepository hr,
                            ParticipantRepository pr, BelongEquipmentRepository be) {
        this.equipmentRepository = er;
        this.hikeRepository = hr;
        this.participantRepository = pr;
        this.belongEquipmentRepository = be;
    }

    /**
     * Récupère le catalogue complet des équipements disponibles dans le système.
     * @return Liste de tous les équipements.
     */
    public List<EquipmentItem> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    /**
     * Crée une nouvelle référence d'équipement dans le catalogue global.
     * Applique les règles de validation métier avant la persistance.
     *
     * @param item L'entité équipement à créer.
     * @return L'équipement sauvegardé avec son ID généré.
     */
    public EquipmentItem createEquipment(EquipmentItem item) {
        // Validation stricte des contraintes métier (Poids, Type, etc.)
        validateEquipmentRules(item);
        return equipmentRepository.save(item);
    }

    /**
     * Supprime définitivement un équipement du catalogue.
     * @param id L'identifiant de l'équipement.
     */
    public void deleteEquipment(Long id) {
        equipmentRepository.deleteById(id);
    }

    /**
     * Associe un équipement existant à une randonnée spécifique.
     * Cette méthode est transactionnelle pour garantir l'intégrité des données lors de la modification de la relation.
     *
     * @param hikeId L'ID de la randonnée cible.
     * @param equipId L'ID de l'équipement à ajouter.
     * @param userId L'ID de l'utilisateur demandeur (pour vérification des droits).
     * @throws ResourceNotFoundException Si la randonnée/équipement n'existe pas ou si l'utilisateur n'est pas le créateur.
     * @throws UnauthorizedAccessException si l'accès à une ressource est refusée
     */
    @Transactional
    public void addEquipmentToHike(Long hikeId, Long equipId, Long userId, Long participantId) {
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new ResourceNotFoundException("Randonnée introuvable"));

        // Vérification de sécurité : Seul le créateur peut modifier
        if (!hike.getCreator().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Accès refusé : Vous n'êtes pas le propriétaire de cette randonnée");
        }

        EquipmentItem item = equipmentRepository.findById(equipId)
                .orElseThrow(() -> new ResourceNotFoundException("Équipement introuvable"));

        // Propriétaire obligatoire pour certains types ---
        boolean needsOwner = (item.getType() == TypeEquipment.VETEMENT) ||
                (item.getType() == TypeEquipment.REPOS);

        if (needsOwner && participantId == null) {
            throw new BusinessValidationException("Un propriétaire n'a pas été défini pour l'objet " + item.getNom());
        }

        // --- Gestion de l'appartenance ---
        if (participantId != null && needsOwner) {
            Participant participant = participantRepository.findById(participantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Participant introuvable"));

            // Règle métier : le participant doit faire partie de cette randonnée
            boolean isParticipantInHike = hike.getParticipants().stream()
                    .anyMatch(p -> p.getId().equals(participantId));

            if (!isParticipantInHike) {
                throw new UnauthorizedAccessException("Le participant spécifié ne fait pas partie de cette randonnée.");
            }

            // Création et sauvegarde de la relation ternaire
            BelongEquipment belong = new BelongEquipment(hike, participant, item);
            belongEquipmentRepository.save(belong);
        }

        // Délégation de la logique métier (ajout dans le sac de la rando)
        hike.addEquipment(item);
        hikeRepository.save(hike);
    }

    /**
     * Retire un équipement de la liste du matériel d'une randonnée.
     *
     * @param hikeId L'ID de la randonnée.
     * @param equipId L'ID de l'équipement à retirer.
     * @param userId L'ID de l'utilisateur pour la sécurité.
     */
    @Transactional
    public void removeEquipmentFromHike(Long hikeId, Long equipId, Long userId) {
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new ResourceNotFoundException("Randonnée introuvable"));

        if (!hike.getCreator().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Accès refusé");
        }

        EquipmentItem item = equipmentRepository.findById(equipId)
                .orElseThrow(() -> new ResourceNotFoundException("Équipement introuvable"));

        // Appel à l'entité pour suppression propre dans la collection
        hike.removeEquipment(item);

        hikeRepository.save(hike);
    }

    /**
     * Validateur interne des règles métier (Business Rules).
     * Centralise les contraintes techniques pour éviter d'avoir des équipements invalides en base.
     * Règle actuelle :
     * - Nom obligatoire
     * - Un équipement doit peser entre 50g et 5kg
     * - Nombre d'équipement dans le lot entre 1 et 3 compris
     * - Masse à vide > 0 et < à masse de l'équipement
     * @param item équipement à vérifier
     */
    private void validateEquipmentRules(EquipmentItem item) {
        if (item.getNom() == null || item.getNom().isEmpty()) {
            throw new BusinessValidationException(
                    "Le nom d'un équipement est obligatoire");
        }
        if (item.getMasseGrammes() < 50 || item.getMasseGrammes() > 5000) {
            throw new BusinessValidationException(
                    "La masse de l'équipement doit être comprise entre 50g et 5kg");
        }

        if (item.getNbItem() < 1 || item.getNbItem() > 3) {
            throw new BusinessValidationException(
                    "Un lot d'équipement peut couvrir 1 à 3 participants");
        }

        if (item.getMasseAVide() < 0 || item.getMasseAVide() > item.getMasseGrammes()) {
            throw new BusinessValidationException(
                    "Un équipement ne peut pas avoir une masse à vide < 0 ou > à sa masse");
        }
    }

    /**
     * Récupère une Map associant l'ID de chaque équipement à son propriétaire pour une randonnée.
     */
    public Map<Long, List<Participant>> getEquipmentOwners(Long hikeId) {
        List<BelongEquipment> belongs = belongEquipmentRepository.findByHikeId(hikeId);

        return belongs.stream()
                .collect(Collectors.groupingBy(
                        belong -> belong.getEquipment().getId(),
                        Collectors.mapping(BelongEquipment::getParticipant, Collectors.toList())
                ));
    }
}