package iut.rodez.projet.sae.fourawalkapi.repository.mysql;

import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Gestion des dialogues bd pour les randonnées
 */
@Repository
public interface HikeRepository extends JpaRepository<Hike, Long> {

    /**
     * Recherche toutes les randonnées créées par un utilisateur
     * @param creatorId identifiant de l'utilisateur créateur de randonnée recherché
     * @return La liste des randonnées.
     */
    List<Hike> findByCreatorId(Long creatorId);

    /**
     * Recherche si il exite déjà un nom de randonné pour un utilisateur
     * pour assuré l'unicité du libellé pour l'utilisateur
     * @param creatorId identifiant de l'utilisateur
     * @param libelle libellé à vérifier
     * @return true si le libellé est déjà utilisé, false sinon
     */
    boolean existsByCreatorIdAndLibelle(Long creatorId, String libelle);
}
