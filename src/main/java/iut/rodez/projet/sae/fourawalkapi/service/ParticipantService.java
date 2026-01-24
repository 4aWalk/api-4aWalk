package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.HikeRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.ParticipantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParticipantService {

    private final HikeRepository hikeRepository;
    private final ParticipantRepository participantRepository;

    public ParticipantService(HikeRepository hr, ParticipantRepository pr) {
        this.hikeRepository = hr;
        this.participantRepository = pr;
    }

    @Transactional
    public Participant addParticipant(Long hikeId, Participant p, Long userId) {
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException("Randonnée introuvable"));

        if (!hike.getCreator().getId().equals(userId)) throw new RuntimeException("Accès refusé");
        if (hike.getParticipants().size() >= 3) throw new RuntimeException("Hike complète (Max 3)");

        p.setCreator(false); // Sécurité
        Participant saved = participantRepository.save(p);

        hike.getParticipants().add(saved);
        hikeRepository.save(hike);

        return saved;
    }

    @Transactional
    public Participant updateParticipant(Long hikeId, Long participantId, Participant details, Long userId) {
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException("Randonnée introuvable"));
        if (!hike.getCreator().getId().equals(userId)) throw new RuntimeException("Accès refusé");

        Participant p = participantRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant introuvable"));

        p.setAge(details.getAge());
        p.setNiveau(details.getNiveau());
        p.setMorphologie(details.getMorphologie());
        p.setBesoinKcal(details.getBesoinKcal());
        p.setBesoinEauLitre(details.getBesoinEauLitre());
        p.setCapaciteEmportMaxKg(details.getCapaciteEmportMaxKg());
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
}