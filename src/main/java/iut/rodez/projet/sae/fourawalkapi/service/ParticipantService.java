package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.ParticipantRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ParticipantService {

    private final ParticipantRepository participantRepository;

    public ParticipantService(ParticipantRepository participantRepository) {
        this.participantRepository = participantRepository;
    }

    // --- ÉTAPE 1 : Création initiale (Nom / Prénom) ---
    public Participant createBasicParticipant(String nomComplet) {
        Participant p = new Participant();
        p.setNomComplet(nomComplet);
        return participantRepository.save(p);
    }

    // --- ÉTAPE 2 : Complétion et Modification ---
    public Participant updateParticipantDetails(Long id, Participant details) {
        Participant existing = participantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Participant non trouvé"));

        // On met à jour uniquement si les nouvelles infos sont fournies
        if (details.getNiveau() != null) existing.setNiveau(details.getNiveau());
        if (details.getMorphologie() != null) existing.setMorphologie(details.getMorphologie());
        if (details.getBesoinEauLitre() > 0) existing.setBesoinEauLitre(details.getBesoinEauLitre());

        // Permet aussi de modifier nom/prénom si besoin
        if (details.getNomComplet() != null) existing.setNomComplet(details.getNomComplet());
        if (details.getNomComplet() != null) existing.setNomComplet(details.getNomComplet());

        return participantRepository.save(existing);
    }


    public void delete(Long id) {
        participantRepository.deleteById(id);
    }

    /**
     * Récupère un participant par son identifiant unique.
     * @param id L'ID du participant
     * @return Un Optional contenant le participant s'il existe
     */
    public Optional<Participant> getById(Long id) {
        return participantRepository.findById(id);
    }
}
