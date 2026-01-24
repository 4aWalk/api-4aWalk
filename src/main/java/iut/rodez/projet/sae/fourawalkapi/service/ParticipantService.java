package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.ParticipantRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ParticipantService {

    private final ParticipantRepository participantRepository;

    public ParticipantService(ParticipantRepository participantRepository) {
        this.participantRepository = participantRepository;
    }

    public Participant createBasicParticipant() {
        Participant p = new Participant();
        return participantRepository.save(p);
    }

    @Transactional
    public Participant updateParticipantDetails(Long id, Participant details) {
        Participant existing = participantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Participant non trouvé"));

        // Mise à jour sélective
        if (details.getAge() > 0) existing.setAge(details.getAge());
        if (details.getNiveau() != null) existing.setNiveau(details.getNiveau());
        if (details.getMorphologie() != null) existing.setMorphologie(details.getMorphologie());
        if (details.getBesoinKcal() > 0) existing.setBesoinKcal(details.getBesoinKcal());
        if (details.getBesoinEauLitre() > 0) existing.setBesoinEauLitre(details.getBesoinEauLitre());
        if (details.getCapaciteEmportMaxKg() > 0) existing.setCapaciteEmportMaxKg(details.getCapaciteEmportMaxKg());

        validateParticipant(existing);
        return participantRepository.save(existing);
    }

    public void validateParticipant(Participant p) {
        if (p.getAge() < 1 || p.getAge() > 120) throw new IllegalArgumentException("Âge invalide.");
    }

    @Transactional
    public void deleteParticipant(Long id) {
        if (!participantRepository.existsById(id)) throw new RuntimeException("ID introuvable.");
        participantRepository.deleteById(id);
    }

    public Participant getById(Long id) {
        return participantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Participant non trouvé"));
    }
}
