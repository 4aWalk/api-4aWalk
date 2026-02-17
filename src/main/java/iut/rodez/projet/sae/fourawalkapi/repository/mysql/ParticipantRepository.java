package iut.rodez.projet.sae.fourawalkapi.repository.mysql;

import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Gestion des dialogues bd pour les participants
 */
@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    /**
     * Recherche de la liste de tous les participants créer par un utilisateur
     * @param creatorId identifiant du créateur
     * @return liste de tous les participants créer par l'utilisateur
     */
    List<Participant> findByCreatorIdAndCreatorFalse(Long creatorId);

    /**
     * Recherche de la liste de tous les participants créateur
     * @param creatorId identifiant du créateur
     * @return liste de tous les participants créateur
     */
    List<Participant> findByCreatorIdAndCreatorTrue(Long creatorId);
}