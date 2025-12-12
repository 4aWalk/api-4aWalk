package iut.rodez.projet.sae.fourawalkapi.repository;

import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HikeRepository extends JpaRepository<Hike, Long> {

    /**
     * Recherche toutes les randonnées créées par un utilisateur donné (UC1 : Liste des randonnées créées).
     * @param creatorId L'ID de l'utilisateur qui a créé la randonnée.
     * @return La liste des randonnées.
     */
    List<Hike> findByCreatorId(Long creatorId);
}
