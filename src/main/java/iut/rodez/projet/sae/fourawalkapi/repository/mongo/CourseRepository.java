package iut.rodez.projet.sae.fourawalkapi.repository.mongo;

import iut.rodez.projet.sae.fourawalkapi.document.Course;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends MongoRepository<Course, String> {
    // Tu peux ajouter des méthodes de recherche personnalisées
    List<Course> findByHikeId(Long hikeId);
}