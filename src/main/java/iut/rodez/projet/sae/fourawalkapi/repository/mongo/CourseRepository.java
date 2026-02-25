package iut.rodez.projet.sae.fourawalkapi.repository.mongo;

import iut.rodez.projet.sae.fourawalkapi.document.Course;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Gestion des dialogues bd pour les parcours
 */
@Repository
public interface CourseRepository extends MongoRepository<Course, String> {
    /**
     * Récupération des parcours d'une même randonnée
     * @param hikeId identifiant de la randonnée
     * @return liste des parcours trouvée
     */
    List<Course> findByHikeId(Long hikeId);

    /**
     * Supprime tout les parcours associé à la randonnée en cours de suppression
     * @param hikeId identifiant de la randonnée supprimé
     */
    void deleteByHikeId(Long hikeId);
}