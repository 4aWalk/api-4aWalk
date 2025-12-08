package iut.rodez.projet.sae.fourawalkapi.repository;

import iut.rodez.projet.sae.fourawalkapi.entity.PointOfInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointOfInterestRepository extends JpaRepository<PointOfInterest, Long> {}
