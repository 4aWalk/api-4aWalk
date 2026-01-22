package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.FoodProduct;
import org.springframework.stereotype.Service;
import java.util.List;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.FoodProductRepository;

@Service
public class FoodService {

    private final FoodProductRepository foodProductRepository;

    public FoodService(FoodProductRepository foodProductRepository) {
        this.foodProductRepository = foodProductRepository;
    }

    public List<FoodProduct> findAll() {
        return foodProductRepository.findAll();
    }

    public void validateFoodProduct(FoodProduct food) {
        if (food.getMasseGrammes() <= 0) {
            throw new IllegalArgumentException("Le produit " + food.getNom() + " doit avoir un poids positif.");
        }
    }
}
