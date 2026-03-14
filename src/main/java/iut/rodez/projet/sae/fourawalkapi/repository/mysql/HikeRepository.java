package iut.rodez.projet.sae.fourawalkapi.repository.mysql;

import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
    @EntityGraph(attributePaths = {"depart", "arrivee"})
    List<Hike> findByCreatorId(Long creatorId);

    /**
     * Chargement de l'ensemble des éléments d'une randonnée pour une seule randonnée chargée
     * @param id
     * @return
     */
    @EntityGraph(attributePaths = {
            "depart",
            "arrivee",
            "participants"
    })
    Optional<Hike> findById(Long id);

    /**
     * Recherche si il exite déjà un nom de randonné pour un utilisateur
     * pour assuré l'unicité du libellé pour l'utilisateur
     *
     * @param creatorId     identifiant de l'utilisateur
     * @param libelle       libellé à vérifier
     * @param excludeHikeId identifiant de la randonné dans le cas d'un update
     * @return true si le libellé est déjà utilisé, false sinon
     */
    boolean existsByCreatorIdAndLibelleAndIdNot(Long creatorId, String libelle, Long excludeHikeId);
}
