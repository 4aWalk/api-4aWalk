package iut.rodez.projet.sae.fourawalkapi.repository;

import iut.rodez.projet.sae.fourawalkapi.entity.Backpack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BackpackRepository extends JpaRepository<Backpack, Long> {
        Optional<Backpack> findByOwnerId(Long ownerId);
}
