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

@Service
public class ParticipantService {

    private final HikeRepository hikeRepository;
    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;

    public ParticipantService(HikeRepository hr, ParticipantRepository pr, UserRepository ur) {
        this.hikeRepository = hr;
        this.participantRepository = pr;
        this.userRepository = ur;
    }

    public List<Participant> getMyParticipants(Long userId) {
        return participantRepository.findByCreatorIdAndCreatorFalse(userId);
    }

    @Transactional
    public Participant addParticipant(Long hikeId, Participant p, Long userId) {
        // 1. Validation métier des données brutes
        validateParticipantRules(p);

        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException("Randonnée introuvable"));

        if (!hike.getCreator().getId().equals(userId)) throw new RuntimeException("Accès refusé");
        if (hike.getParticipants().size() >= 3) throw new RuntimeException("Hike complète (Max 3)");

        p.setCreator(false);
        p.setCreatorId(userId);

        Participant saved = participantRepository.save(p);

        hike.getParticipants().add(saved);
        hikeRepository.save(hike);

        return saved;
    }

    @Transactional
    public Participant updateParticipant(Long hikeId, Long participantId, Participant details, Long userId) {
        // 1. Validation métier des nouvelles données
        validateParticipantRules(details);

        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException("Randonnée introuvable"));
        if (!hike.getCreator().getId().equals(userId)) throw new RuntimeException("Accès refusé");

        Participant p = participantRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant introuvable"));

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

    @Transactional
    public void deleteParticipant(Long hikeId, Long participantId, Long userId) {
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException("Randonnée introuvable"));
        if (!hike.getCreator().getId().equals(userId)) throw new RuntimeException("Accès refusé");

        Participant p = participantRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant introuvable"));

        if (p.getCreator()) throw new RuntimeException("Impossible de supprimer le créateur");

        hike.getParticipants().remove(p);
        hikeRepository.save(hike);
        participantRepository.delete(p);
    }

    /**
     * Valide les règles métiers spécifiques (Eau, Kcal, Age, Sac)
     */
    private void validateParticipantRules(Participant p) {
        // Règle : Eau entre 1 et 8 L
        if (p.getBesoinEauLitre() < 1 || p.getBesoinEauLitre() > 8) {
            throw new RuntimeException("Le besoin en eau doit être compris entre 1 et 8 Litres");
        }

        // Règle : Kcal entre 1700 et 10000
        if (p.getBesoinKcal() < 1700 || p.getBesoinKcal() > 10000) {
            throw new RuntimeException("Le besoin calorique doit être compris entre 1700 et 10000 kcal");
        }

        // Règle : Age entre 10 et 100 ans
        if (p.getAge() < 10 || p.getAge() > 100) {
            throw new RuntimeException("L'âge doit être compris entre 10 et 100 ans");
        }

        // Règle : Sac à dos max 30 kg (Capacité d'emport)
        if (p.getCapaciteEmportMaxKg() > 30.0) {
            throw new RuntimeException("La capacité d'emport ne peut pas dépasser 30 kg");
        }
    }
}