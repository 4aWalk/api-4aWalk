package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.EquipmentItem;
import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.EquipmentItemRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.HikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de gestion du matériel de randonnée.
 * Responsable du cycle de vie des équipements (CRUD) et de leur association
 * avec les randonnées (ajout/retrait du sac global).
 */
@Service
public class EquipmentService {

    private final EquipmentItemRepository equipmentRepository;
    private final HikeRepository hikeRepository;

    /**
     * Injection de dépendances via le constructeur pour assurer l'immutabilité des repositories.
     */
    public EquipmentService(EquipmentItemRepository er, HikeRepository hr) {
        this.equipmentRepository = er;
        this.hikeRepository = hr;
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
     * @throws RuntimeException Si la randonnée/équipement n'existe pas ou si l'utilisateur n'est pas le créateur.
     */
    @Transactional
    public void addEquipmentToHike(Long hikeId, Long equipId, Long userId) {
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException("Randonnée introuvable"));

        // Vérification de sécurité : Seul le créateur peut modifier le contenu du sac
        if (!hike.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Accès refusé : Vous n'êtes pas le propriétaire de cette randonnée");
        }

        EquipmentItem item = equipmentRepository.findById(equipId)
                .orElseThrow(() -> new RuntimeException("Équipement introuvable"));

        // Délégation de la logique métier à l'Entité Hike (Domain Driven Design)
        // C'est l'entité qui sait comment trier et organiser ses listes internes
        hike.addEquipment(item);

        // La sauvegarde de la randonnée propage les changements vers la table de jointure
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
                .orElseThrow(() -> new RuntimeException("Randonnée introuvable"));

        if (!hike.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Accès refusé");
        }

        EquipmentItem item = equipmentRepository.findById(equipId)
                .orElseThrow(() -> new RuntimeException("Équipement introuvable"));

        // Appel à l'entité pour suppression propre dans la collection
        hike.removeEquipment(item);

        hikeRepository.save(hike);
    }

    /**
     * Validateur interne des règles métier (Business Rules).
     * Centralise les contraintes techniques pour éviter d'avoir des équipements invalides en base.
     * Règle actuelle : Un équipement doit peser entre 50g et 5kg.
     * @param item équipement à vérifier
     */
    private void validateEquipmentRules(EquipmentItem item) {
        if (item.getMasseGrammes() < 50 || item.getMasseGrammes() > 5000) {
            throw new RuntimeException("Validation échouée : " +
                    "La masse de l'équipement doit être comprise entre 50g et 5kg");
        }
    }
}