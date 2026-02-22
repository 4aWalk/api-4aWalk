package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.entity.User;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.HikeRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.ParticipantRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service participant gérant la logique métier de base
 */
@Service
public class ParticipantService {

    private static final String MSG_ERROR_NOT_FOUND_HIKE = "RandonneeIntrouvable";
    private static final String REFUSED_ACCES = "Acces_refused";

    private final HikeRepository hikeRepository;
    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;

    public ParticipantService(HikeRepository hr, ParticipantRepository pr, UserRepository ur) {
        this.hikeRepository = hr;
        this.participantRepository = pr;
        this.userRepository = ur;
    }

    /**
     * Récupère la liste des participants créés par un utilisateur spécifique (hors profil créateur).
     *
     * @param userId Identifiant de l'utilisateur.
     * @return Liste des participants gérés par cet utilisateur.
     */
    public List<Participant> getMyParticipants(Long userId) {
        return participantRepository.findByCreatorIdAndCreatorFalse(userId);
    }

    /**
     * Ajoute un participant à une randonnée existante.
     * Vérifie les droits d'accès et les limites de capacité de la randonnée.
     *
     * @param hikeId Identifiant de la randonnée cible.
     * @param p Objet participant à ajouter.
     * @param userId Identifiant de l'utilisateur effectuant la requête.
     * @return Le participant sauvegardé.
     * @throws RuntimeException Si la randonnée n'appartient pas à l'utilisateur ou si elle est pleine.
     */
    @Transactional
    public Participant addParticipant(Long hikeId, Participant p, Long userId) {
        validateParticipantRules(p);

        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException(MSG_ERROR_NOT_FOUND_HIKE));

        if (!hike.getCreator().getId().equals(userId)) throw new RuntimeException(REFUSED_ACCES);
        if (hike.getParticipants().size() >= 3) throw new RuntimeException("Hike complète (Max 3)");

        p.setCreator(false);
        p.setCreatorId(userId);

        Participant saved = participantRepository.save(p);

        hike.getParticipants().add(saved);
        hikeRepository.save(hike);

        return saved;
    }

    /**
     * Met à jour les informations d'un participant.
     * Si le participant est le créateur (profil utilisateur), met à jour également l'entité User associée.
     *
     * @param hikeId Identifiant de la randonnée.
     * @param participantId Identifiant du participant à modifier.
     * @param details Nouvelles données du participant.
     * @param userId Identifiant de l'utilisateur effectuant la modification.
     * @return Le participant mis à jour.
     */
    @Transactional
    public Participant updateParticipant(Long hikeId, Long participantId, Participant details, Long userId) {
        validateParticipantRules(details);

        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException(MSG_ERROR_NOT_FOUND_HIKE));
        if (!hike.getCreator().getId().equals(userId)) throw new RuntimeException(REFUSED_ACCES);

        Participant p = participantRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant introuvable"));

        if (!hike.getParticipants().contains(p)) {
            throw new RuntimeException("Ce participant n'appartient pas à la randonnée spécifiée !");
        }
        p.setNom(details.getNom());
        p.setPrenom(details.getPrenom());
        p.setAge(details.getAge());
        p.setNiveau(details.getNiveau());
        p.setMorphologie(details.getMorphologie());
        p.setBesoinKcal(details.getBesoinKcal());
        p.setBesoinEauLitre(details.getBesoinEauLitre());
        p.setCapaciteEmportMaxKg(details.getCapaciteEmportMaxKg());

        if(p.getCreatorId() == null) {
            p.setCreatorId(userId);
        }

        if (p.getCreator()) {
            User userToUpdate = userRepository.findById(hike.getCreator().getId())
                    .orElseThrow(() -> new RuntimeException("Utilisateur créateur introuvable en base"));

            userToUpdate.setAge(details.getAge());
            userToUpdate.setNiveau(details.getNiveau());
            userToUpdate.setMorphologie(details.getMorphologie());

            userRepository.save(userToUpdate);
        }

        return participantRepository.save(p);
    }

    /**
     * Supprime un participant d'une randonnée.
     * Interdit la suppression du profil créateur.
     *
     * @param hikeId Identifiant de la randonnée.
     * @param participantId Identifiant du participant à supprimer.
     * @param userId Identifiant de l'utilisateur effectuant la suppression.
     */
    @Transactional
    public void deleteParticipant(Long hikeId, Long participantId, Long userId) {
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException(MSG_ERROR_NOT_FOUND_HIKE));
        if (!hike.getCreator().getId().equals(userId)) throw new RuntimeException(REFUSED_ACCES);

        Participant p = participantRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant introuvable"));

        if (p.getCreator()) throw new RuntimeException("Impossible de supprimer le créateur");

        hike.getParticipants().remove(p);
        hikeRepository.save(hike);
        participantRepository.delete(p);
    }

    /**
     * Valide les règles métiers physiologiques et logistiques du participant.
     *
     * @param p Le participant à valider.
     * @throws IllegalArgumentException si une des règles n'est pas respectée.
     */
    private void validateParticipantRules(Participant p) {

        // Vérification de la présence des informations obligatoires
        if (p.getNom() == null || p.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du participant est obligatoire");
        }

        if (p.getPrenom() == null || p.getPrenom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le prénom du participant est obligatoire");
        }

        if (p.getNiveau() == null) {
            throw new IllegalArgumentException("Le niveau est obligatoire");
        }

        if (p.getMorphologie() == null) {
            throw new IllegalArgumentException("La morphologie est obligatoire");
        }

        // Validation des limites d'âge
        if (p.getAge() < 10 || p.getAge() > 100) {
            throw new IllegalArgumentException("L'âge doit être compris entre 10 et 100 ans");
        }

        // Validation des besoins caloriques
        if (p.getBesoinKcal() < 1700 || p.getBesoinKcal() > 10000) {
            throw new IllegalArgumentException("Le besoin calorique doit être compris entre 1700 et 10000 kcal");
        }

        // Validation des besoins en eau
        if (p.getBesoinEauLitre() < 1 || p.getBesoinEauLitre() > 8) {
            throw new IllegalArgumentException("Le besoin en eau doit être compris entre 1 et 8 Litres");
        }

        // Validation de la capacité d'emport
        if (p.getCapaciteEmportMaxKg() < 0 || p.getCapaciteEmportMaxKg() > 30.0) {
            throw new IllegalArgumentException("La capacité d'emport doit être positive et ne peut pas dépasser 30 kg");
        }
    }
}