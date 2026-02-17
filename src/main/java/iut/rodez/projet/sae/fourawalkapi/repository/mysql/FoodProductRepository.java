package iut.rodez.projet.sae.fourawalkapi.repository.mysql;

import iut.rodez.projet.sae.fourawalkapi.entity.FoodProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Gestion des dialogues bd pour la nourriture
 */
@Repository
public interface FoodProductRepository extends JpaRepository<FoodProduct, Long> {
}
