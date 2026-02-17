package iut.rodez.projet.sae.fourawalkapi.repository.mysql;

import iut.rodez.projet.sae.fourawalkapi.entity.PointOfInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Gestion des dialogues bd pour les points d'intêret
 */
@Repository
public interface PointOfInterestRepository extends JpaRepository<PointOfInterest, Long> {}
