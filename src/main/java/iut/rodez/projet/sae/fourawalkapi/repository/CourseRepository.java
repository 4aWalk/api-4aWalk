package iut.rodez.projet.sae.fourawalkapi.repository;

import iut.rodez.projet.sae.fourawalkapi.document.Course;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends MongoRepository<Course, String> {}