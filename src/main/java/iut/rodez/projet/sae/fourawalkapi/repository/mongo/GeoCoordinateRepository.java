package iut.rodez.projet.sae.fourawalkapi.repository.mongo;

import iut.rodez.projet.sae.fourawalkapi.document.GeoCoordinate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeoCoordinateRepository extends MongoRepository<GeoCoordinate, String> {}