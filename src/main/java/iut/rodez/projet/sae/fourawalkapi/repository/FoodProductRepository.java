package iut.rodez.projet.sae.fourawalkapi.repository;

import iut.rodez.projet.sae.fourawalkapi.entity.FoodProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodProductRepository extends JpaRepository<FoodProduct, Long> {}
